package org.wonderly.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
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
 *  This class provides a wizard style, simple cardlayout based panel
 *  that provides a previous and next button and automatic limit checking
 *  and button state control.
 *
 *  @see org.wonderly.swing.wiz.WizardControl
 *  @author <a href="mailto:gregg.wonderly@pobox.com">Gregg Wonderly</a>.
 */
public abstract class CardManager extends JPanel {
	int min, max;
	String cards[];
	CardLayout cl;
	int cur = 0;
	JButton prev, next;
	Logger log = Logger.getLogger( getClass().getName() );

	public CardManager( int min, int max, 
			final String[]cards, final JButton prev, final JButton next ) {
		cl = new CardLayout();
		setLayout( cl );
		this.min = min;
		this.max = Math.min( max, cards.length-1 );
		this.cards = cards;
		this.prev = prev;
		this.next = next;
		log.finer("min: "+min+", max: "+max+", len: "+cards.length );
		prev.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent ev ) {
				cl.show( CardManager.this, cards[ --cur ] );
				check();
			}
		});
		next.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent ev ) {
				if( cur >= CardManager.this.max ) {
					finished();
					return;
				}
				cl.show( CardManager.this, cards[ ++cur ] );
				check();
			}
		});
		check();
	}
	
	protected abstract void finished();

	protected void check() {
		prev.setEnabled( cur > 0 );
		next.setEnabled( cur <= max );
		next.setText( (cur >= max ) ?
			"Finish" : "Next" );
	}
	
	public void switchTo( String card ) {
		cl.show( this, card );
		for( int i = 0; i < cards.length; ++i ) {
			if( card.equals( cards[i] ) ) {
				cur = i;
			}
		}
	}
}
