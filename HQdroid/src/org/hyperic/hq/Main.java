package org.hyperic.hq;

import org.apache.http.client.ResponseHandler;
import org.hyperic.hq.handler.HTTPRequestHelper;

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TabHost;

/**
 * Main contains two tabs: UrgentAlertList and AlertList
 * @author yechen
 *
 */
public class Main extends TabActivity {
	private static final int LOGIN_REQUEST = 1;
	private static final String CLASSTAG=Main.class.getSimpleName();
	private static String API_PATH = "/hqu/hqapi1/alert/get.hqu?id=0";

	private String url ="";
	private String username ="";
	private String password ="";
	SharedPreferences loginPref;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(CLASSTAG, "in onCreate()");
    }
    
    @Override
    public void onResume(){
    	super.onResume();
        Log.v(CLASSTAG, "in onResume()");
        //check if login info needed, mark to always see the login page
        loginPref = getSharedPreferences("loginData", MODE_PRIVATE);
        
        url = loginPref.getString("url", getString(R.string.login_url_default));
        username = loginPref.getString("username", getString(R.string.login_username_default));
        password = loginPref.getString("password", getString(R.string.login_password_default));
        
        Log.v(CLASSTAG, "load preference: url="+url+", username="+username+", password="+password);
        //if no login info, show login
        if(url.equals("") || username.equals("")||password.equals("")){
        	toLogin();
        }else{
        	tryLogin();
        } 
    }
    

    private void addTab(){
        TabHost tabHost = getTabHost();
        TabHost.TabSpec spec;
        Intent intent;
        Resources res = getResources();
        tabHost.clearAllTabs();
        
        intent = new Intent(this,UrgentAlertList.class);
        spec = tabHost.newTabSpec("infixedAlerts").setIndicator(getString(R.string.main_tab_urgent_alert),
        		res.getDrawable(R.drawable.icon_alert)).setContent(intent);
        tabHost.addTab(spec);
        
        intent = new Intent(this,AlertList.class);
        spec = tabHost.newTabSpec("alerts").setIndicator(getString(R.string.main_tab_alert),
        		res.getDrawable(R.drawable.icon_alert)).setContent(intent);
        
        tabHost.addTab(spec);
    }
   
    @Override
	protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        Log.v(CLASSTAG, "in onActivityResult(), requestCode="+requestCode+", resultCode="+resultCode);
    	if (requestCode == LOGIN_REQUEST){
            if (resultCode == RESULT_OK){
            	//from login page
                addTab();
            } else{
                tryLogin();
            }
    	}
    }

    
    private final void tryLogin() {                
		final ResponseHandler<String> responseHandler = 
			HTTPRequestHelper.getResponseHandlerInstance(this.handler);
		HTTPRequestHelper helper = new HTTPRequestHelper(responseHandler);
		//test if the username and password work
		String targetUrl = url+API_PATH;
		Log.v(CLASSTAG, " get url: "+targetUrl);
		helper.performGet(targetUrl, username , password, null);

    }
    private final Handler handler = new Handler(){
    	@Override
    	public void handleMessage(final Message msg){
    		String bundleResult = msg.getData().getString("RESPONSE");
    		Log.v(CLASSTAG, "get RESPONSE: "+bundleResult);
    		
    		//if can connect and get the data
    		if (!bundleResult.contains("AlertResponse")){
    			toLogin();
    			
    		}else{
    			addTab();
    		}
    	}
    };
    private void toLogin(){
    	this.url="";
    	this.username="";
    	this.password="";
        Intent intent = new Intent(this, Login.class);
        startActivityForResult(intent, LOGIN_REQUEST);
    }
}
