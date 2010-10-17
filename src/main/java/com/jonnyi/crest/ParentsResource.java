package com.jonnyi.crest;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.jonnyi.crest.model.Child;
import com.jonnyi.crest.model.ChildCollection;
import com.jonnyi.crest.model.Link;
import com.jonnyi.crest.model.Message;
import com.jonnyi.crest.model.Parent;
import com.jonnyi.crest.model.ParentCollection;

/**
 * Here's a good article on when/why to use what methods.
 * 
 * http://jcalcote.wordpress.com/2008/10/16/put-or-post-the-rest-of-the-story/
 * 
 * TODO: return 404 if parentsCache is null (like when delete is called on /parents)
 * 
 * @author jon
 *
 */
@Path("/parents")
public class ParentsResource {
	
	@HeaderParam("Accept") String accept;
	@Context UriInfo uriInfo;
	@Context HttpHeaders headers;
	
	@QueryParam("verbose") boolean verbose;
	@QueryParam("noclone") boolean noclone;
	
	public ParentsResource() {
		log.println(new Date() + " new " + this.getClass().getName());
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public ParentCollection get() {
		
		if( parentsCache == null )
			throw new WebApplicationException( Response.status( Status.NOT_FOUND).build() );
		
		if( noclone )
			return parentsCache;


		ParentCollection clone = cloneCache();
		buildParentLinks(clone);
		
		if(verbose)
			return clone; 

		
		//now format for response, when returning the list of parents we want to limit
		//the info and provide links for further drill down. Also, I'm removing links to
		//previous and next because when the client has the whole list they already know
		//what's next/prev. Those links are better for when targeting a specific Parent.
		for (Parent p : clone.parents) {
			p.childCollection = null;
			p.number = null;
			p.value = null;
			p.living = null;
			p.links = this.removeRelations(p.links, "previous","next" ); 
		}
		return clone;
	}
	
	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response post( Parent p ) {
		if( p.id != null ) {
			URI uri = uriInfo.getBaseUriBuilder().path(this.getClass() ).path(this.getClass(), "getParent" ).build( p.id );
			String msg = "Received parent entity that contains an id. This implies that you already know the URI of the resource " +
						 "so you should use PUT " + uri + ", otherwise remove id from your entity, and retry (server will specify id, and give you a URI via Location header)";

			throw new WebApplicationException(
					Response.status(Status.BAD_REQUEST).entity(
							Message.error(msg) ).build()
			);
		}
		
		int lastId = parentsCache.parents.get( parentsCache.parents.size()-1 ).id;
		p.id =++lastId;
		p.name += (" "+p.id);
		parentsCache.parents.add( p );
		URI uri = uriInfo.getBaseUriBuilder().path(this.getClass() ).path(this.getClass(), "getParent" ).build( p.id );
		return Response.created(uri).build();
	}

	@PUT
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response put( ParentCollection pc ) {
		parentsCache = pc;

		return Response.ok().build();
	}
	
	
	/**
	 * Deletes the entire parents collection. 
	 * @return
	 */
	@DELETE
	public Response delete() {
		parentsCache = null;
		return Response.ok().build();
	}
	
	
	
	
	
	
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("{parent-id}")
	public Parent getParent(@PathParam("parent-id") int pid) {
		log.println("Entering getParent");
		ParentCollection clone = cloneCache();
		buildParentLinks(clone);
		Parent p = clone.findParent( pid );
		
		if( p == null ) throw new WebApplicationException( Response.Status.NOT_FOUND );
		
		p.childCollection = null;
		p.links = this.removeRelations( p.links, "self" );
		
		return p;
	}

	
	//finish here!!!
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("{parent-id}")
	public Response putParent(@PathParam("parent-id") int pid, Parent newParent ) {
		System.out.println( newParent );
		System.out.println( newParent );
		System.out.println( newParent.id );
		System.out.println( newParent.id );
		System.out.println( newParent.id );
		
		if( parentsCache.findParent( pid ) != null ) {
			log.println( "Parent already exists!!" );
			log.println( "Parent already exists!!" );
			throw new WebApplicationException( Response.Status.CONFLICT );
		}
		
		if( newParent == null ) {
			throw new WebApplicationException( Response.Status.BAD_REQUEST );
		}
		
		return Response.ok("{\"msg\": \"NOT IMPLEMENTED\"}").build();
	}
	
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("{parent-id}/children")
	public ChildCollection getChildren(@PathParam("parent-id") int pid) {
		log.println("Accept header: '" + accept + "'");
		ParentCollection clone = cloneCache();
		buildParentLinks(clone);
		Parent p = clone.findParent( pid );
		ChildCollection cc = null;
		if (p != null) {
			cc = p.childCollection;
			if( verbose == true )
				return cc;
			
			if( cc != null ) {
				for (Child c : cc.children) {
					c.number = null;
					c.value = null;
					c.living = null;
				}
			}
		}
		return cc;
	}

	/**
	 * This resource resets the parents cache as if the app was restarted. 
	 */
	@GET
	@Path("reset")
	public Response reset() {
		parentsCache = createParents(startingPid, parentCount, startingCid, childCount);
		return Response.ok().build();
	}
	
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("{parent-id}/children/{child-id}")
	public Child getChild(@PathParam("parent-id") int pid,
			@PathParam("child-id") int cid) {
		log.println("Accept header: '" + accept + "'");
		ParentCollection clone = cloneCache();
		buildParentLinks(clone);
		Child child = clone.findChild( pid, cid );
		if( child == null )
			throw new WebApplicationException( Status.NOT_FOUND );
		
		return child;
	}

	private void buildParentLinks( ParentCollection pc ) {
		List<Parent> parents = pc.parents;
		int size = parents.size();

		for (int i = 0; i < size; i++) {
			Parent p = parents.get(i);
			UriBuilder builder = uriInfo.getBaseUriBuilder();
			URI uri = builder.path(this.getClass())
					.path(this.getClass(), "getParent").build(p.id);
			p.links.add(Link.self().href(uri));
			if (size > 1) {
				if (i < (size - 1)) {// is there a next?
					builder = uriInfo.getBaseUriBuilder();
					uri = builder.path(this.getClass())
							.path(this.getClass(), "getParent")
							.build(parents.get(i + 1).id);
					p.links.add(Link.next().href(uri));
				}
				if (i > 0) {// is there a previous
					builder = uriInfo.getBaseUriBuilder();
					uri = builder.path(this.getClass())
							.path(this.getClass(), "getParent")
							.build(parents.get(i - 1).id);
					p.links.add(Link.previous().href(uri));
				}

			}
			if (p.childCollection.children != null && !p.childCollection.children.isEmpty()) {
				builder = uriInfo.getBaseUriBuilder();
				uri = builder.path(this.getClass())
						.path(this.getClass(), "getChildren").build(p.id);
				p.links.add(Link.child().href(uri));
				buildChildLinks(p.childCollection.children, p.id );
			}

		}
	}

	/**
	 * This method assume that the collection of children will always have the
	 * same parent. 
	 * @param children
	 * @param pid
	 */
	private void buildChildLinks(List<Child> children, Integer pid ) {
		int size = children.size();
		for (int i = 0; i < size; i++) {
			Child c = children.get(i);
			UriBuilder builder = uriInfo.getBaseUriBuilder();
			URI uri = builder.path(this.getClass())
					.path(this.getClass(), "getChild").build(pid,c.id);
			c.links.add(Link.self().href(uri));
			if (size > 1) {
				if (i < (size - 1)) {// is there a next?
					builder = uriInfo.getBaseUriBuilder();
					Child next = children.get(i + 1);
					uri = builder.path(this.getClass())
							.path(this.getClass(), "getChild")
							.build(pid, next.id);
					c.links.add(Link.next().href(uri));
				}
				if (i > 0) {// is there a previous
					builder = uriInfo.getBaseUriBuilder();
					Child previous = children.get(i - 1);
					uri = builder.path(this.getClass())
							.path(this.getClass(), "getChild")
							.build(pid,previous.id);
					c.links.add(Link.previous().href(uri));
				}

			}
		}
	}


	
	public List<Link> removeRelations( List<Link> links, String...rels ) {
		List<Link> newLinks = new ArrayList<Link>();
		for (Link link : links) {
			boolean keeper = true;
			for (String rel : rels) {
				if( link.rel.equalsIgnoreCase( rel ) ) {
					keeper = false;
					break;
				}
			}
			if( keeper )
				newLinks.add( link );
		}
		return newLinks;
	}

	private static ParentCollection createParents(int pid, int pCount, int cid,
			int cCount) {
		log.println( "creating Parents cache with pid " + pid + " pCount " + pCount + " cid " + cid + " cCount " + cCount );
		
		boolean pliving = false;
		boolean cliving = false;
		List<Parent> newParents = new ArrayList<Parent>();
		for (int i = 0; i < pCount; i++) {
			Parent p = new Parent();
			p.id = ++pid;
			p.name = "Parent Name " + p.id;
			p.value = "Parent Value " + p.id;
			p.number = new BigDecimal(p.id + 420.23);
			p.childCollection.children = new ArrayList<Child>();
			p.living = (pliving = !pliving);
			for (int j = 0; j < cCount; j++) {
				Child c = new Child();
				c.id = ++cid;
				c.name = "Child Name " + c.id;
				c.value = "Child Value " + c.id;
				c.number = new BigDecimal(c.id - 420.23);
				c.living = (cliving = !cliving);
				p.childCollection.children.add(c);
			}
			newParents.add(p);
		}
		ParentCollection pc = new ParentCollection();
		pc.parents = newParents; 
		return pc;
	}

	private static ParentCollection cloneCache() {
		List<Parent> clone = new ArrayList<Parent>();

		for (Parent p : parentsCache.parents) {
			clone.add(new Parent(p));
		}
		ParentCollection pc = new ParentCollection();
		pc.parents = clone;
		return pc;
	} 
	static int startingPid = 1000;
	static int parentCount = 3;
	static int startingCid = 2000;
	static int childCount = 3;
	
	static PrintStream log = System.out;
	static ParentCollection parentsCache = createParents(startingPid, parentCount, startingCid, childCount);
	
}
