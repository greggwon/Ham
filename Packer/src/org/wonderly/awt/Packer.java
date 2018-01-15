package org.wonderly.awt;

import java.awt.*;

/**
 *  The Packer class extends the {@link java.awt.GridBagLayout} to provide a method based
 *  interface to this class.  This makes it possible to position several
 *  widgets into a {@link java.awt.GridBagLayout} without resetting various values in a
 *  {@link java.awt.GridBagLayout} instance.  All methods return the
 *  {@link org.wonderly.awt.PackAs} interface
 *  which allows the reuse of a particular layout strategy for subsequent
 *  widgets via the add() method.
 *
 *  <pre>
 *		Panel p = new Panel();			         // Create new Panel container
 *
 *		Label l = new Label( "label: " );	     // Create a label Component
 *		TextField t = new TextField( 30 );	     // Create a TextField Component
 *
 *		Packer pk = new Packer(p);		         // Create Layout and setLayout on container
 *      int y = -1;                              // Use preincrement for convenience
 *		pk.pack( l ).gridy(++y).gridx(0); 	     // Add to Container and layout
 *		pk.pack( t ).gridy(y).gridx(1).fillx();	 // Add to Container and layout
 *  </pre>
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
 *
 *  @version 1.0
 *  @author <a href="mailto:gregg@wonderly.org">Gregg Wonderly</a>
 *
 */

public class Packer extends GridBagLayout implements PackAs {
	/**
	 *  The current constraints used for subsequent uses of the PackAs
	 *  interface returned by the methods below.
	 */
	protected volatile GridBagConstraints gc = null;

	/**
	 *  The component currently being layed out.
	 */
	protected volatile Component comp;

	/**
	 *  If the Packer( Container ) constructor is used, this
	 *  member contains a reference to the container that this
	 *  instance provides layout for.
	 */
	protected volatile Container container;

	/**
	 *  Create a new Packer instance.  A Packer constructed with this
	 *  constructor will have no "designated" container for the elements
	 *  that it packs.  Thus, each will need to be added to the container
	 *  individually as well as being pack()'d or add()'d with the packer.
	 */
	public Packer () {
		super();
	}

	/**
	 *  Creates a new Packer instance that is used to layout the
	 *  passed container.  This version of the constructor allows for
	 *  more compact code by causing this instance to automatically
	 *  add() the components to the container as well as do the layout.
	 */
	public Packer( Container cont ) {
		super();
		cont.setLayout( this );
		container = cont;
	}

	/**
	 *  Create a copy of this Packer Object.
	 *  @exception CloneNotSupportedException if strange clone errors occur
	 */
	@Override
	public synchronized Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 *  Set the designated container for objects packed by this instance.
	 *  @param cont the Container to use to add the components to when
	 *              .pack() or .add() is invoked.
	 *  @exception IllegalAccessException if container already set
	 */
	public synchronized PackAs setContainer( Container cont ) throws IllegalAccessException {
		if( container != null ) {
		    try {
    			Packer p = (Packer)clone();
    			container.setLayout( p );
    		} catch( CloneNotSupportedException e ) {
    		    throw new IllegalAccessException();
    		}
		}
		container = cont;
		cont.setLayout( this );
		return this;
	}

	/**
	 *  Copy the passed component's contraints as the
	 *  current constraints.  This is typically used
	 *  to reuse constraints set on another component.
	 *  Be careful with this method as it does not
	 *  protect you from cascading reuse issues.
 	 *  This method uses java.awt.GridbagLayout#getConstraints()
	 *  to get a copy of the constraints for <code>comp</code>.
	 *  @param comp the component to get the constraints
	 *
	 *  <pre>
	 *  Packer pk = new Packer(pan);
	 *  pk.pack(lab).gridx(0).inset(10,10,10,10);
	 *  pk.pack(text).like(lab).gridx(1).fillx(); // same insets
	 *  </pre>
	 */
	public synchronized PackAs like( Component comp ) {
		// Get a copy of the components constraints
		gc = getConstraints(comp);
		if(gc == null) {
			throw new IllegalArgumentException(comp+" has no existing constraints");
		}
		setConstraints( comp, gc );
		return this;
	}
	
