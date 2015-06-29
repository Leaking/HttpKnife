package com.httpknife.example;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.httpknife.library.HttpKnife;
import com.httpknife.library.Response;


public class MainActivity extends Activity {

	
//	06-27 15:40:00.990: I/ApiClient(24633): upload userid = 638fa451-cfc6-4650-9d0d-9e0bc6b322cf
//			06-27 15:40:00.990: I/ApiClient(24633): upload f_id = 2555e078-5113-42aa-ab4c-757f6cc226c9
//			06-27 15:40:00.990: I/ApiClient(24633): upload description = gggg

	
	
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
				postFileRequest();
			}
		});
        btnVolly.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			}
		});
    }
    

  // String url = "https://www.v2ex.com/api/members/show.json";
   String url = "https://www.v2ex.com/api/members/show.json";
    public void getRequest(){
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				HttpKnife http = new HttpKnife(MainActivity.this);
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
				HttpKnife http = new HttpKnife(MainActivity.this);
				Map<String ,String> params = new HashMap<String,String>();
				params.put("username", "Livid");
				Response response = http.post(url, params);
				testResult(response);
			}
		}).start();
    }
    
    
    public void postFileRequest(){
    	final String url = "http://sales.yun.pingan.com:80/family_chat/uploadFile.do";
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				HttpKnife http = new HttpKnife(MainActivity.this);
				Map<String ,String> params = new HashMap<String,String>();
				params.put("userID", "638fa451-cfc6-4650-9d0d-9e0bc6b322cf");
				params.put("familyID", "2555e078-5113-42aa-ab4c-757f6cc226c9");
				params.put("description", "Livid");
				
				File file = new File("/storage/emulated/0/Android/data/com.pingan.family.application/cache/cropImage/IMG_20150627_160812.jpg");
				Response response = http.post(url,params,"file","IMG_20150627_160812.jpg",file);
				testResult(response);
			}
		}).start();
    }
    
    public void getGZIPRequest(){
    	final String url = "http://httpbin.org/gzip";
    	
    	
    	
    }
    
    
    public void testResult(Response response){
    	System.out.println(response.statusCode());
		System.out.println(response.headers());
		System.out.println(response.body());
    }
    
    
    
    
   
}
