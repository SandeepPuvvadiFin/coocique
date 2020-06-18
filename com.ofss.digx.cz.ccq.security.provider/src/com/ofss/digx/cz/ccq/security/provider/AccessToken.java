package com.ofss.digx.cz.ccq.security.provider;

import java.time.Instant;

public class AccessToken {
	/**
	 * access token
	 */
	private String token;
	/**
	 * generation time for the token
	 */
	private Instant generationTime;
	/**
	 * expire time
	 */
	private Integer expireTime;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Instant getGenerationTime() {
		return generationTime;
	}

	public void setGenerationTime(Instant generationTime) {
		this.generationTime = generationTime;
	}

	public int getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(int expireTime) {
		this.expireTime = expireTime;
	}

}
