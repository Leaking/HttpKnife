package com.httpknife.library;

import java.util.Map;
import java.util.TreeMap;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HTTP;

public class Response {
	
	private int statusCode;
	private String reasonPhrase;
	private byte[] content;
	private Map<String,String> headers;
	private String charset;
	
	private HttpResponse response;
	
	public Response(HttpResponse response){
		this.response = response;

	}
	
	private void parseStatusCode(){
		this.reasonPhrase = response.getStatusLine().getReasonPhrase();
	}
	
	private void parseReasonPhrase(){
		this.statusCode = response.getStatusLine().getStatusCode();
	}
	
	private void parseCharset(){
		String contentType = headers.get(HTTP.CONTENT_TYPE);
        charset = HTTP.DEFAULT_CONTENT_CHARSET;
        if (contentType != null) {
            String[] params = contentType.split(";");
            for (int i = 1; i < params.length; i++) {
                String[] pair = params[i].trim().split("=");
                if (pair.length == 2) {
                    if (pair[0].equals("charset")) {
                    	charset = pair[1];
                    	return;
                    }
                }
            }
        }
	}
	
	private void parseContent(){
		 

	}

    private void parseHeaders() {
    	Header[] rawHeaders = response.getAllHeaders();
    	headers = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0; i < rawHeaders.length; i++) {
        	headers.put(rawHeaders[i].getName(), rawHeaders[i].getValue());
        }
        parseCharset();
    }
	
	
	
	
}
