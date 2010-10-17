package com.jonnyi.crest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/test")
public class TestResource {

	@GET
	@Produces({ MediaType.APPLICATION_JSON})
	@Path("pause/{seconds}" )
	public String getPause( @PathParam("seconds") long seconds ) {
		try {
			Thread.sleep(seconds*1000);
		} catch( InterruptedException e ) {}
		return "{\"message\":\"Successfully paused for "+seconds+" seconds\"}";
	}
	
	@GET
	@Produces({ MediaType.APPLICATION_JSON})
	@Path("invalid-json" )
	public Response getInvalidJSON() {
		return Response.ok("Invalid JSON").build();
	}
	@GET
	@Produces({ MediaType.APPLICATION_XML})
	@Path("invalid-json" )
	public Response getInvalid() {
		return Response.ok("Invalid XML").build();
	}
}
