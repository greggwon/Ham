package org.wonderly.swing.prefs;

import java.util.*;
import java.util.prefs.*;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import org.wonderly.swing.prefs.man.*;
import org.wonderly.swing.*;

/**
 *  This class maps {@link java.util.prefs.Preferences} entries to Components
 *  utilizing instances of {@link org.wonderly.swing.prefs.PrefManager}.  The
 *  use of this class is simply:
 *<pre>
   Preferences pr = Preferences.getUserNodeForClass( getClass() );
   PreferencesMapper pm = new PreferencesMapper(pr);
   JCheckBox sel = new JCheckBox( "Select All?" );
   JRadioButton red = new JRadioButton("Red");
   JRadioButton blue = new JRadioButton("Blue");
   ButtonGroup grp = new ButtonGroup();
   grp.add(red);
   grp.add(blue);
   JTextField val = new JTextField();
   pm.map( "selall", false, sel );
   pm.map( "red", true, red );
   pm.map( "blue", true, blue );
   pm.map( "val", "", val );
   ... Show user controls
 
   if( userOkayed ) {
   	pm.commit();
 	}
 *</pre>
 *
 *  The overrides of <code>map( String, ???, Component )</code>
 *  provide the interface to mapping components to a particular
 *  representation in the preferences structure.  The mappings
 *  provided here are simply what seems reasonable.  Subclassing
 *  this class to add more mappings is suggested for special
 *  applications where this class does not provide the correct
 *  mappings, or use the <code>public void map( PrefManager, Component)</code>
 *  override to provide your own implementation of the PrefManager for the indicated
 *  component.  A multi-select JList, or some other more complex operation
 *  can be easily implemented then.
 *
 *  @author Gregg Wonderly <a href="mailto:gregg.wonderly@pobox.com">gregg.wonderly@pobox.com</a>
 */
public class PreferencesMapper {
	/**  The component to PrefManager map */
	protected Hashtable comps = new Hashtable();
	/** The preferences node we are anchored at */
	protected Preferences pr;
	/** The logging instance */
	protected Logger log;
	/** The flag for causing fail on commit() to raise an IllegalStateException */
	protected boolean failOnCommitFail = false;
	
	/**
	 *  Set this to true to cause a commit failure to
	 *  raise an IllegalStateException.
	 */
	public void setFailOnCommitFail( boolean how ) {
		failOnCommitFail = how;
	}

	/**
	 *  Construct an instance anchored at the
	 *  indicated preferences node
	 *  @param node the Preferences node to anchor the
	 *    tree of preferences under
	 */
	public PreferencesMapper(Preferences node) {
		pr = node;
		String str = getClass().getName();
		int idx = str.lastIndexOf('.');
		String pkg = str.substring( 0, idx-1 );
		log = Logger.getLogger( pkg );
	}
	
	/**
	 *  Commit all PrefManager's contents.
	 *  This method will call PrefManager.prepare() on
	 *  all components and return false if any prepare
	 *  fails.  If none fail, it will then call 
	 *  PrefManager.commit() or all PrefManager instances
	 *  registered.  If any fails, and failOnCommit has
	 *  been set to true, an exception will be raised.
	 *  Otherwise, any commit failure will be ignored and
	 *  true will be returned.
	 *  @return false if any prepare() fails, otherwise true.
	 *  @exception IllegalStateException if setFailOnCommit(true)
	 *	has been called and a commit() call fails.
	 */
	public boolean commit() throws IllegalStateException {
		Enumeration e = comps.keys();
		log.fine("Committing values to Preferences");
		while( e.hasMoreElements() ) {
			Component cm = (Component)e.nextElement();
			PrefManager pm = (PrefManager)comps.get(cm);
			if( pm.prepare(cm) == false ) {
				log.finer(cm+" failed prepare through "+pm );
				return false;
			}
		}
		log.fine("Prepare of preferences succeeded, committing values");
		e = comps.keys();
		while( e.hasMoreElements() ) {
			Component cm = (Component)e.nextElement();
			PrefManager pm = (PrefManager)comps.get(cm);
			if( pm.commit(cm) == false ) {
				log.finer(cm+" failed commit through "+pm );
				if( failOnCommitFail )
					throw new IllegalStateException("commit failed, after prepare okay for: "+pm );
			}
		}
		return true;
	}

