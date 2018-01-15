package org.wonderly.swing;

//import org.wonderly.awt.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import java.net.*;

/**
<pre>
Copyright (c) 1997-2006, Gregg Wonderly
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.
    * The name of the author may not be used to endorse or promote
      products derived from this software without specific prior
      written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE.
</pre>
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
	protected Hashtable<String,Action> actions = new Hashtable<String,Action>();

//	public static void main( String args[] ) {
//		ActionManager am = new ActionManager();
//		final JTextArea lab = new JTextArea(20,20);
//		final JFrame f = new JFrame("Testing");
//
//		Packer pk = new Packer( f.getContentPane() );
//		int y = -1;
//		lab.setBorder( BorderFactory.createEtchedBorder() );
//		pk.pack( lab ).gridx(0).gridy(++y).fillboth();
//		f.addWindowListener( new WindowAdapter() {
//			public void windowClosing( WindowEvent ev ) {
//				System.exit(1);
//			}
//		});
//
//		am.defineAction( "Test Action", new LabeledAction("Test Action") {
//			public void actionPerformed( ActionEvent ev ) {
//				lab.append("Test Action\n");
//			}
//		});
//
//		am.defineAction( "Test2 Action", new LabeledAction("Test2 Action") {
//			public void actionPerformed( ActionEvent ev ) {
//				lab.append("Test2 Action\n");
//			}
//		});
//
//		am.defineAction( "Test Toggle", new LabeledToggleAction("Test Toggle") {
//			public void actionPerformed( ActionEvent ev ) {
//				lab.append("Test Toggle\n");
//			}
//		});
//
//		am.defineAction( "Test2 Toggle", new LabeledToggleAction("Test2 Toggle") {
//			public void actionPerformed( ActionEvent ev ) {
//				lab.append("Test2 Toggle\n");
//			}
//		});
//
//		pk.pack( am.getJButton( "Test Action" ) ).gridx(0).gridy(++y).west();
//		JRadioButton r1,r2;
//		pk.pack( r1=am.getJRadioButton( "Test Toggle" ) ).gridx(0).gridy(++y).west();
//		pk.pack( r2=am.getJRadioButton( "Test2 Toggle" ) ).gridx(0).gridy(++y).west();
//		ButtonGroup gr = new ButtonGroup();
//		gr.add(r1);
//		gr.add(r2);
//
//		pk.pack( am.getJCheckBox( "Test Toggle" ) ).gridx(0).gridy(++y).west();
//
//		JMenuBar bar = new JMenuBar();
//		f.setJMenuBar( bar );
//		JMenu mf = new JMenu("Test");
//		bar.add( mf );
//		mf.add( am.getJMenuItem("Test Action") );
//		mf.add( am.getJCheckBoxMenuItem("Test Toggle") );
//
//		f.pack();
//		f.setLocationRelativeTo( null );
//		f.setVisible(true);
//	}

	public void defineAction( String key, Action act ) {
		actions.put( key, act );
	}
	
	/**
	 *  Get an action that must be a LabeledToggleAction.  This is
	 *  a convienence method that demands the action be a
	 *  LabeledToggleAction.
	 *  @throws IllegalArgumentException if the action is not a LabeledToggleAction
	 */
	public LabeledToggleAction getToggleAction( String name ) throws IllegalArgumentException {
		Action a = getAction( name );
		if( a instanceof LabeledToggleAction == false ) {
			throw new IllegalArgumentException( name+" is not a LabeledToggleAction instance");
		}
		return (LabeledToggleAction)a;
	}

	/**
	 *  Get a JCheckBox with the indicated action attached to it.
	 *  The action will be called on select and deselect.
	 *  Use a {@link LabeledToggleAction} and call its
	 *  <code>isSelected()</code> method to take action on
	 *  select only.
	 */
	public JCheckBox getJCheckBox( String action ) {
		JCheckBox bx = new JCheckBox();
		bx.setAction( getAction(action) );
		return bx;
	}

	/**
	 *  Get a JButton with the indicated action attached to it.
	 */
	public JButton getJButton( String action ) {
		JButton bx = new JButton();
		bx.setAction( getAction(action) );
		return bx;
	}

	/**
	 *  Get a JRadioButton with the indicated action attached to it.
	 *  The action will be called on select and deselect.
	 *  Use a {@link LabeledToggleAction} and call its
	 *  <code>isSelected()</code> method to take action on
	 *  select only.
	 */
	public JRadioButton getJRadioButton( String action ) {
		JRadioButton bx = new JRadioButton();
		bx.setAction( getAction(action) );
		return bx;
	}

	/**
	 *  Get a JMenuItem with the indicated action attached to it.
	 */
	public JMenuItem getJMenuItem( String action ) {
		JMenuItem bx = new JMenuItem();
		bx.setAction( getAction(action) );
		return bx;
	}

	/**
	 *  Get a JCheckBoxMenuItem with the indicated action attached to it.
	 *  The action will be called on select and deselect.
	 *  Use a {@link LabeledToggleAction} and call its
	 *  <code>isSelected()</code> method to take action on
	 *  select only.
	 */
	public JCheckBoxMenuItem getJCheckBoxMenuItem( String action ) {
		JCheckBoxMenuItem bx = new JCheckBoxMenuItem();
		bx.setAction( getAction(action) );
		return bx;
	}

	/**
	 *  Get the named action instance
	 */
	public Action getAction( final String name ) {
		Action a;
		a = actions.get(name);
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

	/**
	 *  Get a JCheckBoxMenuItem that is in the indicated ButtonGroup and which
	 *  uses the named LabeledToggleAction.
	 *  The 
	 *  @throws IllegalArgumentException if the action is not a LabeledToggleAction
	 */
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