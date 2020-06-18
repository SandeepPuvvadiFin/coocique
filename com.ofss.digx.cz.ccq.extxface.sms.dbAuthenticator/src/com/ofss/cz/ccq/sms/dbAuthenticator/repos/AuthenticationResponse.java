package com.ofss.cz.ccq.sms.dbAuthenticator.repos;

import org.json.JSONObject;

public class AuthenticationResponse {
  private boolean overallResult;
  
  private JSONObject responseObject;
  
  private JsonObjectType jsonObjectType;
  
  AuthenticationResponse(boolean overallResult, JSONObject responseObject, JsonObjectType jsonObjectType) {
    this.overallResult = overallResult;
    this.responseObject = responseObject;
    this.jsonObjectType = jsonObjectType;
  }
  
  public boolean isOverallResult() {
    return this.overallResult;
  }
  
  public void setOverallResult(boolean overallResult) {
    this.overallResult = overallResult;
  }
  
  public JSONObject getResponseObject() {
    return this.responseObject;
  }
  
  public void setResponseObject(JSONObject responseObject) {
    this.responseObject = responseObject;
  }
  
  public JsonObjectType getJsonObjectType() {
    return this.jsonObjectType;
  }
  
  public void setJsonObjectType(JsonObjectType jsonObjectType) {
    this.jsonObjectType = jsonObjectType;
  }
}
