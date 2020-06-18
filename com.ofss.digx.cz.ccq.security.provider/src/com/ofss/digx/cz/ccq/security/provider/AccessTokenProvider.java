package com.ofss.digx.cz.ccq.security.provider;

import java.io.IOException;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.json.JSONObject;

import com.ofss.fc.infra.config.ConfigurationFactory;
import com.ofss.fc.infra.log.impl.MultiEntityLogger;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AccessTokenProvider {

	/**
	 * The component name for this class
	 */
	private static final String THIS_COMPONENT_NAME = AccessTokenProvider.class.getName();

	/**
	 * Instance of java.util.logging.Logger
	 */
	private static transient Logger logger = MultiEntityLogger.getUniqueInstance().getLogger(THIS_COMPONENT_NAME);
	private Preferences securityPreferences = null;

	private AccessToken token = null;

	/**
	 * Singleton instance of the repository
	 */
	private static AccessTokenProvider singletonInstance;

	private String username = null;
	private String password = null;
	private String url = null;

	private AccessTokenProvider() {
		this.securityPreferences = ConfigurationFactory.getInstance().getConfigurations("SecurityConstants");
		this.username = this.securityPreferences.get("ords_username", "lnMMEX0wDDzayaU_W6zGFQ..");
		this.password = this.securityPreferences.get("ords_password", "o0bANrR0TfI8KL7T8ErAPg..");
		this.url = this.securityPreferences.get("ords_accessToken",
				"http://172.28.54.45:8585/ords/api_fas/oauth/token");

	}

	/**
	 * This method returns unique instance of AccessTokenProvider
	 * 
	 * @return AccessTokenProvider
	 */
	public static AccessTokenProvider getInstance() {
		if (singletonInstance == null) {
			synchronized (AccessTokenProvider.class) {
				if (singletonInstance == null) {
					singletonInstance = new AccessTokenProvider();
				}
			}
		}
		return singletonInstance;
	}

	public String getAccessToken() {
		if (token == null || token.getToken() == null || token.getToken().isEmpty() || isTokenExpired()) {
			generateToken();
		}
		return token.getToken();
	}

	public boolean isTokenExpired() {
		if (Instant.now().isBefore(token.getGenerationTime().plusSeconds(token.getExpireTime()).minusSeconds(300))) {
			return false;

		} else {
			return true;
		}
	}

	private void generateToken() {
		System.out.println("");
		OkHttpClient client = new OkHttpClient().newBuilder().build();
		MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
		RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials");

		try {
			Request request = new Request.Builder().url(url).method("POST", body)
					.addHeader("Authorization", Credentials.basic(this.username, this.password))
					.addHeader("Content-Type", "application/x-www-form-urlencoded").build();
			Response response = client.newCall(request).execute();
			if (response.isSuccessful()) {
				JSONObject accessTokenResponse = new JSONObject(response.body().string());
				token = new AccessToken();
				token.setGenerationTime(Instant.now());
				token.setExpireTime((Integer) accessTokenResponse.get("expires_in"));
				token.setToken((String) accessTokenResponse.get("access_token"));
			}

		} catch (IOException e) {
			logger.log(Level.SEVERE, "exception occured while generating token. exception details are", e);
		}
	}

}
