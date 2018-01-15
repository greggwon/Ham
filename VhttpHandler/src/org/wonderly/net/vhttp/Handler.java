package org.wonderly.net.vhttp;

import java.net.*;
import java.io.*;
import javax.swing.*;
import java.util.*;
import java.util.logging.*;
import java.awt.event.*;
import java.security.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *  This is the protocol handler for the vhttp protocol.
 *  This protocol caches http:// requests in a local directory, and displays
 *  download progress if <code>setParent()</code> is called with a non-null frame.
 *  The caching directory is established using <code>setCacheDir( dirName )</code> and
 *  is homed to <code>System.getProperty("user.dir")</code> if it is relative.  If the
 *  caching directory is not set, no caching is done.
 *  <p>
 *  So, to use this in some java application, you need to do
 *  <pre>
		if( System.getProperty("java.protocol.handler.pkgs") == null ) {
			System.getProperties().put("java.protocol.handler.pkgs",
				"org.wonderly.url");
		}
		JFrame top;
		...
		// Set the parent frame for JDialog homing.
		org.wonderly.url.vhttp.Handler.setParent(top);
		// Set caching directory as 'cache' under the running directory.
		org.wonderly.url.vhttp.Handler.setCacheDir("cache");
 *  </pre>
 *  <p>
 *
 *  The following properties are consulted in a static initializer
 *  <dl>
 *		<dt>org.wonderly.vhttp.protocol<dd>defaults to http
 *		<dt>org.wonderly.vhttp.cache<dd>defaults to no cache directory, which is null
 *		<dt>org.wonderly.vhttp.debug<dd>set to non-empty string enables debug logging
 *		    using the <code>org.wonderly.url.vhttp.Handler</code> logger
 *  </dl>
 *
 *  @see #setParent(JFrame)
 *  @see #setCacheDir(String)
 *  @author <a href="mailto:gregg.wonderly@pobox.com">Gregg Wonderly</a>.
 */
public class Handler extends URLStreamHandler {
//	static JFrame parent;
	private static String cache;
	private static String proto = "https";
	private Logger log = Logger.getLogger( getClass().getName() );
	private static ArrayList<DownloadListener> listeners = new ArrayList<DownloadListener>();
	// Reference whether or not we've already added the default download listener.
	private static DefaultDownloadListener defaultListener = null;
	private LinkedBlockingQueue<Runnable> lisQueue = new LinkedBlockingQueue<Runnable>();
	private ThreadPoolExecutor lisExec = new ThreadPoolExecutor( 1, 2, 15, TimeUnit.SECONDS, lisQueue );
	
	static {
		String pr = System.getProperty("org.wonderly.vhttp.protocol");
		if( pr != null )
			proto = pr;
		String cd = System.getProperty("org.wonderly.vhttp.cache");
		if( cd != null )
			cache = cd;
		Logger log = Logger.getLogger(Handler.class.getName());
		if( log.isLoggable(Level.FINE)) {
			log.fine("protocol from \"org.wonderly.vhttp.protocol\" system property: "+proto );
			log.fine("cache from \"org.wonderly.vhttp.cache\" system property: "+cache );
		}
	}

	public static void main( String args[] ) throws Exception {
		if( System.getProperty("java.protocol.handler.pkgs") == null ) {
			System.getProperties().put("java.protocol.handler.pkgs",
				"org.wonderly.url");
		}
		Logger log = Logger.getLogger( Handler.class.getName()+".main" );
		log.setLevel( Level.FINE );
		final JFrame top = new JFrame( "Testing download" );
		top.setSize( 700, 500 );
		top.setLocation( 100, 100 );
		top.setVisible(true);
		org.wonderly.net.vhttp.Handler.setParent(top);
		org.wonderly.net.vhttp.Handler.setCacheDir("c:/test-cache");
		top.addWindowListener( new WindowAdapter() {
			public @Override void windowClosing( WindowEvent ev ) {
//				System.gc();
//				new File( "cache/reggie-dl.jar" ).delete();
				top.dispose();
				System.exit(1);
			}
		});
		for( int i = 0; i < 100; ++i ) {
			log.fine("connection: ["+i+"]");
			URL u = new URL( "vhttp://chipmunk:8090/reggie-dl.jar" );
			Object obj = u.getContent();
			if( log.isLoggable(Level.FINE)) 
				log.fine("got content: "+obj );
			u = null;
			obj = null;
		}
	}
	
