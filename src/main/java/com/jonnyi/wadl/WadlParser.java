package com.jonnyi.wadl;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.jonnyi.wadl.model.ResourceOperation;
import com.jonnyi.wadl.model.RestfulService;
import com.sun.research.ws.wadl.Application;
import com.sun.research.ws.wadl.Doc;
import com.sun.research.ws.wadl.Method;
import com.sun.research.ws.wadl.RepresentationType;
import com.sun.research.ws.wadl.Request;
import com.sun.research.ws.wadl.Resource;
import com.sun.research.ws.wadl.Resources;
import com.sun.research.ws.wadl.Response;

/** 
 * This class reads a wadl and flattens it out. The goal is to easily see all the requests that can
 * be made to the service. 
 * 
 * @author j.iantosca
 */
public class WadlParser {

	private PathStack pathStack = new PathStack();
	private Application jaxbApp;
	private String wadlRef;
	private boolean includeBase;
	
	public void setIncludeBase( boolean boo ) {this.includeBase=boo;}
	
	public WadlParser( Application jaxbApp ) {
		this.jaxbApp = jaxbApp;
	}

	public WadlParser( String wadlRef ) {
		this.wadlRef = wadlRef;
		
	}

	private String findServiceName() {
		String base = this.jaxbApp.getResources().getBase();
		String[] split = base.split("/");
		int last = split.length-1;
		
		if( "/".equals( split[last]) )
			return split[last-1];
		else
			return split[last];
	}

	
	public RestfulService flatten() {
		if( this.jaxbApp == null ) {
			String wadl = WadlLoader.load( wadlRef );
			if( wadl == null )
				throw new RuntimeException( "Unable to load wadl with location '" + wadlRef + "'");
			 
			try {
				Unmarshaller u = jaxbCtx.createUnmarshaller();
				Object o = u.unmarshal( new ByteArrayInputStream(wadl.getBytes()) );
				this.jaxbApp = (Application)o;
			} catch( Exception e ) {
				throw new RuntimeException( "Error parsing wadl at location '" + wadlRef + "'", e);
			}
		}
		
		List<ResourceOperation> resourceOperations = this.flattenToOperationList();
		
		String doc = "";
		for( Doc d : this.jaxbApp.getDoc() ) {
			doc = "<h1>Title: " + d.getTitle() + "</h1>\n";
			for( Object o : d.getContent() ) {
				doc += o;
			}
		}
		List<Object> any = this.jaxbApp.getAny();
		for (Object object : any) {
			System.out.println( object.getClass().getName() );	
		}
		
		RestfulService service = new RestfulService();
		service.setName(this.findServiceName());
		service.setDocumentation( doc );
		service.setResourceOperations( resourceOperations );
		return service;
	}
	/**
	 * This method flattens all the resources in a wadl which are represented as a 
	 * hierarchy (in the wadl).
	 *  
	 * @return
	 */
	private List<ResourceOperation> flattenToOperationList() {
		
		this.pathStack.clear();
		Resources jaxbResources = this.jaxbApp.getResources();
		
		List<ResourceOperation> flatModelResources = new ArrayList<ResourceOperation>();
		
		for (Resource jaxbResource : jaxbResources.getResource() ) {
			this._flatten( jaxbResource, flatModelResources );
		}

		Collections.sort(flatModelResources, 
				new Comparator<ResourceOperation>() {
					@Override
					public int compare(ResourceOperation r1, ResourceOperation r2) {
						return r1.getPath().compareTo(r2.getPath());
					}
				}
		);
		return flatModelResources;
	}
	
