package org.wonderly.swing;

import java.awt.*;
import javax.swing.*;
import java.net.*;
import java.io.*;

/**
 *  An Icon object for use on JToolBar components.
 *
 *  @author Gregg Wonderly <a href="mailto:gregg.wonderly@pobox.com">gregg.wonderly@pobox.com</a>
 */
public class ToolIcon extends ImageIcon {
	static int sz = 2;
	Icon icon;
	public ToolIcon( URL u ) {
		super(u);
		if( super.getIconWidth() > sz )
			sz = super.getIconWidth();
		if( super.getIconHeight() > sz )
			sz = super.getIconHeight();
	}
	public int getIconWidth() {
		return sz;
	}
	public int getIconHeight() {
		return sz;
	}
	public void paintIcon( Component c, Graphics g, int x, int y ) {
//			new Throwable().printStackTrace();
		int cx = x;
		int cy = y;
		int hx = super.getIconWidth();
		int hy = super.getIconHeight();
		if( hx < sz )
			cx += (sz - hx)/2;
		if( hy < sz )
			cy += (sz - hy)/2;
		super.paintIcon( c, g, cx, cy );
	}
}