	public synchronized static void addDownloadListener(DownloadListener lis) {
		listeners.add(lis);
	}
	public synchronized static void removeDownloadListener(DownloadListener lis) {
		listeners.remove(lis);
	}

	// default is that we will check again with HEAD after 30secs to see if
	// the cached file is out of date.
	private static volatile long dltimeout = 30000;
	public static void setDownloadCacheTimeout( long val ) {
		dltimeout = val;
	}
	/**
	 *  Set the currently used protocol for final retrieval
	 */
	public static void setProtocol( String proto ) {
		Handler.proto = proto;
	}

	public static String getProtocol() {
		return proto;
	}

	/**
	 *  Public constructor that allows java.net.URL to construct us
	 *
	 *  @see java.net.URL
	 */
	public Handler() {
		if( log.isLoggable(Level.FINE)) log.fine("Constructing: "+this );
	}

	public static synchronized void setParent( JFrame parent ) {
		Logger log = Logger.getLogger( Handler.class.getName() );
		if(log.isLoggable(Level.FINE)) log.fine( "Parent set to: "+parent );
		
		// Add a default listener for backwards compatibility, to provide a progress indication.
		if (defaultListener == null) {
			addDownloadListener(defaultListener = new DefaultDownloadListener(parent));
		} else {
			defaultListener.parent = parent;
		}
	}

	public static void setCacheDir( String dir ) {
		Logger log = Logger.getLogger( Handler.class.getName() );
		if( log.isLoggable(Level.FINE)) log.fine("setting cache ("+cache+") to: "+dir );
		cache = dir;
		new File(cache).mkdirs();
	}

