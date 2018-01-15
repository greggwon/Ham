package org.wonderly.awt;

import java.awt.*;

/**
 *  This interface is returned/used by the {@link org.wonderly.awt.Packer} layout manager.  The
 *  methods are used to arrange the layout of components.
 *
 *  The majority of these methods correspond directly to elements of the
 *  {@link java.awt.GridBagConstraints} object and its use with the 
 *  {@link java.awt.GridBagLayout} layout
 *  manager.  The purpose of this class is to make the use of these two
 *  objects simpler and less error prone by allowing the complete layout
 *  of an object to be specified in a single line of code, and to discourage
 *  the reuse of GridBagConstraint Objects which leads to subtle layout
 *  interactions.
 *  <p>
 *  <b>NOTE:</b>:<br>
 *  Pay attention to the fact that {@link #fillx()}, {@link #filly()} and {@link #fillboth()} change
 *  the weights of the respective axis.  This is done to unify the expansion
 *  factors to equally distribute the components if you do not specify <code>weightx/weighty</code>
 *  values.  If the <code>weightx/weighty</code> values are not specified explicitly, they default to 1.
 *  Either put the {@link #weightx(double)}/{@link #weighty(double)} call after the 
 *  {@link #fillx()}/{@link #filly()} call to get
 *  something different than equal expansion, or use the {@link #fillx(double)},
 *  {@link #filly(double)} or {@link #fillboth(double,double)} methods that combine
 *  these two calls.
 *  <p>
 *  When putting several buttons into a JPanel, you can use
 *  <code>...fillx(0)</code> to make all of them the same width,
 *  but keep the JPanel from being any wider than the widest button.
 *  @version 3.0
 *  @author <a href="mailto:gregg@wonderly.org">Gregg Wonderly</a>
 *  @see org.wonderly.awt.Packer
 */

public interface PackAs extends java.io.Serializable {
	/**
	 *	Set the passed container as the container to pack
	 *	future components into.
	 *
	 * @param cont the container to pack into
	 *  @exception IllegalAccessException when the container is already set and
	 *             cloning the Packer instance fails.
	 * @return this instance
	 */
	public PackAs into( Container cont ) throws IllegalAccessException;
	/**
	 *	Specifies the insets to apply to the component.
	 *	@param insets the insets to apply
	 * @return this instance
	 */
	public PackAs inset( Insets insets );
	/**
	 *	Specifies the insets to apply to the component.
	 *	@param left the left side inset.
	 *	@param top the top inset.
	 *	@param right the right side inset.
	 *	@param bottom the bottom inset.
	 * @return this instance
	 */
	public PackAs inset( int top, int left, int bottom, int right );
	/**
	 *	Add anchor=NORTH to the constraints for the current
	 *	component.
	 * @return this instance
	 */
	public PackAs north();
	/**
	 *	Add anchor=SOUTH to the constraints for the current
	 *	component.
	 * @return this instance
	 */
	public PackAs south();
	/**
	 *	Add anchor=EAST to the constraints for the current
	 *	component.
	 * @return this instance
	 */
	public PackAs east();
	/**
	 *	Add anchor=WEST to the constraints for the current
	 *	component.
	 * @return this instance
	 */
	public PackAs west();
	/**
	 *	Add anchor=NORTHWEST to the constraints for the current
	 *	component.
	 * @return this instance
	 */
	public PackAs northwest();
	/**
	 *	Add anchor=SOUTHWEST to the constraints for the current
	 *	component.
	 * @return this instance
	 */
	public PackAs southwest();
	/**
	 *	Add anchor=NORTHEAST to the constraints for the current
	 *	component.
	 * @return this instance
	 */
	public PackAs northeast();
	/**
	 *	Add anchor=SOUTHEAST to the constraints for the current
	 *	component.
	 * @return this instance
	 */
	public PackAs southeast();
	/**
	 *  Add gridx=RELATIVE to the constraints for the current
	 *	component.
	 * @return this instance
	 */
	public PackAs left();
	/**
	 *  Add gridy=RELATIVE to the constraints for the current
	 *	component.
	 * @return this instance
	 */
	public PackAs top();
	/**
	 *  Add gridx=REMAINDER to the constraints for the current
	 *	component.
	 * @return this instance
	 */
	public PackAs right();
	/**
	 *  Add gridy=REMAINDER to the constraints for the current
	 *	component.
	 * @return this instance
	 */
	public PackAs bottom();
	/**
	 *  Add gridx=pos.x, gridy=pos.y to the constraints
	 *  for the current component.
	 *  @param pos the gridx() and gridy() position
	 * @return this instance
	 */
	public PackAs grid( Point pos );
	