	/**
	 *  A non chainable version of {@link #like(Component)}
	 *  @see #like(Component)
	 */
	public synchronized void setInitialConstraintsFrom( Component comp ) {
		gc = getConstraints(comp);
		if(gc == null) {
			throw new IllegalArgumentException(comp+" has no existing constraints");
		}
		setConstraints( comp, gc );
	}

	/**
	 *  Get the designated container for this instance.
	 */
	public synchronized Container getContainer() {
		return container;
	}

	/**
	 *  This method is used to specify the container that the next component
	 *  that is added or packed will be placed in.
	 *
	 *  @param cont The container to place future components into.
	 *  @exception IllegalAccessException if container is already set
	 */
	public synchronized PackAs into( Container cont ) throws IllegalAccessException {
		setContainer( cont );
		return this;
	}

	/**
	 *  Establishes a new set of constraints to layout the widget passed.
	 *  The created constraints are applied to the passed Component and
	 *  if a Container is known to this object, the component is added to
	 *  the known container.
	 *
	 *  @param cp The component to layout.
	 */
	public synchronized PackAs pack( Component cp ) {
		if( container != null ) {
			container.add( cp );
		}
		comp = cp;
		gc = new GridBagConstraints();
		setConstraints( comp, gc );
		return this;
	}

	/**
	 *  Reuses the previous set of constraints to layout the widget passed
	 *
	 *  @param cp The component to layout.  It must be added to the
	 *  Container owning this LayoutManager by the calling code.
	 */
	public synchronized PackAs add( Component cp ) {
		if( container != null ) {
			container.add( cp );
		}
		comp = cp;
		if( gc == null ) {
			gc = new GridBagConstraints();
		}
		setConstraints( comp, gc );
		return this;
	}

	/**
	 *  Add anchor=NORTH to the constraints for the current
	 *  component if how == true remove it if false.
	 */
	public synchronized PackAs setAnchorNorth( boolean how ) {
		if( how == true ) {
			gc.anchor = GridBagConstraints.NORTH;
		}
		else {
			gc.anchor &= ~GridBagConstraints.NORTH;
		}
		setConstraints( comp, gc );
		return this;
	}

	/**
	 */
	public synchronized boolean getAnchorNorth( ) {
		return gc.anchor == GridBagConstraints.NORTH;
	}

	/**
	 *  Add anchor=NORTH to the constraints for the current
	 *  component.
	 */
	public synchronized PackAs north() {
		gc.anchor = GridBagConstraints.NORTH;
		setConstraints( comp, gc );
		return this;
	}

	/**
	 *  Add anchor=SOUTH to the constraints for the current
	 *  component if how == true remove it if false.
	 */
	public synchronized PackAs setAnchorSouth( boolean how ) {
		if( how == true ) {
			gc.anchor = GridBagConstraints.SOUTH;
		}
		else {
			gc.anchor &= ~GridBagConstraints.SOUTH;
		}
		setConstraints( comp, gc );
		return this;
	}

	/**
	 */
	public synchronized boolean getAnchorSouth( ) {
		return gc.anchor == GridBagConstraints.SOUTH;
	}

	/**
	 *  Add anchor=SOUTH to the constraints for the current
	 *  component.
	 */
	public synchronized PackAs south() {
		gc.anchor = GridBagConstraints.SOUTH;
		setConstraints( comp, gc );
		return this;
	}

	/**
	 *  Add anchor=EAST to the constraints for the current
	 *  component if how == true remove it if false.
	 */
	public synchronized PackAs setAnchorEast( boolean how ) {
		if( how == true ) {
			gc.anchor = GridBagConstraints.EAST;
		}
		else {
			gc.anchor &= ~GridBagConstraints.EAST;
		}
		setConstraints( comp, gc );
		return this;
	}

