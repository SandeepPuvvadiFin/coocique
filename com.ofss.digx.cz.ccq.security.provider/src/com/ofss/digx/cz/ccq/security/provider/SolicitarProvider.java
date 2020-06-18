package com.ofss.digx.cz.ccq.security.provider;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.ofss.digx.cz.ccq.app.session.dto.SolicitarResponseDTO;
import com.ofss.fc.infra.config.ConfigurationFactory;
import com.ofss.fc.infra.log.impl.MultiEntityLogger;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SolicitarProvider {
	/**
	 * Stores the name of the entity(class) represented by this {@code Class} object
	 * as a {@code String}
	 */
	private static final String THIS_COMPONENT_NAME = SolicitarProvider.class.getName();

	/**
	 * Create instance of multi-entity logger
	 */
	private static final MultiEntityLogger FORMATTER = MultiEntityLogger.getUniqueInstance();
	/**
	 * This is an instance variable which is required to support multi-entity wide
	 * logging.
	 */
	private static final Logger logger = FORMATTER.getLogger(THIS_COMPONENT_NAME);

	public SolicitarResponseDTO GenerateCode(String DigitalSignature) throws Exception {
		String accessToken = AccessTokenProvider.getInstance().getAccessToken();
		Preferences securityPreferences = ConfigurationFactory.getInstance().getConfigurations("SecurityConstants");
		String url = securityPreferences.get("solicitar_URL",
				"http://129.146.96.225:8777/ords/api_fas/autenticacion/solicitar");

		try {
			ValidateDigitalSignature(DigitalSignature);
			OkHttpClient client = new OkHttpClient().newBuilder().build();

			MediaType mediaType = MediaType.parse("text/plain");
			RequestBody body = RequestBody.create(mediaType, "");
			Request request = new Request.Builder().url(url).method("POST", body)
					.addHeader("authorization", "Bearer " + accessToken).addHeader("p_identificacion", DigitalSignature)
					.build();
			Response response = client.newCall(request).execute();
			if (response.isSuccessful()) {
				JSONObject SolicitarResponse = new JSONObject(response.body().string());
				System.out.println(0 == (Integer) SolicitarResponse.get("p_coderr"));
				if (0 == (Integer) SolicitarResponse.get("p_coderr")) {
					SolicitarResponseDTO solResDTO = new SolicitarResponseDTO();
					solResDTO.setCodeReference((String) SolicitarResponse.get("p_codsol"));
					solResDTO.setCodeToDisplay(((String) SolicitarResponse.get("p_codverificacion")));
					solResDTO.setEspiarTimeout((Integer) SolicitarResponse.get("p_timeout"));
					return solResDTO;
				} else {
					throw new Exception((String) SolicitarResponse.get("p_err_descripcion"));
				}
			} else {
				throw new com.ofss.digx.infra.exceptions.Exception("DIGX_CZ_AUTH_002");
			}
			
		} catch (Exception e) {
			if (logger.isLoggable(Level.SEVERE)) {
				logger.log(Level.SEVERE,
						FORMATTER.formatMessage(
								"Failure while generating Digital Signature code %s., exiting function.",
								this.getClass().getName()),
						e);
			}
			throw e;
		}

	}

	private void ValidateDigitalSignature(String digitalSignature) throws com.ofss.digx.infra.exceptions.Exception {
		boolean isvalid = false;
		// to match with 0#-####-####
		final String regex1 = "0[0-9](-)[0-9]{4}(-)[0-9]{4}";
		// match 5########### and 1###########
		final String regex2 = "[15][0-9]{11}";

		if (Pattern.matches(regex1, digitalSignature)) {
			isvalid = true;
			return;
		} else if (Pattern.matches(regex2, digitalSignature)) {
			isvalid = true;
			return;
		}

		if (!isvalid)
			throw new com.ofss.digx.infra.exceptions.Exception("DIGX_CZ_AUTH_001");

	}

	/*
	 * public static void main(String[] args) {
	 * 
	 * // to match with 0#-####-#### final String regex1 =
	 * "0[0-9](-)[0-9]{4}(-)[0-9]{4}"; // match 5########### and 1########### final
	 * String regex2 = "[15][0-9{11}";
	 * 
	 * System.out.println(Pattern.matches(regex1, "02-0599-0219"));
	 * System.out.println(Pattern.matches(regex1, "02-0599e-0219"));
	 * System.out.println(Pattern.matches(regex1, "02-0599-0219435"));
	 * 
	 * }
	 */
}
