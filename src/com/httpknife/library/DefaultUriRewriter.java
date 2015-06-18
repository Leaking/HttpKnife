package com.httpknife.library;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Map;

import org.apache.http.protocol.HTTP;

public class DefaultUriRewriter implements UrlRewriter{

	/**
	 * 1,使用URI toASCILL
	 * 2,使用URLEncodedUtils.format
	 */
	@Override
	public String rewriteWithParam(String originUrl, Map<?, ?> params) {
		try {
			String decodedURL = URLDecoder.decode(originUrl, HTTP.UTF_8);
			 URL _url = new URL(decodedURL);
			 URI _uri = new URI(_url.getProtocol(), _url.getUserInfo(), _url.getHost(), _url.getPort(), _url.getPath(), _url.getQuery(), _url.getRef());
			 originUrl = _uri.toASCIIString();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String rewriteWithEncoding(String originUrl) {
		return null;
	}

}