	/**
	 *  Open a new connection to the ftp URL by creating an instance
	 *  of the inner class, <code>URLConnection</code> that will do the
	 *  creation of the process to run ftp in.
	 */
	public java.net.URLConnection openProxiedConnection( final URL url ) throws IOException {
		URLConnection uc = new ProxyUrlConnection(url);
		return uc;
	}
	private class ProxyUrlConnection extends URLConnection {
		private final URL svurl;
		private volatile boolean useCaching = true;
		public ProxyUrlConnection( URL url ) {
			super(url);
			svurl = url;
		}
		private volatile URLConnection realcon;
		private synchronized URLConnection getReal() throws IOException {
			if( realcon == null ) {
				realcon = openConnection(svurl);
				realcon.setUseCaches( useCaching );
			}
			return realcon;
		}
		private synchronized URLConnection getRealValue() {
			if( realcon == null ) {
				try {
					realcon = openConnection(svurl);
					realcon.setUseCaches( useCaching );
				} catch (IOException ex) {
					Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
					throw new RuntimeException( "Unexpected connection exception", ex );
				}
			}
			return realcon;
		}
		public @Override void addRequestProperty(String key, String value) {
			getRealValue().addRequestProperty( key, value );
		}
		public @Override void connect() throws IOException {
			getReal().connect();
		}
		public @Override boolean getAllowUserInteraction() {
			return getRealValue().getAllowUserInteraction();
		}
		public @Override int getConnectTimeout() {
			return getRealValue().getConnectTimeout();
		}
		public @Override Object getContent() throws IOException {
			return getReal().getContent();
		}
		public @Override Object getContent(Class[] classes) throws IOException {
			return getReal().getContent( classes );
		}
		public @Override String getContentEncoding(){
			return getRealValue().getContentEncoding();
		}
		public @Override int getContentLength() {
			return getRealValue().getContentLength();
		}
		public @Override String getContentType()  {
			return getRealValue().getContentType();
		}
		public @Override long getDate() {
			return getRealValue().getDate();
		}
		public @Override boolean getDefaultUseCaches() {
			return getRealValue().getDefaultUseCaches();
		}
		public @Override boolean getDoInput()  {
			return getRealValue().getDoInput();
		}
		public @Override boolean getDoOutput(){
			return getRealValue().getDoOutput();
		}
		public @Override long getExpiration() {
			return getRealValue().getExpiration();
		}
		public @Override String getHeaderField(int n) {
			return getRealValue().getHeaderField(n);
		}
		public @Override String getHeaderField(String name) {
			return getRealValue().getHeaderField( name );
		}
		public @Override long getHeaderFieldDate(String name, long Default){
			return getRealValue().getHeaderFieldDate( name, Default );
		}
		public @Override int getHeaderFieldInt(String name, int Default) {
			return getRealValue().getHeaderFieldInt( name, Default );
		}
		public @Override String getHeaderFieldKey(int n) {
			return getRealValue().getHeaderFieldKey( n );
		}
		public @Override Map<String,List<String>> getHeaderFields() {
			return getRealValue().getHeaderFields();
		}
		public @Override long getIfModifiedSince() {
			return getRealValue().getIfModifiedSince();
		}
		public @Override InputStream getInputStream() throws IOException {
			return getReal().getInputStream();
		}
		public @Override long getLastModified() {
			return getRealValue().getLastModified();
		}
		public @Override OutputStream getOutputStream() throws IOException {
			return getReal().getOutputStream();
		}
		public @Override Permission getPermission() throws IOException {
			if( realcon != null || cache == null ) {
				return getReal().getPermission();
			}
			int port = url.getPort();
			port = port < 0 ? 80 : port;
			Permission p = new FilePermission(cache+File.separator+svurl.getHost()+File.separator+port+svurl.getPath(),"read");
			if (log.isLoggable(Level.FINE))
				log.fine("using unopened perm: "+p );
			return p;
		}
		public @Override int getReadTimeout() {
			return getRealValue().getReadTimeout();
		}
		public @Override Map<String,List<String>> getRequestProperties() {
			return getRealValue().getRequestProperties();
		}
		public @Override String getRequestProperty(String key) {
			return getRealValue().getRequestProperty(key);
		}
		public @Override URL getURL() {
			return getRealValue().getURL();
		}
		public @Override boolean getUseCaches() {
			if( realcon != null )
				return realcon.getUseCaches();
			else
				return useCaching;
		}
		public @Override void setAllowUserInteraction(boolean allow ){
			getRealValue().setAllowUserInteraction( allow );
		}
		public @Override void setConnectTimeout(int timeout) {
			getRealValue().setConnectTimeout( timeout );
		}
		public @Override void setDefaultUseCaches(boolean defaultUseCaches) {
			getRealValue().setDefaultUseCaches( defaultUseCaches );
		}
		public @Override void setDoInput(boolean doinput) {
			getRealValue().setDoInput(doinput);
		}
		public @Override void setDoOutput(boolean dooutput){
			getRealValue().setDoOutput( dooutput );
		}
		public @Override void setIfModifiedSince( long ifmod ) {
			getRealValue().setIfModifiedSince( ifmod );
		}
		public @Override void setReadTimeout(int timeout) {
			getRealValue().setReadTimeout( timeout );
		}
		public @Override void setRequestProperty(String key, String value) {
			getRealValue().setRequestProperty( key, value );
		}
		public @Override void setUseCaches(boolean usecaches) {
			if( realcon != null )
				realcon.setUseCaches( usecaches );
			this.useCaching = usecaches;
		}
		public @Override String toString() {
			if( realcon == null ) {
				return "Pending CHTTP: connection to "+url ;
			}
			return getRealValue().toString();
		}

	}
	public URLConnection openConnection( final URL url ) throws IOException {
		if(log.isLoggable(Level.FINEST) ) {
			log.log(Level.FINEST,"openConnection: "+url, new Throwable(url.toString()) );
		}
		final IOException exc[] = new IOException[1];
		URLConnection ret = (URLConnection) AccessController.doPrivileged(
			new PrivilegedAction<URLConnection>() {
			public URLConnection run() {
				try {
					return openIt( url );
				} catch( IOException ex ) {
					exc[0] = ex;
					log.log(Level.FINE, ex.toString(), ex );
					return null;
				}
			}
		});
		if( ret == null && exc[0] != null )
			throw exc[0];
		return ret;
	}
	
