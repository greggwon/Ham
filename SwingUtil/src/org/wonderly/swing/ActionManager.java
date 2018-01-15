package org.wonderly.swing;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import java.net.*;

/**
 *  This class provides a mechanism for managing the use of java.awt.Action
 *  instances in an application.  This manager provides a storage and
 *  reference implementation that allows the Actions to have an associated
 *  name.
 *  <p>
 *  There are also convenience functions provided to help with the use of
 *  LabeledToggleAction.
 *
 * <pre>
 		am = new ActionManager();
 		am.defineAction( "New Account", new LabeledAction( "Create New" ) {
			public void actionPerformed( ActionEvent ev ) {
			}
		});
		JMenu j = new JMenu("Accounts");
		j.add( am.getAction("New Account");
 *</pre>
 *  @author <a href="mailto:gregg.wonderly@pobox.com">Gregg Wonderly</a>.
 */
public class ActionManager {
	protected Hashtable actions = new Hashtable();

	public void defineAction( String key, Action act ) {
		actions.put( key, act );
	}
	
	public LabeledToggleAction getToggleAction( String name ) {
		Action a = getAction( name );
		if( a instanceof LabeledToggleAction == false ) {
			throw new IllegalArgumentException( name+" is not a LabeledToggleAction instance");
		}
		return (LabeledToggleAction)a;
	}

	public Action getAction( final String name ) {
		Action a;
		a = (Action)actions.get(name);
		if( a != null )
			return a;
		final LabeledAction la = new LabeledAction( name, name, false ) {
			public void actionPerformed( ActionEvent ev ){
				System.err.println( "\""+name+"\": Action Not Implemented" );
			}
		};
		la.setEnabled(false);
		return la;
	}
	
	/**
	 *  Returns a ToolIcon from the resource in <code>images/&lt;name&gt;</code>.
	 */
	public Icon loadIcon( String name ) {
		URL u = getClass().getClassLoader().getResource( "images/"+name );
		if( u != null )
			return new ToolIcon( u );	
		return null;
	}
	
	/**
	 *  Class to be used for toolbar icons.  The size
	 *  of the largest icon constructed will control
	 *  the size of all icons so that all are the same
	 *  size as the largest.
	 */
	public static class ToolIcon extends ImageIcon {
		protected static int sz = 2;
		protected Icon icon;

		public ToolIcon( URL u ) {
			super(u);
			if( super.getIconWidth() > sz )
				sz = super.getIconWidth();
			if( super.getIconHeight() > sz )
				sz = super.getIconHeight();
		}
		public int getIconWidth() {
			return sz;
		}
		public int getIconHeight() {
			return sz;
		}
		public void paintIcon( Component c, Graphics g, int x, int y ) {
			int cx = x;
			int cy = y;
			int hx = super.getIconWidth();
			int hy = super.getIconHeight();
			if( hx < sz )
				cx += (sz - hx)/2;
			if( hy < sz )
				cy += (sz - hy)/2;
			super.paintIcon( c, g, cx, cy );
		}
	}

	public JCheckBoxMenuItem checkBoxMenuItemAction( ButtonGroup grp, String name ) {
		Action aa = getAction(name);
		if( aa instanceof LabeledToggleAction == false ) {
			throw new IllegalArgumentException( name+" Action ("+aa.getClass().getName()+
				") must be a "+
				LabeledToggleAction.class.getName()+" action");
		}
		final LabeledToggleAction a = (LabeledToggleAction)getAction( name );
		final JCheckBoxMenuItem mi = new JCheckBoxMenuItem( a );
		final ActionListener al[] = new ActionListener[1];
		final ActionListener bl = new ActionListener() {
			public void actionPerformed( ActionEvent ev ) {
				a.setSelected( mi.isSelected() );
			}
		};
		al[0] = new ActionListener() {
			public void actionPerformed( ActionEvent ev ) {
				mi.setSelected( a.isSelected() );
			}
		};
		a.addActionListener( al[0] );
		mi.addActionListener( bl );
		if( grp != null )
			grp.add(mi);
		return mi;
	}

	public JCheckBox checkBoxAction( ButtonGroup grp, String name ) {
		final LabeledToggleAction a = (LabeledToggleAction)getAction( name );
		final JCheckBox mi = new JCheckBox( a );
		final ActionListener bl = new ActionListener() {
			public void actionPerformed( ActionEvent ev ) {
				a.setSelected( mi.isSelected() );
			}
		};
		final ActionListener al = new ActionListener() {
			public void actionPerformed( ActionEvent ev ) {
				mi.setSelected( a.isSelected() );
			}
		};
		a.addActionListener( al );
		mi.addActionListener( bl );
		if( grp != null )
			grp.add(mi);
		return mi;
	}

	public JToggleButton toggleButtonAction( ButtonGroup grp, final JToggleButton but, String name ) {
		final LabeledToggleAction a = (LabeledToggleAction)getAction( name );
		final ActionListener al = new ActionListener() {
			public void actionPerformed( ActionEvent ev ) {
				but.setSelected( a.isSelected() );
			}
		};
		final ActionListener bl = new ActionListener() {
			public void actionPerformed( ActionEvent ev ) {
				a.setSelected( but.isSelected() );
			}
		};
		a.addActionListener( al );
		but.addActionListener( bl );
		if( grp != null )
			grp.add(but);
		return but;
	}
}