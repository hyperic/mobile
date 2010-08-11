package org.hyperic.hq;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.client.ResponseHandler;
import org.hyperic.hq.bean.Alert;
import org.hyperic.hq.bean.AlertActionLog;
import org.hyperic.hq.bean.Resource;
import org.hyperic.hq.handler.AlertXMLHandler;
import org.hyperic.hq.handler.HTTPRequestHelper;
import org.hyperic.hq.handler.ResourceXMLHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AlertDetail extends Activity {
	private final static String CLASSTAG = AlertDetail.class.getSimpleName();
	private TextView nameLbl;
	private TextView timeLbl;
	private TextView resourceLbl;
	private TextView reasonLbl;
	private Button ackedBtn;
	private LinearLayout logsLayout;
	private LinearLayout fixLayout;
	private EditText fixResolutionTxt;
	private Button fixedBtn;
	private Button backBtn;
	private ProgressDialog progressDlg = null;
	
	private String url="";
	private String username="";
	private String password="";
	
	private static final int REQUEST_SHOW=1;
	private static final int REQUEST_ACK=2;
	private static final int REQUEST_FIX=3;
	private static final int REQUEST_GET_RESOURCE=4;
	private static final String API_ALERT_PATH = "/hqu/hqapi1/alert";
	private static final String API_RESOURCE_PATH = "/hqu/hqapi1/resource";
	private Context ctx;
	private int alertId = -1;
	private boolean mostRecent = false; // for ack
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_detail);

        progressDlg = ProgressDialog.show(AlertDetail.this, getString(R.string.alerts_please_wait), 
        		getString(R.string.alerts_retrieving_data), true);
        
        //getParcelableExtra TODO
        final Intent detailIntent = getIntent();
        alertId = detailIntent.getIntExtra("alertId", -1);
        mostRecent = detailIntent.getBooleanExtra("mostRecent", false);
        if (alertId == -1 ) {
        	finish();
        }

        loadPrefs();
        findViews();
        ctx= this;
        
        performRequest(REQUEST_SHOW);
        
        setListeners();
    }

	private void setListeners() {
		ackedBtn.setOnClickListener(ackAlert);
		fixedBtn.setOnClickListener(fixAlert);
		backBtn.setOnClickListener( new Button.OnClickListener(){
			@Override
			public void onClick(View v){
				finish();
			}
		});
	}

	private void findViews() {
		nameLbl = (TextView) findViewById(R.id.nameLbl);   
		timeLbl = (TextView) findViewById(R.id.timeLbl);  
		resourceLbl = (TextView) findViewById(R.id.resourceLbl);
		reasonLbl = (TextView) findViewById(R.id.reasonLbl);
		logsLayout =  (LinearLayout)findViewById(R.id.logsLayout); 
		ackedBtn = (Button) findViewById(R.id.ackedBtn); 
		fixLayout = (LinearLayout)findViewById(R.id.fixLayout); 
		fixResolutionTxt = (EditText)findViewById(R.id.fixResolutionTxt);
		fixedBtn= (Button)findViewById(R.id.fixedBtn);
		backBtn= (Button)findViewById(R.id.backBtn);
	}
	private Button.OnClickListener ackAlert = new Button.OnClickListener(){
		@Override
		public void onClick(View v) {
			performRequest(REQUEST_ACK);
		}
	};
	private Button.OnClickListener fixAlert = new Button.OnClickListener(){
		@Override
		public void onClick(View v) {
			performRequest(REQUEST_FIX);
			
		}
	};

	private void performRequest( int type) {
		final ResponseHandler<String> responseHandler = 
			HTTPRequestHelper.getResponseHandlerInstance(handler);
		//TODO: should check internet connection first
		
		switch (type){
		
			case REQUEST_SHOW:
				new Thread(){
					@Override
					public void run(){
						HTTPRequestHelper helper = new HTTPRequestHelper(responseHandler);
						String targetUrl= url+API_ALERT_PATH+"/get.hqu?id="+String.valueOf(alertId);
						Log.v(CLASSTAG, "REQUEST_SHOW get url: "+targetUrl);
						helper.performGet(targetUrl);
					}
				}.start();
				break;
			case REQUEST_ACK:
				new Thread(){
					@Override
					public void run(){
						HTTPRequestHelper helper = new HTTPRequestHelper(responseHandler);
						String targetUrl= url+API_ALERT_PATH+"/ack.hqu?id="+String.valueOf(alertId);
						Log.v(CLASSTAG, "ACK- get url: "+targetUrl);
						helper.performGet(targetUrl);
					}
				}.start();
				break;
			case REQUEST_FIX:
				new Thread(){
					@Override
					public void run(){
						HTTPRequestHelper helper = new HTTPRequestHelper(responseHandler);
						
						String reason = URLEncoder.encode(fixResolutionTxt.getText().toString());
						String targetUrl= url+API_ALERT_PATH
							+"/fix.hqu?id="+String.valueOf(alertId)+"&reason="+reason;
						Log.v(CLASSTAG, "FIX get url: "+targetUrl);
						helper.performGet(targetUrl);
					}
				}.start();
				break;
		}
	}
	private void prepareResourceName(final int resourceId){
		final ResponseHandler<String> responseHandler = 
			HTTPRequestHelper.getResponseHandlerInstance(setResourceHandler);
		//TODO: should check internet connection first
		new Thread(){
			@Override
			public void run(){
				HTTPRequestHelper helper = new HTTPRequestHelper(responseHandler);
				String targetUrl= url+API_RESOURCE_PATH+"/get.hqu?id="+String.valueOf(resourceId);
				Log.v(CLASSTAG, "Resource name- get url: "+targetUrl);
				helper.performGet(targetUrl);
			}
		}.start();
	}
    private final Handler setResourceHandler = new Handler(){
    	@Override
    	public void handleMessage(final Message msg){
    		String bundleResult = msg.getData().getString("RESPONSE");
    		Log.v(CLASSTAG, "Set Resource Name - got RESPONSE: "+bundleResult);
    		
    		try {
    			InputStream inputBundleResult = new ByteArrayInputStream(bundleResult.getBytes("UTF-8"));
				
	    		SAXParserFactory spf = SAXParserFactory.newInstance();
	    		SAXParser sp = spf.newSAXParser();
	    		XMLReader xr = sp.getXMLReader();
	    		ResourceXMLHandler resXMLHandler = new ResourceXMLHandler();
	    		
	    		resXMLHandler.setParseOthers(false);
	    		xr.setContentHandler(resXMLHandler);
	    		xr.parse(new InputSource(inputBundleResult));
	    		
	    		if(resXMLHandler.isValid()){
	    			if (resXMLHandler.getResList().size()==1){
	    				Resource res = resXMLHandler.getResList().get(0);
	    				resourceLbl.setText(res.getName());
	    			}else{
	    				Log.d(CLASSTAG,"more than one or none resource found with the specified resource id");
	    			}
	    		}
	    		
    		}catch(Exception e){
    			e.printStackTrace();
    			finish();
    		}
    	}
    };
    private final Handler handler = new Handler(){
    	@Override
    	public void handleMessage(final Message msg){
    		String bundleResult = msg.getData().getString("RESPONSE");
    		Log.v(CLASSTAG, "get RESPONSE: "+bundleResult);
    		
    		try {
    			InputStream inputBundleResult = new ByteArrayInputStream(bundleResult.getBytes("UTF-8"));
				
	    		SAXParserFactory spf = SAXParserFactory.newInstance();
	    		SAXParser sp = spf.newSAXParser();
	    		XMLReader xr = sp.getXMLReader();
	    		AlertXMLHandler alertXMLHandler = new AlertXMLHandler();
	    		alertXMLHandler.setIsParseOthers(true);//only need alert data
	    	
	    		xr.setContentHandler(alertXMLHandler);		
	    		xr.parse(new InputSource(inputBundleResult));
    	    			
	    		if(alertXMLHandler.isValid()){
		    		SimpleDateFormat df = new SimpleDateFormat();
		    		df.applyPattern("MM/dd/yyyy kk:mm");   

	    		//process data!!
	    			List<Alert> alertList = alertXMLHandler.getAlertList();
	    			Alert alert = alertList.get(0);
	    			prepareResourceName(alert.getResourceId());
	    			nameLbl.setText(alert.getName());
	    			
	    			timeLbl.setText(df.format(alert.getCtime()));
	    			//resourceLbl.setText(alert.getre)  TODO
	    			reasonLbl.setText(alert.getReason());
	    			List<AlertActionLog> alertActionLogs = alert.getActionLogs();
	    			String text="";
	    			
	    			logsLayout.removeAllViews();
	    			for (int i = 0 ; i<alertActionLogs.size();i++){
	    				AlertActionLog actionLog = alertActionLogs.get(i);
		    			TextView row = new TextView(ctx);
		    			text = df.format(actionLog.getActionTime()) + ": "+actionLog.getDetail();
		    			row.setText(text);
		    			logsLayout.addView(row);
	    			}
	    			

	    			//show fix and ack
	    			if (!alert.isFixed()){
	    				
		    			//show ack
		    			Log.v(CLASSTAG,"InEsc="+alert.isInEsc()+" AckedBy="+alert.getAckedBy()+" mostRecent"+mostRecent);
		    			if (alert.isInEsc() &&  "".equals(alert.getAckedBy()) && mostRecent){
		    				ackedBtn.setVisibility(View.VISIBLE);
		    			}else{
		    				ackedBtn.setVisibility(View.INVISIBLE);
		    			}
	    				
	    				fixLayout.setVisibility(View.VISIBLE);
	    			}else{
	    				fixLayout.setVisibility(View.INVISIBLE);
	    			}
	    			
	    			
	    		}
    		} catch (IllegalStateException e1){
    			e1.printStackTrace();
    			finish();
			} catch (Exception e) {
				Log.d(CLASSTAG, "XMLParsing error");
				e.printStackTrace();
				finish();
			}finally{
				progressDlg.dismiss();
			}
    	}
    };
    
	
    private final void loadPrefs() {
        SharedPreferences loginPref = getSharedPreferences("loginData", MODE_PRIVATE);
        
        url = loginPref.getString("url", getString(R.string.login_url_default));
        username = loginPref.getString("username", getString(R.string.login_username_default));
        password = loginPref.getString("password", getString(R.string.login_password_default));

    }
}
