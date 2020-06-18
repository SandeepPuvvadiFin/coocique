package com.ofss.digx.cz.ccq.sms.dbAuthenticator;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import javax.naming.InitialContext;
import weblogic.logging.NonCatalogLogger;
import weblogic.management.utils.NotFoundException;

public final class DBAuthenticatorDatabase {
  private static NonCatalogLogger a_oLogger = new NonCatalogLogger("Security.Authentication");
  
  Object localHome;
  
  private static DBAuthenticatorDatabase instance;
  
  public DBAuthenticatorDatabase() {
    a_oLogger.debug("DBAuthenticatorDatabase.constructor()");
    Hashtable<Object, Object> localHashtable = new Hashtable<>();
    localHashtable.put("java.naming.factory.initial", "weblogic.jndi.WLInitialContextFactory");
    try {
      InitialContext localInitialContext = new InitialContext(localHashtable);
      this.localHome = localInitialContext.lookup("CZAuthBean#com.ofss.digx.cz.ccq.ejb.auth.ICZAuthBean");
    } catch (Exception localException) {
      a_oLogger.error("Exception while lookup CZAuthBean#com.ofss.digx.cz.ccq.ejb.auth.ICZAuthBean", 
          localException);
    } 
  }
  
  public static DBAuthenticatorDatabase getInstance() {
    if (instance == null || instance.localHome == null)
      instance = new DBAuthenticatorDatabase(); 
    return instance;
  }
  
  public synchronized Map authenticate(String paramString1, String paramString2, boolean paramBoolean, Map requestDetails) throws NotFoundException {
    a_oLogger.debug("CZDBAuthenticatorDatabase.authenticate()");
    Map map = null;
    try {
      if (this.localHome != null) {
        Class[] arrayOfClass = new Class[2];
        arrayOfClass[0] = String.class;
        arrayOfClass[1] = Map.class;
        Method localMethod = this.localHome.getClass().getDeclaredMethod("authenticateUser", arrayOfClass);
        map = (Map)localMethod.invoke(this.localHome, 
            new Object[] { paramString1, requestDetails });
      } 
    } catch (Exception localException) {
      a_oLogger.error("Exception while invoking CZAuthBean#com.ofss.digx.cz.ccq.ejb.auth.ICZAuthBean autheticate", 
          localException);
      throw new NotFoundException(
          "Exception while invoking CZAuthBean#com.ofss.digx.cz.ccq.ejb.auth.ICZAuthBean autheticate");
    } 
    return map;
  }
  
  public synchronized Enumeration getUserGroups(String paramString) {
    a_oLogger.error("DBAuthenticatorDatabase.getUserGroups() for: " + paramString);
    try {
      if (this.localHome != null) {
        Class[] arrayOfClass = new Class[1];
        arrayOfClass[0] = String.class;
        Method localMethod = this.localHome.getClass().getDeclaredMethod("getGrantedRoles", arrayOfClass);
        return Collections.enumeration((List)localMethod.invoke(this.localHome, new Object[] { paramString }));
      } 
    } catch (Exception localException) {
      a_oLogger.error(
          "Exception while invoking CZAuthBean#com.ofss.digx.cz.ccq.ejb.auth.ICZAuthBean getGrantedRoles", 
          localException);
    } 
    return null;
  }
  
  public synchronized String getUserName(String paramString) {
    a_oLogger.error("DBAuthenticatorDatabase.getUserGroups() for: " + paramString);
    try {
      if (this.localHome != null) {
        Class[] arrayOfClass = new Class[1];
        arrayOfClass[0] = String.class;
        Method localMethod = this.localHome.getClass().getDeclaredMethod("getUserName", arrayOfClass);
        return (String)localMethod.invoke(this.localHome, new Object[] { paramString });
      } 
    } catch (Exception localException) {
      a_oLogger.error(
          "Exception while invoking CZAuthBean#com.ofss.digx.cz.ccq.ejb.auth.ICZAuthBean getUserName", 
          localException);
    } 
    return null;
  }
}
