package org.hyperic.hq.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.util.Log;

/**
<Alert id='100692' name='ESX CPU' alertDefinitionId='14458' resourceId='11672' ctime='1279537200000' 
fixed='false' reason='If CPU Usage (Average) &gt; 80.0% (actual value = 86.0%)'></Alert>
*/
public class Alert {
	static String CLASSTAG = Alert.class.getSimpleName();
	static int INIT_ID = -1;

	int id = INIT_ID;
	String name ="";
	int alertDefId;
	int resourceId;
	Date ctime;
	boolean fixed=false; 
	String reason ="";

	List<AlertActionLog> actionLogs = new ArrayList<AlertActionLog>();
	
	//<EscalationState ackedBy="" escalationId="12231" nextActionTime="1280131041494"/>
	boolean inEsc = false;// can only ack alerts in escalation
	String ackedBy="";
	int escId;
	Date nextActionTime=null;
	
	

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

	public List<AlertActionLog> getActionLogs() {
		return actionLogs;
	}

	public void addOneActionLog(Date date, String detail, String user){
		AlertActionLog oneLog = new AlertActionLog();
		oneLog.setActionTime(date);
		oneLog.setDetail(detail);
		oneLog.setUser(user);
		actionLogs.add(oneLog);
	}

	public boolean isInEsc() {
		return inEsc;
	}

	public void setInEsc(boolean inEsc) {
		this.inEsc = inEsc;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAlertDefId() {
		return alertDefId;
	}

	public void setAlertDefId(int alertDefId) {
		this.alertDefId = alertDefId;
	}

	public int getResourceId() {
		return resourceId;
	}

	public void setResourceId(int resourceId) {
		this.resourceId = resourceId;
	}

	public Date getCtime() {
		return ctime;
	}

	public void setCtime(Date ctime) {
		this.ctime = ctime;
	}

	public boolean isFixed() {
		return fixed;
	}

	public void setFixed(boolean fixed) {
		this.fixed = fixed;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
	
	
	

}
