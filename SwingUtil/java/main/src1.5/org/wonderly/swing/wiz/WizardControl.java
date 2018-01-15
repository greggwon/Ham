package org.wonderly.swing.wiz;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComponent;
import java.awt.CardLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;

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
 *  This class provides a more advanced wizard interface than {@link org.wonderly.swing.CardManager}.
 *  It includes support for a {@link Validator} interface which can be used to check when
 *  it is okay to move forward through the wizard.
 *
 *  @see org.wonderly.swing.CardManager
 *  @author <a href="mailto:gregg.wonderly@pobox.com">Gregg Wonderly</a>.
 */
public class WizardControl {
	/** The names of the cards */
	protected String cards[];
	/** The associated CardLayout instance */
	protected CardLayout lay;
	/** The JComponent associated with the CardLayout in lay */
	protected JComponent target;
	/** The Previous function button */
	protected volatile JButton pbut;
	/** The Next function button */
	protected volatile JButton nbut;
	/** The Finished function button, if provided */
	protected volatile JButton fbut;
	/** The currently displayed card index */
	protected volatile int cur;
	/** The {@link Validator} instances for each card */
	protected HashMap<String,Validator>valid = new HashMap<String,Validator>();
	/** The action listeners for the finished action */
	protected ArrayList<ActionListener> al = new ArrayList<ActionListener>();

	/**
	 *  Add a listener to be called when the wizard finishes
	 */
	public void addActionListener( ActionListener lis ) {
		if( al.contains(lis) == false )
			al.add( lis );
	}
	
	/**
	 *  Remove a previously added wizard
	 *  @see #addActionListener(ActionListener)
	 */
	public void removeActionListener( ActionListener lis ) {
		al.remove( lis );
	}

	/**
	 *  Create a new wizard using the passed card names, controlling
	 *  the passed {@link java.awt.CardLayout} instance attached to
	 *  the indicated target JComponent.
	 *  @param cards the names of all cards that will exist in the layout
	 *  @param lay the layout instance to control display with
	 *  @param target the JComponent that whose layout manager is lay.
	 */
	public WizardControl( String[]cards, CardLayout lay, JComponent target ) {
		this.cards  = cards;
		this.lay    = lay;
		this.target = target;
	}

	/**
	 *  Sets the button instance to use for the previous card action
	 */
	public void setPrevious( JButton prev ) {
		pbut = prev;
		pbut.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent ev ) {
				setCard( cur-1);
			}
		});
	}
	
	/**
	 *  Sets the button instance to use for the next card action
	 */
	public void setNext( JButton next ) {
		nbut = next;
		nbut.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent ev ) {
				if( cur < cards.length-1 ) {
					setCard( cur+1 );
				} else if( fbut == null ) {
					ActionEvent ae = new ActionEvent( nbut, 1, "finished" );
					for( ActionListener lis: al ) {
						lis.actionPerformed( ae );
					}
				}
			}
		});
	}
	
	/**
	 *  Sets the button instance to use for the finished action.
	 *  If not set, the "Next" button will be used for finished
	 *  actions.
	 */
	public void setFinished( JButton fin ) {
		fbut = fin;
		fbut.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent ev ) {
				ActionEvent ae = new ActionEvent( fbut, 1, "finished" );
				for( ActionListener lis: al ) {
					lis.actionPerformed( ae );
				}
			}
		});
	}
	
	/**
	 *  Add a validator for the passed card.
	 *  @param card the name of the card the validator applies to
	 *  @param v a Validator instance that will be used to control
	 *           when the "next" action is available while the
	 *           indicated card is visible.
	 */
	public void addValidator( String card, Validator v ) {
		valid.put( card, v );
	}

	/**
	 *  Switch to the indicated card number.  The array of
	 *  card names will be consulted for the name of the
	 *  card to switch to.
	 */
	public void setCard( int c ) {
		cur = c;
		checkReady();
		lay.show( target, cards[cur] );
	}
	
	/**
	 *  Get currently displayed card index
	 */
	public int getCard() {
		return cur;
	}

	/**
	 *  This method is called to validate the state of all controls and
	 *  configure them appropriately
	 */
	public void checkReady() {
		// Initially ready, if no validator
		boolean v = true;
		Validator vd = valid.get( cards[cur] );
		if( vd != null ) {
			// use validators state of ready
			v = vd.ready();
		}
		pbut.setEnabled( cur != 0 );
		nbut.setEnabled( ((fbut == null) ? (cur == cards.length-1) :
			cur != cards.length-1) && v );
		if( fbut != null )
			fbut.setEnabled( cur == cards.length-1 && v );
	}
}