	/**
	 */
	public synchronized boolean getAnchorEast( ) {
		return gc.anchor == GridBagConstraints.EAST;
	}
	
	/**
	 *  Add anchor=EAST to the constraints for the current
	 *  component.
	 */
	public synchronized PackAs east() {
		gc.anchor = GridBagConstraints.EAST;
		setConstraints( comp, gc );
		return this;
	}

	/**
	 *  Add anchor=WEST to the constraints for the current
	 *  component if how == true remove it if false.
	 */
	public synchronized PackAs setAnchorWest( boolean how ) {
		if( how == true ) {
			gc.anchor = GridBagConstraints.WEST;
		}
		else {
			gc.anchor &= ~GridBagConstraints.WEST;
		}
		setConstraints( comp, gc );
		return this;
	}

	/**
	 *  Set the anchor term to the passed mask.
	 *  The mask values are described in the java.awt.GridbagConstraints
	 *  javadoc.  This provides a simple way to support the
	 *  JDK1.6 constraints without having Packer/PackAs
	 *  branch.  In the future, we can add appropriate
	 *  methods for the baseline etc constraints.
	 */
	public synchronized PackAs anchor( int mask ) {
		gc.anchor = mask;
		setConstraints( comp, gc );
		return this;
	}

	/**
	 */
	public synchronized boolean getAnchorWest( ) {
		return gc.anchor == GridBagConstraints.WEST;
	}

	/**
	 *  Add anchor=WEST to the constraints for the current
	 *  component.
	 */
	public synchronized PackAs west() {
		gc.anchor = GridBagConstraints.WEST;
		setConstraints( comp, gc );
		return this;
	}

	/**
	 *  Add anchor=NORTHWEST to the constraints for the current
	 *  component if how == true remove it if false.
	 */
	public synchronized PackAs setAnchorNorthWest( boolean how ) {
		if( how == true ) {
			gc.anchor = GridBagConstraints.NORTHWEST;
		}
		else {
			gc.anchor &= ~GridBagConstraints.NORTHWEST;
		}
		setConstraints( comp, gc );
		return this;
	}

	/**
	 */
	public synchronized boolean getAnchorNorthWest( ) {
		return gc.anchor == GridBagConstraints.NORTHWEST;
	}

	/**
	 *  Add anchor=NORTHWEST to the constraints for the current
	 *  component.
	 */
	public synchronized PackAs northwest() {
		gc.anchor = GridBagConstraints.NORTHWEST;
		setConstraints( comp, gc );
		return this;
	}

	/**
	 *  Add anchor=SOUTHWEST to the constraints for the current
	 *  component if how == true remove it if false.
	 */
	public synchronized PackAs setAnchorSouthWest( boolean how ) {
		if( how == true ) {
			gc.anchor = GridBagConstraints.SOUTHWEST;
		}
		else {
			gc.anchor &= ~GridBagConstraints.SOUTHWEST;
		}
		setConstraints( comp, gc );
		return this;
	}

	/**
	 */
	public synchronized boolean getAnchorSouthWest( ) {
		return gc.anchor == GridBagConstraints.SOUTHWEST;
	}

	/**
	 *  Add anchor=SOUTHWEST to the constraints for the current
	 *  component.
	 */
	public synchronized PackAs southwest() {
		gc.anchor = GridBagConstraints.SOUTHWEST;
		setConstraints( comp, gc );
		return this;
	}

	/**
	 *  Add anchor=NORTHEAST to the constraints for the current
	 *  component if how == true remove it if false.
	 */
	public synchronized PackAs setAnchorNorthEast( boolean how ) {
		if( how == true ) {
			gc.anchor = GridBagConstraints.NORTHEAST;
		}
		else {
			gc.anchor &= ~GridBagConstraints.NORTHEAST;
		}
		setConstraints( comp, gc );
		return this;
	}

