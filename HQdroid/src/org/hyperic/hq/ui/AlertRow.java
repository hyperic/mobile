package org.hyperic.hq.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hyperic.hq.bean.AlertActionLog;

import android.os.Bundle;

public class AlertRow {
	private String alertName;
	private String ctime;
	private int id;
	private String detailMsg;
	private String resourceName;
	
	boolean inEsc = false;// can only ack alerts in escalation
	String ackedBy="";
	int escId;
	Date nextActionTime=null;
	//private List<Bundle> actionLogsBdl= new ArrayList<Bundle>();
	private List<AlertActionLog> actionLogs;


	private int icon=1;
	public static int ICON_NO_FIX_NO_ACK = 1;
	public static int ICON_NO_FIX_ACKED = 2;
	public static int ICON_FIXED = 3;
	
	public Bundle getEscalationBundle(){
		Bundle result = new Bundle();
		if (inEsc){
			SimpleDateFormat df = new SimpleDateFormat();
			df.applyPattern("kk:mm MM/dd");
			result.putString("IN_ESC", "true");
			result.putString("ACKED_BY", ackedBy);
			result.putString("ESC_ID", String.valueOf(escId));
			result.putString("NEXT_ACTION_TIME", df.format(nextActionTime));
		}else{
			result.putString("IN_ESC", "false");
		}
		return result;	
	}
	
	public Bundle getAlertBundle(){
		Bundle result = new Bundle();
		result.putString("ALERT_NAME", alertName);
		result.putString("CTIME", ctime);
		result.putString("EDTAIL_MSG", detailMsg);
		result.putString("RESOURCE_NAME", resourceName);
		return result;
	}
	
	public Bundle getActionLogBundle() {
		Bundle result = new Bundle();
		result.putInt("ACTION_LOG_SIZE", actionLogs.size());
		
		AlertActionLog thisLog;
		SimpleDateFormat df = new SimpleDateFormat();
		df.applyPattern("kk:mm MM/dd");
		
		for (int i= 0;i<actionLogs.size();i++){
			
			thisLog = actionLogs.get(i);
			result.putString(""+i+"_ACTION_TIME", df.format(thisLog.getActionTime()));
			result.putString(""+i+"_DETAIL"+i, thisLog.getDetail());
			result.putSerializable(""+i+"_USER", thisLog.getUser());
		}
		return result;
	}

	public void setActionLogs(List<AlertActionLog> actionLogs) {
		//this.actionLogs = actionLogs;

		this.actionLogs = actionLogs;
		
	}
	
	
	public String getDetailMsg() {
		return detailMsg;
	}

	public void setDetailMsg(String detailMsg) {
		this.detailMsg = detailMsg;
	}	
	
	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getAlertName() {
		return alertName;
	}
	public void setAlertName(String alertName) {
		this.alertName = alertName;
	}
	public String getCtime() {
		return ctime;
	}
	public void setCtime(String ctime) {
		this.ctime = ctime;
	}

	public boolean isInEsc() {
		return inEsc;
	}

	public void setInEsc(boolean inEsc) {
		this.inEsc = inEsc;
	}

	public String getAckedBy() {
		return ackedBy;
	}

	public void setAckedBy(String ackedBy) {
		this.ackedBy = ackedBy;
	}

	public int getEscId() {
		return escId;
	}

	public void setEscId(int escId) {
		this.escId = escId;
	}

	public Date getNextActionTime() {
		return nextActionTime;
	}

	public void setNextActionTime(Date nextActionTime) {
		this.nextActionTime = nextActionTime;
	}


}
