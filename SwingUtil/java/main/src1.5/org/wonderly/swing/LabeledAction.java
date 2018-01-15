package org.wonderly.swing;

import java.awt.*;
import javax.swing.*;
import java.net.*;
import java.io.*;

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