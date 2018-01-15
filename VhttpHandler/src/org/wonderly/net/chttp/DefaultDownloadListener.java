package org.wonderly.net.chttp;

import java.awt.Dialog.ModalityType;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import org.wonderly.awt.Packer;
import org.wonderly.swing.SyncThread;

/**
 * This is a default {@link DownloadListener} implementation which provides a
 * popup dialog indicating the progress of a downloading file.   The constructor
 * takes, as its argument, the {@link Window} to parent dialogs onto.
 *
 * @author gregg
 */
public class DefaultDownloadListener implements DownloadListener {

	private static volatile JDialog dlg = null;
	private static volatile Packer dlgpk;
	volatile Window parent = null;
	private volatile JProgressBar bar = null;
	private volatile JLabel lblProg = null;
	private volatile JLabel lblInfo = null;
	private final URL url;
	private final long len;
	private volatile int x;
	private volatile int y;
	private final ConcurrentHashMap<URL, PackLocation> map =
		new ConcurrentHashMap<URL, PackLocation>();
	private static volatile int off = 0;
	private static volatile int xoff = 0;
	private final ModalityType mod;
	private final Logger log = Logger.getLogger(getClass().getName());

	public static void main( String args[] ) {
		Logger log = Logger.getLogger( DefaultDownloadListener.class.getName() );
		JFrame f = new JFrame("Download testing");
		f.setSize(300,300);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
		f.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent ev) {
				System.exit(0);
			}
		});
		DefaultDownloadListener dl = new DefaultDownloadListener( f );
		List<URL>ls = new ArrayList<URL>();
		List<Long>lc = new ArrayList<Long>();
		List<Long>la = new ArrayList<Long>();
		for( int i = 0; i < 10; ++i ) {
			try {
				URL u = new URL("file:/a" + i);
				ls.add( u );
				lc.add( (long)(Math.random() * 10000000L ));
				la.add( 0L );
			} catch (MalformedURLException ex) {
				log.log(Level.SEVERE, ex.toString(), ex);
			}
		}
		log.info("now have "+ls.size()+" entries defined" );
		while( ls.size() > 0 ) {
			int idx = (int)(ls.size()*Math.random());
			URL u = ls.get(idx);
			long l = lc.get(idx);
			long a = la.get(idx);
			long v = (long)(Math.random()*102400);
			if( a == 0 ) {
				dl.downloadStarted( u, l );
				if (log.isLoggable(Level.FINE))
					log.fine("starting: "+u+" for "+l);
			}
			{
				if( l >= v ) {
					l -= v;
				} else {
					v = l;
					l = 0;
				}
				a += v;
				la.set( idx, a );
				lc.set(idx,l);
				dl.downloadProgress( u, a);
				if (log.isLoggable(Level.FINE))
					log.fine("progressed "+u+" for "+v+", left="+l);
				if( l == 0 ) {
					dl.downloadFinished( u );
					if (log.isLoggable(Level.FINE))
						log.fine("finished "+u );
					ls.remove(idx);
					lc.remove(idx);
				}
			}
			try {
				if (log.isLoggable(Level.FINER))
					log.finer("wait till next time");
				Thread.sleep(10 + (long) (30*Math.random()));
			} catch (InterruptedException ex) {
				log.log(Level.SEVERE, ex.toString(), ex);
			}
		}
		dlg.dispose();
	}
	/**
	 * Create a listener with the {@link java.awt.Dialog.ModalityType#MODELESS}
	 * modality selected.
	 * @param parent The parent window any download dialogs should be parented to.
	 */
	public DefaultDownloadListener(final Window parent ) {
		this( parent, ModalityType.MODELESS );
	}
	/**
	 * Create a listener with the indicated modality type applied to
	 * created dialogs
	 * @param parent The parent window any download dialogs should be parented to.
	 * @param mod the modality type to apply to created download dialogs.
	 */
	public DefaultDownloadListener(final Window parent, ModalityType mod ) {
		this.parent = parent;
		this.mod = mod;
		this.url = null;
		this.len = 0;
	}
	public void shutdown() {
		if( dlg != null )
			dlg.dispose();
		dlg = null;
	}
	private DefaultDownloadListener(URL url, long len, ModalityType mod ) {
		this.url = url;
		this.len = len;
		this.mod = mod;
		synchronized (DefaultDownloadListener.class) {
			x = off + xoff;
			y = off;
			off += 30;
			if (off > 300) {
				off = 0;
				xoff += 10;
				if (xoff > 100) {
					xoff = 0;
				}
			}
		}
		init(url, len, mod);
	}

	public synchronized void downloadStarted(final URL url, final long fileLength) {
		if (map.get(url) != null) {
			final PackLocation dl = map.remove(url);
			try {
				SwingUtilities.invokeAndWait( new Runnable() {
					public void run() {
						dlgpan.remove(dl);
						dlg.pack();
					}
				});
			} catch (Exception ex) {
				Logger log = Logger.getLogger(Handler.class.getName());
				log.log(Level.SEVERE, ex.toString(), ex);
			}
		}
		PackLocation pl = getPackLocation( url, fileLength );
		opened.incrementAndGet();
		showDialog();
		dlgpk.pack( pl ).gridx(0).gridy( opened.get() + 1 ).fillx();
		dlg.pack();
		map.put(url, pl );//new DefaultDownloadListener(url, fileLength, mod ));
	}

	private void showDialog() {
		if( dlg != null ) {
			dlg.toFront();
			return;
		}
		log.info( "creating initial dialog parent="+parent );
		dlg = new JDialog( parent, "Downloading", mod );
		dlg.setDefaultCloseOperation( JDialog.DO_NOTHING_ON_CLOSE );
		dlgpan = new JPanel();
		dlg.setContentPane( new JScrollPane( dlgpan ) );
		dlgpk = new Packer( dlgpan);
		dlg.setVisible(true);
		JPanel p = new JPanel();
		Packer pk = new Packer( p );
		dlg.setLocationRelativeTo(parent);
		packCheckLabel(pk);
		dlgpk.pack( p ).gridx(0).gridy(0).fillx();
		dlg.pack();
	}
	/**
	 * Subclasses can override this and make the "checking downloads" display into something
	 * else.  Also overriding {@link #checking(java.net.URL)} and {@link #checkComplete(java.net.URL, boolean, Throwable)}
	 * will let you see when the classloading subsystem is causing downloads to be requested and the cache
	 * is being checked.
	 * @param pk a Packer instance to use to fill in the default pane.  it can be ignored
	 * and another container/window used to display the status.
	 */
	protected void packCheckLabel( Packer pk ) {
		pk.pack( new JLabel("Checking Downloads: ") ).gridx(0).gridy(0).inset(3,3,3,3);
		pk.pack( dlglab ).gridx(1).gridy(0).fillx();
	}
	private static class PackLocation extends JPanel {
		JLabel lblProg;
		JLabel lblInfo;
		JProgressBar bar;
		URL url;
		long len;
		public PackLocation( URL url, long len, JProgressBar bar, JLabel prog, JLabel info ) {
			setBorder( BorderFactory.createEtchedBorder());
			this.bar = bar;
			Font f = new Font( "serif", Font.PLAIN, 10 );
			bar.setFont(f);
			this.lblProg = prog;
			this.lblInfo = info;
			lblProg.setToolTipText( url.toString() );
			lblInfo.setToolTipText( url.toString() );
			bar.setToolTipText( url.toString() );
			setToolTipText( url.toString() );
			lblProg.setFont(f);
			lblInfo.setFont(f);
			this.url = url;
			this.len = len;
			Packer pk = new Packer( this );
			int y = openedCount();
			pk.pack(lblProg).fillx().gridx(1).gridy(0).inset(2, 3, 2, 3);
			lblProg.setBorder(BorderFactory.createEtchedBorder());
			pk.pack(bar).fillx().gridx(0).gridy(0).inset(2,3, 2, 3);
			pk.pack(lblInfo).gridx(0).gridy(1).gridw(2).fillx().inset(2, 3, 2, 3);
			lblInfo.setBorder(BorderFactory.createEtchedBorder());
			bar.setMinimum(1);
			bar.setMaximum((int) len );
			bar.setValue(1);
			bar.setStringPainted(true);
		}
	}

	private static AtomicInteger opened = new AtomicInteger();
	private static int openedCount() {
		return opened.get();
	}

	private synchronized PackLocation getPackLocation( URL url, long len ) {
		PackLocation pl = new PackLocation( url, len,
			bar = new JProgressBar(0, 0, 1000),
			lblProg = new JLabel(""+len, JLabel.CENTER),
			lblInfo = new JLabel( "Downloading " +
				(url.getHost().length() == 0 ?
				"to "+url.getFile() : 
				"from "+url.getHost()), JLabel.CENTER ) );

		return pl;
	}

	private void init(final URL url, final long fileLength, final ModalityType mod ) {
		new SyncThread() {
			public void setup() {
				bar = new JProgressBar(0, 0, 1000);
				lblProg = new JLabel("0 of 0 - 0%", JLabel.CENTER);
				lblInfo = new JLabel("Downloading from " + url.getHost(), JLabel.CENTER);
				dlg = new JDialog( parent, "Downloading " + url.getFile(), mod );
				dlg.setAlwaysOnTop(true);
				Packer pk = new Packer(dlg.getContentPane());
				pk.pack(lblProg).fillx().gridx(0).gridy(0).inset(5, 5, 5, 5);
				lblProg.setBorder(BorderFactory.createEtchedBorder());
				pk.pack(bar).fillx().gridx(0).gridy(1).inset(5, 5, 5, 5);
				pk.pack(lblInfo).gridx(0).gridy(2).fillx().inset(5, 5, 5, 5);
				lblInfo.setBorder(BorderFactory.createEtchedBorder());
				bar.setMinimum(1);
				bar.setMaximum((int) fileLength);
				bar.setValue(1);
				bar.setStringPainted(true);
				dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
				dlg.pack();
				dlg.setLocationRelativeTo(parent);
				dlg.setLocation(dlg.getLocation().x + x, dlg.getLocation().y + y);
				dlg.setVisible(true);
			}
		}.block();
	}

	public void downloadProgress(URL url, final long progress) {
		final PackLocation defl = map.get(url);
		if (defl == null) {
			return;
		}
		new SyncThread() {

			public void setup() {
				defl.bar.setValue((int) progress);
				long max = defl.bar.getMaximum();
				int pct = (int) ((progress * 100) / max);
				defl.lblProg.setText( progress + " of " + max /*+ " - " +pct + "%"*/);
			}
		}.block();
	}

	public void downloadFinished(URL url) {
		final PackLocation defl = map.remove(url);
		new SyncThread() {

			public void setup() {
				if (defl != null && dlg != null) {
					dlgpan.remove( defl );
					dlg.pack();
				}
			}
		}.block();
	}
	int dlgcnt;
	JLabel dlglab = new JLabel();
	private JPanel dlgpan;
	public synchronized void checking(URL url) {
		log.info("checking url: "+url);
		dlgcnt++;
//		showDialog();
		setCntLabel();
	}

	public synchronized void checkComplete(URL url, boolean willDownload, Throwable th ) {

		if (log.isLoggable(Level.FINER))
			log.log(Level.FINER,"finished checking: "+url, th );
		else
			log.info("finished checking: "+url );
		dlgcnt--;
//		showDialog();
		setCntLabel();
	}
	protected void setCntLabel() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					dlglab.setText("" + dlgcnt);
				}
			});
		} catch (InterruptedException ex) {
			log.log(Level.SEVERE, ex.toString(), ex);
		} catch (InvocationTargetException ex) {
			log.log(Level.SEVERE, ex.toString(), ex);
		}
	}
}
