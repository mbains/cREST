package com.jonnyi.crest.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ParentCollection {
	
	public List<Parent> parents = new ArrayList<Parent>();
	
	public Parent findParent(int pid ) {
		if( this.parents == null )
			return null;
		
		for (Parent p : this.parents)
			if (p.id == pid)
				return p;

		return null;
	}
	
	public Child findChild(int pid, int cid ) {
		Parent p = this.findParent(pid);
		
		if( p == null || p.childCollection == null )
			return null;
		
		return p.childCollection.findChild( cid );
	}
}
