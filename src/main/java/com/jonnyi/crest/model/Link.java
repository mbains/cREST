package com.jonnyi.crest.model;

import java.net.URI;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

public class Link {
	
	static String SELF = "self";
	static String PARENT = "parent";
	static String CHILD = "child";
	static String NEXT = "next";
	static String PREVIOUS = "previous";
	
	@XmlAttribute
	public String rel;
	
	@XmlAttribute
	public URI href;
	
	public static Link relation( String val ) {
		Link l = new Link();
		l.rel = val;
		return l;
	}
	public static Link next() {
		return relation(NEXT);
	}
	public static Link previous() {
		return relation(PREVIOUS);
	}
	public static Link child() {
		return relation(CHILD);
	}
	public static Link self() {
		return relation(SELF);
	}
	public Link href( URI uri ) {
		this.href = uri;
		return this;
	}
}
