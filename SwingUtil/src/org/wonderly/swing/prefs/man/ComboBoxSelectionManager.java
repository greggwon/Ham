package org.wonderly.swing.prefs.man;

import java.util.prefs.*;
import java.util.logging.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import org.wonderly.swing.prefs.*;
import java.util.logging.*;

/**
 *  Manage the state of an integer valued Preference
 *  that is the index of the current selected item in
 *  an associated JComboBox component.
 *  @author Gregg Wonderly <a href="mailto:gregg.wonderly@pobox.com">gregg.wonderly@pobox.com</a>
 */
public class ComboBoxSelectionManager implements PrefManager {
	protected Logger log;
	protected Preferences pr;
	protected String pref;
	protected int def;
	protected int val;

	public ComboBoxSelectionManager( Preferences prNode,
			String prefName, int def ) {
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
		JComboBox fl = (JComboBox)comp;
		val = fl.getSelectedIndex();
		return true;
	}

	public boolean commit( Component comp ) {
		pr.putInt( pref, val );
		return true;
	}

	public void setValueIn( final Component comp ) {
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				JComboBox fl = (JComboBox)comp;
				try {
					int idx = pr.getInt( pref, def );
					if( idx < fl.getItemCount() )
						fl.setSelectedIndex( idx );
				} catch( Exception ex ) {
					log.log( Level.INFO, ex.toString(), ex );
				}
			}
		});
	}	
}