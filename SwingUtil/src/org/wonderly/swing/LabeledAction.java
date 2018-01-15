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
 *  <p>
 * The Action.SHORT_DESCRIPTION field is typically used as the value of the tooltip text for componentry
 * which uses the action.  Be careful about using the same text for name that you use for desc in the arguments
 * to the constructor so that duplicate information is not visible to the user in the tooltip.
 * @author Gregg Wonderly <a href="mailto:gregg.wonderly@pobox.com">gregg.wonderly@pobox.com</a>
 */
public abstract class LabeledAction extends AbstractAction {
	
	/**
	 *  Construct a new action with the indicated name and
	 *  description.  
	 *  Invokes <code>this( desc, desc )</code>
	 * @param desc The description and name value to use.
	 * @deprecated Don't use this for components that create tooltips from the Action.SHORT_DESCRIPTION value.
	 * Use this for components which use either Action.NAME or Action.SHORT_DESCRIPTION, but not both.
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
	 * @param name The name to use for Action.NAME
	 * @param icon the value for the Action.SMALL_ICON property
	 */
	public LabeledAction( Icon icon, String name ) {
        if( icon != null )
        	putValue( Action.SMALL_ICON, icon );
		if( name != null )
        	putValue( Action.NAME, name );
	}
	
	/**
	 * Create an action with an icon, name and description.
	 * @param icon The icon to use
	 * @param name The name value for Action.NAME
	 * @param desc A description for Action.SHORT_DESCRIPTION
	 */
	public LabeledAction( Icon icon, String name, String desc ) {
		this( icon, name );
		if( desc != null )
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
	
	/**
	 *  Construct a new action with the indicated name and
	 *  description.
	 * @param name The name for the action for Action.NAME
	 * @param icon the value for the Action.SMALL_ICON property
	 * @param desc the value for the Action.SHORT_DESCRIPTION property
	 * @param enabled The actions initial enabled state
	 */
	public LabeledAction( Icon icon, String name, String desc, boolean enabled ) {
		this( icon, name, desc );
		this.setEnabled( enabled );
	}
}