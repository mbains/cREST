package com.jonnyi.wadl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class is responsible for loading a wadl. Clients can invoke one of the two methods...
 * 
 * load( String wadl ) - this method will invoke all loaders one after another until one of them
 * returns the wadl. 
 * 
 * load( String wadl, WadlLoader loader ) - this method is more efficient than the last. If you know
 * ahead of time which loader should be used, call this method. 
 * 
 * @author jiantosca
 *
 */
public abstract class WadlLoader {
	
	protected WadlLoader successor;
	
	public void setSuccessor( WadlLoader successor ) {
		this.successor = successor;
	}
	
	protected InputStream locateInputStream( String wadl ) {
		InputStream in = this.doLocateInputStream( wadl );
		
		System.out.println( this.getClass().getName() + " doLocateInputStream() returned " + in );
		
		if( in == null && this.successor != null ) {
			try {
				return this.successor.locateInputStream( wadl );				
			} catch( Exception e ) {/*swallow it so we don't break the chain.*/}
		}
		return in;
	}
	
	protected abstract InputStream doLocateInputStream( String wadl );
	
	
	public static String load( String wadl ) {
		return load( wadl, null );
	}
	
	public static String load( String wadl, WadlLoader loader ) {
		InputStream in;
		if( loader != null )
			in = loader.locateInputStream( wadl );
		
		in = rootLoader.locateInputStream( wadl );
		
		return toWadlString( in );
	}
	
	private static WadlLoader rootLoader;
	static {
		rootLoader = new ClassPathWadlLoader();
		WadlLoader loader2 = new FileWadlLoader();
		WadlLoader loader3 = new URLWadlLoader();
		loader2.setSuccessor( loader3 );
		rootLoader.setSuccessor( loader2 );
	}
	
	
	/**
	 * This method takes an InputStream and put the contents of
	 * it into the returned String.
	 * 
	 * @param in
	 * @return String if in != null, and in has data. 
	 */
	private static String toWadlString( InputStream in ) {
		if( in == null ) 
			return null;
		
		int length;
        byte[] buffer = new byte[8192];
        ByteArrayOutputStream out = null;
		try {
			out = new ByteArrayOutputStream();
			 while ((length = in.read(buffer)) != -1) {
				out.write(buffer, 0, length);
			 }
		} catch (IOException e) {
			throw new RuntimeException( "Error loading InputStream: " + in );
		} finally {
			try {
				in.close();	
			} catch ( Exception e ) {}
		}
		return new String( out.toByteArray() ); 
	}
}

class ClassPathWadlLoader extends WadlLoader {
	@Override
	public InputStream doLocateInputStream(String wadl) {
		ClassLoader loader = this.getClass().getClassLoader();
		URL url = loader.getResource( wadl );
		System.out.println( "Classpath Loader: " + url );
		if( url == null )
			return null;
		try {
			return url.openStream();
		} catch (IOException e) {
			throw new RuntimeException( "found resource '" + url + "' on the classpath, but can't open its stream.");
		}
	}
}

class FileWadlLoader extends WadlLoader {
	@Override
	public InputStream doLocateInputStream(String wadl) {
		File f = new File( wadl );
		InputStream in = null;
		
		if( f.exists() )
			try {
				in = new FileInputStream( f );
			} catch( FileNotFoundException e ) {}
			
		return in;
	}
}

class URLWadlLoader extends WadlLoader {
	@Override
	public InputStream doLocateInputStream(String wadl) {
		InputStream in = null;
		
		try {
			URL url = new URL( wadl );
			in = url.openStream();
		} catch (Exception e) {}
		
		return in;
	}
}
