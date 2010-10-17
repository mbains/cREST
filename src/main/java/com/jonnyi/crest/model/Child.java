package com.jonnyi.crest.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Child {
	
	public Child() {}
	
	public Child(Child c) {
		this.links = new ArrayList<Link>();
		this.id = c.id;
		//this.parentId = c.parentId;
		this.living = c.living;
		this.number = c.number;
		this.name = c.name;
		this.value = c.value;		
	}
	
	public Integer id;
	//public Integer parentId;
	public Boolean living = true; 
	public BigDecimal number = new BigDecimal( 4.20 );
	public String name = "Default Child Name";
	public String value = "Default Child Value";
	
	public List<Link> links = new ArrayList<Link>();
}
