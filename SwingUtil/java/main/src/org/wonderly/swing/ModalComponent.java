package org.wonderly.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.util.logging.*;

/**
 *  This class provides a mechanism for associating the enabled state
 *  of many components with a JToggleButton implementation.  The typical
 *  use is something like the following.
 *
 *<pre>
JPanel p = new JPanel(p);
Packer pk = new Packer(p);
<b>JCheckBox cb = new JCheckBox("Use File?");</b>
<b>ModalComponent mc = new ModalComponent(cb);</b>
pk.pack( cb ).gridx(0).gridy(0).fillx();

<b>JPanel fp = new JPanel();</b>
Packer fpk = new Packer( fp );
pk.pack( fp ).gridx(0).gridy(1).fillx();
<b>mc.add( fp );</b>
fp.setBorder( BorderFactory.createTitledBorder("File Name") );
<b>JTextField fileName = new JTextField();</b>
<b>mc.add( fileName);</b>

 *</pre>
 *  The <b>bold</b> lines are the ones that must be there the others
 *  are a specific type of layout for the components.
 *
 */
public class ModalComponent {
	protected Hashtable comps = new Hashtable();
	protected int enables[];
	protected Component modal;
	protected Logger log = Logger.getLogger( "org.wonderly.swing.modal");

	public String toString() {
		if( modal instanceof JToggleButton )
			return ((JToggleButton)modal).getText();
		return modal.getClass().getName();
	}

	public boolean equals( Object obj ) {
		if( obj instanceof ModalComponent == false )
			return false;
		return modal.equals( ((ModalComponent)obj).modal );
	}

	public int hashCode() {
		return modal.hashCode();
	}

	public void relate( ModalComponent mc ) {
		Vector v = (Vector)comps.get(this);
		if( v == null ) {
			v = new Vector();
			comps.put(this,v);
		}
		v.addElement(mc);		
	}

	/**
 	 *  Create the modal control and attach an action listener
	 *  to <code>box</code> to call configure each time the 
	 *  an ActionEvent fires
	 *  @param box the toggle button to act on.
	 */
	public ModalComponent( final JToggleButton box ) {
		modal = box;
		box.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent ev ) {
				log.fine("toggle actionEvent: "+ev);
				configure( box.isSelected() );
			}
		});
//		logFinest(log);
	}
	
//	void logFinest( Logger log ) {
//		log.setLevel(Level.FINEST );
//		if( (log = log.getParent()) != null ) {
//			logFinest( log );
//			Handler h[] = log.getHandlers();
//			for( int i = 0; h != null && i < h.length; ++i ) {
//				h[i].setLevel( Level.FINEST );
//			}
//		}
//	}

	/**
	 *   @param box the combobox whose elements are monitored
	 *   @param enabled the indexes for this JList selections where components
	 *          become enabled.
	 */
	public ModalComponent( final JComboBox box, final int enabled[] ) {
		modal = box;
		enables = enabled;
		box.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent ev ) {
				log.fine("combo actionEvent: "+ev);
				configure( box.getSelectedIndex(), enabled );
			}
		});
	}

	/**
	 *   @param list the list whose elements are monitored
	 *   @param enabled the indexes for this JList selections where components
	 *          become enabled.
	 */
	public ModalComponent( final JList list, final int enabled[] ) {
		modal = list;
		enables = enabled;
		list.addListSelectionListener( new ListSelectionListener() {
			public void valueChanged( ListSelectionEvent ev ) {
				if( ev.getValueIsAdjusting() )
					return;
				log.fine("list actionEvent: "+ev);
				configure( list.getSelectedIndex(), enabled );
			}
		});
	}

	private void configure( int idx, int enabled[] ) {
		// Handle case of any selection causes enable
		if( enabled == null ) {
			configure( idx != -1 );
			return;
		}

		boolean sel = false;
//		int idx = ((JComboBox)modal).getSelectedIndex();
		for( int i = 0; i < enabled.length; ++i ) {
			if( idx == enabled[i] ) {
				sel = true;
				break;
			}
		}
		configure(sel);
	}

	/**
	 *  This method is called by the ActionListener established
	 *  at the time this instance is constructed.  Normally, this
	 *  method needs to be called after all components are added
	 *  to configure them based on the current state of the
	 *  controlling JToggleButton.
	 */
	public void configure() {
		if( modal instanceof JComboBox ) {
			configure( ((JComboBox)modal).getSelectedIndex(), enables );
			return;
		} else if( modal instanceof JList ) {
			configure( ((JList)modal).getSelectedIndex(), enables );
			return;
		}
		configure(new Hashtable(), 
			((JToggleButton)modal).isSelected() );
	}
	
	private void configure( boolean how ) {
		configure( new Hashtable(), how );
	}

	private void configure( Hashtable covered, int idx ) {
		boolean sel = false;
//		int idx = ((JComboBox)modal).getSelectedIndex();
		for( int i = 0; i < enables.length; ++i ) {
			if( idx == enables[i] ) {
				sel = true;
				break;
			}
		}
		configure( covered, sel );
	}
	private void configure( Hashtable covered, boolean sel ) {

//		boolean sel = modal.isSelected();
		Enumeration e = comps.keys();
		while( e.hasMoreElements() ) {
			Object obj = e.nextElement();
			if( obj instanceof Component == false )
				continue;
			Component comp = (Component)obj;
			log.finer("configure: "+comp);
			comp.setEnabled( sel );
		}
		covered.put( this, this );
		log.finer( "covered: "+covered );

		Vector v = (Vector)comps.get(this);
		log.finer( "relate: "+v );
		if( v == null || v.size() == 0 )
			return;
		for( int i = 0; i < v.size(); ++i ) {
			ModalComponent mc = (ModalComponent)v.elementAt(i);
			log.finest( "mc: "+mc+", covered? "+covered.get(mc) );
			if( covered.get( mc ) != null )
				continue;
			if( mc.modal instanceof JToggleButton ) {
				mc.configure( covered, ((JToggleButton)mc.modal).isSelected() );
			} else if( mc.modal instanceof JComboBox ) {
				mc.configure( covered, ((JComboBox)mc.modal).getSelectedIndex() );
			} else if( mc.modal instanceof JList ) {
				mc.configure( covered, ((JList)mc.modal).getSelectedIndex() );
			} else {
				throw new IllegalArgumentException( "Unsupported component type: "+
					mc.modal.getClass().getName() );
			}
		}
	}

	public void add( Component comp ) {
		comps.put( comp, new Vector() );
	}
}