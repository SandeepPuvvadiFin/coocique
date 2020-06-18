package com.ofss.cz.ccq.sms.dbAuthenticator.bean;

import java.util.List;
import java.util.Map;

public interface IDBIdentityBean {
  Map authenticateUser(String paramString, Map paramMap);
  
  List getGrantedRoles(String paramString);
}
