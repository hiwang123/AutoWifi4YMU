package com.example.autowifi4ymu;

import java.io.IOException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

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
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	
	Button applyButton;
	EditText idText, pwdText;
	TextView mTextView;
	static Context mContext;
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
		mContext = getApplicationContext();
		
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
				SharedPreferences pref = getSharedPreferences(STATE, Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = pref.edit();
				editor.putBoolean("activate", activate);
				editor.putString("idString", idString);
				editor.putString("pwdString", pwdString);
				editor.commit();
				Toast.makeText(MainActivity.this, "Saved", Toast.LENGTH_SHORT).show();
				checkConnectedTargetWifi();
			}
		});
		mTextView=(TextView) findViewById(R.id.textView5);
		mTextView.setText("使用:輸入學校行政系統之帳密,Go!\n"+
				"區域:陽明大學有wifi之處\n" +
				"功能:連線至ymu後,自動認證帳密\n" +
				"1.每次使用wifi時不需重新開啟此app\n"+
				"2.更改學校行政系統帳密需重新輸入\n"+
				"3.wifi狀態需為「已連線」,認證完成會顯示'connecting'\n" +
				"4.若無法自動登入,需重新啟動此app再按Go!\n");
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
			SharedPreferences prefs = context.getSharedPreferences(STATE, Context.MODE_PRIVATE);
			idString = prefs.getString("idString","");
			pwdString = prefs.getString("pwdString","");
			this.context = context;
			Toast.makeText(context, "wifi", Toast.LENGTH_SHORT).show();
	        if ( WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action) ) {
	        	NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
	            if (netInfo.isConnected()) {
	            	//Toast.makeText(context, "networkStateChange", Toast.LENGTH_SHORT).show();
	                checkConnectedTargetWifi();
	            }
	        }
		}
		
	}
	
	public static void checkConnectedTargetWifi(){
		String targetSSID = "ymu";
		boolean connected = false;
		
		WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifi = wifiManager.getConnectionInfo();
        if (wifi != null) {
            String ssid = wifi.getSSID();
            connected = ssid.equals(targetSSID) || ssid.equals("\""+targetSSID+"\"");
        }
        if(connected){
        	Toast.makeText(mContext, "connecting to "+targetSSID, Toast.LENGTH_SHORT).show();
        	AutoWifiAuthentic();
        }
    }
	
	public static void AutoWifiAuthentic(){
		
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				//HttpClient client = getNewHttpClient();
				HttpClient client = new DefaultHttpClient();
				HttpParams params = new BasicHttpParams();
				params.setParameter("http.protocol.handle-redirects",false);
				HttpPost httppost1 = new HttpPost("https://securelogin.arubanetworks.com/auth/index.html/u");
				HttpPost httppost2 = new HttpPost("http://3comwx.ym.edu.tw/aaa/wba_form2.html");
				httppost1.setParams(params);
				httppost2.setParams(params);
				String result1="e", result2="e";

		        try {
		        	// Add data
		        	List<NameValuePair> nameValuePairs1 = new ArrayList<NameValuePair>(2);
		            
		        	nameValuePairs1.add(new BasicNameValuePair("user", idString));
		        	nameValuePairs1.add(new BasicNameValuePair("password", pwdString));
		             
					httppost1.setEntity(new UrlEncodedFormEntity(nameValuePairs1));
					
					// Execute HTTP Post Request
					HttpResponse response1 = client.execute(httppost1);

			        result1 = EntityUtils.toString(response1.getEntity());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        
		        try {
		        	// Add data
		        	List<NameValuePair> nameValuePairs2 = new ArrayList<NameValuePair>(3);
		            	        	
		        	nameValuePairs2.add(new BasicNameValuePair("fname", "wba_login"));
		        	nameValuePairs2.add(new BasicNameValuePair("username", idString));
		            nameValuePairs2.add(new BasicNameValuePair("key", pwdString));
		            
					httppost2.setEntity(new UrlEncodedFormEntity(nameValuePairs2));
					
					// Execute HTTP Post Request
					HttpResponse response2 = client.execute(httppost2);

			        result2 = EntityUtils.toString(response2.getEntity());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        
			}
		});
		thread.start();
	}		
	
}
