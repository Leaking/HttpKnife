package com.httpknife.library;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	private static final String CRLF = "\r\n";

	public static final String CHARSET_UTF8 = "UTF-8";
	/**
	 * 提交字符串形式的键值对，post请求
	 */
	public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";

	/**
	 * 'application/json' content type header value
	 */
	public static final String CONTENT_TYPE_JSON = "application/json";

	private static final String BOUNDARY = "00content0boundary00";

	private static final String CONTENT_TYPE_MULTIPART = "multipart/form-data; boundary="
			+ BOUNDARY;

	private static final String MUTIPART_LINE = "--" + BOUNDARY + CRLF;
	private static final String MUTIPART_END_LINE = "--" + BOUNDARY
			+ "--" + CRLF;

	public interface RequestHeader {
		public static final String USER_AGENT = "User-Agent";
		public static final String CONTENT_TYPE = "Content-Type";

	}

	public static final int DEFAULT_CONNECT_TIMEOUT_MS = 2500;
	public static final int DEFAULT_READ_TIMEOUT_MS = 2500;

	private HttpURLConnection connection;
	private int responseCode;
	private Context context;

	public Http(Context context) {
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

	public Response get(String url, Map<?, ?> params) {
		UrlRewriter rw = new DefaultUriRewriter();
		url = rw.rewriteWithParam(url, params);
		System.out.println("encode and add params url =========");
		System.out.println(url);
		return get(url);
	}

	private void post(String url) throws ProtocolException,
			MalformedURLException {
		openConnection(new URL(url));
		connection.setRequestMethod(Method.POST);
		connection.setDoOutput(true);
	}

	/**
	 * 
	 * @param url
	 * @param params
	 * @param filename
	 * @param fileParamName
	 * @param file
	 * @return
	 */
	public Response post(String url, Map<String, String> params,
			String name, String filename, File file) {
		try {
			post(url);
			addHeader(RequestHeader.CONTENT_TYPE, getMutiPartBodyContentType());
			form(params, null, name, filename, file);
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

	/**
	 * post提交键值对
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	public Response post(String url, Map<String, String> params) {
		try {
			post(url);
			String contentType = getBodyContentType(CONTENT_TYPE_FORM,
					getParamsEncoding());
			addHeader(RequestHeader.CONTENT_TYPE, contentType);
			byte[] body = form(params, getParamsEncoding());
			DataOutputStream out = new DataOutputStream(
					connection.getOutputStream());
			out.write(body);
			out.close();
			HttpResponse httpResponse = responseFromConnection();
			Response response = new Response(httpResponse);
			return response;
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public void form(Map<String, String> params, String encoding,
			String name, String filename, File file) {
		try {
			DataOutputStream dos = new DataOutputStream(
					connection.getOutputStream());
			dos.writeBytes(MUTIPART_LINE);
			if (params != null) {
				for (Entry<String, String> entry : params.entrySet()) {
					partString(dos, entry.getKey(), entry.getValue());
					//dos.writeBytes(MUTIPART_MIDDLE_LINE);
				}
			}
			partFile(dos, name, filename, file);
			dos.writeBytes(MUTIPART_END_LINE);
			dos.flush();
			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void partString(DataOutputStream dos, String key, String value)
			throws IOException {
		if(value == null || value.isEmpty())
			throw new IllegalArgumentException("上传键值对不能为空");
		dos.writeBytes("Content-Disposition: form-data; name=\"" + key + "\""
				+ CRLF);
		dos.writeBytes(CRLF);
		dos.writeBytes(value);
		dos.writeBytes(CRLF);
	}

	public void partFile(DataOutputStream dos, String name,
			String fileName, File file) throws IOException {
		if(name == null || file == null)
			return;
		if(fileName == null || fileName.isEmpty()){
			throw new IllegalArgumentException("上次文件的文件名不能为空");
		}
		dos.writeBytes("Content-Disposition: form-data; name=\"" + name
				+ "\";filename=\"" + fileName + "\"" + CRLF);
		dos.writeBytes("Content-Type: "
				+ URLConnection.guessContentTypeFromName(fileName));
		dos.writeBytes(CRLF);
		dos.writeBytes(CRLF);
		System.out.println("guessContentTypeFromName "
				+ URLConnection.guessContentTypeFromName(fileName));
		FileInputStream inputStream = new FileInputStream(file);
		byte[] buffer = new byte[4096];
		int bytesRead = -1;
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			dos.write(buffer, 0, bytesRead);
		}
		dos.writeBytes(CRLF);
		inputStream.close();
	}

	public byte[] form(Map<String, String> params, String encoding) {
		StringBuilder encodedParams = new StringBuilder();
		try {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				encodedParams
						.append(URLEncoder.encode(entry.getKey(), encoding));
				encodedParams.append('=');
				encodedParams.append(URLEncoder.encode(entry.getValue(),
						encoding));
				encodedParams.append('&');
			}
			return encodedParams.toString().getBytes(encoding);
		} catch (UnsupportedEncodingException uee) {
			throw new RuntimeException("Encoding not supported: " + encoding,
					uee);
		}

	}

	public String getMutiPartBodyContentType() {
		return CONTENT_TYPE_MULTIPART;
	}

	public String getBodyContentType(String contentType, String charset) {
		return contentType + "; charset=" + charset;
	}

	public String getParamsEncoding() {
		return CHARSET_UTF8;
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
