/*******************************************************************************
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
 *******************************************************************************/
package com.ofss.digx.cz.ccq.appx.framework.session;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.security.auth.Subject;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ofss.digx.appx.AbstractRESTApplication;
import com.ofss.digx.cz.ccq.framework.security.session.entity.CZUserSession;
import com.ofss.digx.cz.ccq.framework.security.session.entity.CZUserSessionKey;
import com.ofss.digx.infra.exceptions.Exception;
import com.ofss.fc.infra.config.ConfigurationFactory;
import com.ofss.fc.infra.das.orm.DataAccessManager;
import com.ofss.fc.infra.log.impl.MultiEntityLogger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import oracle.security.jps.util.SubjectUtil;

/**
 * Rest service providing mechanisms for performing session related activities
 * like validating requests and creating nonce for new sessions.
 */
@Tag(name = "User Session Management", description = "Create and delete user session along with nonce generation.")
@Path("/session")
public class CZSession extends AbstractRESTApplication implements ISession {

	/**
	 * Stores the name of the entity(class) represented by this {@code Class} object
	 * as a {@code String}.
	 */
	private static final String THIS_COMPONENT_NAME = CZSession.class.getName();

	/**
	 * Create instance of multi-entity logger.
	 */
	private static final MultiEntityLogger FORMATTER = MultiEntityLogger.getUniqueInstance();

	/**
	 * This is an instance variable which is required to support multi-entity wide
	 * logging.
	 */
	private static final Logger LOGGER = FORMATTER.getLogger(THIS_COMPONENT_NAME);
	/**
	 * Preference Name to identify Framework properties from the Configuration Pool.
	 */
	private static final String FRAMEWORK_CONSTANTS = "FWConfig";
	/**
	 * A {@link Preferences} object to store framework configuration indicating the
	 * framework property name and framework property value of the configuration to
	 * be loaded.
	 */
	private static Preferences fwConstants = ConfigurationFactory.getInstance().getConfigurations(FRAMEWORK_CONSTANTS);
	/**
	 * Application Identifier to open new ORM Session.
	 */
	private static final String APPLICATION_ID = fwConstants.get("APPLICATION_ID", "DIGX");

	/**
	 * Public constructor.
	 */
	public CZSession() {
	}

	/**
	 * REST service creates a {@link HttpSession} for the current operating user. An
	 * HttpSession will be created in case, one does not exist already. The
	 * httpSession instance generated here will be used to identify an anonymous
	 * user session and all sensitive data for this session will be protected. The
	 * concepts of Nonce and Indirection will be available to use with this session.
	 * Hence, Originations being available to anonymous users will be secured
	 * against a session.
	 * 
	 * @return Generate an httpSsession
	 * @digx.Category Authentication
	 * @digx.Subcategory Session
	 */
	@Override
	@Operation(summary = "Create a new HTTP session if one does not already exist.", 
	description = "Creates a new HTTP session on request. "
			+ "The API is required to be invoked only if subsequent APIs being called are being invoked without authenticated credentials and atleast one of the following conditions is met: "
			+ "<ul><li>The subsequent APIs being called require an HTTP session.</li>"
			+ "<li>The subsequent APIs being called require Nonce validation</li>"
			+ "<li>The subsequent APIs being have fields in the response or request that need indirection.</li></ul>")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Session created successfully"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Exception.class))) })
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{digitalID}")
	public Response create(
			@Parameter(in = ParameterIn.PATH, required = true, name = "digitalID", description = "Digital id of user", schema = @Schema(type = "String")) @PathParam("digitalID") String digitalID) {
		Response response = null;
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.log(Level.FINE, "Entered into Session Create()");
		}
		com.ofss.fc.infra.das.orm.Session session = null;
		try {
			if (getHttpRequest().getSession(false) != null) {
				Subject subject = SubjectUtil.getCurrentSubject();
				String userName = SubjectUtil.getUserName(subject);
				CZUserSessionKey userSessionKey = new CZUserSessionKey();
				userSessionKey.setSessionId(getHttpRequest().getSession().getId());
				userSessionKey.setUserName(userName);
				CZUserSession czuserSession = new CZUserSession();
				czuserSession.setKey(userSessionKey);
				czuserSession.setDigitalID(digitalID);
				session = DataAccessManager.getManager().openSession(APPLICATION_ID);
				session.beginTransaction();
				czuserSession = (CZUserSession) session.save(czuserSession);
				DataAccessManager.getManager().commitTransaction();
			} else {
				LOGGER.log(Level.SEVERE, FORMATTER
						.formatMessage("session is null while create for czsession. digital id =%s", digitalID));
			}
		} catch (java.lang.Exception e) {
			LOGGER.log(Level.SEVERE, FORMATTER.formatMessage(
					"Exception encountered while invoking the core service create for czsession. digital id =%s",
					digitalID), e);
			response = buildResponse(new Exception(e), Response.Status.BAD_REQUEST);
		} finally {
			if (session != null)
				DataAccessManager.getManager().closeSession(session);
		}

		response = Response.status(Response.Status.CREATED).entity(new String("[]")).build();
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.log(Level.FINE, "Exiting from Session Create()");
		}

		return response;
	}

}
