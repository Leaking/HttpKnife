package com.httpknife.example;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.httpknife.github.Github;
import com.httpknife.github.GithubImpl;
import com.httpknife.library.Base64;
import com.httpknife.library.HttpKnife;
import com.httpknife.library.Response;

public class MainActivity extends Activity {

	private static final String LOG_TAG = "MainActivity";
	Button createToken;
	Button removeToken;
	Button listToken;
	Handler handler;
	Github github;
	
	String username = "";
	String password = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		createToken = (Button) findViewById(R.id.createToken);
		listToken = (Button) findViewById(R.id.listToken);
		removeToken = (Button) findViewById(R.id.removeToken);

		github = new GithubImpl(MainActivity.this);

		createToken.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				System.out.println("createToken");
				new Thread(new Runnable() {

					@Override
					public void run() {
						github.createToken(username, password);
					}
				}).start();
			}
		});
		listToken.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				System.out.println("listToken");
				new Thread(new Runnable() {

					@Override
					public void run() {
						github.listToken(username, password);
					}
				}).start();

			}
		});
		removeToken.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				System.out.println("removeToken");
				new Thread(new Runnable() {

					@Override
					public void run() {
						github.removeToken(username, password);
					}
				}).start();

			}
		});

	}

	/**
	 * get请求，带参数以及不带参数 ok
	 */
	public void getRequest() {
		final String url = "https://www.v2ex.com/api/members/show.json";
		new Thread(new Runnable() {

			@Override
			public void run() {
				HttpKnife http = new HttpKnife(MainActivity.this);
				Map<String, String> params = new HashMap<String, String>();
				params.put("username", "Livid");
				// params.put("keyword", "first+book");
				Response response = http.get(url, params).response();
				testResult(response);
			}
		}).start();
	}

	/**
	 * 提交表单请求 OK
	 */
	public void postRequest() {
		final String url = "http://httpbin.org/post";
		new Thread(new Runnable() {

			@Override
			public void run() {
				HttpKnife http = new HttpKnife(MainActivity.this);
				Map<String, String> params = new HashMap<String, String>();
				params.put("username", "Livid");

				Map<String, String> params2 = new HashMap<String, String>();
				params2.put("uuuuuu", "sssss");

				Response response = http.post(url).form(params).form(params2)
						.response();
				testResult(response);
			}
		}).start();
	}

	/**
	 * 上传文件请求 ok
	 */
	public void postFileRequest() {
		final String url = "http://httpbin.org/post";
		new Thread(new Runnable() {

			@Override
			public void run() {
				HttpKnife http = new HttpKnife(MainActivity.this);
				Map<String, String> params = new HashMap<String, String>();
				params.put("userID", "638fa451-cfc6-4650-9d0d-9e0bc6b322cf");
				params.put("familyID", "2555e078-5113-42aa-ab4c-757f6cc226c9");
				params.put("description", "Livid");

				File file = new File(
						"/storage/emulated/0/Android/data/com.pingan.family.application/cache/cropImage/IMG_20150627_160812.jpg");
				File tempFile = createTempFile("gggg.txt", 1223);
				Response response = http.post(url)
						.mutipart(params, "test", "file.txt", tempFile)
						.response();
				testResult(response);
			}
		}).start();
	}

	public File createTempFile(String namePart, int byteSize) {
		try {
			File f = File.createTempFile(namePart, "_handled", getCacheDir());
			FileOutputStream fos = new FileOutputStream(f);
			Random r = new Random();
			byte[] buffer = new byte[byteSize];
			r.nextBytes(buffer);
			fos.write(buffer);
			fos.flush();
			fos.close();
			return f;
		} catch (Throwable t) {
			Log.e(LOG_TAG, "createTempFile failed", t);
		}
		return null;
	}

	/**
	 * gzip请求 ok
	 */
	public void getGZIPRequest() {
		final String url = "http://httpbin.org/gzip";
		new Thread(new Runnable() {

			@Override
			public void run() {
				HttpKnife http = new HttpKnife(MainActivity.this);
				Response response = http.get(url).gzip().response();
				testResult(response);
			}
		}).start();
	}

	public void author(final String username, final String password) {
		final String url = "https://api.github.com/user";

		new Thread(new Runnable() {

			@Override
			public void run() {
				HttpKnife http = new HttpKnife(MainActivity.this);
				Map<String, String> headers = new HashMap<String, String>();
				headers.put("Accept", "application/vnd.github.beta+json");
				headers.put("User-Agent", "GitHubJava/2.1.0");
				headers.put("Authorization",
						"Basic " + Base64.encode(username + ':' + password));
				Response response = http.get(url).headers(headers).response();
				testResult(response);
			}
		}).start();

	}

	public void loginWithToken() {
		final String url = "https://api.github.com/user";

		new Thread(new Runnable() {

			@Override
			public void run() {
				HttpKnife http = new HttpKnife(MainActivity.this);
				Map<String, String> headers = new HashMap<String, String>();
				headers.put("Accept", "application/vnd.github.beta+json");
				headers.put("User-Agent", "GitHubJava/2.1.0");
				headers.put("Authorization",
						"Token 5xxxxxxxxx66182463da54fc911f7de7224d58");
				Response response = http.get(url).headers(headers).response();
				testResult(response);
			}
		}).start();
	}

	public void getOrCreateToken(final String username, final String password) {
		final String url = "https://api.github.com/authorizations";

		new Thread(new Runnable() {

			@Override
			public void run() {
				HttpKnife http = new HttpKnife(MainActivity.this);
				Map<String, String> headers = new HashMap<String, String>();
				headers.put("Accept", "application/vnd.github.beta+json");
				headers.put("User-Agent", "GitHubJava/2.1.0");
				headers.put("Authorization",
						"Basic " + Base64.encode(username + ':' + password));

				JSONObject json = new JSONObject();
				try {
					json.put("note", "Git 22222n Demo");
				} catch (JSONException e) {
					e.printStackTrace();
				}

				Response response = http.get(url).headers(headers).response();
				testResult(response);
			}
		}).start();

	}

	public void token(final String username, final String password) {
		final String url = "https://api.github.com/authorizations";

		new Thread(new Runnable() {

			@Override
			public void run() {
				HttpKnife http = new HttpKnife(MainActivity.this);
				Map<String, String> headers = new HashMap<String, String>();
				headers.put("Accept", "application/vnd.github.beta+json");
				headers.put("User-Agent", "GitHubJava/2.1.0");
				headers.put("Authorization",
						"Basic " + Base64.encode(username + ':' + password));

				JSONObject json = new JSONObject();
				try {
					json.put("note", "Git  Demo");

				} catch (JSONException e) {
					e.printStackTrace();
				}

				Response response = http.post(url).headers(headers).json(json)
						.response();
				testResult(response);
			}
		}).start();

	}

	public void events() {
		final String url = "https://api.github.com/users/Leaking/received_events?page=1&per_page=10";

		new Thread(new Runnable() {

			@Override
			public void run() {
				HttpKnife http = new HttpKnife(MainActivity.this);
				Map<String, String> headers = new HashMap<String, String>();
				headers.put("Accept", "application/vnd.github.beta+json");
				headers.put("User-Agent", "GitHubJava/2.1.0");
				headers.put("Authorization",
						"Token xxxxxxxxxxb6c22e25e64fff6f9dc7618a7f54f9733");

				Response response = http.get(url).headers(headers).response();
				try {
					JSONArray jsonArray = new JSONArray(response.body());
					for (int i = 0; i < jsonArray.length(); i++) {
						System.out.println(jsonArray.get(i));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				// testResult(response);
			}
		}).start();
	}

	public void volleyEvnent() {
		// final String url =
		// "https://api.github.com/users/Leaking/received_events?page=2";
		final String url = "https://api.github.com/users/Leaking/received_events/public";

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", "application/vnd.github.beta+json");
		headers.put("User-Agent", "GitHubJava/2.1.0");
		// headers.put("Authorization",
		// "Token xxxxxxb6c22e25e64fff6f9dc7618a7f54f9733");
		RequestQueue queue = Volley.newRequestQueue(this);
		JsonObjectRequest rq = new JsonObjectRequest(headers, url, null,
				new Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						// TODO Auto-generated method stub
						System.out.println("response =====");
						System.out.println(response.toString());
					}
				}, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						// TODO Auto-generated method stub
						System.out.println("error =====");
						System.out.println(error);
					}
				});
		queue.add(rq);
	}

	public void testResult(Response response) {
		System.out.println(response.statusCode());
		System.out.println(response.headers());
		System.out.println(response.body());
		System.out.println(response.jsonArray());
	}

}
