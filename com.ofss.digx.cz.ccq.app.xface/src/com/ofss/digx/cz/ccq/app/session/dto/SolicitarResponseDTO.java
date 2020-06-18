package com.ofss.digx.cz.ccq.app.session.dto;

import com.ofss.digx.service.response.BaseResponseObject;

public class SolicitarResponseDTO extends BaseResponseObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8687727578241320186L;
	private String codeToDisplay;
	
	private int espiarTimeout;
	
	private String codeReference;

	public String getCodeToDisplay() {
		return codeToDisplay;
	}

	public void setCodeToDisplay(String code) {
		this.codeToDisplay = code;
	}

	public int getEspiarTimeout() {
		return espiarTimeout;
	}

	public void setEspiarTimeout(int espiarTimeout) {
		this.espiarTimeout = espiarTimeout;
	}

	public String getCodeReference() {
		return codeReference;
	}

	public void setCodeReference(String codeReference) {
		this.codeReference = codeReference;
	}

}
