package com.httpknife.library;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

public class Http {

	/**
	 * Supported request methods.
	 */
	public interface Method {
		String GET = "GET";
		String POST = "POST";
		String PUT = "PUT";
		String DELETE = "DELETE";
		String HEAD = "HEAD";
		String OPTIONS = "OPTIONS";
		String TRACE = "TRACE";
		String PATCH = "PATCH";
	}

	public interface DefaultRequestHeaderValue {
		public static final String CHARSET_UTF8 = "UTF-8";
		/**
		 * 提交字符串形式的键值对，post请求
		 */
		public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";

		/**
		 * 'application/json' content type header value
		 */
		public static final String CONTENT_TYPE_JSON = "application/json";
	}

	public interface RequestHeader {
		public static final String USER_AGENT = "User-Agent";
		public static final String CONTENT_TYPE = "Content-Type";

	}

	public static final int DEFAULT_CONNECT_TIMEOUT_MS = 2500;
	public static final int DEFAULT_READ_TIMEOUT_MS = 2500;

	private HttpURLConnection connection;
	private int responseCode;
	private Context context;

	public Http(Context context, String url) {
		this.context = context;

	}

	/**
	 * 根据url建立一个新连接
	 * 
	 * @param url
	 */
	private void openConnection(URL url) {
		try {
			connection = (HttpURLConnection) url.openConnection();
			initConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initConnection() {
		connection.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT_MS);
		connection.setReadTimeout(DEFAULT_READ_TIMEOUT_MS);
		connection.setUseCaches(true);
		connection.setDoInput(true);
		// 需要传入数据才用这个，否则容易报出method not allow
		// connection.setDoOutput(true);
		
		
		//URLEncodedUtils.format(parameters, encoding)
	}

	private void addHeaders(HashMap<String, String> headers) {
		for (String headerName : headers.keySet()) {
			addHeader(headerName, headers.get(headerName));
		}
	}

	public void addHeader(String name, String value) {
		connection.setRequestProperty(name, value);
	}

	public String getCustomUserAgent() {
		String userAgent = "Request/0";
		try {
			String packageName = context.getPackageName();
			PackageInfo info = context.getPackageManager().getPackageInfo(
					packageName, 0);
			userAgent = packageName + "/" + info.versionCode;
		} catch (NameNotFoundException e) {

		}
		return userAgent;
	}

	public Response get(String url) {
		try {
			openConnection(new URL(url));
			connection.setRequestMethod(Method.GET);
			HttpResponse httpResponse = responseFromConnection();
			Response response = new Response(httpResponse);
			return response;
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return null;
	}

	
	public Response get(String url,Map<String,String> params) {
		UrlRewriter rw = new DefaultUriRewriter();
		url = rw.rewriteWithParam(url, params);
		System.out.println("encode and add params url =========");
		System.out.println(url);
		return get(url);
	}

	/**
	 * 获取响应报文
	 * 
	 * @return
	 * @throws IOException
	 */
	private HttpResponse responseFromConnection() throws IOException {
		BasicHttpResponse response = new BasicHttpResponse(
				statusLineFromConnection());
		response.setEntity(entityFromConnection());
		headersFromConnection(response);
		return response;
	}

	/**
	 * 获取响应报文的起始行
	 * 
	 * @return
	 * @throws IOException
	 */
	private StatusLine statusLineFromConnection() throws IOException {
		ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
		int responseCode = connection.getResponseCode();
		if (responseCode == -1) {
			throw new IOException(
					"Could not retrieve response code from HttpUrlConnection.");
		}
		StatusLine responseStatus = new BasicStatusLine(protocolVersion,
				connection.getResponseCode(), connection.getResponseMessage());
		return responseStatus;
	}

	/**
	 * 获取响应报文实体
	 * 
	 * @return
	 */
	private HttpEntity entityFromConnection() {
		BasicHttpEntity entity = new BasicHttpEntity();
		InputStream inputStream;
		try {
			inputStream = connection.getInputStream();
		} catch (IOException ioe) {
			inputStream = connection.getErrorStream();
		}
		entity.setContent(inputStream);
		entity.setContentLength(connection.getContentLength());
		entity.setContentEncoding(connection.getContentEncoding());
		entity.setContentType(connection.getContentType());
		return entity;
	}

	/**
	 * 获取响应报文头部
	 * 
	 * @param response
	 */
	public void headersFromConnection(HttpResponse response) {
		for (Entry<String, List<String>> header : connection.getHeaderFields()
				.entrySet()) {
			if (header.getKey() != null) {
				Header h = new BasicHeader(header.getKey(), header.getValue()
						.get(0));
				response.addHeader(h);
			}
		}
	}

}