	/**
	 */
	public synchronized boolean getAnchorNorthEast( ) {
		return gc.anchor == GridBagConstraints.NORTHEAST;
	}

	/**
	 *  Add anchor=NORTHEAST to the constraints for the current
	 *  component.
	 */
	public synchronized PackAs northeast() {
		gc.anchor = GridBagConstraints.NORTHEAST;
		setConstraints( comp, gc );
		return this;
	}

	/**
	 *  Add anchor=SOUTHEAST to the constraints for the current
	 *  component if how == true remove it if false.
	 */
	public synchronized PackAs setAnchorSouthEast( boolean how ) {
		if( how == true ) {
			gc.anchor = GridBagConstraints.SOUTHEAST;
		}
		else {
			gc.anchor &= ~GridBagConstraints.SOUTHEAST;
		}
		setConstraints( comp, gc );
		return this;
	}

	/**
	 */
	public synchronized boolean getAnchorSouthEast( ) {
		return gc.anchor == GridBagConstraints.SOUTHEAST;
	}

	/**
	 *  Add anchor=SOUTHEAST to the constraints for the current
	 *  component.
	 */
	public synchronized PackAs southeast() {
		gc.anchor = GridBagConstraints.SOUTHEAST;
		setConstraints( comp, gc );
		return this;
	}

	/**
	 *  Add gridx=RELATIVE to the constraints for the current
	 *  component if how == true 0 it if false.
	 */
	public synchronized PackAs setXLeftRelative( boolean how ) {
		if( how == true ) {
			gc.gridx = GridBagConstraints.RELATIVE;
		}
		else {
			gc.gridx = 0;
		}
		setConstraints( comp, gc );
		return this;
	}

	/**
	 */
	public synchronized boolean getXLeftRelative( ) {
		return gc.gridx == GridBagConstraints.RELATIVE;
	}

	/**
	 *  Add gridx=RELATIVE to the constraints for the current
	 *  component.
	 */
	public synchronized PackAs left() {
		gc.gridx = GridBagConstraints.RELATIVE;
		setConstraints( comp, gc );
		return this;
	}

	/**
	 *  Add gridy=RELATIVE to the constraints for the current
	 *  component if how == true 0 it if false.
	 */
	public synchronized PackAs setYTopRelative( boolean how ) {
		if( how == true ) {
			gc.gridy = GridBagConstraints.RELATIVE;
		}
		else {
			gc.gridy = 0;
		}
		setConstraints( comp, gc );
		return this;
	}

	/**
	 */
	public synchronized boolean getYTopRelative( ) {
		return gc.gridy == GridBagConstraints.RELATIVE;
	}

	/**
	 *  Add gridy=RELATIVE to the constraints for the current
	 *  component.
	 */
	public synchronized PackAs top() {
		gc.gridy = GridBagConstraints.RELATIVE;
		setConstraints( comp, gc );
		return this;
	}

	/**
	 *  Add gridWidth=RELATIVE to the constraints for the current
	 *  component if how == true 1 it if false.
	 */
	public synchronized PackAs setXRightRelative( boolean how ) {
		if( how == true ) {
			gc.gridwidth = GridBagConstraints.RELATIVE;
		}
		else {
			gc.gridwidth = 1;
		}
		setConstraints( comp, gc );
		return this;
	}

	/**
	 */
	public synchronized boolean getXRightRelative( ) {
		return gc.gridwidth == GridBagConstraints.RELATIVE;
	}

	/**
	 *  Add gridwidth=RELATIVE to the constraints for the current
	 *  component.
	 */
	public synchronized PackAs right() {
		gc.gridwidth = GridBagConstraints.RELATIVE;
		setConstraints( comp, gc );
		return this;
	}

