package com.ofss.cz.ccq.sms.dbAuthenticator.bean;

import com.ofss.cz.ccq.sms.dbAuthenticator.repos.CZDBIdentityRepository;
import com.ofss.fc.infra.log.impl.MultiEntityLogger;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import oracle.security.idm.IMException;

@Stateless(name = "DBIdentityBean", mappedName = "DBIdentityBean")
public class DBIdentityBean implements IDBIdentityBean {
  private static final String THIS_COMPONENT_NAME = CZDBIdentityRepository.class.getName();
  
  private static final MultiEntityLogger formatter = MultiEntityLogger.getUniqueInstance();
  
  private static transient Logger logger = MultiEntityLogger.getUniqueInstance()
    .getLogger(THIS_COMPONENT_NAME);
  
  public Map authenticateUser(String nationalID, Map requestDetails) {
    CZDBIdentityRepository repos = new CZDBIdentityRepository();
    try {
      return repos.authenticateUser(nationalID, requestDetails);
    } catch (IMException e) {
      if (logger.isLoggable(Level.SEVERE))
        logger.log(Level.SEVERE, formatter.formatMessage(
              "Error while generating response of method authenticateUser in the class %s. Exception details are: %s. ", new Object[] { THIS_COMPONENT_NAME, e })); 
      return null;
    } 
  }
  
  public List getGrantedRoles(String name) {
    CZDBIdentityRepository repos = new CZDBIdentityRepository();
    try {
      return repos.getGrantedRoles(name);
    } catch (IMException e) {
      if (logger.isLoggable(Level.SEVERE))
        logger.log(Level.SEVERE, formatter.formatMessage(
              "Error while generating response of method getGrantedRoles in the class %s. Exception details are: %s. ", new Object[] { THIS_COMPONENT_NAME, e })); 
      return null;
    } 
  }
}
