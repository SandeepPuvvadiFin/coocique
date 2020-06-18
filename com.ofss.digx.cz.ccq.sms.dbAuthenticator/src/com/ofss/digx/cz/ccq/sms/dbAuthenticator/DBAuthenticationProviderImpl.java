package com.ofss.digx.cz.ccq.sms.dbAuthenticator;

import java.util.HashMap;
import javax.security.auth.login.AppConfigurationEntry;
import javax.servlet.Filter;
import weblogic.logging.NonCatalogLogger;
import weblogic.management.security.ProviderMBean;
import weblogic.security.provider.PrincipalValidatorImpl;
import weblogic.security.spi.AuthenticationProviderV2;
import weblogic.security.spi.IdentityAsserterV2;
import weblogic.security.spi.PrincipalValidator;
import weblogic.security.spi.SecurityServices;
import weblogic.security.spi.ServletAuthenticationFilter;

@SuppressWarnings("deprecation")
public class DBAuthenticationProviderImpl implements AuthenticationProviderV2, ServletAuthenticationFilter {
	private static final String THIS_COMPONENT_NAME = DBAuthenticationProviderImpl.class.getName();

	private String a_sDescription;

	private DBAuthenticatorDatabase a_oDatabase;

	private AppConfigurationEntry.LoginModuleControlFlag a_oControlFlag;

	private static NonCatalogLogger a_oLogger = new NonCatalogLogger("Security.Authentication");

	public void initialize(ProviderMBean mbean, SecurityServices services) {
		a_oLogger.debug("DBAuthenticationProviderImpl.initialize");
		a_oLogger.debug("------------DBAuthenticationProviderImpl---------init-");
		CZOBDXDBAuthenticatorMBean oMyMBean = (CZOBDXDBAuthenticatorMBean) mbean;
		this.a_sDescription = String.valueOf(oMyMBean.getDescription()) + "\n" + oMyMBean.getVersion();
		String flag = oMyMBean.getControlFlag();
		if (flag.equalsIgnoreCase("REQUIRED")) {
			this.a_oControlFlag = AppConfigurationEntry.LoginModuleControlFlag.REQUIRED;
		} else if (flag.equalsIgnoreCase("OPTIONAL")) {
			this.a_oControlFlag = AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL;
		} else if (flag.equalsIgnoreCase("REQUISITE")) {
			this.a_oControlFlag = AppConfigurationEntry.LoginModuleControlFlag.REQUISITE;
		} else if (flag.equalsIgnoreCase("SUFFICIENT")) {
			this.a_oControlFlag = AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT;
		} else {
			throw new IllegalArgumentException("invalid flag value" + flag);
		}
	}

	public String getDescription() {
		return this.a_sDescription;
	}

	public void shutdown() {
		a_oLogger.debug("DBAuthenticatorDatabase.shutdown");
	}

	/**
	 * Returns configuration responsible for invoking exposed methods of
	 * Authentication
	 * 
	 * @param options
	 * @return AppConfigurationEntry
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private AppConfigurationEntry getConfiguration(HashMap options) {
		if (options.get("database") == null) {
			a_oDatabase = DBAuthenticatorDatabase.getInstance();
			options.put("database", a_oDatabase);
		}
		return new AppConfigurationEntry(DBLoginModuleImpl.class.getCanonicalName(), a_oControlFlag, options);
	}

	public AppConfigurationEntry getLoginModuleConfiguration() {
		HashMap<Object, Object> oOptions = new HashMap<>();
		return getConfiguration(oOptions);
	}

	public AppConfigurationEntry getAssertionModuleConfiguration() {
		HashMap<Object, Object> options = new HashMap<>();
		options.put("IdentityAssertion", "true");
		return getConfiguration(options);
	}

	public PrincipalValidator getPrincipalValidator() {
		return (PrincipalValidator) new PrincipalValidatorImpl();
	}

	public IdentityAsserterV2 getIdentityAsserter() {
		return null;
	}

	public Filter[] getServletAuthenticationFilters() {
		a_oLogger.debug("Entered DBAuthServletAuthenticationFilter.getServletAuthenticationFilters");
		Filter[] authFilters = new Filter[1];
		URLRedirecitonFilter urlRedirectionFilter = new URLRedirecitonFilter();
		authFilters[0] = urlRedirectionFilter;
		a_oLogger.debug("Exit DBAuthServletAuthenticationFilter.getServletAuthenticationFilters");
		return authFilters;
	}
}
