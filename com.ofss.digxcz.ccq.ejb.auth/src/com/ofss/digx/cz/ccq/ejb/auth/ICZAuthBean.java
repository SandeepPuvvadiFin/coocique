package com.ofss.digx.cz.ccq.ejb.auth;

import com.ofss.digx.infra.exceptions.Exception;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import oracle.security.idm.IMException;

public interface ICZAuthBean extends Remote {
  Map authenticateUser(String paramString, Map paramMap) throws RemoteException;
  
  List getGrantedRoles(String paramString) throws RemoteException, IMException;
  
  boolean getUserLockStatus(String paramString) throws RemoteException, Exception;
  
  String getUserName(String paramString) throws RemoteException, IMException;
}
