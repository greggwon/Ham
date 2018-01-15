package org.wonderly.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;

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
 *  A labeled action that can toggle its selected state.  This
 *  action should be set on a JCheckBoxMenuItem or JCheckBox so
 *  that the state of that objects selected status is mirrored
 *  into this object, and then back out to any other parallel
 *  components.  The <code>setupToggleFor( ... )</code> methods
 *  will cause a new action listener to be set on this object
 *  to relay its state changes into the passed components state.
 *
 *  @author Gregg Wonderly <a href="mailto:gregg.wonderly@pobox.com">gregg.wonderly@pobox.com</a>
 */
public abstract class LabeledToggleAction extends LabeledAction {
	boolean sel;
	SelectChecker chk;
	Vector<ActionListener> lis = new Vector<ActionListener>();
	Logger log = Logger.getLogger( getClass().getPackage().toString() );
	
	public String toString() {
		return getValue(Action.SHORT_DESCRIPTION)+": "+sel;
	}

	public void addActionListener( ActionListener l ) {
		lis.addElement(l);
	}

	public void removeActionListener( ActionListener l ) {
		lis.removeElement(l);
	}

	private void notifyListeners( ActionEvent ev ) {
		for( int i = 0; i < lis.size(); ++i ) {
			ActionListener al = (ActionListener)lis.elementAt(i);
			al.actionPerformed(ev);
		}
	}
	protected void notifyListeners() {
		notifyListeners( new ActionEvent(this,1,""+new Boolean(isSelected())) );
	}
	public void setSelected( boolean how ) {
		sel = how;
		if( chk != null )
			chk.setSelected(how);
		notifyListeners();
	}
	public boolean isSelected() {
		if( sel && chk != null )
			return sel = chk.isSelected();
		return sel;
	}

	public LabeledToggleAction( String desc ) {
		super(desc);
	}

	public LabeledToggleAction( Icon icon, String desc ) {
		super(icon,desc);
	}

	public LabeledToggleAction( String name, String desc ) {
		super(name,desc);
	}

	public LabeledToggleAction( String name, String desc, boolean enabled ) {
		super( name, desc, enabled );
	}

	public LabeledToggleAction( Icon icon, String desc, boolean enabled ) {
		super( icon, desc, enabled );
	}
	
	static interface SelectChecker {
		public boolean isSelected();
		public void setSelected(boolean how);
	}

    public void setupToggleFor( final JCheckBoxMenuItem itm ) {
    	addActionListener( new ActionListener() {
    		public void actionPerformed( ActionEvent ev ) {
    			itm.setSelected( isSelected() );
    			log.fine("selecting: "+itm );
    		}
    	});
    	chk = new SelectChecker() {
    		public boolean isSelected() {
    			return itm.isSelected();
    		}
    		public void setSelected( boolean how ) {
    			itm.setSelected(how);
    		}
    	};
    	itm.addActionListener( new ActionListener() {
    		public void actionPerformed( ActionEvent ev ) {
    			sel = true;
    		}
    	});
    }

    public void setupToggleFor( final JToggleButton itm ) {
    	addActionListener( new ActionListener() {
    		public void actionPerformed( ActionEvent ev ) {
    			itm.setSelected( isSelected() );
    			log.fine("selecting: "+itm );
    		}
    	});
    	chk = new SelectChecker() {
    		public boolean isSelected() {
    			return itm.isSelected();
    		}
    		public void setSelected( boolean how ) {
    			itm.setSelected(how);
    		}
    	};
    	itm.addActionListener( new ActionListener() {
    		public void actionPerformed( ActionEvent ev ) {
    			sel = true;
    		}
    	});
    }

    public void setupToggleFor( final JRadioButton itm ) {
    	addActionListener( new ActionListener() {
    		public void actionPerformed( ActionEvent ev ) {
    			itm.setSelected( isSelected() );
    			log.fine("selecting: "+itm );
    		}
    	});
    	chk = new SelectChecker() {
    		public boolean isSelected() {
    			return itm.isSelected();
    		}
    		public void setSelected( boolean how ) {
    			itm.setSelected(how);
    		}
    	};
    	itm.addActionListener( new ActionListener() {
    		public void actionPerformed( ActionEvent ev ) {
    			sel = true;
    		}
    	});
    }

    public void setupToggleFor( final JCheckBox itm ) {
    	addActionListener( new ActionListener() {
    		public void actionPerformed( ActionEvent ev ) {
    			itm.setSelected( isSelected() );
    			log.fine("selecting: "+itm );
    		}
    	});
    	chk = new SelectChecker() {
    		public boolean isSelected() {
    			return itm.isSelected();
    		}
    		public void setSelected( boolean how ) {
    			itm.setSelected(how);
    		}
    	};
    	itm.addActionListener( new ActionListener() {
    		public void actionPerformed( ActionEvent ev ) {
    			sel = true;
    		}
    	});
    }
}