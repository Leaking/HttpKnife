package com.httpknife.github;


/**
 * 
 * @author Quinn
 * 
 */
public interface Github {

	public final static String HTTPS = "https://";
	public final static String HOST = "api.github.com";
	public final static String URL_SPLITTER = "/"; 
	public final static String API_HOST = HTTPS + HOST + URL_SPLITTER;
	
	public final static String CREATE_TOKEN = API_HOST + "authorizations"; 
	
	
	
	/**
	 * If the token has already existed,list all token and find it out,remove it and recreate it.
	 * @return
	 */
	public String createToken();
	
	
	/**
	 * List all token,the "token" attribute is empty.
	 */
	public void listToken();
	
	
	/**
	 * Remove token
	 */
	public void removeToken();
	
	
	/**
	 * login 
	 */
	public void loginUser();
	
}
