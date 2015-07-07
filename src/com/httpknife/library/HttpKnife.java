package com.httpknife.library;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

public class HttpKnife {

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
	private static final String CHARSET_UTF8 = "UTF-8";
	/**
	 * 提交字符串形式的键值对，post请求
	 */
	private static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";

	/**
	 * 'application/json' content type header value
	 */
	private static final String CONTENT_TYPE_JSON = "application/json";

	private static final String BOUNDARY = "00content0boundary00";

	private static final String CONTENT_TYPE_MULTIPART = "multipart/form-data; boundary="
			+ BOUNDARY;

	private static final String MUTIPART_LINE = "--" + BOUNDARY + CRLF;
	private static final String MUTIPART_END_LINE = "--" + BOUNDARY + "--"
			+ CRLF;
	private static final String GZIP = "gzip";


	public interface RequestHeader {
		public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
		public static final String USER_AGENT = "User-Agent";
		public static final String CONTENT_TYPE = "Content-Type";
		public static final String AUTHORIZATION = "Authorization";

	}

	public interface ResponseHeader {
		public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
	}

	public static final int DEFAULT_CONNECT_TIMEOUT_MS = 2500;
	public static final int DEFAULT_READ_TIMEOUT_MS = 2500;

	private HttpURLConnection connection;
	private boolean connect = false;
	private OutputStream output;
	private Context context;
	private Map<String, String> customHeaders;

	/**
	 * 构造器
	 * 
	 * @param context
	 */
	public HttpKnife(Context context) {
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
		if (connection == null) {
			throw new IllegalStateException("You have not build the connection");
		}
		connection.setRequestProperty(name, value);
	}

	public String getResponseheader(final String name) {
		return connection.getHeaderField(name);
	}

