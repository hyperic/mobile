package org.hyperic.hq;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.client.ResponseHandler;
import org.hyperic.hq.bean.Alert;
import org.hyperic.hq.handler.AlertXMLHandler;
import org.hyperic.hq.handler.HTTPRequestHelper;
import org.hyperic.hq.ui.AlertRow;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
/**
 * show all alert
 * @author yechen
 *
 */
public class AlertList extends ListActivity  {
	private String begin = "0"; //TODO load pref
	private String count = "30";
	private String sev = "1";//TODO, load pref. 1 is load all severity
	private int lastAlertId = 0;
	private Context ctx = this;	
	private static String CLASSTAG = ListActivity.class.getSimpleName();
	private static String API_ALERT_PATH = "/hqu/hqapi1/alert";

	private ProgressDialog progressDlg = null;
	private ArrayList<AlertRow> alertRowList = new ArrayList<AlertRow>();
	private AlertListAdapter alertAdapter;
	
	//request code
	private static final int SHOW_DETAIL_REQUEST = 1;
	private static final int SHOW_PREFERENCE =2;
	
	private String url ="";
	private String username ="";
	private String password ="";
    private Menu menu;
	private long displayRefreshTime=1000*10; // 10 sec
    private volatile Thread refreshThread;

    
    private synchronized void startThread(){
    	Log.v(CLASSTAG,"Start refresh page thread.");
    	if(refreshThread == null){
    		refreshThread = new Thread(refresh);
    		refreshThread.start();
    	}
    }
    private synchronized void stopThread(){
    	Log.v(CLASSTAG,"Stop refresh page thread.");
    	if(refreshThread!=null){
    		Thread moribund = refreshThread;
    		refreshThread =null;
    		moribund.interrupt();
    	}
    }
    
