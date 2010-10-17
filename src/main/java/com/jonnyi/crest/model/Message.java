package com.jonnyi.crest.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Message {
	
	@XmlAttribute
	public String type;
	
	public String text;
	
	public static Message error( String s) {
		Message m = new Message();
		m.type = "error";
		m.text = s;
		return m;
	}
}
