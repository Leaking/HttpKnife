package com.httpknife.example;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.httpknife.library.Base64;
import com.httpknife.library.HttpKnife;
import com.httpknife.library.Response;

public class MainActivity extends Activity {


	private static final String LOG_TAG = "MainActivity";
	Button btnGet;
	Button btnVolly;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		btnGet = (Button) findViewById(R.id.get);
		btnVolly = (Button) findViewById(R.id.volleyget);
		btnGet.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				System.out.println("clickckckc");
				author("github nickname","github password");
			}
		});
		btnVolly.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
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
				Response response = http.post(url, params).response();
				testResult(response);
			}
		}).start();
	}

	/**
	 * 上传文件请求 not ok
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
				File tempFile = createTempFile("gggg.txt",1223);
				Response response = http.post(url, params, "file",
						"gggg", tempFile).response();
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
				HttpKnife http = new HttpKnife(MainActivity.this).gzip();
				Response response = http.get(url).response();
				testResult(response);
			}
		}).start();

	}
	
	public void author(final String username, final String password){
		final String url = "https://api.github.com/user";
		
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				HttpKnife http = new HttpKnife(MainActivity.this);
				Map<String,String> headers = new HashMap<String,String>();
				headers.put("Accept", "application/vnd.github.beta+json");
				headers.put("User-Agent", "GitHubJava/2.1.0");
				headers.put("Authorization", "Basic " + Base64.encode(username + ':' + password));
				Response response = http.get(url).headers(headers).response();
				testResult(response);
			}
		}).start();
		

		
	}

	public void testResult(Response response) {
		System.out.println(response.statusCode());
		System.out.println(response.headers());
		System.out.println(response.body());
		System.out.println(response.json());
	}

}
