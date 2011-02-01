package com.jonnyi.wadl;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.jonnyi.wadl.model.ResourceOperation;
import com.jonnyi.wadl.model.RestfulService;
import com.sun.research.ws.wadl.Application;

@Path("")
public class WadlReaderResource {

	private @QueryParam("w") String wadlRef;
	private @QueryParam("base") boolean qsBase;
	private @QueryParam("qsum") boolean qsQuickSummary;
	
	/**
	 * 
	 * javax.ws.rs.WebApplicationException: com.sun.jersey.api.MessageException: 
	 * A message body writer for Java class org.tiaa.wsrr.model.RestfulService, and
	 *  Java type class org.tiaa.wsrr.model.RestfulService, and MIME media type 
	 *  application/json was not found
	 *  
	 *  How do I create my own message body writer? We could use 'em for versioning and 
	 *  our custom media types somehow?
	 *  
	 * This resource creates a resource oriented view of a wadl. 
	 * @param wadlUri
	 * @return
	 */
	@GET
	//@Path("wadl-summarizer/{wadl-uri: .*}")
	@Path("wadl-summarizer")
	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.TEXT_PLAIN})
	public Response getWadl() {
		Response resp;
		try {
			WadlParser wp = new WadlParser( wadlRef );	
			wp.setIncludeBase(this.qsBase);
			RestfulService service = wp.flatten();
			if(this.qsQuickSummary) {
				List<ResourceOperation> rops = this.formatForQuickSummary(service);
				GenericEntity<List<ResourceOperation>> ge = new GenericEntity<List<ResourceOperation>>(rops){};
				resp = Response.ok( ge ).build();	
			} else {
				resp = Response.ok( service ).build();	
			}
			
		} catch( Exception e ) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream( out );
			e.printStackTrace( ps );
			resp = Response.serverError().entity("Unable to parse wadl '" + wadlRef + "'. \n" + new String(out.toByteArray() )).build();
		}
		return resp;	
	}
	
	public List<ResourceOperation> formatForQuickSummary(RestfulService service) {
		List<ResourceOperation> newRops = new ArrayList<ResourceOperation>();
		
		for( ResourceOperation rop : service.getResourceOperations() ) {
			ResourceOperation newRop = new ResourceOperation();
			newRop.setName( rop.getName() );
			newRops.add( newRop );
		}
		return newRops;
	}
	
	@POST
	@Path("wadl-summarizer")
	@Consumes("application/vnd.sun.wadl+xml")
	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML,MediaType.TEXT_PLAIN})
	public Response postWadl(Application jaxbApp) {
		Response resp;
		try {
			WadlParser wp = new WadlParser( jaxbApp );
			System.out.println(this.qsBase);
			wp.setIncludeBase(this.qsBase);
			RestfulService service = wp.flatten();
			if(this.qsQuickSummary) {
				List<ResourceOperation> rops = this.formatForQuickSummary(service);
				GenericEntity<List<ResourceOperation>> ge = new GenericEntity<List<ResourceOperation>>(rops){};
				resp = Response.ok( ge ).build();	
			} else {
				resp = Response.ok( service ).build();	
			}
		} catch( Exception e ) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream( out );
			e.printStackTrace( ps );
			resp = Response.serverError().entity("Unable to parse posted wadl '" + jaxbApp + "'. \n" + new String(out.toByteArray() )).build();
		}
		return resp;	
	}
}