	public static void holdOffDownloadOf( URL u, Long time ) {
		Logger log = Logger.getLogger( Handler.class.getName() );
		if( log.isLoggable(Level.FINE) ) {
			log.fine("holdoffdownload: "+u+", till: "+
				new Date(time.longValue()+(60*1000) )+": lastget="+lastget );
		}
		lastget.put( u.toString(), new Long( time+(60*1000)) );
	}
	
	// This hashtable holds references until GC and then we'll check again
	private static ConcurrentHashMap<String,Long> lastget = new ConcurrentHashMap<String,Long>();
	
	private java.net.URLConnection openIt( URL url ) throws IOException {
		
		if( log.isLoggable(Level.FINE)) log.fine("=== openit(\""+url+"\") cache: "+cache );
		String p = url.getPath();
		if( cache == null ) {
			if( log.isLoggable(Level.FINE)) log.fine("no caching enabled, going direct: "+
				proto+"://"+url.getHost()+":"+url.getPort()+p);
			return new URL( proto+"://"+url.getHost()+":"+url.getPort()+p ).openConnection();
		}
		log.log(Level.FINEST,  "url path: "+p, new Throwable(p) );
		File dc = new File( cache );
		dc = new File( dc, url.getHost() );
		dc = new File( dc, url.getPort()+"" );
		if( dc.exists() == false ) {
			if( log.isLoggable(Level.FINE)) log.fine("making initial dir: "+dc );
			dc.mkdirs();
		} else {
			if( log.isLoggable(Level.FINE)) log.fine("existing cache dir: "+dc );
		}
		
		if( log.isLoggable( Level.FINE ) ) {
			log.fine("trimming lastget entries: "+lastget.size() );
		}

		for( String k : lastget.keySet() ) {
			Long l = lastget.get( k );
			if( l != null && l < System.currentTimeMillis() - dltimeout ) {
				if( log.isLoggable( Level.FINE ) ) {
					log.fine("removed last get time for: "+k );
				}
				lastget.remove( k );
			}
		}
		// Put logging messages here to help track when downloads fail, why exceptions aren't seen....
		String f = url.getFile();
		File cd = new File( dc, p );
		if( log.isLoggable(Level.FINER)) log.finer("cd is: "+cd );
		File pd = new File( cd.getParent());
		if( log.isLoggable(Level.FINER)) log.finer("pd is: "+pd );
		String durl = System.getProperty("user.home")+"/.jarcache/";
		String npref = "";
		if( durl.charAt(0) != '/' && durl.charAt(0) != '\\' )
			npref = "/";
		if( log.isLoggable(Level.FINER)) log.finer("directory url is: "+"file:"+npref+durl);
		if( log.isLoggable(Level.FINER)) log.finer("dc is: "+"file:"+npref+dc);
		durl = "file:"+npref+durl;
		String fp = dc.toString().replace('\\','/')/*.replace(' ','+')*/+
			p.replace('\\','/')/*.replace(' ','+')*/;
		if( log.isLoggable(Level.FINER)) log.finer("fp: "+fp );
		URL uf = new URL( new URL( "file:"+npref+ durl/*.replace(' ','+')*/ ), "file:"+npref+fp ); //+"/"+f );
		if( log.isLoggable(Level.FINER)) log.finer("uf path: "+uf.getPath() );
		File file = new File( uf.getPath() ); //new File( cd,  f );
		File ff = new File( file.getParent() );
		if( ff.exists() == false ) {
			if( log.isLoggable(Level.FINE)) log.fine("making cache directory: "+ff);
			ff.mkdirs(); 
		}

		URL u = new URL( proto+"://"+url.getHost()+":"+url.getPort()+p ); //+"/"+f );
		Long lastTime = (Long)lastget.get(url.toString());
		if( log.isLoggable(Level.FINE)) {
			log.fine("Checking cachefile: "+file+" with url="+url+", time: "+
				(lastTime == null ? "<none>" : (""+
				new Date( lastTime.longValue() )))+", lastget: "+lastget );
		}
		boolean checking = false;
		String tnm = Thread.currentThread().getName();
		java.net.URLConnection uc = null;
		if( lastTime == null || (System.currentTimeMillis() - lastTime.longValue()) > dltimeout ) {

			if( log.isLoggable(Level.FINE)) log.fine("Connecting to "+u+" to check date/time/size");
			uc = u.openConnection();
			if( log.isLoggable(Level.FINER)) log.finer("Connected to "+u+" to check date/time/size");
			
			try {
				Thread.currentThread().setName( "Check: "+u.toString() );
			
				// Deny caching from previous fetch
				uc.setUseCaches(false); 

				if( uc instanceof HttpURLConnection ) {
					if( log.isLoggable(Level.FINER)) log.finer("Notifying Listeners for Check of: "+url );
					
					// Checking...
					notifyChecking(url);
					checking = true;
					
					if( log.isLoggable(Level.FINER)) log.finer("Requesting HEAD for check of: "+url );
					((HttpURLConnection)uc).setRequestMethod("HEAD");
				}
		
				// Force connection to web server
				InputStream tmpIs = null;
				try {
					tmpIs = uc.getInputStream();
				} catch( IOException ex ) {
					log.log( Level.FINE, ex.toString(), ex );
				} catch( Exception ex ) {
					log.log( Level.SEVERE, ex.toString(), ex );
				} finally {
					try {
						if (tmpIs != null) tmpIs.close();
					} catch (Exception exx) { exx.printStackTrace(); }
				}
				if( log.isLoggable(Level.FINER)) log.finer("Connected to "+u);
		
				if( file.exists() ) {
					if( log.isLoggable(Level.FINE)) log.fine( "Got connection, checking cached file info: "+u );
					long flast = file.lastModified();
					long ulast = uc.getLastModified();
					int len = uc.getContentLength();
					if( log.isLoggable(Level.FINER))  {
						log.finer("local file: ("+file+")");
						log.finer( "local Date: "+new Date( flast )+", len="+file.length() );
						log.finer("remote file ("+u+")" );
						log.finer("remote Date: "+new Date(ulast)+", len="+len );
					}
					if( ulast > flast ) {
						file.delete();
						if( log.isLoggable(Level.FINE)) log.fine("file out of date");
					} else if( flast >= ulast && file.length() != uc.getContentLength() ) {
						file.delete();
						if( log.isLoggable(Level.FINE)) log.fine("file wrong size");
					} else {
						if( log.isLoggable(Level.FINE)) log.fine("Cache uptodate");
					}
				}
				lastget.put( url.toString(), new Long( System.currentTimeMillis() ) );
			} finally {
				// Force stream closed, don't need HEAD output after here.
				try {
					uc.getInputStream().close();
				} catch( Exception ex ) {
					log.log( Level.FINEST, ex.toString(), ex );
				}
				Thread.currentThread().setName( tnm );
			}
		} else {
			if( log.isLoggable(Level.FINE)) {
				log.fine("Using cached time check to "+u+" to check date/time/size");
			}
		}

		if( log.isLoggable(Level.FINER)) log.finer("after date check, file: "+file+" exists? "+file.exists() );
		// Is the file already in the cache?
		
		// Check complete
		if( checking )
			notifyCheckComplete(url, !file.exists());
		
		if( file.exists() == false ) {
			if( log.isLoggable(Level.FINE)) log.fine("No local file, downloading...");
			Thread.currentThread().setName( "Download: "+u.toString() );
			uc = u.openConnection();
			pd.mkdirs();
			
			if( log.isLoggable(Level.FINER)) log.finer("creating dialog for progress display");

			try {
				log.fine( "downloading to \""+file+"\", via: \""+u+"\"" );
				
				
				// Make connection first
				InputStream is = uc.getInputStream();

				// Find out how much to download
				long length = uc.getContentLength();
				
				notifyDownloadStarted(url, length);
				
				// Create output stream to cached file
				OutputStream os = new FileOutputStream(file);
				byte[]arr = new byte[1024];
				int n;
				try {
					int cnt = 0;
					while( ( n = is.read(arr, 0, arr.length) ) >= 0 ) {
						os.write( arr, 0, n );
						cnt += n;
						notifyDownloadProgress(url, cnt);
					}					
					
					// Check if download was aborted prematurely.
					if (cnt != length) {
						throw new IOException("Read failed on URL: "+url+", only got "+cnt+" of "+length+" bytes");
					}
					
				} catch( IOException ex ) {
					log.log(Level.WARNING, ex.toString(), ex );
					try {
						if( os != null ) {
							os.close();
						}
					} catch( IOException exx ) {
						log.log(Level.WARNING, exx.toString(), exx );
					} finally {
						try {
							if (is != null) {
								is.close();
							}
						} catch ( IOException exxx) {
							log.log(Level.WARNING, exxx.toString(), exxx );
						}
					}
					
					file.delete();
					
					// Just return uncached stream if caching fails
					//return u.openConnection();
					throw ex;
				} finally {
					is.close();
					os.close();
				}
			} finally {	
				Thread.currentThread().setName( tnm );
			}

			notifyDownloadFinished(url);
		}

		try {
			Thread.currentThread().setName( "Using: "+u.toString() );
			if( log.isLoggable(Level.FINER)) log.finer("File part of url: "+fp );
			u = new URL( new URL( "file:"+npref+durl/*.replace(' ','+')*/ ), "file:"+npref+fp ); //+"/"+f );
			URLConnection ruc = null;
			if( log.isLoggable(Level.FINER)) log.finer("open url to cached data: "+u );
			ruc = u.openConnection();
			if( log.isLoggable(Level.FINE)) log.fine("returning url to cached data: "+u );
			
			return ruc;
		} catch( IOException ex ) {
			log.log(Level.WARNING, ex.toString(), ex );
		} finally {
			Thread.currentThread().setName( tnm );
		}

		return u.openConnection();
	}