	/**
	 *  Add gridx=posx, gridy=posy to the constraints
	 *  for the current component.
	 *  @param posx the gridx() position
	 *  @param posy the gridy() position
	 * @return this instance
	 */
	public PackAs grid( int posx, int posy );
	/**
	 *  Add gridx=tot to the constraints for the current
	 *	component.
	 *
	 *	@param pos - the value to set gridx to.
	 * @return this instance
	 */
	public PackAs gridx( int pos );
	/**
	 *  Add gridy=tot to the constraints for the current
	 *	component.
	 *
	 *	@param pos - the value to set gridy to.
	 * @return this instance
	 */
	public PackAs gridy( int pos );
	/**
	 *  Add gridheight=tot to the constraints for the current
	 *	component.
	 *
	 *	@param cnt - the value to set gridheight to.
	 * @return this instance
	 */
	public PackAs gridh( int cnt );
	/**
	 *  Add gridwidth=tot to the constraints for the current
	 *	component.
	 *
	 *	@param cnt - the value to set gridwidth to.
	 * @return this instance
	 */
	public PackAs gridw( int cnt );
	/**
	 *  Add ipadx=cnt to the constraints for the current
	 *	component.
	 *
	 *	@param cnt - the value to set ipadx to.
	 * @return this instance
	 */
	public PackAs padx( int cnt );
	/**
	 *  Add ipady=cnt to the constraints for the current
	 *	component.
	 *
	 *	@param cnt - the value to set ipady to.
	 * @return this instance
	 */
	public PackAs pady( int cnt );
	/**
	 *  Add fill=HORIZONTAL,weightx=wtx to the constraints for the current
	 *	component.
	 * @param wtx weight
	 * @return this instance
	 */
	public PackAs fillx(double wtx);
	/**
	 *  Add fill=HORIZONTAL,weightx=1 to the constraints for the current
	 *	component.
	 * @return this instance
	 */
	public PackAs fillx();
	/**
	 *  Add fill=VERTICAL,weighty=wty to the constraints for the current
	 *	component.
	 * @param wty weight
	 * @return this instance
	 */
	public PackAs filly(double wty);
	/**
	 *  Add fill=VERTICAL,weighty=1 to the constraints for the current
	 *	component.
	 * @return this instance
	 */
	public PackAs filly();
	/**
	 *  Add fill=BOTH,weighty=1,weightx=1 to the constraints for the current
	 *	component.
	 * @return this instance
	 */
	public PackAs fillboth();
	/**
	 *  Add fill=BOTH,weighty=wty,weightx=wtx to the constraints for the current
	 *	component.
	 * @param wtx weight
	 * @param wty weight
	 * @return this instance
	 */
	public PackAs fillboth(double wtx, double wty);
	/**
	 *  Add weightx=wt to the constraints for the current
	 *	component.
	 *
	 *	@param wt - the value to set weightx to.
	 * @return this instance
	 */
	public PackAs weightx( double wt );
	/**
	 *  Add weighty=wt to the constraints for the current
	 *	component.
	 *
	 *	@param wt - the value to set weightx to.
	 * @return this instance
	 */
	public PackAs weighty( double wt );
	/**
	 *  Reuses the previous set of constraints to layout the passed Component.
	 *  This method <b>DOES NOT</b> use a copy of those constraints.
	 *
	 *  @param c The component to layout.
	 *  @see #like(Component)
	 * @return this instance
	 */
	public PackAs add( Component c );

	/**
	 *  Creates a new set of constraints to layout the passed Component.
	 *
	 *  @param c The component to layout.
	 *  @see #add(Component)
	 *  @see #like(Component)
	 * @return this instance
	 */
	public PackAs pack( Component c );

	/**
	 *  Add gridwidth=REMAINDER to the constraints for the current
	 *	component.
	 * @return this instance
	 */
	public PackAs remainx();

	/**
	 *  Add gridheight=REMAINDER to the constraints for the current
	 *	component.
	 * @return this instance
	 */
	public PackAs remainy();

	/**
	 *  Copy the constraints previously used for the passed
	 *  Component.  The returned PackAs instance utilizes the
	 *  constraints that 'c' currently has at the time of the
	 *  call to <code>like()</code>.
	 *  @param c the component to get the constraints from.
	 *  @see #add(Component)
	 * @return this instance
	 */
	public PackAs like(Component c);

	/**
	 *  Set the anchor term to the passed mask.
	 *  The mask values are described in the GridbagConstraints
	 *  javadoc.  This provides a simple way to support the
	 *  JDK1.6 constraints without having Packer/PackAs
	 *  branch.  In the future, we can add appropriate
	 *  methods for the baseline etc constraints.
	 * @param mask mask to use
	 * @return this instance
	 */
	public PackAs anchor( int mask );
}