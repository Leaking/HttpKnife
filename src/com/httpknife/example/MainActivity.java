package com.httpknife.example;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.httpknife.library.Http;
import com.httpknife.library.Response;


public class MainActivity extends Activity {

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
				postRequest();
			}
		});
        btnVolly.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getVolley();
			}
		});
    }
    
    public void getVolley(){
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
				JsonObjectRequest js = new JsonObjectRequest("https://www.v2ex.com/api/members/show.json?username=Livid", null, new Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						System.out.println("volley resopnse = " + response);
					}
				}, new ErrorListener(){

					@Override
					public void onErrorResponse(VolleyError error) {
						System.out.println("volley onErrorResponse = " + error);

					}
					
				});
				queue.add(js);
			}
		}).start();
    }
    
  // String url = "https://www.v2ex.com/api/members/show.json";
   String url = "https://www.v2ex.com/api/members/show.json";
    public void getRequest(){
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				Http http = new Http(MainActivity.this);
				Map<String ,String> params = new HashMap<String,String>();
				params.put("username", "Livid");
//				params.put("keyword", "first+book");
				Response response = http.get(url,params);
				testResult(response);
			}
		}).start();
    }

    public void postRequest(){
    	final String url = "http://httpbin.org/post";
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				Http http = new Http(MainActivity.this);
				Map<String ,String> params = new HashMap<String,String>();
				params.put("username", "Livid");
				Response response = http.post(url, params);
				testResult(response);
			}
		}).start();
    }
    
    
    public void testResult(Response response){
    	System.out.println(response.statusCode());
		System.out.println(response.headers());
		System.out.println(response.body());
		System.out.println(response.json());
    }
    
    
    
    
   
}
