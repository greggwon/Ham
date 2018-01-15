package org.wonderly.swing;

import java.awt.*;
import javax.swing.*;
import java.net.*;
import java.io.*;

/**
 *  This class provides a simple labeled action that can be associated with
 *  any component.  The <code>desc</code> argument is used to provide the
 *  descriptive string for menus or buttons.  For buttons that only need an
 *  icon, you can use the sequence:
 * <pre>
 *   LabeledAction act = ...
 *   JButton b = new JButton( act );
 *   b.setText( null );
 * </pre>
 *
 *  to remove the text from the button.
 *
 *  @author Gregg Wonderly <a href="mailto:gregg.wonderly@pobox.com">gregg.wonderly@pobox.com</a>
 */
public abstract class LabeledAction extends AbstractAction {
	
	/**
	 *  Construct a new action with the indicated name and
	 *  description.  
	 *  Invokes <code>this( desc, desc )</code>
	 */
	public LabeledAction( String desc ) {
		this( desc, desc );
	}

	/**
	 *  Set the icon to use for this action
	 *  @param icon the value to set on the Action.SMALL_ICON property
	 */
	public void setIcon( Icon icon ) {
     	putValue( Action.SMALL_ICON, icon );
	}

	/**
	 *  Construct a new action with the indicated name and
	 *  description.  
	 *  @param icon the value for the Action.SMALL_ICON property
	 *  @param desc the value for the Action.SHORT_DESCRIPTION property
	 */
	public LabeledAction( Icon icon, String desc ) {
        if( icon != null )
        	putValue( Action.SMALL_ICON, icon );
		if( desc != null )
        	putValue( Action.NAME, desc );
       	putValue( Action.SHORT_DESCRIPTION, desc );
	}

	/**
	 *  Construct a new action with the indicated name and
	 *  description.  
	 *  @param name the value for the Action.NAME property
	 *  @param desc the value for the Action.SHORT_DESCRIPTION property
	 */
	public LabeledAction( String name, String desc ) {
        if( name != null )
        	putValue( Action.NAME, name );
       	putValue( Action.SHORT_DESCRIPTION, name );
		if( desc != null )
       		putValue( Action.SHORT_DESCRIPTION, desc );
	}

	/**
	 *  Construct a new action with the indicated name and
	 *  description.  
	 *  @param name the value for the Action.NAME property
	 *  @param desc the value for the Action.SHORT_DESCRIPTION property
	 *  @param enabled The actions initial enabled state
	 */
	public LabeledAction( String name, String desc, boolean enabled ) {
		this( name, desc );
		this.setEnabled( enabled );
	}

	/**
	 *  Construct a new action with the indicated name and
	 *  description.  
	 *  @param icon the value for the Action.SMALL_ICON property
	 *  @param desc the value for the Action.SHORT_DESCRIPTION property
	 *  @param enabled The actions initial enabled state
	 */
	public LabeledAction( Icon icon, String desc, boolean enabled ) {
		this( icon, desc );
		this.setEnabled( enabled );
	}
}