	public HttpKnife gzip() {
		addHeader(RequestHeader.HEADER_ACCEPT_ENCODING, GZIP);
		return this;
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

	public HttpKnife headers(Map<String, String> headers) {
		for (String key : headers.keySet()) {
			addHeader(key, headers.get(key));
		}
		return this;
	}

	/**
	 * 不带参数的get请求
	 * 
	 * @param url
	 * @return
	 */
	public HttpKnife get(String url) {
		try {
			openConnection(new URL(url));
			connection.setRequestMethod(Method.GET);
			return this;
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 带参数的get请求
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	public HttpKnife get(String url, Map<?, ?> params) {
		UrlRewriter rw = new DefaultUriRewriter();
		url = rw.rewriteWithParam(url, params);
		System.out.println("encode and add params url =========");
		System.out.println(url);
		return get(url);
	}

	/**
	 * post请求初始化
	 * 
	 * @param url
	 * @throws ProtocolException
	 * @throws MalformedURLException
	 */
	public HttpKnife post(String url) {
		try {
			openConnection(new URL(url));
			connection.setRequestMethod(Method.POST);
			connection.setDoOutput(true);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		}
		return this;
	}

	/**
	 * post请求，mutipart类型
	 * 
	 * @param url
	 * @param params
	 * @param filename
	 * @param fileParamName
	 * @param file
	 * @return
	 */

	public OutputStream openOutput() throws IOException {
		if (connect)
			return output;
		else {
			output = connection.getOutputStream();
			connect = true;
			return output;
		}
	}

	public HttpKnife form(Map<String, String> params) {
		try {
			if (!connect) {
				String contentType = getBodyContentType(CONTENT_TYPE_FORM,
						getParamsEncoding());
				addHeader(RequestHeader.CONTENT_TYPE, contentType);
			}
			String encoding = getParamsEncoding();
			StringBuilder encodedParams = new StringBuilder();
			for (Map.Entry<String, String> entry : params.entrySet()) {
				encodedParams
						.append(URLEncoder.encode(entry.getKey(), encoding));
				encodedParams.append('=');
				encodedParams.append(URLEncoder.encode(entry.getValue(),
						encoding));
				encodedParams.append('&');
			}
			byte[] body = encodedParams.toString().getBytes(encoding);
			DataOutputStream out = new DataOutputStream(openOutput());
			out.write(body);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	public HttpKnife json(JSONObject json) {
		try {
			String contentType = getBodyContentType(CONTENT_TYPE_JSON,
					getParamsEncoding());
			addHeader(RequestHeader.CONTENT_TYPE, contentType);
			byte[] body = json.toString().getBytes(getParamsEncoding());
			DataOutputStream out = new DataOutputStream(openOutput());
			out.write(body);
			return this;
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * post表格，包含字键值，文件
	 * 
	 * @param params
	 * @param encoding
	 * @param name
	 * @param filename
	 * @param file
	 */
	public HttpKnife mutipart(Map<String, String> params, String name,
			String filename, File file) {
		try {
			addHeader(RequestHeader.CONTENT_TYPE, getMutiPartBodyContentType());
			DataOutputStream dos = new DataOutputStream(openOutput());
			if (params != null) {
				for (Entry<String, String> entry : params.entrySet()) {
					dos.writeBytes(MUTIPART_LINE);
					partString(entry.getKey(), entry.getValue());
				}
			}
			if (name == null || file == null){
				dos.writeBytes(MUTIPART_END_LINE);
				return this;
			}
			if (filename == null || filename.isEmpty()) {
				throw new IllegalArgumentException("上次文件的文件名不能为空");
			}
			dos.writeBytes(MUTIPART_LINE);
			partFile(name, filename, file);
			dos.writeBytes(MUTIPART_END_LINE);
			return this;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * 提交字符串
	 * 
	 * @param dos
	 * @param key
	 * @param value
	 * @throws IOException
	 */
	public HttpKnife partString(String key, String value) throws IOException {
		if (value == null || value.isEmpty())
			throw new IllegalArgumentException("上传键值对不能为空");
		DataOutputStream dos = new DataOutputStream(openOutput());
		dos.writeBytes("Content-Disposition: form-data; name=\"" + key + "\""
				+ CRLF);
		dos.writeBytes(CRLF);
		dos.writeBytes(value);
		dos.writeBytes(CRLF);
		return this;
	}

	/**
	 * 提交文件
	 * 
	 * @param dos
	 * @param name
	 * @param fileName
	 * @param file
	 * @throws IOException
	 */
	public HttpKnife partFile(String name, String fileName, File file)
			throws IOException {
		DataOutputStream dos = new DataOutputStream(openOutput());
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
		return this;
	}

	/**
	 * 后去mutipart的contenttype
	 * 
	 * @return
	 */
	public String getMutiPartBodyContentType() {
		return CONTENT_TYPE_MULTIPART;
	}

	/**
	 * 请求主体媒体类型以及编码格式
	 * 
	 * @param contentType
	 * @param charset
	 * @return
	 */
	public String getBodyContentType(String contentType, String charset) {
		return contentType + "; charset=" + charset;
	}

	/**
	 * 请求主体编码格式
	 * 
	 * @return
	 */
	public String getParamsEncoding() {
		return CHARSET_UTF8;
	}

	public HttpKnife put(String url) {
		return this;
	}

	public HttpKnife delete(String url) {
		try {
			openConnection(new URL(url));
			connection.setRequestMethod(Method.DELETE);
			return this;
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public HttpKnife head(String url) {
		try {
			openConnection(new URL(url));
			connection.setRequestMethod(Method.HEAD);
			return this;
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public HttpKnife option(String url) {
		try {
			openConnection(new URL(url));
			connection.setRequestMethod(Method.OPTIONS);
			return this;
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public HttpKnife trace(String url) {
		try {
			openConnection(new URL(url));
			connection.setRequestMethod(Method.TRACE);
			return this;
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public HttpKnife patch(String url) {
		try {
			openConnection(new URL(url));
			connection.setRequestMethod(Method.PATCH);
			return this;
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Basic Authorization
	 * 
	 * @param username
	 * @param password
	 */
	public void basicAuthorization(String username, String password) {
		addHeader(RequestHeader.AUTHORIZATION,
				"Basic " + Base64.encode(username + ':' + password));
	}

	/**
	 * 获取响应报文
	 * 
	 * @return
	 * @throws IOException
	 */
	public Response response() {
		try {
			if (connect) {
				output.flush();
				output.close();
			}
			BasicHttpResponse httpResponse = new BasicHttpResponse(
					statusLineFromConnection());
			httpResponse.setEntity(entityFromConnection());
			headersFromConnection(httpResponse);
			Response response = new Response(httpResponse);
			return response;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
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
	 * @throws IOException
	 */
	private HttpEntity entityFromConnection() throws IOException {
		BasicHttpEntity entity = new BasicHttpEntity();
		InputStream inputStream;
		try {
			inputStream = connection.getInputStream();
		} catch (IOException ioe) {
			inputStream = connection.getErrorStream();
		}
		if (GZIP.equals(getResponseheader(ResponseHeader.HEADER_CONTENT_ENCODING))) {
			entity.setContent(new GZIPInputStream(inputStream));
		} else {
			entity.setContent(inputStream);
		}
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
