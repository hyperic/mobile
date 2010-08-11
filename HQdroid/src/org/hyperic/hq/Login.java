package org.hyperic.hq;

import org.apache.http.client.ResponseHandler;
import org.hyperic.hq.handler.HTTPRequestHelper;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * login page, Main.java's sub activity
 * @author yechen
 *
 */
public class Login extends Activity {
	private  static final String CLASSTAG = Login.class.getSimpleName();
    private Button loginBtn;   	
    private Button resetBtn;
    private EditText urlTxt;
    private EditText usernameTxt;
    private EditText passwordTxt;
    private TextView errorMsgLbl;
	private String url ="";
	private String username ="";
	private String password ="";
	private static String API_PATH = "/hqu/hqapi1/alert/get.hqu?id=0";//TODO is there any better way to check if login success?
	SharedPreferences loginPref;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(CLASSTAG, "in onCreate()");
        setContentView(R.layout.login);
        findViews();
        loadPref();
        
        setListeners();//for the two button
    }
    private void loadPref(){
        loginPref = getSharedPreferences("loginData", MODE_PRIVATE);
        
        url = loginPref.getString("url", getString(R.string.login_url_default));
        username = loginPref.getString("username", getString(R.string.login_username_default));

        urlTxt.setText(url);
        usernameTxt.setText(username);
        
    }
    
    
    private Button.OnClickListener login = new Button.OnClickListener(){
    	public void onClick(View v){
    		username = usernameTxt.getText().toString();
    		password = passwordTxt.getText().toString();
        	url = formatURL(urlTxt.getText().toString());
        	
        	if (username.equals("") || password.equals("") || url.equals("")){
        		errorMsgLbl.setText(R.string.login_error_empty);
        	}else{
	   			performRequest(url, username, password);
	   			//TODO: users now have to wait for login 
	   			//we can show progress dialog here or disable login button.
        	}
    	}
    };
	private void performRequest(final String url, final String username, final String password) {
		final ResponseHandler<String> responseHandler = 
			HTTPRequestHelper.getResponseHandlerInstance(this.handler);
		new Thread(){
			@Override
			public void run(){
				HTTPRequestHelper helper = new HTTPRequestHelper(responseHandler);
				//try to login with the provided information
				String targetUrl = url+API_PATH;
				Log.v(CLASSTAG, " get url: "+targetUrl);
				helper.performGet(targetUrl, username , password, null);

			}
		}.start();
	}
    private final Handler handler = new Handler(){
    	@Override
    	public void handleMessage(final Message msg){
    		String bundleResult = msg.getData().getString("RESPONSE");
    		Log.v(CLASSTAG, "get RESPONSE: "+bundleResult);
    		
    		//Another RESPONSE: Error - Socket is not connected
    		if (bundleResult.contains("Error - Target host must not be null, or set in parameters.")
    				|| bundleResult.contains("Error - Host is unresolved")){
    			passwordTxt.setText("");
    			errorMsgLbl.setText(R.string.login_error_server);    			
    		}else if (bundleResult.contains("HTTP Status 401")){ //TODO: for test~
    			//doesn't pass auth, show error msg
    			passwordTxt.setText("");
    			errorMsgLbl.setText(R.string.login_error_login);

    		}else if  (bundleResult.contains("<AlertResponse")){
    			// save to preference
    	        loginPref = getSharedPreferences("loginData", MODE_PRIVATE);			
    	        SharedPreferences.Editor editor = loginPref.edit();
    	        editor.clear();
    	        editor.putString("url", url);
    	        editor.putString("username",username);
    	        editor.putString("password", password);
    	        
                if (editor.commit()) {
                    setResult(RESULT_OK);
                }
                
    	        finish();//to finish this activity, go back to main
    		}else{
    			//not able to connect to server for other reason
    			passwordTxt.setText("");
    			errorMsgLbl.setText(R.string.login_error_general);
    		}
    	}
    };
    
    private Button.OnClickListener reset = new Button.OnClickListener(){
    	public void onClick(View v){
    		urlTxt.setText("");
    		usernameTxt.setText("");
    		passwordTxt.setText("");
    	}
    };


    private void findViews(){ 
    	loginBtn = (Button) findViewById(R.id.loginBtn);   	
        resetBtn = (Button) findViewById(R.id.resetBtn);   	
		urlTxt = (EditText) findViewById(R.id.urlTxt);
		usernameTxt = (EditText) findViewById(R.id.usernameTxt);
		passwordTxt = (EditText) findViewById(R.id.passwordTxt);
		errorMsgLbl = (TextView) findViewById(R.id.errorMsgLbl);
    }
    private void setListeners(){
        loginBtn.setOnClickListener(login); 
        resetBtn.setOnClickListener(reset);
    }

    /**
     * put http in the front and get rid of the / in the back
     */
    private String formatURL(String url){
    	
    	if(!url.startsWith("http")){
    		url = "http://".concat(url);
    	}
    	if(url.endsWith("/")){
    		url.substring(0, url.length());
    	}
    	return url;
    }
}
