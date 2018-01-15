package org.wonderly.swing;

import java.awt.*;
import org.wonderly.awt.*;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;

/**
 *  This class is a convience class that provides the facilities of
 *  disabling and then enabling a component with the SwingWorkers
 *  <code>construct()</code> method called inbetween.  You can override
 *  <code>setup()</code> and <code>finished()</code> methods to do
 *  before and after work, just remember to call <code>super.setup()</code>
 *  and/or <code>super.finished()</code> from your override so that the
 *  enable/disable and cursor setup can be done.
 *
 *  There are a wide range of constructors provided here to try and make
 *  it easy to use this class with Action, Component and JComponent.  In
 *  most applications, there is not a mixture of these components when it
 *  comes time to manipulate a group of things.  However, there can be.
 *  The <code>setup()</code> override lets you handle this by adding any
 *  additional <code>setEnabled(false)</code> calls that you need.  The
 *  <code>finish()</code> override will let you reverse those actions and
 *  perform any other important configuration.  In particular, when you are
 *  reloading a <code>JList</code>, you may also have some list manipulation
 *  buttons that are disabled when there is not anything selected.  Here
 *  is some example code to use as a template for such.
 *<pre>
 	JList list = ...
 	Action add, del, edit;
 	new ComponentUpdateThread( new Action[] { add, del, edit } ) {
 		public void setup() {
 			super.setup();
 			list.setEnabled(false);
 			list.clearSelection();
 		}
 		public Object construct() {
 			try {
 				Vector v = remote.getData();
 				Collections.sort( v );
 				return v;
 			} catch( Exception ex ) {
 				reportException(ex);
 			}
 			return null;
 		}
 		public void finished() {
 			try {
 				Vector v = (Vector)getValue();
 				list.setListData(v);
 			} finally {
 				super.finished();
 				list.setEnabled(true);
 				edit.setEnabled(false);
 				del.setEnaled(false);
 			}
 		}
 	}.start();
 *</pre>
 *
 *  @author <a href="mailto:gregg.wonderly@pobox.com">Gregg Wonderly</a>.
 */
public abstract class ComponentUpdateThread extends SwingWorker {
	private static Cursor defWaitCrs = new Cursor( Cursor.WAIT_CURSOR );
	private static Cursor defDefCrs = new Cursor( Cursor.DEFAULT_CURSOR );
	private Component comp[];
	private Action act[];
	private Cursor waitCrs = defWaitCrs;
	private Cursor defCrs = defDefCrs;
	private String setupTip, finishTip;
	private boolean enable;
	private boolean setupSet, finishedSet;
	private JDialog dlg;

	/**
	 *  Set the value of the wait cursor
	 */
	public void setWaitCursor( Cursor crs ) {
		waitCrs = crs;
	}

	/**
	 *  Set the value of the final/default cursor to leave active
	 */
	public void setDefaultCursor( Cursor crs ) {
		defCrs = crs;
	}

	/**
	 *  Get the current value of the wait Cursor
	 */
	public Cursor getWaitCursor() {
		return waitCrs;
	}

	/**
	 *  Set default enable state for component manipulation
	 */
	public void setEnabled( boolean how ) {
		enable = how;
	}
	
	/**
	 *  Get current default enable state for component manipulation
	 */
	public boolean getEnabled() {
		return enable;
	}

	/**
	 *  Get the current value of the final/default cursor.
	 */
	public Cursor getDefaultCursor() {
		return defCrs;
	}

	/**
	 *  Set the tooltip text to set on the components at setup time
	 */
	public void setSetupToolTipText( String str ) {
		setupSet = true;
		setupTip = str;
	}

	/**
	 *  Get the setup tool tip text
	 */
	public String getSetupToolTipText() {
		return setupTip;
	}

