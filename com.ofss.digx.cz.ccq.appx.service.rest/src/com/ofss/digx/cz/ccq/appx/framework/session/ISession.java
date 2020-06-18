package com.ofss.digx.cz.ccq.appx.framework.session;

import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;

/**
 * REST Service Interface defines method to perform Session related activities.
 */
public interface ISession {

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
	 */
	public Response create(String digitalID);
}