	/**
	 *  Map an int preferences value to the named preference.
	 *  @param pref the path to the Preferences node to use
	 *  @param def the default int value
	 *  @param comp the text component to manage the value in
	 */
	public void map( String pref, int def, JTextComponent comp ) {
		log.finer( "Create mapping for "+pref+
			" using "+comp+" with default: "+def );
		Preferences pn = nodeFor(pref);
		pref = lastComponent(pref);
		PrefManager pm = new IntTextFieldManager( pn, pref, def );
		pm.setValueIn( comp );
		comps.put( comp, pm );
	}

	/**
	 *  Map a String preferences value to the named preference.
	 *  @param pref the path to the Preferences node to use
	 *  @param def the default String value
	 *  @param comp the text component to manage the value in
	 */
	public void map( String pref, String def, JTextComponent comp ) {
		log.finer( "Create mapping for "+pref+
			" using "+comp+" with default: "+def );
		Preferences pn = nodeFor(pref);
		pref = lastComponent(pref);
		PrefManager pm = new StringTextFieldManager( pn, pref, def );
		pm.setValueIn( comp );
		comps.put( comp, pm );
	}

	/**
	 *  Map a String preferences value to the named preference.
	 *  @param pref the path to the Preferences node to use
	 *  @param def the default String value
	 *  @param comp the label component to manage the value in
	 */
	public void map( String pref, String def, JLabel comp ) {
		log.finer( "Create mapping for "+pref+
			" using "+comp+" with default: "+def );
		Preferences pn = nodeFor(pref);
		pref = lastComponent(pref);
		PrefManager pm = new StringTextFieldManager( pn, pref, def );
		pm.setValueIn( comp );
		comps.put( comp, pm );
	}

	/**
	 *  Map a JComboBox selected index to the named preference.
	 *  @param pref the path to the Preferences node to use
	 *  @param def the default int selected index
	 *  @param comp the combo box instance to map to
	 */
	public void map( String pref, int def, JComboBox comp ) {
		log.finer( "Create mapping for "+pref+
			" using "+comp+" with default: "+def );
		Preferences pn = nodeFor(pref);
		pref = lastComponent(pref);
		PrefManager pm = new ComboBoxSelectionManager( pn, pref, def );
		pm.setValueIn( comp );
		comps.put( comp, pm );
	}

	/**
	 *  Map a JSlider position to the named preference.
	 *  @param pref the path to the Preferences node to use
	 *  @param def the default int position
	 *  @param comp the slider instance to map to
	 */
	public void map( String pref, int def, JSlider comp ) {
		log.finer( "Create mapping for "+pref+
			" using "+comp+" with default: "+def );
		Preferences pn = nodeFor(pref);
		pref = lastComponent(pref);
		PrefManager pm = new SliderSelectionManager( pn, pref, def );
		pm.setValueIn( comp );
		comps.put( comp, pm );
	}

	/**
	 *  Map an int JSpinner value to the named preference.
	 *  @param pref the path to the Preferences node to use
	 *  @param def the default int selected index
	 *  @param comp the spinner instance to map to
	 */
	public void map( String pref, int def, JSpinner comp ) {
		log.finer( "Create mapping for "+pref+
			" using "+comp+" with default: "+def );
		Preferences pn = nodeFor(pref);
		pref = lastComponent(pref);
		PrefManager pm = new IntSpinnerManager( pn, pref, def );
		pm.setValueIn( comp );
		comps.put( comp, pm );
	}

	/**
	 *  Map a double JSpinner value to the named preference.
	 *  @param pref the path to the Preferences node to use
	 *  @param def the default double selected index
	 *  @param comp the spinner instance to map to
	 */
	public void map( String pref, double def, JSpinner comp ) {
		log.finer( "Create  with default: "+def );
		Preferences pn = nodeFor(pref);
		pref = lastComponent(pref);
		PrefManager pm = new DoubleSpinnerManager( pn, pref, def );
		pm.setValueIn( comp );
		comps.put( comp, pm );
	}

	/**
	 *  Map a JSpinner selected node/row to the named preference.
	 *  @param pref the path to the Preferences node to use
	 *  @param def the default selected node/row
	 *  @param comp the tree instance to map to
	 */
	public void map( String pref, int def, JTree comp ) {
		log.finer( "Create mapping for "+pref+
			" using "+comp+" with default: "+def );
		Preferences pn = nodeFor(pref);
		pref = lastComponent(pref);
		PrefManager pm = new ListSelectionManager( pn, pref, def );
		pm.setValueIn( comp );
		comps.put( comp, pm );
	}

