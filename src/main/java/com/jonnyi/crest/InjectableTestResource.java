package com.jonnyi.crest;

import java.util.Locale;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Path("inject")
public class InjectableTestResource {

	@Context Locale locale; 
	
	@GET
	public Response get() {
		String entity = "default entity for InjectableTestResource\n";
		entity+=this.locale.toString();
		return Response.ok(entity).build();
	}
}
