package org.hyperic.hq.handler;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.hyperic.hq.bean.Alert;
import org.hyperic.hq.bean.Resource;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ResourceXMLHandler extends DefaultHandler{

	boolean isInResourceResponse = false;
	boolean isStatusSuccess = false;
	
	boolean currentElemtnt = false;
	String currentValue = null;
	boolean isInResource = false;
	boolean isParseOthers = false; //is parse stuffs other than resource attributes
	
	Resource currentRes = null;
	
	List<Resource> resList = new ArrayList<Resource>();
	


	public List<Resource> getResList() {
		return resList;
	}

	public void setResList(List<Resource> resList) {
		this.resList = resList;
	}

	public boolean isParseOthers() {
		return isParseOthers;
	}

	public void setParseOthers(boolean isParseOthers) {
		this.isParseOthers = isParseOthers;
	}

	public boolean isValid(){
		return isInResourceResponse && isStatusSuccess;
	}
	
	@Override
	public void startElement(String uri, String localName, 
			String qName, Attributes attributes)throws SAXException{
		currentElemtnt=true;
		if("ResourceResponse".equals(localName) || "ResourcesResponse".equals(localName) ){
			isInResourceResponse = true;

		}else if("Resource".equals(localName)){
			Resource res = new Resource();
			res.setId(Integer.parseInt(attributes.getValue("id")));
			res.setName(attributes.getValue("name"));
			res.setDesc(attributes.getValue("description"));
			res.setLocation(attributes.getValue("location"));
			
			currentRes = res;
			resList.add(res);
			
		}else if (isParseOthers && "ResourceProrotype".equals(localName)){
			// attribute: id, name
		}else if (isParseOthers && "ResourceInfo".equals(localName)){
			//key=installPath, autoIdentifier
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)throws SAXException{
		currentElemtnt=false;
		if("Status".equals(localName)){
			if ("Success".equals(currentValue)){
				isStatusSuccess = true;
			}
		}else if("Resource".equals(localName)){
			currentRes = null;
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
