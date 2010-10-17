package com.jonnyi.crest.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Parent 
	implements Cloneable {
	
	public Parent() {}
	
	public Parent( Parent p ) {
		this.links = new ArrayList<Link>();
		this.id = p.id;
		this.living = p.living; 
		this.number = p.number;
		this.name = p.name;
		this.value = p.value;
		this.childCollection = new ChildCollection();
		
		if( p.childCollection.children != null )
			for (Child c : p.childCollection.children )
				this.childCollection.children.add( new Child(c) );
	}
	
	public Integer id;
	public Boolean living = true; 
	public BigDecimal number = new BigDecimal( 310.65 );
	public String name = "Default Parent Name";
	public String value = "Default Parent Value";
	public List<Link> links = new ArrayList<Link>();
	public ChildCollection childCollection = new ChildCollection();

}