	/**
	 *  Map a JTable selected row to the named preference.
	 *  @param pref the path to the Preferences node to use
	 *  @param def the default selected row
	 *  @param comp the table instance to map to
	 */
	public void map( String pref, int def, JTable comp ) {
		log.finer( "Create mapping for "+pref+
			" using "+comp+" with default: "+def );
		Preferences pn = nodeFor(pref);
		pref = lastComponent(pref);
		PrefManager pm = new ListSelectionManager( pn, pref, def );
		pm.setValueIn( comp );
		comps.put( comp, pm );
	}

	/**
	 *  Map a JList selected row to the named preference.
	 *  @param pref the path to the Preferences node to use
	 *  @param def the default selected row
	 *  @param comp the list instance to map to
	 */
	public void map( String pref, int def, JList comp ) {
		log.finer( "Create mapping for "+pref+
			" using "+comp+" with default: "+def );
		Preferences pn = nodeFor(pref);
		pref = lastComponent(pref);
		PrefManager pm = new ListSelectionManager( pn, pref, def );
		pm.setValueIn( comp );
		comps.put( comp, pm );
	}

	/**
	 *  Map a JTabbedPanes selected tab index to the named preference.
	 *  @param pref the path to the Preferences node to use
	 *  @param def the default selected row
	 *  @param tabs the list instance to map to
	 */
	public void map( String pref, final int def, final JTabbedPane tabs ) {
		log.finer( "Create mapping for "+pref+
			" using "+tabs+" with default: "+def );
		final Preferences pn = nodeFor(pref);
		final String ipref = lastComponent(pref);
		PrefManager pm = new PrefManager() {
			int idx;
			public boolean prepare( Component comp ) {
				idx = tabs.getSelectedIndex();
				return true;
			}
			public boolean commit( Component comp ) {
				pn.putInt( ipref, idx );
				return true;
			}
			public void setValueIn( Component comp ) {
				SwingUtilities.invokeLater( new Runnable() {
					public void run() {
						tabs.setSelectedIndex(
							pn.getInt(ipref,def) );
					}
				});
			}
		};
		pm.setValueIn( tabs );
		comps.put( tabs, pm );
	}

	/**
	 *  Map a JToggleButton selected state to the named preference.
	 *  @param pref the path to the Preferences node to use
	 *  @param def the default selected state
	 *  @param comp the toggle button instance to map to
	 */
	public void map( String pref, boolean def, JToggleButton comp ) {
		log.finer( "Create mapping for "+pref+
			" using "+comp+" with default: "+def );
		Preferences pn = nodeFor(pref);
		pref = lastComponent(pref);
		PrefManager pm = new BooleanToggleButtonManager( pn, pref, def );
		pm.setValueIn( comp );
		comps.put( comp, pm );
	}

	/**
	 *  Map a JCheckBoxMenuItem selected state to the named preference.
	 *  @param spref the path to the Preferences node to use
	 *  @param def the default selected state
	 *  @param mi the checkbox menu item instance to map to
	 */
	public void map( final String spref, final boolean def, 
				final JCheckBoxMenuItem mi ) {
		log.finer( "Create mapping for "+spref+
			" using "+mi+" with default: "+def );
		final Preferences pn = nodeFor(spref);
		final String pref = lastComponent(spref);
		PrefManager pm = new PrefManager() {
			public boolean prepare( Component comp ) {
				return true;
			}
			public boolean commit( Component comp ) {
				pn.putBoolean( pref, mi.isSelected() );
				return true;
			}
			public void setValueIn( Component comp ) {
				SwingUtilities.invokeLater( new Runnable() {
					public void run() {
						mi.setSelected( pn.getBoolean( pref, def ) );
					}
				});
			}
		};
		pm.setValueIn( mi );
		comps.put( mi, pm );
	}

	/**
	 *  Provide the complete PrefManager to associate with
	 *  the passed component.  The implementation of PrefManager
	 *  passed is responsible for managing all state.  It needs to
	 *  know how to store the data etc.  Thus, it might not use
	 *  the Preferences mechanism at all, and instead if might store
	 *  into a database, a disk file etc.
	 *
	 *  @param man the PrefManager to use for this component
	 *  @param comp the checkbox menu item instance to map to
	 */
	public void map( PrefManager man, Component comp ) {
		comps.put( comp, man );
		man.setValueIn( comp );
	}

	/**
	 *  Get the preferences node that this instance is
	 *  mapped to.
	 */
	public Preferences getPreferencesNode() {
		return pr;
	}

