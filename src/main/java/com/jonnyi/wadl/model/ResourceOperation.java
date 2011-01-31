package com.jonnyi.wadl.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.sun.research.ws.wadl.Doc;

/**
 * This class represents a single action that can be performed 
 * on a resource.  If you consider a "Transaction" resource, it might
 * have a URI like so...
 * 
 * http://whatever.com/transactions/{trans-id}
 * 
 * ...and client's can do an HTTP...
 * 
 * GET to read the transaction.
 * POST to update the transaction.
 * PUT to create a transaction.
 * DELETE to delete the transaction.
 * 
 * This single Transaction resource would be represented by 
 * 4 instances of this class, one for each request method. This 
 * would be the same granularity as operations on a soap service. 
 * 
 *  
 * @author j.iantosca
 */
@XmlRootElement
public class ResourceOperation {
	
	private String id, path, method, name, description;
	private List<String> requestMediaTypes, responseMediaTypes;
	private List<Doc> methodDocs, resourceDocs, requestRepDocs, responseRepDocs; 
	
	public ResourceOperation() {}
	
	public String getName() {
		if( this.name == null)
			this.name = this.toOperationName();
		
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public List<String> getRequestMediaTypes() {
		return requestMediaTypes;
	}

	public void setRequestMediaTypes(List<String> reqMediaTypes) {
		this.requestMediaTypes = reqMediaTypes;
	}

	public List<String> getResponseMediaTypes() {
		return responseMediaTypes;
	}

	public void setResponseMediaTypes(List<String> respMediaTypes) {
		this.responseMediaTypes = respMediaTypes;
	}
	
	public List<Doc> getMethodDocs() {
		return methodDocs;
	}

	public void setMethodDocs(List<Doc> methodDocs) {
		this.methodDocs = methodDocs;
	}

	public List<Doc> getResourceDocs() {
		return resourceDocs;
	}

	public void setResourceDocs(List<Doc> resourceDocs) {
		this.resourceDocs = resourceDocs;
	}

	public List<Doc> getRequestRepDocs() {
		return requestRepDocs;
	}

	public void setRequestRepDocs(List<Doc> requestRepDocs) {
		this.requestRepDocs = requestRepDocs;
	}

	public List<Doc> getResponseRepDocs() {
		return responseRepDocs;
	}

	public void setResponseRepDocs(List<Doc> responseRepDocs) {
		this.responseRepDocs = responseRepDocs;
	}

	public String toOperationName() {
		return new StringBuilder( this.method ).append(' ').append( this.path ).toString();
	}
	
	public String toVerboseString() {
		
		String reqTypes = "";
		String respTypes = "";
		
		if( this.requestMediaTypes!=null)
			for( String reqt : this.requestMediaTypes )
				reqTypes += (reqt+" ");
		
		if( this.responseMediaTypes!=null)
			for( String respt : this.responseMediaTypes )
				respTypes += (respt+" ");
		
		reqTypes = ("".equals(reqTypes)) ? null:reqTypes;
		respTypes = ("".equals(respTypes)) ? null:respTypes;
		
		return 
			"ResourceOperation: \n" +
			"	name (operation):      " + this.getName() + " \n" + 
			"	id:                    " + this.id + " \n" +
			"	method:                " + this.method + " \n" +
			"	path:                  " + this.path + " \n" +
//			"	request media types:   " + reqTypes + " \n" +
//			"	response  media types: " + respTypes + " \n" +
			"	request media types:   " +  this.requestMediaTypes + " \n" +
			"	response  media types: " +  this.responseMediaTypes + " \n" +						
			"	method docs:           " + this.methodDocs + " \n" +
			"	resource docs:         " + this.resourceDocs + " \n";
	}
	
}
