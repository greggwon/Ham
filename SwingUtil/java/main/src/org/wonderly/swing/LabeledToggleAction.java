package org.wonderly.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;

/**
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
	Vector lis;
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
		lis = new Vector();
	}
	public LabeledToggleAction( Icon icon, String desc ) {
		super(icon,desc);
		lis = new Vector();
	}
	public LabeledToggleAction( String name, String desc ) {
		super(name,desc);
		lis = new Vector();
	}
	public LabeledToggleAction( String name, String desc, boolean enabled ) {
		super( name, desc, enabled );
		lis = new Vector();
	}
	public LabeledToggleAction( Icon icon, String desc, boolean enabled ) {
		super( icon, desc, enabled );
		lis = new Vector();
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