package org.hyperic.hq.bean;

import java.util.Date;

//<AlertActionLog timestamp='1279105395912' detail='Suppress alerts' user=''/>
public class AlertActionLog {
	Date actionTime = null;
	String detail="";
	String user="";
	public Date getActionTime() {
		return actionTime;
	}
	public void setActionTime(Date actionTime) {
		this.actionTime = actionTime;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	

}
