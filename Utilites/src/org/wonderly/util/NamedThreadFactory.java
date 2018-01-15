/*
 * NamedThreadFactory.java
 *
 * Created on March 6, 2007, 5:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.wonderly.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class Provides a ThreadFactory for {@link java.util.concurrent.ThreadPoolExecutor}.
 * The threads created have a configured name prefix, explicit Throwable catches and associated logging
 * to keep threads from dying due to uncaught exceptions.  Threads can also be specified to be created
 * as daemon threads.
 *
 * The internal {@link java.util.logging.Logger} instance will include the prefix name as a
 * suffix to the logger name so that logging of specific instances can be controlled separately.
 *
 * @author gregg
 */
public class NamedThreadFactory implements ThreadFactory {
	/**
	 * The name prefix threads are created with.
	 */
	private String name;
	/**
	 * The {@link java.util.logging.Logger} instance used.  This will include a "."+name suffix
	 * to allow each instances logging to be controlled separately.  Thus names need to follow
	 * the requirements for use in {@link java.util.Logger} names.
	 */
	private Logger log = Logger.getLogger( getClass().getName() );
	/**
	 * Whether threads should be set as daemon threads or not.
	 */
	private boolean isDaemon;

	/**
	 * Create a ThreadFactory which creates threads with the indicated name prefix and sets the daemon
	 * setting to the passed value.
	 * @param name The name prefix for threads created by this factory
	 * @param isDaemon Whether daemon threads should be created.
	 */
	public NamedThreadFactory( String name, boolean isDaemon ) {
		this(name);
		this.isDaemon = isDaemon;
	}

	/**
	 * Creates a Thread factory with threads named with the indicated prefix and set to non-daemon threads.
	 * @param name The name prefix for threads created by this factory
	 */
	public NamedThreadFactory( String name ) {
		this.name = name;
		try {
			// If there is ever a limit on log name construction, catch any
			// associated errors and just use the classes named logger.
			log = Logger.getLogger( getClass().getName()+"."+name );
		} catch( Exception ex ) {
			log.log( Level.SEVERE, ex.toString(), ex );
		}
		if( log.isLoggable( Level.FINE ) ) {
			log.fine( this+" instance created" );
		}
	}

	/**
	 * The incrementing threads number to provide unique threads names.
	 */
	private AtomicInteger cnt = new AtomicInteger(0);
	/**
	 * Creates a new threads with the appropriate name prefix and daemon designation.
	 * @param r The {@link java.lang.Runnable} instance to cause the thread to execute.
	 * @return The newly created thread.
	 */
	public Thread newThread( final Runnable r ) {
		Thread th = new Thread( new Runnable() {
			public void run() {
				try {
					r.run();
				} catch( Throwable ex ) {
					log.log( Level.WARNING, ex.toString()+": Exception unhandled in thread pool thread", ex );
				}
			}
		});
		th.setName( name+"-"+cnt.incrementAndGet() );
		th.setDaemon( isDaemon );
		if( log.isLoggable( Level.FINE ) )
			log.fine("Created new thread: \""+th.getName()+"\" (daemon="+isDaemon+")" );
		return th;
	}
	
	public String toString() {
		return "NamedThreadFactory: \""+name+"\" (daemon="+isDaemon+")";
	}
}
