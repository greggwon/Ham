package org.wonderly.swing.prefs.man;

import java.awt.*;
import java.util.prefs.*;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.text.*;
import org.wonderly.swing.prefs.*;

/**
 *  Manage the state of a String Preference value with
 *  an associated JTextComponent component.
 *  @author Gregg Wonderly <a href="mailto:gregg.wonderly@pobox.com">gregg.wonderly@pobox.com</a>
 */
public class StringTextFieldManager implements PrefManager {
	protected Logger log;
	protected Preferences pr;
	protected String pref;
	protected String def;
	protected String val;

	public StringTextFieldManager( Preferences prNode, String prefName, String def ) {
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

	public boolean prepare( Component comp ) {
		if( comp instanceof JTextComponent ) {
			JTextComponent fl = (JTextComponent)comp;
			val = fl.getText();
		} else if( comp instanceof JLabel ) {
			JLabel fl = (JLabel)comp;
			val = fl.getText();
		} else {
			return false;
		}
		return true;
	}

	public boolean commit( Component comp ) {
		pr.put( pref, val );
		return true;
	}

	public void setValueIn( final Component comp ) {
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				if( comp instanceof JTextComponent ) {
					JTextComponent fl = (JTextComponent)comp;
					fl.setText( pr.get( pref, def ) );
				} else if( comp instanceof JLabel ) {
					JLabel fl = (JLabel)comp;
					fl.setText( pr.get( pref, def ) );
				}
			}
		});
	}	
}