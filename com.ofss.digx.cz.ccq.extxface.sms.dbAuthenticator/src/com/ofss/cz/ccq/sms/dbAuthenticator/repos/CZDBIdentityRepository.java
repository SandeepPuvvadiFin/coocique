package com.ofss.cz.ccq.sms.dbAuthenticator.repos;

import com.ofss.digx.app.adapter.AdapterFactoryConfigurator;
import com.ofss.digx.app.adapter.IAdapterFactory;
import com.ofss.digx.app.audit.authenticationAudit.AuthenticationAuditEventHandler;
import com.ofss.digx.app.audit.authenticationAudit.AuthenticationEvent;
import com.ofss.digx.app.sms.adapter.user.party.relationship.IUserPartyRelationshipAdapter;
import com.ofss.digx.app.sms.dto.user.party.relationship.UserPartyRelationshipDTO;
import com.ofss.digx.app.sms.dto.user.party.relationship.UserPartyRelationshipListResponse;
import com.ofss.digx.common.util.SessionContextHelper;
import com.ofss.digx.datatype.complex.Party;
import com.ofss.digx.domain.sms.entity.user.party.relationship.UserPartyRelationship;
import com.ofss.digx.infra.exceptions.Exception;
import com.ofss.fc.app.context.SessionContext;
import com.ofss.fc.datatype.Date;
import com.ofss.fc.infra.config.ConfigurationFactory;
import com.ofss.fc.infra.log.impl.MultiEntityLogger;
import com.ofss.fc.infra.thread.ThreadAttribute;
import com.ofss.sms.dbAuthenticator.domain.UserProfile;
import com.ofss.sms.dbAuthenticator.domain.Users;
import com.ofss.sms.dbAuthenticator.domain.UsersKey;
import com.ofss.sms.dbAuthenticator.repos.DBIdentityRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import oracle.security.idm.IMException;
import org.apache.hc.client5.http.fluent.Content;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.util.Timeout;
import org.json.JSONObject;

public class CZDBIdentityRepository extends DBIdentityRepository {
	private static final String THIS_COMPONENT_NAME = CZDBIdentityRepository.class.getName();

	private static final MultiEntityLogger formatter = MultiEntityLogger.getUniqueInstance();

	private static transient Logger logger = MultiEntityLogger.getUniqueInstance().getLogger(THIS_COMPONENT_NAME);

	private Map constantToNativePropMap = new HashMap<>();

	public static final String SECURITYCONSTANTS = "SecurityConstants";

	private final Preferences securityPreferences;

	public CZDBIdentityRepository() {
		this.securityPreferences = ConfigurationFactory.getInstance().getConfigurations("SecurityConstants");
	}

