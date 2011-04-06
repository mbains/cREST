package com.jonnyi.crest;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Test;

import com.jonnyi.crest.model.Parent;


public class ParentsResourceTest {
	
	//@Test
	public void parentsGet() {
//		ClientConfig cc = new DefaultClientConfig();
//		cc.getClasses().add(JAXBContextResolver.class);
//		
//		Client c = Client.create(cc);
//		WebResource wr = c.resource("http://localhost:8088/rest/parents");
//		ClientResponse cr = wr.queryParam("noclone", "true").get( ClientResponse.class );
////		System.out.println( cr.getEntity( String.class ) );
////		if( true ) return;
//		ParentCollection pc = cr.getEntity( ParentCollection.class );
//		
//		//make sure we have the parents that are expected...
//		int expectedSize = 3;
//		assertTrue( "Expecting parents size of " + expectedSize,
//				pc.parents.size() == expectedSize );
//		
//		assertTrue( "Expected id not found in ParentCollection", this.hasParents(pc.parents, 1001,1002,1003) );
//		
//		//assertTrue( "Expected child not found in Parent ", pc.findChild(1001, 2001) != null );
//		System.out.println( pc.findParent( 1001 ).childCollection.children );
	}
	
	
	
	
	
	
	/*
	 * Returns true when all the ids are found in the parents collection.
	 */
	private boolean hasParents( List<Parent> parents, int...ids ) {
		for (int id : ids) {
			boolean foundIt = false;
			for (Parent p : parents) {
				if( p.id == id ) {
					foundIt = true;
					break;
				}
			}
			if( foundIt == false ) {
				System.out.println( "Unable to find id " + id + " in the parents collection." );
				return false;
			}
				
		}
		return true;
	}
}