	/**
	 *  Set the tooltip text to set on the components at finished time
	 */
	public void setFinishedToolTipText( String str ) {
		finishedSet = true;
		finishTip = str;
	}

//	public static void main( String args[] ) {
//		JFrame f = new JFrame( "testFrame");
//		Packer pk = new Packer(f.getContentPane());
//		JPanel p = new JPanel();
//		Packer ppk = new Packer( p );
//		final JPanel q = new JPanel();
//		Packer qpk = new Packer( q );
//		qpk.pack( p ).gridx(0).gridy(0).gridh(3).fillboth();
//		final JList lst = new JList(new DefaultListModel());
//		ppk.pack( new JScrollPane(lst) ).filly().gridx(1).gridy(0);
//		for(int i =0;i < 20;++i )
//			((DefaultListModel)lst.getModel()).addElement("entry "+i );
//		final JDesktopPane dp = new JDesktopPane();
//		ppk.pack( dp ).fillboth().gridx(0).gridy(0);
//		dp.add( new JInternalFrame() );
//		new Thread() {
//			public void run() {
//				JOptionPane.showInternalConfirmDialog( dp, "Ready to continue");
//			}
//		}.start();
//		qpk.pack( new JButton("Yes") ).gridx(1).gridy(0).weightx(0).fillx();
//		qpk.pack( new JButton("No") ).gridx(1).gridy(1).weightx(0).fillx();
//		qpk.pack( new JButton("Cancel") ).gridx(1).gridy(2).weightx(0).fillx();
//		pk.pack( q ) .gridx(1).gridy(0).gridh(3).fillboth();
//		final JButton up = new JButton("Enable");
//		final JButton down = new JButton("Disable");
//		pk.pack( up ).gridx(0).gridy(0);
//		pk.pack( down ).gridx(0).gridy(1);
//		
//		up.addActionListener( new ActionListener() {
//			public void actionPerformed(ActionEvent ev) {
//				setCompEnabled( q, true, defDefCrs, "Now enabled" );
//			}
//		});
//		
//		down.addActionListener( new ActionListener() {
//			public void actionPerformed(ActionEvent ev) {
//				setCompEnabled( q, false, defWaitCrs, "Now disabled" );
//			}
//		});
//		f.pack();
//		f.setVisible(true);
//	}
	
	public static void main( String args[] ) {
		new ComponentUpdateThread( (JFrame)null ) {
			public Object construct() {
				try {
					Thread.sleep(40000);
				} catch( Exception ex ) {
				}
				return null;
			}
		}.start();
	}

	/**
	 *  Get the finished tool tip text
	 */
	public String getFinishedToolTipText() {
		return finishTip;
	}

	/**
	 *  This method sets the passed component tree to be either
	 *  disabled with wait cursors, or enabled with the default
	 *  cursor.
	 *
	 *  @param c component to manipulate (including all children)
	 *  @param how how to enable the component
	 *  @param doEnable whether to do setEnable(how)
	 *  @param cursor cursor to set on component, null to skip changing cursor
	 *  @param setTip if true set value of 'tip' as tooltip, if false, to set a tooltip.
	 *  @param tip tool tip to set on component, null to skip setting tooltip.
	 */
	public static void setCompEnabled( Component c, boolean how, boolean doEnable, Cursor cursor, boolean setTip, String tip ) {
		// If no component, just return
		if( c == null )
			return;
		if( doEnable )
			c.setEnabled( how );
		if( cursor != null )
			c.setCursor( cursor );
		if( setTip != false && c instanceof JComponent )
			((JComponent)c).setToolTipText( tip );
		c.repaint();
		if( c instanceof Container && c != null ) {
			Component arr[] = ((Container)c).getComponents();
			for( int i = 0; i < arr.length; ++i ) {
				try {
					if( arr[i] instanceof Component && arr[i] != null )
						setCompEnabled( (Component) arr[i], how, doEnable, cursor, setTip, tip );
				} catch( Exception ex ) {
					ex.printStackTrace();
				}
			}
		}
	}

	/**
	 *  This method sets the passed component tree to be either
	 *  disabled with wait cursors, or enabled with the default
	 *  cursor.
	 *
	 *  @param c component to manipulate (including all children)
	 *  @param how how to enable the component
	 *  @param doEnable whether to do setEnable(how)
	 */
	public static void setActionEnabled( Action c, boolean how, boolean doEnable ) {
		// If no Action, just return
		if( c == null )
			return;
		if( doEnable )
			c.setEnabled( how );
	}