	/**
	 *  Walk any remaining nodes in the preferences path
	 *  to get to the base node where the final component
	 *  of the path is rooted.
	 */
	private Preferences nodeFor( String pref ) {
		if( pref.indexOf("/") == -1 )
			return pr;
		Preferences pn = pr;
		String arr[] = pref.split("/");
		for( int i = 0; i < arr.length -1; ++i ) {
//			System.out.println("step down to: \""+arr[i]+"\"" );
			pn = pn.node(arr[i].replace(' ','_'));
		}
		return pn;
	}

	/**
	 *  Map a JSplitPane divider location to the named preference.
	 *  @param prefn the path to the Preferences node to use
	 *  @param defloc the default divider location
	 *  @param spl the JSplitPane menu item instance to map to
	 */
	public void map( final String prefn, final int defloc, 
			final JSplitPane spl ) {

		final Preferences pn = nodeFor( prefn );
		final String pref = lastComponent(prefn);

		PrefManager pm = new PrefManager() {
			int val;
			public boolean prepare( Component comp ) {
				return true;
			}
			public boolean commit( Component comp ) {
				val = spl.getDividerLocation();
				pn.putInt( pref, val );
				return true;
			}

			// Just initialize on startup
			public void setValueIn( Component comp ) {
				SwingUtilities.invokeLater( new Runnable() {
					public void run() {
						// Get current value or use default
						double nloc = pn.getInt( pref, defloc );
						log.info("setDivider("+
							prefn+", "+defloc+", "+nloc+")");
						// Set location
						spl.setDividerLocation( (int)nloc );
						spl.revalidate();
						spl.repaint();
					}
				});
			}
		};

		pm.setValueIn( spl );
		comps.put( spl, pm );
		log.finer("locating split: " + defloc );
	}

	/**
	 *  Map a Window location and size to the named preference.
	 *  The visibility of the Window is also saved.
	 *  @param pref the path to the Preferences node to use
	 *  @param loc the default location
	 *  @param sz the default location
	 *  @param w the Window instance to map to
	 */
	public void map( String pref, Point loc, Dimension sz, Window w ) {
		Preferences pn = nodeFor( pref );
		pref = lastComponent(pref);
		PrefManager pm = new WindowManager( pn, pref, loc, sz );
		pm.setValueIn( w );
		comps.put( w, pm );
		log.finer("locating window: "+w+" to "+loc+" @ "+sz );
	}

	/**
	 *  Map a Window location and size to the named preference.
	 *  The default location and size are the windows current
	 *  values for getLocation() and getSize().
	 *  @param pref the path to the Preferences node to use
	 *  @param w the Window instance to map to
	 *  @see #map(String,Point,Dimension,Window)
	 */
	public void map( String pref, Window w ) {
		map( pref, w.getLocation(), w.getSize(), w );
	}


	/**
	 *  Map a JComponent location and size to the named preference.
	 *  @param pref the path to the Preferences node to use
	 *  @param loc the default location
	 *  @param sz the default location
	 *  @param w the component instance to map to.
	 *    Typically a Window, but can be a Component.
	 *  This method is primarily used for
	 *  JInternalFrame.  This method also saves the isIcon()
	 *  and isMaximum() properties.
	 *  Also, since JFrame and JDialog are not JComponents, we
	 *  have to have separate methods to handle these and the
	 *  Window version of the map() method is for that.
	 */
	public void map( String pref, Point loc, Dimension sz, JComponent w ) {
		Preferences pn = nodeFor( pref );
		pref = lastComponent(pref);
		PrefManager pm = new WindowManager( pn, pref, loc, sz );
		pm.setValueIn( w );
		comps.put( w, pm );
		log.finer("locating component: "+w+" to "+loc+" @ "+sz );
	}

	/**
	 *  Map a JComponent location and size to the named preference.
	 *  The default location and size are taken from the
	 *  component's current values.
	 *  @param pref the path to the Preferences node to use
	 *  @param w the component instance to map to
	 *  @see #map(String,Point,Dimension,JComponent)
	 */
	public void map( String pref, JComponent w ) {
		map( pref, w.getLocation(), w.getSize(), w );
	}

	/**
	 *  Get the last component value of the preferences
	 *  tree.  This value is used as the place to
	 *  anchor/store the preference(s) of an entry
	 *  stored in the map herein.
	 *  @param pref the Perferences path to use
	 */
	private String lastComponent( String pref ) {
		if( pref.indexOf("/") == -1 )
			return pref;
		String arr[] = pref.split("/");
		return arr[arr.length-1];
	}
}