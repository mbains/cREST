package com.jonnyi.crest.support;

import java.util.List;
import java.util.Locale;

import javax.ws.rs.ext.Provider;

import com.sun.jersey.api.core.HttpContext;
//http://codahale.com/what-makes-jersey-interesting-injection-providers/
@Provider
public class LocaleProvider 
	extends CrestAbstractInjectableProvider<Locale> {
	
	public LocaleProvider() {
		 super(Locale.class);
		 System.out.println( "LocaleProvider" );
		 System.out.println( "LocaleProvider" );
		 System.out.println( "LocaleProvider" );
		 System.out.println( "LocaleProvider" );
	}
    
	@Override
    public Locale getValue(HttpContext c) {
        final List<Locale> locales = c.getRequest().getAcceptableLanguages();
        if (locales.isEmpty()) {
          return Locale.US;
        }
        return locales.get(0);
    }
}
