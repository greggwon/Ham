package org.wonderly.swing.prefs.man;

import java.awt.*;
import java.util.prefs.*;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.text.*;
import org.wonderly.swing.prefs.*;

/**
 *  Manage the state of an integer valued Preference
 *  that is the index of the current selected item in
 *  an associated JList component.
 *  @author Gregg Wonderly <a href="mailto:gregg.wonderly@pobox.com">
 *		gregg.wonderly@pobox.com</a>
 */
public class ListSelectionManager implements PrefManager {
	protected Logger log;
	protected Preferences pr;
	protected String pref;
	protected int def;
	protected int val;

	public static void main( String args[] ) {
		ListSelectionManager lm = 
			new ListSelectionManager( Preferences.userNodeForPackage( 
			ListSelectionManager.class), "pref1", 2 );
		lm.log.info("Check logger");
	}

	public ListSelectionManager( Preferences prNode,
			String prefName, int def ) {
		// Chop of classname and last package component for logger.
		String p = getClass().getName();
		int idx = p.lastIndexOf(".");
		p = p.substring( 0, idx);
		idx = p.lastIndexOf(".");
		p = p.substring( 0, idx)+"."+prefName.replace('/','.');
		log = Logger.getLogger( p );

		this.def = def;
		pr = prNode;
		pref = prefName;
	}

	public boolean prepare( Component comp ) {
		if( comp instanceof JList ) {
			JList fl = (JList)comp;
			val = fl.getSelectedIndex();
		} else if( comp instanceof JTable ) {
			JTable fl = (JTable)comp;
			val = fl.getSelectedRow();
		} else if( comp instanceof JTree ) {
			JTree fl = (JTree)comp;
			int r[] = fl.getSelectionRows();
			if( r != null && r.length > 0 )
				val = r[0];
		} else {
			return false;
		}
		return true;
	}

	public boolean commit( Component comp ) {
		log.info("Commit with val: "+val);
		pr.putInt( pref, val );
		return true;
	}

	public void setValueIn( final Component comp ) {
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				if( comp instanceof JList ) {
					JList fl = (JList)comp;
					fl.setSelectedIndex( pr.getInt( pref, def ) );
				} else if( comp instanceof JTable ) {
					JTable fl = (JTable)comp;
					int idx = pr.getInt( pref, def );
					log.info("set value in("+def+"): "+idx );
					if( fl.getRowCount() > idx && idx >= 0 )
						fl.addRowSelectionInterval( idx, idx );
				} else if( comp instanceof JTree ) {
					JTree fl = (JTree)comp;
					int idx = pr.getInt( pref, def );
					fl.setSelectionRow( idx );
				}
			}
		});
	}	
}