	/**
	 *  Add gridWidth=RELATIVE to the constraints for the current
	 *  component if how == true 1 it if false.
	 */
	public synchronized PackAs setYBottomRelative( boolean how ) {
		if( how == true ) {
			gc.gridheight = GridBagConstraints.RELATIVE;
		}
		else {
			gc.gridheight = 1;
		}
		setConstraints( comp, gc );
		return this;
	}

	/**
	 */
	public synchronized boolean getYBottomRelative( ) {
		return gc.gridheight == GridBagConstraints.RELATIVE;
	}

	/**
	 *  Add gridheight=RELATIVE to the constraints for the current
	 *  component.
	 */
	public synchronized PackAs bottom() {
		gc.gridheight = GridBagConstraints.RELATIVE;
		setConstraints( comp, gc );
		return this;
	}

	public synchronized PackAs grid( Point p ) {
		gridx(p.x);
		return gridy(p.y);
	}

	public synchronized PackAs grid( int x, int y ) {
		gridx(x);
		return gridy(y);
	}
	
	public synchronized Point getGrid() {
		return new Point( getGridX(), getGridY() );
	}

	/**
	 *  Add gridx=tot to the constraints for the current
	 *  component.
	 *
	 *  @param tot - the value to set gridx to.
	 */
	public synchronized PackAs gridx( int tot ) {
		gc.gridx = tot;
		setConstraints( comp, gc );
		return this;
	}
	
	public synchronized int getGridX() {
		return gc.gridx;
	}

	/**
	 *  Add gridy=tot to the constraints for the current
	 *  component.
	 *
	 *  @param tot - the value to set gridy to.
	 */
	public synchronized PackAs gridy( int tot ) {
		gc.gridy = tot;
		setConstraints( comp, gc );
		return this;
	}
	
	public synchronized int getGridY() {
		return gc.gridy;
	}

	/**
	 *  Add gridwidth=tot to the constraints for the current
	 *  component.
	 *
	 *  @param tot - the value to set gridwidth to.
	 */
	public synchronized PackAs gridw( int tot ) {
		gc.gridwidth = tot;
		setConstraints( comp, gc );
		return this;
	}
	
	public synchronized int getGridW() {
		return gc.gridwidth;
	}

	/**
	 *  Add gridheight=tot to the constraints for the current
	 *  component.
	 *
	 *  @param tot - the value to set gridheight to.
	 */
	public synchronized PackAs gridh( int tot ) {
		gc.gridheight = tot;
		setConstraints( comp, gc );
		return this;
	}
	
	public synchronized int getGridH() {
		return gc.gridheight;
	}

	/**
	 *  Add ipadx=cnt to the constraints for the current
	 *  component.
	 *
	 *  @param cnt - the value to set ipadx to.
	 */
  	public synchronized PackAs padx( int cnt ) {
		gc.ipadx = cnt;
		setConstraints( comp, gc );
		return this;
	}
	
	public synchronized int getPadX() {
		return gc.ipadx;
	}

	/**
	 *  Add ipady=cnt to the constraints for the current
	 *  component.
	 *
	 *  @param cnt - the value to set ipady to.
	 */
	public synchronized PackAs pady( int cnt ) {
		gc.ipady = cnt;
		setConstraints( comp, gc );
		return this;
	}
	
	public synchronized int getPadY() {
		return gc.ipady;
	}

	/**
	 *  Add fill=HORIZONTAL, weightx=1 to the constraints for the current
	 *  component if how == true.  fill=0, weightx=0 if how is false.
	 */
	public synchronized PackAs setFillX( boolean how ) {
		if( how == true ) {
			gc.fill = GridBagConstraints.HORIZONTAL;
			gc.weightx = 1;
		} else {
			gc.weightx = 0;
			gc.fill = 0;
		}
		setConstraints( comp, gc );
		return this;
	}
	
	public synchronized boolean getFillX() {
		return gc.fill == GridBagConstraints.HORIZONTAL;
	}

