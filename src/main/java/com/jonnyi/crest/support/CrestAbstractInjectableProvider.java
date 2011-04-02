package com.jonnyi.crest.support;

import java.lang.reflect.Type;

import javax.ws.rs.core.Context;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

//http://codahale.com/what-makes-jersey-interesting-injection-providers/
public abstract class CrestAbstractInjectableProvider<E> 
	extends AbstractHttpContextInjectable<E>
	implements InjectableProvider<Context, Type> {


    private final Type type;

    public CrestAbstractInjectableProvider(Type type) {
        this.type = type;
    }

    @Override
    public Injectable<E> getInjectable(ComponentContext ic, Context a, Type c) {
    	System.out.println( "Jersey asking if i can inject a: " + c );
    	//System.out.println( "   ComponentContext.getAnnotations(): " + ic.getAnnotations() );
    	for(int i = 0; i < ic.getAnnotations().length; i++)
    		System.out.println( "                                      " + ic.getAnnotations()[i]);
    	
    	//System.out.println( "   ComponentContext.getAccesibleObject(): " + ic.getAccesibleObject() );
 	    System.out.println();
    	if (c.equals(this.type)) {
            return getInjectable(ic, a);
        }
        return null;
    }

    public Injectable<E> getInjectable(ComponentContext ic, Context a) {
        return this;
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }
	
}
