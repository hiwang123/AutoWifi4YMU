package com.example.autowifi4ymu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import android.support.v7.app.ActionBarActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	
	Button applyButton;
	EditText idText, pwdText;
	boolean activate;
	static final String STATE = "state";
	public static String idString, pwdString;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initialize();
		
		SharedPreferences prefs = getSharedPreferences(STATE, Context.MODE_PRIVATE);
		idString = prefs.getString("idString","");
		pwdString = prefs.getString("pwdString","");
		idText.setText(idString);
		pwdText.setText(pwdString);
		
	}
	
	public void initialize(){
		idText = (EditText) findViewById(R.id.editText1);
		pwdText = (EditText) findViewById(R.id.editText2);
		applyButton = (Button) findViewById(R.id.button1);
		applyButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				idString = idText.getText().toString();
				pwdString = pwdText.getText().toString();
				Toast.makeText(MainActivity.this, "Saved", Toast.LENGTH_SHORT).show();
				wifiInitialize();
			}
		});
	}
	
	public void wifiInitialize(){
		WifiManager wiFiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		if( !wiFiManager.isWifiEnabled() ){
			wiFiManager.setWifiEnabled(true);
			wiFiManager.setWifiEnabled(false);
		}
		else{
			wiFiManager.setWifiEnabled(false);
			wiFiManager.setWifiEnabled(true);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		SharedPreferences pref = getSharedPreferences(STATE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean("activate", activate);
		editor.putString("idString", idString);
		editor.putString("pwdString", pwdString);
		editor.commit();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		SharedPreferences pref = getSharedPreferences(STATE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean("activate", activate);
		editor.putString("idString", idString);
		editor.putString("pwdString", pwdString);
		editor.commit();
		super.onPause();
	}
	
	public static class WifiReceiver extends BroadcastReceiver{
		
		Context context;
		
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
		//	Toast.makeText(context, action, Toast.LENGTH_SHORT).show();
			this.context = context;
	        if ( WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action) ) {
	        	NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
	            if (netInfo.isConnected()) {
	            	Toast.makeText(context, "Wifi on", Toast.LENGTH_SHORT).show();
	                checkConnectedTargetWifi();
	            }
	        }
		}
		
		
		public void checkConnectedTargetWifi(){
			String targetSSID = "NTU";
			boolean connected = false;
			
			WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifi = wifiManager.getConnectionInfo();
	        if (wifi != null) {
	            String ssid = wifi.getSSID();
	            connected = targetSSID.equals(ssid) || targetSSID.equals("\""+ssid+"\"");
	       //     Toast.makeText(context, ssid+" "+idString, Toast.LENGTH_SHORT).show();
	        }
	        if(connected){
	        	Toast.makeText(context, "connecting to "+targetSSID, Toast.LENGTH_SHORT).show();
	        	AutoWifiAuthentic();
	        }
	        else{
	      //  	Toast.makeText(context, "oops!", Toast.LENGTH_SHORT).show();
	        }
	    }
		
		public void AutoWifiAuthentic(){
			
			//String result="e";
			
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					HttpClient client = new DefaultHttpClient();
					HttpParams params = new BasicHttpParams();
					params.setParameter("http.protocol.handle-redirects",false);
					HttpPost httppost = new HttpPost("http://140.112.4.237/auth/loginnw.html");
					httppost.setParams(params);
					String result="e";

			        try {
			        	// Add data
			        	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			            nameValuePairs.add(new BasicNameValuePair("username", idString));
			            nameValuePairs.add(new BasicNameValuePair("password", pwdString));
						httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
						
						// Execute HTTP Post Request
						HttpResponse response = client.execute(httppost);
						//result = response.getHeaders("Location")[0].getValue();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			        
			        HttpGet httpGet = new HttpGet("http://www.google.com.tw/");
			        try {
						HttpResponse response = client.execute(httpGet);
						Looper.prepare();  
						Toast.makeText(context, "Connect Successful", Toast.LENGTH_SHORT).show();
						Looper.loop();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Looper.prepare();  
		                Toast.makeText(context, "Wrong password", Toast.LENGTH_SHORT).show();  
		                Looper.loop();
						e.printStackTrace();
					} finally {
					     client.getConnectionManager().shutdown();
					}
				}
			});
			thread.start();
		}		
	}
	
}
