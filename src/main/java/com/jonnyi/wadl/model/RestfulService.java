package com.jonnyi.wadl.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
@XmlRootElement
public class RestfulService 
	extends Service {
	
	//maybe we can put this in the super-class?
	//wadl can have a list of <doc> elements, for now the parser
	//will just concatenate all the docs into one string... think about
	//alternatives later. 
	private String documentation;
	
	private List<ResourceOperation> resourceOperations;

	public List<ResourceOperation> getResourceOperations() {
		return resourceOperations;
	}
	public void setResourceOperations(List<ResourceOperation> resourceOperations) {
		this.resourceOperations = resourceOperations;
	}
	
	
	public String getDocumentation() {
		return documentation;
	}
	public void setDocumentation(String documentation) {
		this.documentation = documentation;
	}
	
	public String toVerboseString() {
		StringBuilder b = new StringBuilder();
		b.append("Doc: " + this.documentation  + "\n\n");
		for (ResourceOperation rop : this.resourceOperations) {
			b.append( rop.toVerboseString() );
		}
		return b.toString();
	}
}