	/**
	 *  Creates an instance that will show a working dialog parented
	 *  to the passed frame.
	 */
	public ComponentUpdateThread( JFrame frm ) {
		showDialog( frm );
	}
	private Component frame;
	private String title = "Working...";
	private String msg = "Operation in Progress";
	
	/**
	 *  Creates an instance that will show a working dialog parented
	 *  to the passed dialog.  Typically this is used when the whole
	 *  dialog needs to be inaccessible while work is performed.
	 *  @see SimpleProgress
	 */
	public ComponentUpdateThread( JDialog dlg ) {
		showDialog( dlg );
	}

	/**
	 *  Shows a dialog that disables access to the indicated frame
	 */
	public void showDialog( JFrame frm ) {
		frame = frm;
		openDialog(true);
	}
	
	/**
	 *  Shows a dialog that disables access to the indicated dialog
	 */
	public void showDialog( JDialog dlg ) {
		frame = dlg;
		openDialog(true);
	}
	

	/**
	 *  @param block passed to JDialog contructor to specify if it is a blocking dialog
	 */
	public void showDialog( JFrame frm, boolean block ) {
		frame = frm;
		openDialog(block);
	}
	
	/**
	 *  @param block passed to JDialog contructor to specify if it is a blocking dialog
	 */
	public void showDialog( JDialog dlg, boolean block ) {
		frame = dlg;
		openDialog(block);
	}
	
	/**
	 *  @param block passed to JDialog contructor to specify if it is a blocking dialog
	 */
	protected void openDialog(boolean block) {
		if( frame instanceof JDialog )
			dlg = new JDialog( (JDialog)frame, title, block );
		else
			dlg = new JDialog( (JFrame)frame, title, block );
		Packer pk = new Packer( dlg.getContentPane() );
		JLabel il = new JLabel();
		Icon progIcon = new ProgressIcon(il);
		il.setIcon( progIcon );
		il.setText( msg );
		pk.pack( il ).gridx(0).gridy(0).inset(4,4,4,4);
//		pk.pack( new JLabel( msg ) ).gridx(1).gridy(0).inset(40,40,40,40);
		dlg.pack();
		dlg.setLocationRelativeTo( frame );
		new Thread() {
			public void run() {
				dlg.setVisible(true);
				dlg.dispose();
			}
		}.start();
	}
	
	private static Timer timer;
	private static class ProgressIcon implements Icon {
		int wh = 0;
		int inc = 2;
		double rad = 0;
		TimerTask task;
		JLabel comp;
		Object del = new Object() {
			protected void finalize() {
				task.cancel();
			}
		};
		public ProgressIcon(final JLabel comp) {
			this( comp, 2 );
		}
		public ProgressIcon(final JLabel comp, int inc) {
			this.comp = comp;
			if( timer == null ) {
				timer = new Timer();
			}
			task = new TimerTask() {
				public void run() {
					try {
//						System.out.println("Timer fired" );
						SwingUtilities.invokeAndWait( new Runnable() {
							public void run() {
								comp.paintImmediately( comp.getBounds() );
							}
						});
					} catch( Exception ex ) {
						ex.printStackTrace();
					}
				}
			};
//			System.out.println("Scheduling painter...");
			timer.schedule( task, 0, 250 );
		}
		public void paintIcon( Component cmp, Graphics g, int x, int y ) {
//			System.out.println( "Painting icon ("+wh+" > "+inc+") ..."+x+","+y );
			Color c = new Color( (wh*3)+30, (wh*4)+30, (wh*7)+100 );
			int iw = getIconWidth();
			int ih = getIconHeight();
			g.setColor( comp.getBackground() );
			g.fillRect( x, y, iw, ih );
			g.setColor(c);
			((Graphics2D)g).setStroke( new BasicStroke(2) );
			for( int i = 0; i < 12; ++i ) {
				double drad = ((i*(360/12)) * 2 * Math.PI)/360.0;
				double rx = Math.cos( drad );
				double ry = Math.sin( drad );
				double in = .7*(iw/2.0);
				double out = .95*(iw/2.0);
				((Graphics2D)g).draw(new java.awt.geom.Line2D.Double(
					(double)x+(iw/2.0)+(rx*in), 
					(double)y+(ih/2.0)+(ry*in),
					(x+(iw/2.0)+(rx*out)), 
					(y+(ih/2.0)+(ry*out)) ));
			}

			rad += ((360/12) * 2 * Math.PI)/360.0;
			double rx = Math.cos(rad);
			double ry = Math.sin(rad);
			rx *= (iw/2.0)-1;
			ry *= (ih/2.0)-1;
//			g.fillOval(
//				(int)(x+(iw/2)+rx)-iw/8, 
//				(int)(y+(ih/2)+ry)-ih/8,
//				iw/2, ih/2 );
			((Graphics2D)g).draw( new java.awt.geom.Ellipse2D.Double(
				x,//+(iw/2), 
				y,//+(ih/2),
				iw-1, ih-1 ) );
			((Graphics2D)g).setStroke(new BasicStroke(1));
			((Graphics2D)g).draw(new java.awt.geom.Line2D.Double(
				x+(iw/2.0), y+(ih/2.0),
				(x+(iw/2.0)+rx), (y+(ih/2.0)+ry)
					));
			wh += inc;
			if( wh < 0 || wh > 12 ) {
				inc = -inc;
				wh += inc;
				//rad = 0;
			}			
		}
		public int getIconWidth() {
			return 32;
		}
		public int getIconHeight() {
			return 32;
		}
	}