	public String getNativeProperty(String p) {
		return (String) this.constantToNativePropMap.get(p);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map authenticateUser(String nationalID, Map<String, String> requestDetails) throws IMException {
		Map map = new HashMap();
		AuthenticationEvent authenticationEvent = new AuthenticationEvent();
		String userName = "";
		try {
			authenticationEvent.setStartTime(new Date());
			SessionContext sessionContext = SessionContextHelper.getInstance().createSesstionContext();
			ThreadAttribute.set("CTX", sessionContext);
			if (requestDetails != null || !requestDetails.isEmpty()) {
				requestDetails.put("loginType", "via digital signature: national id" + nationalID);
				authenticationEvent.setRequestMap(requestDetails);
			}
			Users userDomain = readUser(nationalID);
			if (userDomain != null) {
				userName = userDomain.getKey().getUserName();
				System.out.println("cz authenticateUser: username is " + userName);
				UserProfile usersProfile = new UserProfile();
				Date currDateTime = new Date();
				Date pwdExpDate = new Date();
				Users usersDomain = new Users();
				UsersKey key = new UsersKey();
				key.setUserName(userName);
				usersDomain.setKey(key);
				usersProfile.setKey(key);
				pwdExpDate.setDateString(userDomain.getPwdExpiryDate().toString().substring(0, 13));
				UserProfile profile = usersProfile.read(usersProfile);
				Date lastLoginDate = null;
				if (profile.getLastLoggedInDateTime() != null) {
					lastLoginDate = new Date();
					lastLoginDate.setDateString(profile.getLastLoggedInDateTime().substring(0, 13));
				}
				if (userDomain.getLockStatus()) {
					currDateTime = new Date();
					usersProfile.setLastFailedLoggedInDateTime(currDateTime.toString());
					usersProfile.updateLastFailedLoggedInDateTime(usersProfile);
					throw new Exception("DIGX_DB_AUTH_005");
				}
				usersDomain.setLockCounter(Integer.valueOf(0));
				usersDomain.updateLockCounter(usersDomain);
				usersProfile.updateLastLoggedInDateTime(usersProfile);
			}
		} catch (Exception e) {
			if (logger.isLoggable(Level.SEVERE))
				logger.log(Level.SEVERE, formatter.formatMessage(
						"Error while generating response of method authenticateUser in the class %s. Exception details are: %s. ",
						new Object[] { THIS_COMPONENT_NAME }), (Throwable) e);
			map.put(e.getErrorCode(), String.valueOf(e.getErrorCode()) + ":" + e.getDisplayMessage() + ":" + userName);
			return map;
		} finally {
			authenticationEvent.setUserName(userName);
			authenticationEvent.setErrrorCodeMap(map);
			authenticationEvent.setEndTime(new Date());
			AuthenticationAuditEventHandler.getInstance().fire(authenticationEvent);
		}
		return map;
	}

	private AuthenticationResponse authneticateViaNationalId(String nationalID) throws Exception {
		try {
			String solicitarURL = this.securityPreferences.get("solicitarURL",
					"http://172.28.54.193:8080/ords/api_fas/autenticacion/solicitar");
			Content contentSolicitarURL = Request.post(solicitarURL).connectTimeout(Timeout.ofSeconds(60L))
					.addHeader("accept", "application/json").addHeader("p_identificacion", nationalID).execute()
					.returnContent();
			JSONObject solicitarObject = new JSONObject(contentSolicitarURL.asString());
			String p_coderr = (String) solicitarObject.get("p_coderr");
			if ("0".equals(p_coderr)) {
				String p_codsol = (String) solicitarObject.get("p_codsol");
				Integer p_timeout = (Integer) solicitarObject.get("p_timeout");
				String esperarURL = this.securityPreferences.get("esperarURL",
						"http://172.28.54.193:8080/ords/api_fas/autenticacion/esperar");
				Content contentesperarURL = Request.post(esperarURL)
						.connectTimeout(Timeout.ofSeconds(p_timeout.intValue())).addHeader("accept", "application/json")
						.addHeader("p_codsol", p_codsol).execute().returnContent();
				JSONObject esperarObject = new JSONObject(contentesperarURL.asString());
				if ("0".equals(esperarObject.get("p_coderr")))
					return new AuthenticationResponse(true, esperarObject, JsonObjectType.esperar);
				return new AuthenticationResponse(false, esperarObject, JsonObjectType.esperar);
			}
			return new AuthenticationResponse(false, solicitarObject, JsonObjectType.solicitar);
		} catch (java.lang.Exception e) {
			if (logger.isLoggable(Level.SEVERE))
				logger.log(Level.SEVERE,
						formatter.formatMessage("Error while while doAuthentication.Exception details are: %s. ",
								new Object[] { THIS_COMPONENT_NAME }),
						e);
			throw new Exception(e);
		}
	}

	private boolean validateNationalId(String nationalID) {
		return true;
	}

	public Users readUser(String nationalID) throws Exception {
		System.out.println("nationalid is" + nationalID);
		Users users = new Users();
		Users usersRead = null;
		UserProfile userProfile = new UserProfile();
		try {
			usersRead = findUser(nationalID);
			if (usersRead == null)
				throw new Exception("DIGX_DB_AUTH_002");
			UsersKey usersKey = new UsersKey();
			usersKey.setUserName(usersRead.getKey().getUserName());
			users.setKey(usersKey);
			userProfile.setKey(usersKey);
			if (usersRead.getDeleteStatus()) {
				Date currDateTime = new Date();
				userProfile.setLastFailedLoggedInDateTime(currDateTime.toString());
				userProfile.updateLastFailedLoggedInDateTime(userProfile);
				throw new Exception("DIGX_DB_AUTH_008");
			}
			Date expiryDate = new Date();
			expiryDate.setDateString(usersRead.getPwdExpiryDate().toString().substring(0, 13));
			Date todaysDate = new Date();
			if (usersRead.getPasswordHistoryList().size() == 0 && expiryDate.compareTo(todaysDate) < 0)
				throw new Exception("DIGX_DB_AUTH_011");
		} catch (Exception _exception) {
			if (logger.isLoggable(Level.SEVERE))
				logger.log(Level.SEVERE,
						formatter.formatMessage("Error while generating response of method readUser in the class %s",
								new Object[] { THIS_COMPONENT_NAME }),
						(Throwable) _exception);
			throw _exception;
		}
		return usersRead;
	}

	public Users findUser(String nationalID) throws Exception {
		Context ctx = null;
		Connection conn = null;
		ResultSet rs = null;
		List<String> PartyList = new ArrayList<>();
		Hashtable<String, String> ht = new Hashtable<>();
		ht.put("java.naming.factory.initial", "weblogic.jndi.WLInitialContextFactory");
		try {
			ctx = new InitialContext(ht);
			DataSource ds = (DataSource) ctx.lookup("OBDX_BU_B1A1");
			conn = ds.getConnection();
			PreparedStatement stmt = conn.prepareStatement(
					" SELECT idcorporate FROM FCC_VW_CUSTOMER_DETAILS WHERE  UNIQUE_ID_VALUE = ?  order by IDCORPORATE ");
			stmt.setString(1, nationalID);
			rs = stmt.executeQuery();
			while (rs.next())
				PartyList.add(rs.getString("idcorporate"));
			if (PartyList.size() < 1) {
				if (logger.isLoggable(Level.SEVERE))
					logger.log(Level.SEVERE, formatter.formatMessage("no party id found against the national id %s",
							new Object[] { nationalID }));
				throw new Exception("DIGX_PI_0162");
			}
			System.out.println("party id is" + (String) PartyList.get(0));
			UserPartyRelationship userpartyRel = new UserPartyRelationship();
			userpartyRel.setPartyId(PartyList.get(0));
			userpartyRel = userpartyRel.search(userpartyRel).get(0);
			System.out.println("username is :" + userpartyRel.getUserId());
			return fetchUserDomain(userpartyRel.getUserId());
		} catch (Exception e) {
			if (logger.isLoggable(Level.SEVERE))
				logger.log(Level.SEVERE,
						formatter.formatMessage(
								"Something went wrong while querying for partyid with national id is %s",
								new Object[] { nationalID, THIS_COMPONENT_NAME }),
						(Throwable) e);
			throw e;
		} catch (SQLException | javax.naming.NamingException e) {
			if (logger.isLoggable(Level.SEVERE))
				logger.log(Level.SEVERE, formatter.formatMessage(
						"SQLException or NamingException: Something went wrong while querying for partyid with national id is %s",
						new Object[] { nationalID, THIS_COMPONENT_NAME }), e);
			throw new Exception("DIGX_PI_0162");
		} finally {
			try {
				conn.close();
				rs.close();
				ctx.close();
			} catch (java.lang.Exception e) {
				if (logger.isLoggable(Level.SEVERE))
					logger.log(Level.SEVERE, formatter.formatMessage(
							"java exception: Something went wrong while closing the connection. national id is %s",
							new Object[] { nationalID, THIS_COMPONENT_NAME }), e);
				throw new Exception("DIGX_PI_0162");
			}
		}
	}

	private Users fetchUserDomain(String username) throws Exception {
		Users userdomain = new Users();
		UsersKey key = new UsersKey();
		key.setUserName(username);
		userdomain.setKey(key);
		return userdomain.read(userdomain);
	}

	private List<String> fetchUsersForParty(String partyId) throws Exception {
		IAdapterFactory userPartyRelationAdapterFactory = AdapterFactoryConfigurator.getInstance()
				.getAdapterFactory("USER_PARTY_RELATIONSHIP_ADAPTER_FACTORY");
		IUserPartyRelationshipAdapter userPartyRelationshipAdapter = (IUserPartyRelationshipAdapter) userPartyRelationAdapterFactory
				.getAdapter("USER_PARTY_RELATIONSHIP_ADAPTER");
		UserPartyRelationshipDTO userPartyRelationshipDTO = new UserPartyRelationshipDTO();
		List<String> users = null;
		if (partyId != null) {
			userPartyRelationshipDTO.setPartyId(new Party(partyId));
			UserPartyRelationshipListResponse userPartyRelationshipListResponse = userPartyRelationshipAdapter
					.search(userPartyRelationshipDTO);
			if (userPartyRelationshipListResponse != null
					&& userPartyRelationshipListResponse.getUserPartyRelationshipDTOs() != null
					&& userPartyRelationshipListResponse.getUserPartyRelationshipDTOs().size() > 0) {
				users = new ArrayList<>();
				Iterator<UserPartyRelationshipDTO> iterator = userPartyRelationshipListResponse
						.getUserPartyRelationshipDTOs().iterator();
				while (iterator.hasNext()) {
					UserPartyRelationshipDTO partyRelationshipDTO = iterator.next();
					users.add(partyRelationshipDTO.getUserId());
				}
			}
		}
		return users;
	}
}