	/**
	 *  Add fill=HORIZONTAL, weightx=wtx to the constraints for the current
	 *  component.
	 */
	public synchronized PackAs fillx(double wtx) {
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.weightx = wtx;
		setConstraints( comp, gc );
		return this;
	}

	/**
	 *  Add fill=HORIZONTAL, weightx=1 to the constraints for the current
	 *  component.
	 */
	public synchronized PackAs fillx() {
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.weightx = 1;
		setConstraints( comp, gc );
		return this;
	}

	/**
	 *  Add fill=VERITCAL to the constraints for the current
	 *  component if how == true 1 it if false.
	 */
	public synchronized PackAs setFillY( boolean how ) {
		if( how == true ) {
			gc.fill = GridBagConstraints.VERTICAL;
			gc.weighty = 1;
		} else {
			gc.weighty = 0;
			gc.fill = 0;
		}
		setConstraints( comp, gc );
		return this;
	}
	
	public synchronized boolean getFillY() {
		return gc.fill == GridBagConstraints.VERTICAL;
	}

	/**
	 *  Add fill=VERTICAL, weighty=wty to the constraints for the current
	 *  component.
	 */
	public synchronized PackAs filly( double wty )  {
		gc.fill = GridBagConstraints.VERTICAL;
		gc.weighty = wty;
		setConstraints( comp, gc );
		return this;
	}

	/**
	 *  Add fill=VERTICAL, weighty=1 to the constraints for the current
	 *  component.
	 */
	public synchronized PackAs filly()  {
		gc.fill = GridBagConstraints.VERTICAL;
		gc.weighty = 1;
		setConstraints( comp, gc );
		return this;
	}

	/**
	 *  Add fill=BOTH, weightx=1, weighty=1 to the constraints for the current
	 *  component if how == true, fill=0, weightx=0, weighty=0 if it is false.
	 */
	public synchronized PackAs setFillBoth( boolean how ) {
		if( how == true ) {
			gc.fill = GridBagConstraints.BOTH;
			gc.weightx = 1;
			gc.weighty = 1;
		} else {
			gc.weightx = 0;
			gc.weighty = 0;
			gc.fill = 0;
		}
		setConstraints( comp, gc );
		return this;
	}
	
	public synchronized boolean getFillBoth() {
		return gc.fill == GridBagConstraints.BOTH;
	}

	/**
	 *  Add fill=BOTH, weighty=1, weightx=1 to the constraints for the current
	 *  component.
	 */
	public synchronized PackAs fillboth(double wtx, double wty) {
		gc.fill = GridBagConstraints.BOTH;
		gc.weightx = wtx;
		gc.weighty = wty;
		setConstraints( comp, gc );
		return this;
	}

	/**
	 *  Add fill=BOTH, weighty=1, weightx=1 to the constraints for the current
	 *  component.
	 */
	public synchronized PackAs fillboth() {
		gc.fill = GridBagConstraints.BOTH;
		gc.weightx = 1;
		gc.weighty = 1;
		setConstraints( comp, gc );
		return this;
	}

	/**
	 *  Specify the insets for the component.
	 *
	 *  @param insets the insets to apply
	 */
	public synchronized PackAs inset( Insets insets ) {
		gc.insets = insets;
		setConstraints( comp, gc );
		return this;
	}

	/**
	 *  sets top Insets on the constraints for the current
	 *  component to the value specified.
	 */
	public synchronized PackAs setInsetTop( int val ) {
		Insets i = gc.insets;
		if( i == null ) {
			i = new Insets( 0,0,0,0 );
		}
		gc.insets = new Insets( val, i.left, i.bottom, i.right );
		setConstraints( comp, gc );
		return this;
	}
	
	public synchronized int getInsetTop() {
		return gc.insets.top;
	}
	
	public synchronized int getInsetBottom() {
		return gc.insets.bottom;
	}
	
	public synchronized int getInsetLeft() {
		return gc.insets.left;
	}
	
	public synchronized int getInsetRight() {
		return gc.insets.right;
	}