	public ComponentUpdateThread( Action upd ) {
		this( new Action[] { upd } );
	}
	
	public ComponentUpdateThread( Action upd[] ) {
		act = upd;
		setEnabled(false);
	}
	
	public ComponentUpdateThread( Component upd ) {
		this( new Component[] { upd } );
	}
	
	public ComponentUpdateThread( Component upd[] ) {
		comp = upd;
		setEnabled(false);
	}
	
	public ComponentUpdateThread( Action upd, Cursor waitCursor, Cursor defaultCursor ) {
		this(new Action[] { upd });
		setWaitCursor( waitCursor );
		setDefaultCursor( defaultCursor );
	}
	
	public ComponentUpdateThread( Action upd, Cursor waitCursor, Cursor defaultCursor, String setupTip, String finishTip ) {
		this(upd, waitCursor, defaultCursor);
		setSetupToolTipText( setupTip );
		setFinishedToolTipText( finishTip );
	}
	
	public ComponentUpdateThread( JComponent upd, Cursor waitCursor, Cursor defaultCursor ) {
		this(new Component[] { upd });
		setWaitCursor( waitCursor );
		setDefaultCursor( defaultCursor );
	}
	
	public ComponentUpdateThread( JComponent upd, Cursor waitCursor, Cursor defaultCursor, String setupTip, String finishTip ) {
		this(upd, waitCursor, defaultCursor);
		setSetupToolTipText( setupTip );
		setFinishedToolTipText( finishTip );
	}
	
	/** 
	 *  Default setup implementation, override this and call this one if you
	 *  have setup work to do.
	 *  e.g.
	 *  <pre>
	 *     public void setup() {
	 *         super.setup();
	 *         ...your setup work goes here...
	 *     }
	 *  </pre>
	 */
	public void setup() {
		for( int i = 0; comp != null && i < comp.length; ++i ) {
			setCompEnabled( comp[i], false, !getEnabled(), waitCrs, setupSet, setupTip  );
		}
		for( int i = 0; act != null && i < act.length; ++i ) {
			setActionEnabled( act[i], false, !getEnabled()  );
		}
	}
	
	/** 
	 *  Default finished implementation, override this and call this one if you
	 *  have finished work to do.
	 *  e.g.
	 *  <pre>
	 *     public void finished() {
	 *         ...your finish up work goes here...
	 *         super.finished();
	 *     }
	 *  </pre>
	 */
	public void finished() {
		for( int i = 0; comp != null && i < comp.length; ++i ) {
			setCompEnabled( comp[i], true, !getEnabled(), defCrs, finishedSet, finishTip );
		}
		for( int i = 0; act != null && i < act.length; ++i ) {
			setActionEnabled( act[i], true, !getEnabled() );
		}
		if( dlg != null ) {
			dlg.setVisible(false);
			dlg.dispose();
			dlg = null;
		}
	}
}
		