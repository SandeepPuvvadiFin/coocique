package com.ofss.digx.cz.ccq.framework.security.session.entity;

import com.ofss.fc.framework.domain.AbstractDomainObjectKey;

/**
 * 
 * @author kinjal
 *
 */
public class CZUserSessionKey extends AbstractDomainObjectKey {

	private static final long serialVersionUID = 8913313531832091141L;

	/**
	 * Represents a unique user id.
	 */
	private String userName;

	/**
	 * Represents a unique user id.
	 */
	private String sessionId;

	/**
	 * Returns the sessionId in form of {@link String}.
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * Sets the sessionId.
	 * 
	 * @param sessionId in the form of {@link String}.
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * Returns the key.
	 * 
	 * @return key in the form of {@link String}.
	 */
	@Override
	public String keyAsString() {

		return getUserName();
	}

	/**
	 * Returns user name.
	 * 
	 * @return user name in the form of {@link String}.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Sets the user name.
	 * 
	 * @param userName {@link String} contains the username.
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

}
