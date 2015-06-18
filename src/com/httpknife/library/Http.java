package com.httpknife.library;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
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
		try {
			openConnection(new URL(url));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
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
		connection.setDoOutput(true);
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

	public void get() {
		try {
			connection.setRequestMethod(Method.GET);
			responseFromConnection();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		System.out.println("status line -----");
		System.out.println(responseStatus.getStatusCode());
		System.out.println(responseStatus.getReasonPhrase());
		System.out.println(responseStatus.getProtocolVersion());
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
		// 取得输入流，并使用Reader读取
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		System.out.println("=============================");
		System.out.println("Contents of get request");
		System.out.println("=============================");
		String lines;
		try {
			while ((lines = reader.readLine()) != null) {
				System.out.println(lines);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return entity;
	}

	/**
	 * 获取响应报文头部
	 * 
	 * @param response
	 */
	public void headersFromConnection(HttpResponse response) {
		System.out.println("reponse headers ========");
		for (Entry<String, List<String>> header : connection.getHeaderFields()
				.entrySet()) {
			if (header.getKey() != null) {
				System.out.println("name = " + header.getKey() + "values = " + header.getValue());
				Header h = new BasicHeader(header.getKey(), header.getValue()
						.get(0));
				response.addHeader(h);
			}
		}
	}
	
	
}