	/**
	 * This method is used by the public flatten method. It accepts a wadl resource, and a 
	 * list of ResourceOperation which it populates.
	 * 
	 * If a wadl resource contains methods (as in HTTP methods) then we'll create a new
	 * ResourceOperation, and add it to the resourceOperations list. In addition, if the
	 * wadl resource had child resources, then this method will call iteself
	 * for further creation of ResourceOperations. 
	 * 
	 * @param jxbr
	 * @param modelResources
	 */
	private void _flatten( Resource jxbr, List<ResourceOperation> resourceOperations ) {
		String jxbPath = jxbr.getPath();
		if( jxbPath.startsWith("/") )
			this.pathStack.push( jxbPath );
		else
			this.pathStack.push( "/"+jxbPath );

		List<Method> jxbMethods = listMethods(jxbr);
		List<Resource> jxbResources = listResources(jxbr);
		
		if(jxbMethods!=null) {  //now we know this resource responds to at least one http method.
			for (Method jxbm : jxbMethods) {
				ResourceOperation rop = new ResourceOperation();
				
				String path = this.pathStack.currentPath().replaceAll("//", "/");
				
				path = (this.includeBase)? this.jaxbApp.getResources().getBase()+path.replaceFirst("/", ""):path;
				
				rop.setPath( path );
				rop.setId( jxbm.getId() );
				rop.setMethod(jxbm.getName() );
				rop.setMethodDocs( jxbm.getDoc() );
				rop.setResourceDocs( jxbr.getDoc() );

				Request jxbReq;
				//might be useful to extract <params> as in the future (query string and path template)
				//and what about grammars!
				if( (jxbReq = jxbm.getRequest()) != null )
					rop.setRequestMediaTypes( this.listMediaTypes( jxbReq.getRepresentation() ) );
				
				Response jxbResp;
				if( (jxbResp = jxbm.getResponse()) != null ) {

					List<JAXBElement<RepresentationType>> jxbRepOrFault = jxbResp.getRepresentationOrFault();
					
					rop.setResponseMediaTypes( 
							listMediaTypes ( listRepresentationTypes( jxbRepOrFault )));
					
				}
				resourceOperations.add( rop );
			}
		}
		
		//this is where recursion happens...
		if(jxbResources!=null)
			for (Resource jaxbResource : jxbResources )
				_flatten(jaxbResource,resourceOperations);

		this.pathStack.pop();
	}

	/**
	 * This method returns a List of RepresentationType's for the given List of JAXBElements 
	 * @param jxbelements
	 * @return
	 */
	private List<RepresentationType> listRepresentationTypes( List<JAXBElement<RepresentationType>> jxbelements ) {
		List<RepresentationType> list = new ArrayList<RepresentationType>();
		
		for (JAXBElement<RepresentationType> jaxbElement : jxbelements) {
			list.add( jaxbElement.getValue() );
		}
			

		return list;
	}
	
	/**
	 * This method returns a List of media types for a given wald's RepresentationType.
	 * 
	 * @param jxbreps
	 * @return
	 */
	private List<String> listMediaTypes( List<RepresentationType> jxbreps ) {
		List<String> list = new ArrayList<String>();
		
		for (RepresentationType jxbrep : jxbreps)
			list.add( jxbrep.getMediaType() );

		return list;
		
	}
	
	/**
	 * This method takes a resource and returns a list of it's 
	 * supported http methods (GET, POST, PUT, etc..).
	 * 
	 * @param jxbr
	 * @return
	 */
	private List<Method> listMethods( Resource jxbr ) {
		List<Method> methods = new ArrayList<Method>();
		
		for( Object o : jxbr.getMethodOrResource() )
			if( o instanceof Method )
				methods.add((Method)o);
		
		return (methods.size() > 0) ? methods:null; 
	}
	
	/**
	 * This method takes a wadl Resource and returns a List of it's 
	 * child resources. 
	 *
	 * @param jxbr
	 * @return
	 */
	private List<Resource> listResources( Resource jxbr ) {
		List<Resource> jxbRs = new ArrayList<Resource>();
		
		for( Object o : jxbr.getMethodOrResource() )
			if( o instanceof Resource )
				jxbRs.add((Resource)o);
		
		return (jxbRs.size() > 0) ? jxbRs:null; 
	}
	
	private static JAXBContext jaxbCtx;
	static  {
		try {
			//jaxbCtx = JAXBContext.newInstance("org.tiaa.wadl.jaxb:org.tiaa.wsrr.jaxb");
			jaxbCtx = JAXBContext.newInstance("com.sun.research.ws.wadl");
		} catch( JAXBException e ) {
			throw new RuntimeException( "App can't run w/out a jaxb context, fix this!", e );
		}
	}
	
	/**
	 * A simple stack to keep track of URIs during recursion in _flatten().
	 * 
	 * I would have used java.util.Stack, but I wanted my stack to be able to
	 * concatinate part of the stack's contents (see currentPath()).
	 * 
	 * @author Jon
	 */
	private static class PathStack {
		
		List<String> pathStack = new ArrayList<String>();
		public PathStack() {}
		
		public void push( String s ) {
			this.pathStack.add( s );
		}
		
		public void pop() {
			this.pathStack.remove( (this.pathStack.size()-1) );
		}
		
		public String currentPath() {
			StringBuilder builder = new StringBuilder();
			for( String s : this.pathStack )
				builder.append( s );
			
			return builder.toString();
		}
		
		public void clear() {
			pathStack = new ArrayList<String>();
		}
	}
	
	
	public static void main( String[] args ) {}
}

