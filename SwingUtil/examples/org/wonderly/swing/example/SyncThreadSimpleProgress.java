/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.wonderly.swing.example;

import java.util.logging.Logger;
import java.util.logging.Level;
import javax.swing.JFrame;
import org.wonderly.awt.Packer;
import javax.swing.JLabel;
import org.wonderly.swing.SyncThread;
import org.wonderly.swing.SimpleProgress;

/**
 *  An example using SyncThread and SimpleProgress to show the
 *  steps of a multiple step process.
 * @author gregg
 */
public class SyncThreadSimpleProgress {
	Logger log = Logger.getLogger(getClass().getName());
	public static void main( String args[] ) {
		final Logger log = Logger.getLogger( "testing");
		JFrame f = new JFrame("Testing");
		Packer pk = new Packer( f.getContentPane() );
		final SimpleProgress prog = new SimpleProgress( "Testing", 0 );
		SyncThread th = new SyncThread( prog );
		for( int i = 0; i < 10; ++i ) {
			final int idx = i;
			final JLabel lab = new JLabel( "Item #"+i );
			pk.pack( lab ).gridx( 0 ).gridy(i).fillx(0);
			th.add( new SyncThread<Integer,String>() {
				public String getName() {
					return "Work item #"+idx;
				}
				public @Override Integer run() {
					int amt = (int)Math.round(500+Math.random()*1000.0);
					try {
						Thread.sleep(amt);
					} catch (InterruptedException ex) {
						log.log(Level.SEVERE, ex.toString(), ex);
					}
					publish( "Slept "+amt+" millis");
					try {
						Thread.sleep(amt);
					} catch (InterruptedException ex) {
						log.log(Level.SEVERE, ex.toString(), ex);
					}
					return amt;
				}
				public void process( String msg ) {
					lab.setText( msg );
					prog.setCurrentEntity( idx+": "+msg );
				}
			});
		}
		log.info("starting all steps");
		th.block();
		log.info("completed all steps");
	}
}
