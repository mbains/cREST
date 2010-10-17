package com.jonnyi.crest;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

public class LoggingFilter 
	implements ContainerRequestFilter, ContainerResponseFilter {

	@Context HttpServletRequest servletReq;
	
	public ContainerRequest filter(ContainerRequest req) {
		System.out.println( "******************** REQUEST *********************" );
		Enumeration e = servletReq.getHeaderNames();
		while(e.hasMoreElements()) {
			String key = (String)e.nextElement();
			System.out.println(key + ": " + servletReq.getHeader(key));
		}
		System.out.println( "******************** REQUEST *********************" );
		return req;
	}
	
	public ContainerResponse filter(ContainerRequest req,
            ContainerResponse resp) {
		return resp;
	}
}
