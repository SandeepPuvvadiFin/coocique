package com.ofss.cz.ccq.sms.dbAuthenticator.repos;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.hc.client5.http.fluent.Form;
import org.apache.hc.client5.http.fluent.Content;
import org.apache.hc.client5.http.fluent.Executor;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.util.Timeout;
import org.json.JSONObject;

public class test {
	public static void main(String[] args) {

		try {

			
			HttpHost host = HttpHost.create("http://129.146.96.225:8777");
			String username = "lnMMEX0wDDzayaU_W6zGFQ..";
			char[] password = "o0bANrR0TfI8KL7T8ErAPg..".toCharArray();
			Executor executor = Executor.newInstance().auth(host, username, password).
					authPreemptive(host);
			

			String ans = executor.execute(Request.post("http://129.146.96.225:8777/ords/api_fas/oauth/token").useExpectContinue()
					.bodyString("grant_type:client_credentials", ContentType.APPLICATION_FORM_URLENCODED))
					.returnContent().asString();
			System.out.println(ans);

			Content accessToken = Request.post("http://129.146.96.225:8777/ords/api_fas/oauth/token")
					.bodyForm(Form.form().add("username", "lnMMEX0wDDzayaU_W6zGFQ..")
							.add("password", "o0bANrR0TfI8KL7T8ErAPg..").build())
					.bodyString("grant_type:client_credentials", ContentType.APPLICATION_FORM_URLENCODED).execute()
					.returnContent();

			System.out.println(accessToken.asString());
			JSONObject myObject = new JSONObject(
					"{\n\t\t\t\t  \"p_coderr\": \"string\",\n\t\t\t\t  \"p_codsol\": \"codsolvalue\",\n\t\t\t\t  \"p_codverificacion\": \"string\",\n\t\t\t\t  \"p_err_descripcion\": \"string\",\n\t\t\t\t  \"p_resumendoc\": \"string\",\n\t\t\t\t  \"p_timeout\": 0\n\t\t\t\t}");
			myObject.get("p_codsol");
			System.out.println(Request.get("http://129.146.96.225:7777/digx/v1/enumerations/gender").execute()
					.returnContent().asString());
			Content response = Request.post("http://172.28.54.193:8080/ords/api_fas/autenticacion/solicitar")
					.connectTimeout(Timeout.ofSeconds(60L)).addHeader("accept", "application/json")
					.addHeader("p_identificacion", "123412345").execute().returnContent();
			System.out.println(response.asString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	




}
