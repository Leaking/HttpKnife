package com.httpknife.library;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

public class Response {
	
	private int statusCode;
	private String reasonPhrase;
	private byte[] contentBytes;
	private String contentString;
	private Map<String,String> headers;
	private String charset;
	private boolean hasParseHeader = false;
	
	private HttpResponse response;
	
	public Response(HttpResponse response){
		this.response = response;
		parseStatusCode().parseReasonPhrase().parseHeaders().parseContent();

	}
	
	private Response parseStatusCode(){
		this.reasonPhrase = response.getStatusLine().getReasonPhrase();
		return this;
	}
	
	private Response parseReasonPhrase(){
		this.statusCode = response.getStatusLine().getStatusCode();
		return this;
	}
	
	private Response parseCharset(){
		if(hasParseHeader == false){
			throw new IllegalStateException("You have not parse headers");
		}
		String contentType = headers.get(HTTP.CONTENT_TYPE);
        this.charset = HTTP.DEFAULT_CONTENT_CHARSET;
        if (contentType != null) {
            String[] params = contentType.split(";");
            for (int i = 1; i < params.length; i++) {
                String[] pair = params[i].trim().split("=");
                if (pair.length == 2) {
                    if (pair[0].equals("charset")) {
                    	this.charset = pair[1];
                    	return this;
                    }
                }
            }
        }
        return this;
	}
	
	/**
	 * turn reponse content into byte array
	 */
	private Response parseContent(){
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[16384];
		try {
			InputStream is = response.getEntity().getContent();
			while ((nRead = is.read(data, 0, data.length)) != -1) {
			  buffer.write(data, 0, nRead);
			}
			buffer.flush();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.contentBytes = buffer.toByteArray();
		if(this.charset == null){
			throw new IllegalStateException("You have parse charset");
		}
		try {
			this.contentString = new String(this.contentBytes,this.charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return this;
	}

    private Response parseHeaders() {
    	Header[] rawHeaders = response.getAllHeaders();
    	headers = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0; i < rawHeaders.length; i++) {
        	headers.put(rawHeaders[i].getName(), rawHeaders[i].getValue());
        }
        hasParseHeader = true;
        parseCharset();
        return this;
    }

	public int statusCode() {
		return statusCode;
	}

	public String reasonPhrase() {
		return reasonPhrase;
	}

	public byte[] contentBytes() {
		return contentBytes;
	}

	public String body() {
		return contentString;
	}

	public JSONObject json(){
		try {
			return new JSONObject(contentString);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Map<String, String> headers() {
		return headers;
	}

	public String charset() {
		return charset;
	}

	public boolean isHasParseHeader() {
		return hasParseHeader;
	}

	public HttpResponse getResponse() {
		return response;
	}
	
	
	
	
}
