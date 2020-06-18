package com.ofss.digx.cz.ccq.framework.security.session.entity;

import java.lang.reflect.Field;

import com.ofss.digx.cz.ccq.framework.security.session.entity.repository.CZUserSessionRepository;
import com.ofss.digx.infra.exceptions.Exception;
import com.ofss.fc.framework.domain.AbstractDomainObject;
import com.ofss.fc.framework.domain.IPersistenceObject;

/**
 * 
 * @author kinjal
 *
 */
public class CZUserSession extends AbstractDomainObject implements IPersistenceObject {

	private static final long serialVersionUID = 266416346745258485L;

	private CZUserSessionKey key;

	private boolean digitalSignLogin;

	private String digitalID;

	public boolean isDigitalSignLogin() {
		return digitalSignLogin;
	}

	public void setDigitalSignLogin(boolean digitalSignLogin) {
		this.digitalSignLogin = digitalSignLogin;
	}

	public String getDigitalID() {
		return digitalID;
	}

	public void setDigitalID(String digitalID) {
		this.digitalID = digitalID;
	}

	public CZUserSessionKey getKey() {
		return key;
	}

	public void setKey(CZUserSessionKey key) {
		this.key = key;
	}

	@Override
	protected void validate() {

	}

	public void create(CZUserSession cZUserSession) throws Exception {
		CZUserSessionRepository.getInstance().create(cZUserSession);
	}

	public CZUserSession read(CZUserSessionKey key) throws Exception {
		return CZUserSessionRepository.getInstance().read(key);
	}

	public void update(CZUserSession cZUserSession) throws Exception {
		CZUserSessionRepository.getInstance().update(cZUserSession);
	}

	public void delete(CZUserSession cZUserSession) throws Exception {
		CZUserSessionRepository.getInstance().delete(cZUserSession);
	}

}