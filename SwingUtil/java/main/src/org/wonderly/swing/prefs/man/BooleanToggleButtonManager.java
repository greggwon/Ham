package org.wonderly.swing.prefs.man;

import java.util.prefs.*;
import java.util.logging.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import org.wonderly.swing.prefs.*;

/**
 *  Manage the state of a boolean Preference value with
 *  an associated JToggleButton component.
 *  @author Gregg Wonderly <a href="mailto:gregg.wonderly@pobox.com">gregg.wonderly@pobox.com</a>
 */
public class BooleanToggleButtonManager implements PrefManager {
	protected Logger log;
	protected Preferences pr;
	protected String pref;
	protected boolean def;
	protected boolean val;

	public BooleanToggleButtonManager( Preferences prNode,
			String prefName, boolean def ) {
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
		JToggleButton fl = (JToggleButton)comp;
		val = fl.isSelected();
		return true;
	}

	public boolean commit( Component comp ) {
		pr.putBoolean( pref, val );
		return true;
	}

	public void setValueIn( final Component comp ) {
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				JToggleButton fl = (JToggleButton)comp;
				fl.setSelected( pr.getBoolean( pref, def ) );
			}
		});
	}	
}