package com.httpknife.github;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.httpknife.library.Base64;
import com.httpknife.library.HttpKnife;
import com.httpknife.library.Response;

public class GithubImpl implements Github{

	public final static String HTTPS = "https://";
	public final static String HOST = "api.github.com";
	public final static String URL_SPLITTER = "/"; 
	public final static String API_HOST = HTTPS + HOST + URL_SPLITTER;
	
	public final static String ACCEPT = "application/vnd.github.beta+json";
	public final static String AGENT_USER = "GithubKnife/1.0";
	public final static String TOKEN_NOTE = "GithubKnife APP Token";
	
	
	
	
	public final static String CREATE_TOKEN = API_HOST + "authorizations"; //POST
	public final static String LIST_TOKENS = API_HOST + "authorizations"; //GET
	public final static String REMOVE_TOKEN = API_HOST + "authorizations" + URL_SPLITTER; //DELETE

	
	private HttpKnife http;
	
	public GithubImpl(Context context){
		http = new HttpKnife(context);
	}
	
	
	
	@Override
	public String createToken(String username,String password){
		JSONObject json = new JSONObject();
		try {
			json.put("note", TOKEN_NOTE);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Response response = http.post(CREATE_TOKEN).headers(configreHttpHeader()).basicAuthorization(username, password).json(json)
				.response();
		testResult(response);
		return null;
	}

	@Override
	public String listToken(String username,String password){
		Response response = http.get(LIST_TOKENS).headers(configreHttpHeader()).basicAuthorization(username, password).response();
		testResult(response);

		return "";
	}

	@Override
	public void removeToken(String username,String password){
		//String id = listToken(username,password);
		String id = "20030183";
		Response response = http.delete(REMOVE_TOKEN + id).headers(configreHttpHeader()).basicAuthorization(username, password).response();
		testResult(response);

	}

	@Override
	public void loginUser(String token) {
		
	}


	@Override
	public Map<String,String> configreHttpHeader() {
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("Accept", ACCEPT);
		headers.put("User-Agent", AGENT_USER);
		return headers;
	}

	
	public void testResult(Response response) {
		System.out.println(response.statusCode());
		System.out.println(response.headers());
		System.out.println(response.body());
	}
	
}
