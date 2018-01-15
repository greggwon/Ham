package org.wonderly.swing.prefs.man;

import java.util.prefs.*;
import java.util.logging.*;
import javax.swing.*;
import java.awt.*;
import javax.swing.text.*;
import org.wonderly.swing.prefs.*;
import java.util.logging.*;

/**
 *  Manage the location and size of a Component as
 *  a set of preferences.
 *  @author Gregg Wonderly <a href="mailto:gregg.wonderly@pobox.com">gregg.wonderly@pobox.com</a>
 */
public class WindowManager implements PrefManager {
	protected Logger log;
	protected Preferences pr;
	protected String pref;
	protected Point defLoc;
	protected Point valLoc;
	protected Dimension defSz;
	protected Dimension valSz;
	protected boolean ismax;
	protected boolean ismin;
	protected boolean isvis;

	public WindowManager( Preferences prNode,
			String prefName, Point loc, Dimension sz ) {
		// Chop off classname and last package component for logger.
		String p = getClass().getName();
		int idx = p.lastIndexOf('.');
		p = p.substring( 0, idx);
		idx = p.lastIndexOf('.');
		p = p.substring( 0, idx);
		log = Logger.getLogger( p+"."+prefName.replace('/','.') );

		this.defLoc = loc;
		this.defSz = sz;
		pr = prNode.node(prefName);
		pref = prefName;
	}

	public boolean prepare( Component fl ) {
		valLoc = fl.getLocation();
		valSz = fl.getSize();
		isvis = fl.isVisible();
		if( fl instanceof JInternalFrame ) {
			JInternalFrame f = (JInternalFrame)fl;
			ismin = f.isIcon();
			ismax = f.isMaximum();
		}
		return true;
	}

	public boolean commit( Component comp ) {
		log.fine("position window: "+pref+"("+valLoc+") with dim="+valSz );

		Preferences pn = pr.node("location");
		pn.putInt( "x", valLoc.x );
		pn.putInt( "y", valLoc.y );

		pn = pr.node("size");
		pn.putInt( "w", valSz.width );
		pn.putInt( "h", valSz.height );

		pn = pr.node("info");
		pn.putBoolean( "vis", isvis );
		pn.putBoolean( "max", ismax );
		pn.putBoolean( "min", ismin );

		return true;
	}

	public void setValueIn( final Component fl ) {
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				try {
					Preferences pn = pr.node("location");
					fl.setLocation(
						pn.getInt("x",fl.getLocation().x),
						pn.getInt("y",fl.getLocation().y ) );
					pn = pr.node("size");
					fl.setSize(
						pn.getInt("w", fl.getSize().width), 
						pn.getInt("h", fl.getSize().height ) );
					log.fine("Position("+pref+") @ "+
						fl.getLocation()+" with "+fl.getSize() );
					pn = pr.node("info");
					fl.setVisible( pn.getBoolean("vis", true ) );
					if( fl instanceof JInternalFrame ) {
						JInternalFrame f = (JInternalFrame)fl;
						f.setIcon( pn.getBoolean("min", false ) );
						f.setMaximum( pn.getBoolean("max", false ) );
					}
				} catch( Exception ex ) {
					log.log( Level.INFO, ex.toString(), ex );
				}
			}
		});
	}	
}