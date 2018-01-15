package org.wonderly.swing.prefs;

import java.awt.Component;

/**
 *  This interface is implemented by Preferences access
 *  Managers used by {@link PreferencesMapper}.  Implementations
 *  of this interface are used to manage the mapping of
 *  values to UI state or other similar mappings.  The
 *  {@link PreferencesMapper} class will call {@link
 *  #setValueIn( Component )} when the initial call to
 *  one of the <code>map()</code> methods is called.  When
 *  {@link PreferencesMapper}.commit() is called, the
 *  {@link #prepare(Component)} and then the {@link #commit(Component)}
 *  will be called to cause the storing of the preferred
 *  values of an associated Component. For preferences
 *  associated with a UI, but not with a components state,
 *  you can use invisible JPanel's or JLabel or other
 *  unalterable components in the call to {@link
 *  PreferencesMapper}.map(PrefManager,Component).
 *
 *  @author Gregg Wonderly <a href="mailto:gregg.wonderly@pobox.com">
 *	gregg.wonderly@pobox.com</a>
 */
public interface PrefManager {
	/**
	 *  This method should look at the content of the
	 *  passed component and validate that it is ready
	 *  to be stored into Preferences.
	 *  @return true if data is valid, false if not
	 */
	public boolean prepare( Component comp );
	/**
	 *  This method should store the value for the passed
	 *  component into Preferences and return true
	 *  if it was successful.  If it returns false,
	 *  some applications might use this information to
	 *  take some specific action to deal with the failure
	 *  @return true if commit was successful, false if not
	 */
	public boolean commit( Component comp );
	/**
	 *  This method should extract the current value from
	 *  Preferences and set the components value/state
	 *  to reflect that value.
	 */
	public void setValueIn( Component comp );
}