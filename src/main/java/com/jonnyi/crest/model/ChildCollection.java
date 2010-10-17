package com.jonnyi.crest.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ChildCollection {
	public List<Child> children = new ArrayList<Child>();
	
	
	public Child findChild( int cid ) {
		
		if( this.children != null )
			for (Child child : children)
				if( child.id == cid )
					return child;
		
		return null;
	}
}