	/**
	 *  sets bottom Insets on the constraints for the current
	 *  component to the value specified.
	 */
	public synchronized PackAs setInsetBottom( int val ) {
		Insets i = gc.insets;
		if( i == null ) {
			i = new Insets( 0,0,0,0 );
		}
		gc.insets = new Insets( i.top, i.left, val, i.right );
		setConstraints( comp, gc );
		return this;
	}

	/**
	 *  sets left Insets on the constraints for the current
	 *  component to the value specified.
	 */
	public synchronized PackAs setInsetLeft( int val ) {
		Insets i = gc.insets;
		if( i == null ) {
			i = new Insets( 0,0,0,0 );
		}
		gc.insets = new Insets( i.top, val, i.bottom, i.right );
		setConstraints( comp, gc );
		return this;
	}

	/**
	 *  sets right Insets on the constraints for the current
	 *  component to the value specified.
	 */
	public synchronized PackAs setInsetRight( int val ) {
		Insets i = gc.insets;
		if( i == null ) {
			i = new Insets( 0,0,0,0 );
		}
		gc.insets = new Insets( i.top, i.left, i.bottom, val );
		setConstraints( comp, gc );
		return this;
	}

	/**
	 *  Specify the insets for the component.
	 *
	 *  @param left the inset from the left.
	 *  @param top the inset from the top.
	 *  @param right the inset from the right.
	 *  @param bottom the inset from the bottom.
	 */
	public synchronized PackAs inset( int top, int left, int bottom, int right ) {
		gc.insets = new Insets( top, left, bottom, right );
		setConstraints( comp, gc );
		return this;
	}

	/**
	 *  Add weightx=wt to the constraints for the current
	 *  component.
	 *
	 *  @param wt - the value to set weightx to.
	 */
	public synchronized PackAs weightx( double wt ) {
		gc.weightx = wt;
		setConstraints( comp, gc );
		return this;
	}

	public synchronized double getWeightX() {
		return gc.weightx;
	}

	public synchronized double getWeightY() {
		return gc.weighty;
	}

	/**
	 *  Add weighty=wt to the constraints for the current
	 *  component.
	 *
	 *  @param wt - the value to set weightx to.
	 */
	public synchronized PackAs weighty( double wt ) {
		gc.weighty = wt;
		setConstraints( comp, gc );
		return this;
	}

	/**
	 *  Add gridWidth=REMAINDER to the constraints for the current
	 *  component if how == true 1 it if false.
	 */
	public synchronized PackAs setRemainX( boolean how ) {
		if( how == true ) {
			gc.gridwidth = GridBagConstraints.REMAINDER;
		}
		else {
			gc.gridwidth = 1;
		}
		setConstraints( comp, gc );
		return this;
	}

	public synchronized boolean getRemainX() {
		return gc.gridwidth == GridBagConstraints.REMAINDER;
	}

	/**
	 *  Add gridwidth=REMAINDER to the constraints for the current
	 *  component.
	 */
	public synchronized PackAs remainx() {
		gc.gridwidth = GridBagConstraints.REMAINDER;
		setConstraints( comp, gc );
		return this;
	}

	/**
	 *  Add gridWidth=REMAINDER to the constraints for the current
	 *  component if how == true 1 it if false.
	 */
	public synchronized PackAs setRemainY( boolean how ) {
		if( how == true ) {
			gc.gridheight = GridBagConstraints.REMAINDER;
		}
		else {
			gc.gridheight = 1;
		}
		setConstraints( comp, gc );
		return this;
	}

	public synchronized boolean getRemainY() {
		return gc.gridheight == GridBagConstraints.REMAINDER;
	}

	/**
	 *  Add gridheight=REMAINDER to the constraints for the current
	 *  component.
	 */
	public synchronized PackAs remainy() {
		gc.gridheight = GridBagConstraints.REMAINDER;
		setConstraints( comp, gc );
		return this;
	}
}