	private void notifyDownloadStarted( final URL url, final long length) {
		// Inform listeners that the download is starting.
		for( final DownloadListener lis : listeners) {
			lisExec.execute( new Runnable() {
				public void run() {	
					try {
						lis.downloadStarted( url, length);
					} catch( Exception ex ) {
						log.log(Level.WARNING, ex.toString(), ex );
					}
				}
			});
		}		
	}

	private void notifyDownloadProgress( final URL url, final int cnt) {
		// Inform listeners of download progress
		for( final DownloadListener lis : listeners) {
			lisExec.execute( new Runnable() {
				public void run() {	
					try {
						lis.downloadProgress(url, cnt);
					} catch( Exception ex ) {
						log.log(Level.WARNING, ex.toString(), ex );
					}
				}
			});
		}
	}

	private void notifyDownloadFinished( final URL url) {
		// Inform listeners of download finished.
		for( final DownloadListener lis : listeners) {
			lisExec.execute( new Runnable() {
				public void run() {	
					try {
						lis.downloadFinished(url);
					} catch( Exception ex ) {
						log.log(Level.WARNING, ex.toString(), ex );
					}
				}
			});
		}
	}

	private void notifyChecking( final URL url) {
		for( final DownloadListener lis : listeners) {
			lisExec.execute( new Runnable() {
				public void run() {	
					try {
						lis.checking(url);
					} catch (Exception ex) {
						log.log(Level.WARNING, ex.toString(), ex );
					}
				}
			});
		}
	}

	private void notifyCheckComplete( final URL url, final boolean willDownload) {
		final Throwable th = new Throwable("Download requested from");
		for( final DownloadListener lis : listeners) {
			lisExec.execute( new Runnable() {
				public void run() {	
					try {
						lis.checkComplete(url, willDownload, th);
					} catch (Exception ex) {
						log.log(Level.WARNING, ex.toString(), ex );
					}
				}
			});
		}
	}
}
