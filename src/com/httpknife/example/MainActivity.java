package com.httpknife.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.httpknife.library.Http;


public class MainActivity extends Activity {

	Button btnGet;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnGet = (Button) findViewById(R.id.get);
        btnGet.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getRequest();
			}
		});
    }
    
    
    public void getRequest(){
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				Http http = new Http(MainActivity.this,"https://www.baidu.com/");
				http.get();
			}
		}).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
