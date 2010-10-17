package com.jonnyi.crest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.jonnyi.crest.model.Child;
import com.jonnyi.crest.model.ChildCollection;
import com.jonnyi.crest.model.Message;
import com.jonnyi.crest.model.Parent;
import com.jonnyi.crest.model.ParentCollection;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;

@Provider
public final class JAXBContextResolver implements ContextResolver<JAXBContext> {

	private final JAXBContext context;

	private final Set<Class<?>> types;

	private final Class<?>[] cTypes = new Class[] { ParentCollection.class, Parent.class, ChildCollection.class, Child.class, Message.class };
 
	public JAXBContextResolver() throws JAXBException {
		JSONConfiguration config = JSONConfiguration.natural()
				.rootUnwrapping(true).build();

		this.context = new JSONJAXBContext(config, cTypes);
		this.types = new HashSet<Class<?>>(Arrays.asList(cTypes));
	}

	public JAXBContext getContext(Class<?> objectType) {
		return (types.contains(objectType)) ? context : null;
	}
}
