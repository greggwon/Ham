package org.wonderly.swing.prefs.man;

import java.awt.*;
import java.util.prefs.*;
import java.util.logging.*;
import javax.swing.*;
import org.wonderly.swing.prefs.*;

/**
 *  Manage the state of a integer Preference value with
 *  an associated JSpinner component that contains Double values.
 *  @author Gregg Wonderly <a href="mailto:gregg.wonderly@pobox.com">gregg.wonderly@pobox.com</a>
 */
public class DoubleSpinnerManager implements PrefManager {
	protected Logger log;
	protected Preferences pr;
	protected String pref;
	protected double def;
	protected double val;
	
	public DoubleSpinnerManager( Preferences prNode, String prefName, double def ) {
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
		JSpinner fl = (JSpinner)comp;
		val = ((Number)fl.getValue()).doubleValue();
		return true;
	}

	public boolean commit( Component comp ) {
		pr.putDouble( pref, val );
		return true;
	}

	public void setValueIn( final Component comp ) {
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				JSpinner fl = (JSpinner)comp;
				fl.setValue( new Double( pr.getDouble( pref, def ) ) );
			}
		});
	}	
}