package com.ofss.digx.cz.ccq.sms.dbAuthenticator;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import weblogic.logging.NonCatalogLogger;
import weblogic.management.utils.NotFoundException;
import weblogic.security.principal.WLSGroupImpl;
import weblogic.security.principal.WLSUserImpl;

public class DBLoginModuleImpl implements LoginModule {
  private Subject a_oSubject;
  
  private CallbackHandler a_oCallbackHandler;
  
  private DBAuthenticatorDatabase a_oDatabase;
  
  private boolean a_bIsIdentityAssertion;
  
  private static final String THIS_COMPONENT_NAME = DBLoginModuleImpl.class.getName();
  
  private boolean a_bLoginSucceeded;
  
  private boolean a_bPrincipalsInSubject;
  
  private Vector a_collPrincipalsForSubject = new Vector();
  
  private static NonCatalogLogger a_oLogger = new NonCatalogLogger("Security.Authentication");
  
  public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
    a_oLogger.debug("------------initialize----------");
    this.a_oSubject = subject;
    this.a_oCallbackHandler = callbackHandler;
    this.a_bIsIdentityAssertion = "true".equalsIgnoreCase((String)options.get("IdentityAssertion"));
    this.a_oDatabase = (DBAuthenticatorDatabase)options.get("database");
    a_oLogger.debug("------------initialize----------");
  }
  
  public boolean login() throws LoginException {
    a_oLogger.debug("------------login----------");
    Callback[] callbacks = getCallbacks();
    byte b;
    int i;
    Callback[] arrayOfCallback1;
    for (i = (arrayOfCallback1 = callbacks).length, b = 0; b < i; ) {
      Callback callback = arrayOfCallback1[b];
      System.out.println("callback:" + callback);
      b++;
    } 
    String sUserName = getUserName(callbacks);
    if (sUserName.length() > 0 && !this.a_bIsIdentityAssertion)
      try {
        Map<String, String> requestDetails = new HashMap<>();
        String sPasswordHave = getPasswordHave(sUserName, callbacks);
        Map<String, String> map = this.a_oDatabase.authenticate(sUserName, sPasswordHave, false, requestDetails);
        if (map == null || !map.isEmpty()) {
          Map.Entry<String, String> entry = map.entrySet().iterator().next();
          throwFailedLoginException(entry.getValue());
          this.a_bLoginSucceeded = false;
          return false;
        } 
      } catch (NotFoundException shouldNotHappen) {
        a_oLogger.alert(shouldNotHappen.getMessage());
        a_oLogger.debug(shouldNotHappen.getLocalizedMessage());
        this.a_bLoginSucceeded = false;
        throwFailedLoginException("Authentication failed");
      }  
    this.a_bLoginSucceeded = true;
    this.a_collPrincipalsForSubject.add(new WLSUserImpl(this.a_oDatabase.getUserName(sUserName)));
    addGroupsForSubject(sUserName);
    return this.a_bLoginSucceeded;
  }
  
  public boolean commit() throws LoginException {
    if (this.a_bLoginSucceeded) {
      this.a_oSubject.getPrincipals().addAll(this.a_collPrincipalsForSubject);
      this.a_bPrincipalsInSubject = true;
      return true;
    } 
    return false;
  }
  
  public boolean abort() throws LoginException {
    if (this.a_bPrincipalsInSubject) {
      this.a_oSubject.getPrincipals().removeAll(this.a_collPrincipalsForSubject);
      this.a_bPrincipalsInSubject = false;
    } 
    return true;
  }
  
  public boolean logout() throws LoginException {
    a_oLogger.debug("DBLoginModuleImpl.logout");
    return true;
  }
  
  private void throwLoginException(String msg) throws LoginException {
    a_oLogger.debug("Throwing LoginException(" + msg + ")");
    throw new LoginException(msg);
  }
  
  private void throwFailedLoginException(String msg) throws FailedLoginException {
    a_oLogger.debug("Throwing FailedLoginException(" + msg + ")");
    throw new FailedLoginException(msg);
  }
  
  private Callback[] getCallbacks() throws LoginException {
    Callback[] callbacks;
    if (this.a_oCallbackHandler == null)
      throwLoginException("No CallbackHandler Specified"); 
    if (this.a_oDatabase == null)
      throwLoginException("database not specified"); 
    this.a_bIsIdentityAssertion = false;
    if (this.a_bIsIdentityAssertion) {
      callbacks = new Callback[1];
    } else {
      callbacks = new Callback[2];
      callbacks[1] = new PasswordCallback("password: ", false);
    } 
    callbacks[0] = new NameCallback("username: ");
    try {
      this.a_oCallbackHandler.handle(callbacks);
    } catch (IOException e) {
      throw new LoginException(e.toString());
    } catch (UnsupportedCallbackException e) {
      throwLoginException(String.valueOf(e.toString()) + " " + e.getCallback().toString());
    } 
    return callbacks;
  }
  
  private String getUserName(Callback[] callbacks) throws LoginException {
    String userName = ((NameCallback)callbacks[0]).getName();
    if (userName == null)
      throwLoginException("Username not supplied."); 
    a_oLogger.debug("\tuserName\t= " + userName);
    return userName;
  }
  
  private void addGroupsForSubject(String userName) {
    for (Enumeration<String> e = this.a_oDatabase.getUserGroups(userName); e.hasMoreElements(); ) {
      String groupName = e.nextElement();
      a_oLogger.debug("\tgroupName\t= " + groupName);
      this.a_collPrincipalsForSubject.add(new WLSGroupImpl(groupName));
    } 
  }
  
  private String getPasswordHave(String userName, Callback[] callbacks) throws LoginException {
    PasswordCallback passwordCallback = (PasswordCallback)callbacks[1];
    char[] password = passwordCallback.getPassword();
    passwordCallback.clearPassword();
    if (password == null || password.length < 1)
      throwLoginException("Authentication Failed: User " + userName + ". Password not supplied"); 
    String passwd = new String(password);
    return passwd;
  }
}
