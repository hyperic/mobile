package org.hyperic.hq.handler;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.hyperic.hq.bean.Alert;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class AlertXMLHandler extends DefaultHandler{
	

	private List<Alert> alertList = null;
	boolean isInAlertResponse = false;
	boolean isStatusSuccess = false;
	
	boolean currentElemtnt = false;
	String currentValue = null;
	boolean isInAlert = false;
	Alert currentAlert = null;
	boolean isParseOthers = true;
	
	public void setIsParseOthers(boolean isParseOthers){
		this.isParseOthers = isParseOthers;
	}
	
	public boolean isValid(){
		return isInAlertResponse && isStatusSuccess;
	}
	
	public List<Alert> getAlertList() {
		return alertList;
	}

	
	@Override
	public void startElement(String uri, String localName, 
			String qName, Attributes attributes)throws SAXException{
		currentElemtnt=true;
		if("AlertsResponse".equals(localName) || "AlertResponse".equals(localName) ){
		//find.hqu will has "AlertsResponse", get.hqu has "AlertResponse"

			isInAlertResponse = true;
			alertList = new ArrayList<Alert>();
		}else if("Alert".equals(localName)){
			Alert alert = new Alert();
			alert.setId(Integer.parseInt(attributes.getValue("id")));
			alert.setName(attributes.getValue("name"));
			alert.setAlertDefId(Integer.parseInt(attributes.getValue("alertDefinitionId")));
			alert.setResourceId(Integer.parseInt(attributes.getValue("resourceId")));
			alert.setCtime(new Date(Long.parseLong(attributes.getValue("ctime"))));
			alert.setReason(attributes.getValue("reason"));
			if("true".equals(attributes.getValue("fixed"))){
				alert.setFixed(true);
			}else{
				alert.setFixed(false);
			}
			alert.setReason(attributes.getValue("reason"));
			currentAlert = alert;
			alertList.add(alert);
		}else if (isParseOthers && "AlertActionLog".equals(localName)){
			if(currentAlert!=null){
				currentAlert.addOneActionLog(new Date(Long.parseLong(attributes.getValue("timestamp"))), 
						attributes.getValue("detail"), attributes.getValue("user"));
			}
		}else if (isParseOthers && "EscalationState".equals(localName)){
		//<EscalationState ackedBy="" escalationId="12231" nextActionTime="1280131041494"/>
			if(currentAlert!=null){
				currentAlert.setInEsc(true);
				currentAlert.setAckedBy(attributes.getValue("ackedBy"));
				currentAlert.setEscId(Integer.parseInt(attributes.getValue("escalationId")));
				currentAlert.setNextActionTime(new Date(Long.parseLong(attributes.getValue("nextActionTime"))));
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)throws SAXException{
		currentElemtnt=false;
		
		if("Status".equals(localName)){
			if ("Success".equals(currentValue)){
				isStatusSuccess = true;
			}
		}else if("Alert".equals(localName)){
			currentAlert = null;
		}
	}
	
	/**
	 * get tag characters. only useful in "Status"
	 */
	@Override
	public void characters(char[] ch, int start, int length){
		if(currentElemtnt){
			currentValue = new String(ch, start, length);
			currentElemtnt = false;
		}
	}
	
}
