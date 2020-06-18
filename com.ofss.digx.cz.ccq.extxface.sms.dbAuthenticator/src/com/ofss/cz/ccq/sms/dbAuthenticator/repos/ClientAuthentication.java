package com.ofss.cz.ccq.sms.dbAuthenticator.repos;

import java.io.IOException;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A simple example that uses HttpClient to execute an HTTP request against a
 * target site that requires user authentication.
 */
public class ClientAuthentication {
	public static void main(String[] args) throws IOException {
		OkHttpClient client = new OkHttpClient().newBuilder().build();
		MediaType mediaType = MediaType.parse("text/plain");
		RequestBody body = RequestBody.create(mediaType, "");
		Request request = new Request.Builder().url("http://129.146.96.225:8777/ords/api_fas/autenticacion/solicitar")
				.method("POST", body).addHeader("authorization", "Bearer qBuBLHjoAZ3R31R9IAogXA")
				.addHeader("p_identificacion", "123123123121").build();
		Response response = client.newCall(request).execute();
		JSONObject SolicitarResponse = new JSONObject(response.body().string());
		if (0 == (Integer) SolicitarResponse.get("p_coderr")) {
			String pcodsol = (String) SolicitarResponse.get("p_codsol");
			System.out.println(pcodsol);
			String p_codverificacion = (String) SolicitarResponse.get("p_codverificacion");
			System.out.println(p_codverificacion);
			Integer timeout = ((Integer) SolicitarResponse.get("p_timeout"));
			System.out.println(timeout);
		} else {
			// throw new Exception((String) SolicitarResponse.get("p_err_descripcion"));
		}

		/**
		 * OkHttpClient client = new OkHttpClient().newBuilder() .build(); MediaType
		 * mediaType = MediaType.parse("application/x-www-form-urlencoded"); RequestBody
		 * body = RequestBody.create(mediaType, "grant_type=client_credentials"); String
		 * creds =
		 * Credentials.basic("lnMMEX0wDDzayaU_W6zGFQ..","o0bANrR0TfI8KL7T8ErAPg..");
		 * System.out.println(creds); Request request = new Request.Builder()
		 * .url("http://129.146.96.225:8777/ords/api_fas/oauth/token") .method("POST",
		 * body) // .addHeader("Authorization", "Basic
		 * bG5NTUVYMHdERHpheWFVX1c2ekdGUS4uOm8wYkFOclIwVGZJOEtMN1Q4RXJBUGcuLg==")
		 * .addHeader("Authorization", creds) .addHeader("Content-Type",
		 * "application/x-www-form-urlencoded") .build(); try { Response response =
		 * client.newCall(request).execute(); if(response.isSuccessful()) { String
		 * responseBody=response.body().string(); System.out.println(responseBody);
		 * JSONObject myObject = new JSONObject(responseBody);
		 * System.out.println("accesstoken is :"+myObject.get("access_token"));
		 * System.out.println("expires_in is :"+myObject.get("expires_in")); } } catch
		 * (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); }
		 **/
	}
}
