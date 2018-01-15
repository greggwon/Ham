package org.wonderly.net.vhttp;

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
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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

			public @Override void windowClosing(WindowEvent ev) {
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
	private volatile boolean shutdown;
	public void shutdown() {
		Handler.removeDownloadListener( this );
		shutdown = true;
		if( dlg != null )
			dlg.dispose();
		dlg = null;
		if( toFut != null )
			toFut.cancel(true);
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
		if( shutdown )
			return;
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
//				Logger log = Logger.getLogger(Handler.class.getName());
				log.log(Level.SEVERE, ex.toString(), ex);
			}
		}
		PackLocation pl = getPackLocation( url, fileLength );
		opened.incrementAndGet();
		showDialog();
		dlgpk.pack( pl ).gridx(0).gridy( opened.get() + 1 ).fillx();
		map.put(url, pl );//new DefaultDownloadListener(url, fileLength, mod ));
		dlg.pack();
	}

	private void showDialog() {
		if( dlg != null ) {
			dlg.toFront();
			return;
		}
		log.info( "creating initial dialog parent="+parent );
		dlg = new JDialog( parent, "Downloading", mod );
		dlg.addWindowListener(new WindowAdapter() {
			public @Override void windowClosing(WindowEvent ev) {
				synchronized( toLock ) {
					dlg.dispose();
					dlg = null;
					if( toFut != null )
						toFut.cancel(true);
					toFut = null;
				}
			}
		});
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
	 * else.  Also overriding {@link #checking(java.net.URL)} and {@link #checkComplete(java.net.URL,boolean,Throwable)}
	 * will let you see when the classloading subsystem is causing downloads to be requested and the cache
	 * is being checked.
	 * @param pk a Packer instance to use to fill in the default pane.  it can be ignored
	 * and another container/window used to display the status.
	 */
	protected void packCheckLabel( Packer pk ) {
		pk.pack( txtlab ).gridx(0).gridy(0).inset(3,3,3,3);
		pk.pack( dlglab ).gridx(1).gridy(0).fillx();
	}
	private JLabel txtlab = new JLabel("Checking Downloads: ");

	private final Object toLock = new Object();
	private void cancelTimeout() {
		log.info("cancelling timeout");
		ScheduledFuture ft = null;
		synchronized( toLock ) {
			if( toFut != null ) {
				toFut.cancel(true);
				ft = toFut;
				toFut = null;
			}
		}
		try {
			if( ft != null )
				ft.get();
		} catch (InterruptedException ex) {
			log.log(Level.SEVERE, ex.toString(), ex);
		} catch (CancellationException ex) {
			log.log(Level.FINE, ex.toString(), ex);
		} catch (ExecutionException ex) {
			log.log(Level.SEVERE, ex.toString(), ex);
		}
		try {
			SwingUtilities.invokeAndWait( new Runnable() {
				public void run() {
					txtlab.setText("Checking Downloads:");
					dlglab.setText(""+checkCount);
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			// TODO Auto-generated catch block
			log.log(Level.SEVERE, e.toString(), e);
		}
	}
	int timeout = 20;
	private void startTimeout() {
		synchronized( toLock ) {
			if( toFut != null ) {
				toFut.cancel(true);
				try {
					toFut.get();
				} catch (InterruptedException ex) {
					log.log(Level.SEVERE, ex.toString(), ex);
				} catch (ExecutionException ex) {
					log.log(Level.SEVERE, ex.toString(), ex);
				}
			}
			timeout = 20;
			toFut = exec.scheduleAtFixedRate( new Runnable() {
				public void run() {
					if( --timeout >= 0 ) {
						try {
							SwingUtilities.invokeAndWait( new Runnable() {
								public void run() {
									txtlab.setText("Closing in ");
									dlglab.setText(""+timeout);
								}
							});
						} catch (InvocationTargetException | InterruptedException e) {
							// TODO Auto-generated catch block
							log.log(Level.SEVERE, e.toString(), e);
						}
						if( timeout > 0)
							return;
					}
					synchronized( toLock ) {
						JDialog tdlg = dlg;
						dlg = null;
						if( toFut.isCancelled() == false ) {
							if( tdlg != null )
								tdlg.dispose();
						}
						toFut.cancel( true );
					}
				}
			}, 1, 1, TimeUnit.SECONDS );
		}
	}
	ScheduledFuture toFut;
	private static class PackLocation extends JPanel {
		private JLabel lblProg;
		private JLabel lblInfo;
		private JProgressBar bar;
		private URL url;
		private long len;

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
			lblProg = new JLabel(len+" pending of "+len, JLabel.CENTER),
			lblInfo = new JLabel( "Downloading " +
				(url.getHost().length() == 0 ?
				"to "+url.getFile() : 
				"from "+url.getHost()), JLabel.CENTER ) );
		pl.setMinimumSize( pl.getPreferredSize() );
		return pl;
	}

	private void init(final URL url, final long fileLength, final ModalityType mod ) {
		new SyncThread() {
			public @Override void setup() {
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

	public @Override void downloadProgress(URL url, final long progress) {
		if( shutdown )
			return;
		final PackLocation defl = map.get(url);
		if (defl == null) {
			return;
		}
		new SyncThread() {

			public @Override void setup() {
				defl.bar.setValue((int) progress);
				long max = defl.bar.getMaximum();
				int pct = (int) ((progress * 100) / max);
				defl.lblProg.setText( inKilos(progress) + " of " + inKilos(max) /*+ " - " +pct + "%"*/);
			}
		}.block();
	}

	private String inKilos( long val ) {
		if( val > 1024 )
			return val+"";

		if( val < 100*1024*1024 ) {
			return (int)(val/1024.0)+"KB";
		}

		return (long)(val/(1024.0*1024))+"MB";
	}

	public @Override void downloadFinished(URL url) {
		if( shutdown )
			return;

		checkCount.decrementAndGet();
		if( checkCount.get() == 0 ) {
			doTimeoutStart();
		}
		final PackLocation defl = map.remove(url);
		new SyncThread() {

			public @Override void setup() {
				if (defl != null && dlg != null) {
					dlgpan.remove( defl );
					//dlg.pack();
				}
			}
		}.block();
	}

	private final AtomicInteger checkCount = new AtomicInteger();
	private final JLabel dlglab = new JLabel();
	private volatile JPanel dlgpan;

	public synchronized void checking(URL url) {
		if( shutdown )
			return;
		if (log.isLoggable(Level.FINE))
			log.fine("checking url: "+url);
		if( checkCount.getAndIncrement() == 0 )
			cancelTimeout();

		setCntLabel();
	}

	public @Override synchronized void checkComplete(URL url, boolean willDownload, Throwable th ) {
		if( shutdown )
			return;

		if (log.isLoggable(Level.FINER))
			log.log(Level.FINER,"finished checking: "+url, th );
		else
			log.info("finished checking: "+url );
		if( !willDownload )
			checkCount.decrementAndGet();
		if( checkCount.get() == 0 ) {
			doTimeoutStart();
		}
//		showDialog();
		setCntLabel();
	}

	private void doTimeoutStart() {
		try {
			SwingUtilities.invokeAndWait( new Runnable() {
					public void run() {
						if( dlg != null )
							dlg.pack();
					}
				});
		} catch (InvocationTargetException ex) {
			// TODO Auto-generated catch block
			log.log(Level.SEVERE,  ex.toString(),  ex);
		} catch (InterruptedException ex) {
			// TODO Auto-generated catch block
			log.log(Level.SEVERE,  ex.toString(),  ex);
		}
		startTimeout();
	}
	private final ScheduledThreadPoolExecutor exec = 
			new ScheduledThreadPoolExecutor(1,new org.wonderly.util.NamedThreadFactory("DownloadDialogTimeoutThread",true));

	protected void setCntLabel() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					dlglab.setText("" + checkCount);
				}
			});
		} catch (InterruptedException ex) {
			log.log(Level.SEVERE, ex.toString(), ex);
		} catch (InvocationTargetException ex) {
			log.log(Level.SEVERE, ex.toString(), ex);
		}
	}
}
