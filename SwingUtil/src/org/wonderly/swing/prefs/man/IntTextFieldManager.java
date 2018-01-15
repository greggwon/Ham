package org.wonderly.swing.prefs.man;

import java.awt.*;
import java.util.prefs.*;
import java.util.logging.*;
import javax.swing.*;
import org.wonderly.swing.prefs.*;

/**
 *  Manage the state of a integer Preference value with
 *  an associated JTextComponent component.
 *  @author Gregg Wonderly <a href="mailto:gregg.wonderly@pobox.com">gregg.wonderly@pobox.com</a>
 */
public class IntTextFieldManager implements PrefManager {
	protected Logger log;
	protected Preferences pr;
	protected String pref;
	int def;
	
	public IntTextFieldManager( Preferences prNode, String prefName, int def ) {
		// Chop of classname and last package component for logger.
		String p = getClass().getName();
		int idx = p.lastIndexOf('.');
		p = p.substring( 0, idx);
		idx = p.lastIndexOf('.');
		p = p.substring( 0, idx);
		log = Logger.getLogger( p+"."+prefName.replace('/','.') );

		this.def = def;
		pr = prNode;
		pref = prefName;
	}

	int val;
	public boolean prepare( Component comp ) {
		JTextField fl = (JTextField)comp;
		String str = fl.getText();
		val = 0;
		try {
			val = Integer.parseInt( str );
			return true;
		} catch( NumberFormatException ex ) {
			log.log( Level.SEVERE, ex.toString(), ex );
		}
		return false;
	}

	public boolean commit( Component comp ) {
		pr.putInt( pref, val );
		return true;
	}

	public void setValueIn( final Component comp ) {
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				JTextField fl = (JTextField)comp;
				fl.setText( pr.getInt( pref, def )+"" );
			}
		});
	}	
}