    private Runnable refresh= new Runnable(){
		@Override
		public void run() {
			while(Thread.currentThread()==refreshThread){
				try {
					Thread.sleep(displayRefreshTime);
					Log.v(CLASSTAG,"refresh!");
					getAlerts();
				} catch (InterruptedException e) {
					Log.d(CLASSTAG, "InterruptedException");
				}
			}
			
		}
    };



    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        progressDlg = ProgressDialog.show(AlertList.this, getString(R.string.alerts_please_wait), 
        		getString(R.string.alerts_retrieving_data), true);
       
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_list);
        loadHQSysPref();
        
    	loadLoginInfoPrefs();
    	
        //alertRowList = new ArrayList<AlertRow>();
        this.alertAdapter = new AlertListAdapter(this,R.layout.alert_list_row,alertRowList);
        setListAdapter(alertAdapter);
        
    }

    @Override
    public void onResume(){
    	super.onResume();
    	getAlerts();
    	
        startThread();
    }
    
    @Override
    public void onPause(){
    	super.onPause();
    	stopThread();
    }    	
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu m) {
    	this.menu = m;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
		return true;
    	
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case R.id.refresh:
        		getAlerts();
        		break;
        	case R.id.preference:
                Intent intent = new Intent(this, Preference.class);
                this.startActivityForResult(intent, SHOW_PREFERENCE);
                break;

        }
        return false;
    }

    @Override
	protected void onActivityResult(int requestCode, int resultCode,
		Intent data) {
    	if (requestCode == SHOW_PREFERENCE){
    		loadHQSysPref();
    	}
    	getAlerts();
    }
    private void loadHQSysPref(){
        SharedPreferences sysPref = 
           	PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        count = sysPref.getString("alert_counts_preference", String.valueOf(count));
        displayRefreshTime =  Long.parseLong(sysPref.getString("page_refresh_time_preference", 
          		String.valueOf(displayRefreshTime)));
          Log.v(CLASSTAG,"load sys pref: count="+count+", displayRefreshTime="+displayRefreshTime);
    }    
    
	private void getAlerts() {

    	//alertRowList = new ArrayList<AlertRow>(); //TODO: put it here or class field
		final ResponseHandler<String> responseHandler = 
			HTTPRequestHelper.getResponseHandlerInstance(this.handler);
		
		new Thread(){
			@Override
			public void run(){
				HTTPRequestHelper helper = new HTTPRequestHelper(responseHandler);
				//*begin, *end, *count, *sev, inEsc, notFixed
				String parmBegin = "begin="+begin; //TODO
				String parmCount = "count="+count;
				String parmSev = "severity="+sev;//TODO, not sure it's min or specific, 1 is lowest
				String end = "end="+String.valueOf(System.currentTimeMillis());
				String targetUrl = url +API_ALERT_PATH+"/find.hqu?"+parmBegin+"&"+end+"&"+parmCount+"&"+parmSev;
				Log.v(CLASSTAG, " get url: "+targetUrl);
				helper.performGet(targetUrl, username , password, null);
			}
		}.start();
	}
	
    private final Handler handler = new Handler(){
    	@Override
    	public void handleMessage(final Message msg){
    		String bundleResult = msg.getData().getString("RESPONSE");
    		
 		try {
    			InputStream inputBundleResult = new ByteArrayInputStream(bundleResult.getBytes("UTF-8"));
				
	    		SAXParserFactory spf = SAXParserFactory.newInstance();
	    		SAXParser sp = spf.newSAXParser();
	    		XMLReader xr = sp.getXMLReader();
	    		AlertXMLHandler alertXMLHandler = new AlertXMLHandler();
	    		alertXMLHandler.setIsParseOthers(false);//only need alert data
	    		
	    		xr.setContentHandler(alertXMLHandler);
	    		
				
	    		xr.parse(new InputSource(inputBundleResult));
    		
	    		SimpleDateFormat df = new SimpleDateFormat();
	    		df.applyPattern("kk:mm MM/dd");
	    		
	    		if(alertXMLHandler.isValid()){
	    			alertRowList.clear();

	    			//process data!!
	    			List<Alert> alertList = alertXMLHandler.getAlertList();
	    			for (int i = 0; i<alertList.size();i++){
	    				Alert alert = alertList.get(i);
	    				if (alert.getId()>lastAlertId){
	    					lastAlertId = alert.getId();
	    				}
	    				AlertRow one = new AlertRow();
	    				
	    				if (alert.isFixed()==true){
	    					one.setIcon(AlertRow.ICON_FIXED);
	    				}else{
	    					one.setIcon(AlertRow.ICON_NO_FIX_NO_ACK);
	    				}
	    				one.setAlertName(alert.getName());
	    				one.setCtime(df.format(alert.getCtime()));
	    				one.setId(alert.getId());
	    				one.setResourceName(alert.getReason());//TODO
	    				alertRowList.add(one);
	    			}
	    		//}else if (bundleResult.trim().contains("HTTP Status 401")){
	    			//login fail, go to login page
	    			//TODO
	    			
	    		}else{
	    			//failed to get correct respond. TODO 
	    		}
			} catch (Exception e) {
				Log.d(CLASSTAG, "XMLParsing error");
			}
			alertAdapter.notifyDataSetChanged();
			progressDlg.dismiss();

    	}
    };
    
    private final void loadLoginInfoPrefs() {
        SharedPreferences loginPref = getSharedPreferences("loginData", MODE_PRIVATE);
        
        url = loginPref.getString("url", getString(R.string.login_url_default));
        username = loginPref.getString("username", getString(R.string.login_username_default));
        password = loginPref.getString("password", getString(R.string.login_password_default));
    }
 
    private class AlertListAdapter extends ArrayAdapter<AlertRow>{
    	private ArrayList<AlertRow> items;
    	
    	public AlertListAdapter(Context context, int textViewResourceId, ArrayList<AlertRow> items){
    		super(context, textViewResourceId, items);
    		this.items = items;
    	}
    	
    	@Override
    	public View getView(int position, View convertView, ViewGroup parent ){
    		View view = convertView;
    		if (view==null){
    			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    			view = inflater.inflate(R.layout.alert_list_row, null);
    		}
    		AlertRow row=null;
    		if(items.get(position)!=null) row = items.get(position);
    		if (row != null){
    			TextView alertNameView = (TextView) view.findViewById(R.id.alert_name_view);
    			TextView alertCtimeView = (TextView) view.findViewById(R.id.alert_ctime_view);
    			TextView alertResourceView = (TextView) view.findViewById(R.id.alert_resource_view);
    			ImageView fixImg = (ImageView) view.findViewById(R.id.alert_icon);
    			TextView hiddenMsgView = (TextView) view.findViewById(R.id.hidden_msg_view);

    			
    			
    			if (alertNameView != null){
    				alertNameView.setText(row.getAlertName());   				
    			}
    			if (alertCtimeView != null){
    				alertCtimeView.setText(row.getCtime());
    			}
    			if (alertResourceView != null){
    				alertResourceView.setText(row.getResourceName());
    			}
    			if (hiddenMsgView != null){
    				hiddenMsgView.setText(row.getDetailMsg());
    			}
    			if (fixImg != null){
    				if (row.getIcon()==AlertRow.ICON_NO_FIX_NO_ACK){
    					fixImg.setImageDrawable(getResources().getDrawable(R.drawable.icon_available_red));
    				}else if (row.getIcon()==AlertRow.ICON_FIXED){
    					fixImg.setImageDrawable(getResources().getDrawable(R.drawable.icon_available_green));
    				}
    			}
    			view.setId(row.getId()); 
    			
    		}
    		return view;
    	}

    }//AlertListAdapter

    @Override
	protected void onListItemClick(ListView l, View v, int position, long id){
		// v is the item that is being clicked, position is the position of the view in the list, id is the row id
		int alertId = v.getId();
		alertRowList.get(position).getId();
        Intent intent = new Intent(this, AlertDetail.class);
        intent.putExtra("alertId", alertId);
        intent.putExtra("mostRecent", (alertId==this.lastAlertId));
        this.startActivityForResult(intent, SHOW_DETAIL_REQUEST);
	}

}
