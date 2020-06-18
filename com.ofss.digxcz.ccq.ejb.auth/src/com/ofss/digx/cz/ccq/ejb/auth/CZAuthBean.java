package com.ofss.digx.cz.ccq.ejb.auth;

import com.ofss.cz.ccq.sms.dbAuthenticator.repos.CZDBIdentityRepository;
import com.ofss.digx.common.util.SessionContextHelper;
import com.ofss.digx.enumeration.ModuleId;
import com.ofss.digx.infra.error.AbstractErrorCodeMapper;
import com.ofss.digx.infra.error.ErrorCodeMapperFactory;
import com.ofss.digx.infra.exceptions.Exception;
import com.ofss.fc.app.context.SessionContext;
import com.ofss.fc.infra.das.orm.DataAccessManager;
import com.ofss.fc.infra.das.orm.Session;
import com.ofss.fc.infra.log.impl.MultiEntityLogger;
import com.ofss.fc.infra.thread.ThreadAttribute;
import com.ofss.sms.dbAuthenticator.domain.Users;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import oracle.security.idm.IMException;

@Stateless
public class CZAuthBean implements ICZAuthBean {
  private static final String THIS_COMPONENT_NAME = CZAuthBean.class.getName();
  
  private static transient Logger logger = Logger.getLogger(THIS_COMPONENT_NAME);
  
  private static MultiEntityLogger formatter = MultiEntityLogger.getUniqueInstance();
  
  public Map authenticateUser(String idNumber, Map requestDetails) {
    Session session = null;
    boolean isSessionOpen = false;
    Map<String, String> trsformedErrorCodeMap = new HashMap<>();
    AbstractErrorCodeMapper errorCodeMapper = null;
    CZDBIdentityRepository repos = new CZDBIdentityRepository();
    try {
      if (DataAccessManager.getManager().isSessionOpen()) {
        session = DataAccessManager.getManager().fetchCurrentSession();
      } else {
        session = DataAccessManager.getManager().openSession();
        session.beginTransaction();
        isSessionOpen = true;
      } 
      errorCodeMapper = ErrorCodeMapperFactory.getInstance().getErrorCodeMapper(ModuleId.SMS);
      Map<String, String> digxErrorCodeMap = repos.authenticateUser(idNumber, requestDetails);
      if (digxErrorCodeMap != null && !digxErrorCodeMap.isEmpty())
        for (String key : digxErrorCodeMap.keySet()) {
          String thirdPartyErrorCode = errorCodeMapper.getLocalErrorCode(key);
          trsformedErrorCodeMap.put(thirdPartyErrorCode, digxErrorCodeMap.get(key));
        }  
    } catch (IMException e) {
      logger.log(Level.SEVERE, formatter.formatMessage("FatalException thrown  %s, AuthBean.authenticateUser()", new Object[] { THIS_COMPONENT_NAME }), (Throwable)e);
      String thirdPartyErrorCode = errorCodeMapper
        .getLocalErrorCode("DIGX_DB_AUTH_000");
      trsformedErrorCodeMap.put(thirdPartyErrorCode, thirdPartyErrorCode);
      return trsformedErrorCodeMap;
    } finally {
      if (isSessionOpen) {
        session.fetchCurrentTransaction().commit();
        DataAccessManager.getManager().closeSession(session);
      } 
    } 
    return trsformedErrorCodeMap;
  }
  
  public List getGrantedRoles(String name) throws IMException {
    CZDBIdentityRepository repos = new CZDBIdentityRepository();
    Session session = null;
    boolean isSessionOpen = false;
    try {
      SessionContext sessionContext = SessionContextHelper.getInstance().createSesstionContext();
      ThreadAttribute.set("CTX", sessionContext);
      if (DataAccessManager.getManager().isSessionOpen()) {
        session = DataAccessManager.getManager().fetchCurrentSession();
      } else {
        session = DataAccessManager.getManager().openSession();
        session.beginTransaction();
        isSessionOpen = true;
      } 
      Users user = repos.findUser(name);
      return repos.getGrantedRoles(user.getKey().getUserName());
    } catch (Throwable cause) {
      throw new IMException(cause);
    } finally {
      if (isSessionOpen) {
        session.fetchCurrentTransaction().commit();
        DataAccessManager.getManager().closeSession(session);
      } 
    } 
  }
  
  public String getUserName(String digitalID) throws RemoteException, IMException {
    CZDBIdentityRepository repos = new CZDBIdentityRepository();
    Session session = null;
    boolean isSessionOpen = false;
    try {
      SessionContext sessionContext = SessionContextHelper.getInstance().createSesstionContext();
      ThreadAttribute.set("CTX", sessionContext);
      if (DataAccessManager.getManager().isSessionOpen()) {
        session = DataAccessManager.getManager().fetchCurrentSession();
      } else {
        session = DataAccessManager.getManager().openSession();
        session.beginTransaction();
        isSessionOpen = true;
      } 
      Users user = repos.findUser(digitalID);
      return user.getKey().getUserName();
    } catch (Throwable cause) {
      throw new IMException(cause);
    } finally {
      if (isSessionOpen) {
        session.fetchCurrentTransaction().commit();
        DataAccessManager.getManager().closeSession(session);
      } 
    } 
  }
  
  public boolean getUserLockStatus(String username) throws RemoteException, Exception {
    CZDBIdentityRepository repos = new CZDBIdentityRepository();
    Session session = null;
    boolean isSessionOpen = false;
    try {
      SessionContext sessionContext = SessionContextHelper.getInstance().createSesstionContext();
      ThreadAttribute.set("CTX", sessionContext);
      if (DataAccessManager.getManager().isSessionOpen()) {
        session = DataAccessManager.getManager().fetchCurrentSession();
      } else {
        session = DataAccessManager.getManager().openSession();
        session.beginTransaction();
        isSessionOpen = true;
      } 
      Users user = repos.readUser(username);
      return user.getLockStatus();
    } catch (Exception e) {
      logger.log(Level.SEVERE, formatter.formatMessage("FatalException thrown  %s, AuthBean.authenticateUser()", new Object[] { THIS_COMPONENT_NAME }), (Throwable)e);
    } finally {
      if (isSessionOpen) {
        session.fetchCurrentTransaction().commit();
        DataAccessManager.getManager().closeSession(session);
      } 
    } 
    return true;
  }
}
