/*
 * RadarPane.java
 *
 * Created on July 31, 2006, 10:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.wonderly.ham.noaa;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.ImageObserver;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.wonderly.awt.Packer;
import org.wonderly.swing.ComponentUpdateThread;
import org.wonderly.swing.SimpleProgress;

public class RadarPane {
	RadarImage map;
	String products[] = {"N0R","NCR","N0S","N0V","N1P","NTP"};
	String productDesc[] = new String[] { "Short Range Base", 
		"Short Range Composite",
		"Velocity: Storm Relative",
		"Velocity: Base",
		"Rain Fall: 1 HR",
		"Rain Fall: Total" };
	volatile String product = "N0R";
	String which = "Short";
	private static final Logger log = Logger.getLogger(RadarPane.class.getName() );
	JLabel slab;
	String defproto = "https";

	String getProto() {
		return showdl != null ? showdl.isSelected() ? "vhttp" : "https" : defproto;
	}

	public String getTopo( String site ) {
		return getTopoPath( which )+"/"+site+"_Topo_"+which+".jpg";
	}
	
	public String getSitePrefix() {
		return getProto()+"://radar.weather.gov/ridge";
	}

	public String getTopoPath( String which ) {
		return getSitePrefix()+"/Overlays/Topo/"+which;
	}

	public String getRadar( String site ) {
		return getRadarPath(product)+"/"+site+"_"+product+"_0.gif";
	}

	public String getRadarPath( String product ) {
		return getSitePrefix()+"/RadarImg/"+product;
	}

	public String getLegend( String site ) {
		return getLegendPath( product ) + "/" + site + "_" + product + "_Legend_0.gif";
	}

	public String getLegendPath( String product ) {
		return getSitePrefix()+"/Legend/" + product;
	}

	public String getAnims( String site ) {
		return getSitePrefix()+"/"+
			site.toUpperCase()+"_"+product+"_overlayfiles.txt";
	}

	public String getCounty( String site ) {
		return getCountyPath(which)+"/"+site+"_County_"+which+".gif";
	}

	public String getCountyPath( String which ) {
		return getSitePrefix()+"/Overlays/County/"+which;
	}

	public String getRivers( String site ) {
		return getRiversPath( which )+"/"+site+"_Rivers_"+which+".gif";
	}

	public String getRiversPath( String which ) {
		return getSitePrefix()+"/Overlays/Rivers/"+which;
	}
	
	public String getHighways( String site ) {
		return getHighwaysPath(	which )+"/"+site+"_Highways_"+which+".gif";
	}
	
	public String getHighwaysPath( String which ) {
		return getSitePrefix()+"/Overlays/Highways/"+which;
	}
	
	public String getCities( String site ) {
		return getCitiesPath( which )+"/"+site+"_City_"+which+".gif";
	}
	
	public String getCitiesPath( String which ) {
		return getSitePrefix()+"/Overlays/Cities/"+which;
	}
	
	public String getWarnings( String site ) {
		return getWarningsPath(which)+"/"+site+"_Warnings_0.gif";
	}
	
	public String getWarningsPath( String which ) {
		return getSitePrefix()+"/Warnings/"+which;
	}

	static String[]sites = new String[] {
		"Tulsa-INX", "OKC-TLX", "Vance AFB-VNX", 
		"Wichita-ICT", "Springfield-SGF", "Little Rock-LZK", 
		"FortWorth-FWS", "Fredrick-FDR", "Dodge City-DDC" 
	};
		
	static JCheckBox showdl;
	JPanel top;
	JPanel bot;
	
	public JPanel getTop() {
		return top;
	}
	public JPanel getBottom() {
		return bot;
	}
	static JFrame frame;

	public static void main( String args[] ) throws Exception {
		String laf = UIManager.getSystemLookAndFeelClassName();
		try {
		  	UIManager.setLookAndFeel(laf);
		    // If you want the Cross Platform L&F instead, comment out the
			// above line and uncomment the following:

		    // UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName());

		} catch (UnsupportedLookAndFeelException exc) {
		    System.err.println("Warning: UnsupportedLookAndFeel: " + laf);
		} catch (Exception exc) {
		    System.err.println("Error loading " + laf + ": " + exc);
		}

		org.wonderly.net.vhttp.Handler.setCacheDir( System.getProperty("user.home")+"/.nwsradar");
		System.getProperties().put("java.protocol.handler.pkgs","org.wonderly.net");
		frame = new JFrame( "Radar");
		//frame.setPreferredSize( new Dimension( 700, 700 ));
		//org.wonderly.net.vhttp.Handler.setParent(f);
		final JTabbedPane tabs = new JTabbedPane();
		Packer pk = new Packer( frame.getContentPane() );
		pk.pack( tabs ).gridx(0).gridy(1).fillboth();
		if( args.length > 0 )
			sites = args;
		JToolBar bar = new JToolBar();
		showdl = new JCheckBox( "Show Download" );
		showdl.setSelected(true);
		//bar.add( showdl );
		bar.setFloatable( false );
		//pk.pack( bar ).gridx(0).gridy(0).fillx();

		String s = sites[0];
		int idx = s.indexOf("-");
		if( idx > 0 ) {
			s = sites[0].substring(idx+1);
		}
		
		final ArrayList<RadarPane> panes = new ArrayList<RadarPane>();
		RadarPane rp;
		JPanel p = new JPanel();
		rp = new RadarPane( s );
		Packer mpk = new Packer( p );
		mpk.pack( rp.getTop() ).gridx(0).gridy(0).fillboth();
		mpk.pack( rp.getBottom() ).gridx(0).gridy(1).fillx();
		tabs.add( sites[0].substring(0,sites[0].indexOf("-")), p );
		panes.add(rp);
		frame.pack();
		frame.setLocationRelativeTo( null );
		frame.addWindowListener( new WindowAdapter() {
			public void windowClosing( WindowEvent ev ) {
				System.exit(1);
			}
		});
		frame.setVisible(true);
		for( int i = 1; i < sites.length; ++i ) {
			String site = sites[i];
			s = site;
			idx = s.indexOf("-");
			if( idx > 0 ) {
				s = site.substring(idx+1);
			}
			rp = new RadarPane( s );
			p = new JPanel();
			mpk = new Packer( p );
			mpk.pack( rp.getTop() ).gridx(0).gridy(0).fillboth();
			mpk.pack( rp.getBottom() ).gridx(0).gridy(1).fillx();
			tabs.add( site.substring(0,site.indexOf("-")), p );
			panes.add(rp);
			for( int j = 0; j < rp.parts.length; ++j ) {
				rp.parts[j].setSelected(false);
			}
		}

		final int ltab[] = new int[]{ tabs.getSelectedIndex() };
		tabs.addChangeListener( new ChangeListener() {
			public void stateChanged( ChangeEvent ev ) {
				log.info("TabCount="+tabs.getTabCount());
				if( ltab[0] == -1 ) {
					ltab[0] = tabs.getSelectedIndex();
				} else {
					for( int i = 0; i < panes.get(ltab[0]).parts.length; ++i ) {
						RadarPane rdp = panes.get(ltab[0]);
						RadarPart rp = rdp.parts[i];
						rdp.lastParts[i] = rp.isSelected();
						rp.setSelected(false);
					}
					ltab[0] = tabs.getSelectedIndex();
					for( int i = 0; i < panes.get(ltab[0]).parts.length; ++i ) {
						RadarPane rdp = panes.get(ltab[0]);
						RadarPart rp = rdp.parts[i];
						rp.setSelected( rdp.lastParts[i] );
						rp.setInterval( rp.getInterval() );
					}
				}
			}
		});

	}

	public RadarPane( String args[] ) throws IOException {
		this(args[0]);
	}

	static ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor( 4 );
	class RadarPart extends JCheckBox implements Runnable,ImageObserver {
		private static final long serialVersionUID = 1L;
		int layer;
		ImageURL url;
		volatile boolean vis;
		volatile Image img;
		int intv;
		volatile boolean stopped;
		
		public synchronized void stopping() {
			stopped = true;
		}

		public synchronized void setInterval( int secs ) {
			intv = secs;
			log.fine("rescheduling "+this+" for "+intv+"secs update");
			if( exec.remove( this ) ) {
			}
			exec.submit( this );
			addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent ev ) {
					if( exec.remove( RadarPart.this ) ) {
					}
					exec.submit( RadarPart.this );
				}
			});
		}

		public int getInterval() {
			return intv;
		}

		public String toString() {
			return getText()+": "+new File(url.getURL()).getName();
		}

		public void run() {
			if( stopped ) {
				log.fine("shutdown refresh on: "+this );
				return;
			}
			if( isSelected() ) {
				log.fine( "loading: "+this );
				try {
					loadImage();
					SwingUtilities.invokeLater( new Runnable() {
						public void run() {
							map.setPreferredSize(new Dimension(
									img.getWidth(RadarPart.this), img.getHeight(RadarPart.this)));
							log.info("PreferredSize: "+map.getPreferredSize());
							map.repaint();
						}
					});
				} catch( Exception ex ) {
					log.log( Level.SEVERE, ex.toString(), ex );
				}
			}
			log.fine("resched: "+this+" to "+intv+"secs");
			exec.remove( this );
			exec.schedule( this, intv, TimeUnit.SECONDS );
		}
	
		public void setVisibility( boolean how ) {
			vis = how;
		}

		public RadarPart( String label, ImageURL url, int layer ) throws IOException {
			super(label);
			this.url = url;
			this.layer = layer;
		}

		public void loadImage() {
			try {
				log.fine(this+": loading: "+url.getURL() );
				URL u =new URL( url.getURL() );
				log.fine("opening: "+u );
				URLConnection uc = u.openConnection();
				Object o = u.getContent();
				log.fine(u+": type="+uc.getContentType()+", obj="+o);
				InputStream is = uc.getInputStream();
				int b = is.read();
				log.fine(uc.getContentType()+": length["+b+"]: "+uc.getContentLength() );
				final byte[]data = new byte[ uc.getContentLength() ];
				data[0] = (byte)b;
				//DataInputStream ds = new DataInputStream( uc.getInputStream() );
				//ds.readFully( data );
				int cnt = 0;
				int off = 1;
				try {
					while( ( cnt = is.read(data, off, data.length-off) ) > 0 ) {
						log.fine("reading "+cnt+" more bytes");
						off += cnt;
					}
				} finally {
					is.close();
				}
				log.fine("read: "+data.length+" bytes");
				//ds.close();
				Image im = Toolkit.getDefaultToolkit().createImage( data );
				MediaTracker tk = new MediaTracker( this );
				tk.addImage( im, 1 );
				tk.waitForAll();
				if( (tk.statusAll(true) & (tk.ABORTED|tk.ERRORED)) == 0 ) {
					synchronized(this) {
						img = im;
					}
				} else {
					log.warning("tracker error ("+tk.statusAll(true)+") abort="+tk.ABORTED+", error="+tk.ERRORED+", Success="+tk.COMPLETE+", Pending="+tk.LOADING );
				}
			} catch( Exception ex ) {
				log.log( Level.WARNING,"Image URL \""+url.getURL()+"\" not loaded: "+ex.toString(),ex);
			}
		}
	}

	public class RadarParts {
		RadarPart[]parts;
		String time;
		
		public void stopping() {
			for( RadarPart pt : parts ) {
				pt.stopping();
			}
		}
		public RadarParts(String time) {
			this.time = time;
			parts = new RadarPart[8];
		}

		public RadarParts(RadarPart[] newParts ) {
			time = "";
			parts = newParts;
		}

		public void addTopo( final String str ) throws IOException {
			parts[0] = new RadarPart( "Topo", new ImageURL() {
				public String getURL(){ return getSitePrefix()+"/"+str;}}, 0 );

		}

		public void addRadar( final String str ) throws IOException {
			parts[1] = new RadarPart( "Radar", new ImageURL() {
				public String getURL(){ return getSitePrefix()+"/"+str;}}, 1 );

		}

		public void addCounties( final String str ) throws IOException {
			parts[2] = new RadarPart( "Counties", new ImageURL() {
				public String getURL(){ return getSitePrefix()+"/"+str;}}, 2 );

		}

		public void addRivers( final String str ) throws IOException {
			parts[3] = new RadarPart( "Rivers", new ImageURL() {
				public String getURL(){ return getSitePrefix()+"/"+str;}}, 3 );

		}

		public void addHighways( final String str ) throws IOException {
			parts[4] = new RadarPart( "Highways", new ImageURL() {
				public String getURL(){ return getSitePrefix()+"/"+str;}}, 4 );

		}

		public void addCities( final String str ) throws IOException {
			parts[5] = new RadarPart( "Cities", new ImageURL() {
				public String getURL(){ return getSitePrefix()+"/"+str;}}, 5 );

		}

		public void addWarnings( final String str ) throws IOException {
			parts[6] = new RadarPart( "Warnings", new ImageURL() {
				public String getURL(){ return getSitePrefix()+"/"+str;}}, 6 );

		}

		public void addLegend( final String str ) throws IOException {
			parts[7] = new RadarPart( "Legend", new ImageURL() {
				public String getURL(){ return getSitePrefix()+"/"+str;}}, 7 );

		}
	}
	
	private static void runInSwing( final Runnable r ) {
		if( SwingUtilities.isEventDispatchThread() ) {
			r.run();
		} else {
			try {
				SwingUtilities.invokeAndWait( r );
			} catch( Exception ex ) {
			}
		}
	}
	
	public void stopping() {
		try {
			log.info("Canceling timer: "+timer );
			if( timer != null ) {
				timer.cancel();
			}
			timer = null;
		} catch( Exception ex ) {
			log.log( Level.WARNING, ex.toString(), ex );
		}
		try {
			map.stopping();
		} catch( NullPointerException ex ) {
			log.log( Level.INFO, ex.toString(), ex );
		} catch( Exception ex ) {
			log.log( Level.WARNING, ex.toString(), ex );
		}
	}

	Timer timer = new Timer(true);
	class RadarImage extends JPanel {
		/** Animated parts */
		RadarParts parts;
		/** Static parts */
		RadarParts sparts;
		/** Animation control task */
		TimerTask animTask;
		volatile boolean closed;
		Logger log = Logger.getLogger( getClass().getName() );

		public RadarImage( RadarPart partss[] ) {
			this.sparts = new RadarParts( partss );
			this.parts = this.sparts;
		}

		RadarPart part(int idx) {
			return parts.parts[idx];
		}

		public synchronized void stopping() {
			closed = true;
			if( lastAnim != null ) {
				try {
					lastAnim.stopping();
				} catch( Exception ex ) {
					log.log( Level.INFO, ex.toString(), ex );
				}
			}
			try {
				sparts.stopping();
			} catch( Exception ex ) {
				log.log( Level.INFO, ex.toString(), ex );		
			}
		}
		volatile AnimLoader lastAnim;
		public synchronized void setModeAnim( final boolean how, final String site, final String url ) throws IOException {
			// Stop any running animation load
			log.info("\n\n\nChanging animation mode to: "+how+"\n\n\n\n");
			if( lastAnim != null ) {
				try {
					log.info("stopping existing loader");
					lastAnim.stopping();
				} catch( Exception ex ) {
					log.log( Level.FINER, ex.toString()+": animation wait hiccuped", ex );
				}
				lastAnim = null;
			}
			if( !how ) {
				log.info("turning off animation");
				try {
					runInSwing( new Runnable() {
						public void run() {
							log.info("Switching back to static parts");
							map.parts = map.sparts;
							map.repaint();
						}
					});
				} catch( Exception ex ) {
					log.log( Level.FINE, ex.toString(), ex );
				}
			} else {
				map.lastAnim = new AnimLoader( url );
				map.lastAnim.load();
			}
		}

		private class AnimLoader {
			volatile boolean stopping;
			String url;
			Object stopLock = new Object();
			volatile boolean running;
			ArrayList<RadarParts>plist = new ArrayList<RadarParts>();
			public AnimLoader( String url ) {
				this.url = url;
			}
			private void release() {
//				ArrayList<RadarParts>plist = lastAnim.plist;
				for( int i = 0; i < plist.size(); ++i ) {
					for( int j = 0; j < plist.get(i).parts.length; ++j ) {
						plist.get(i).parts[j].img.flush();
					}
				}
			}
			public synchronized void stopping() {
				synchronized( stopLock ) {
					this.stopping = true;
					log.info("stopping is true");
					while( running ) {
						try {
							log.info("waiting for running to be false");
							stopLock.wait();
							log.info("waited, running is: "+running);
						} catch( Exception ex ) {
							log.log( Level.INFO, ex.toString(), ex );
						}
					}
				}
				try {
					for( RadarParts pts : plist ) {
						pts.stopping();
					}
				} catch( Exception ex ) {
					log.log( Level.INFO, ex.toString(), ex );
				}
				release();
			}
			public void load() {
				// Start animation thread
				running = true;
				new ComponentUpdateThread<Boolean>() {
					public Boolean construct() {
						try {
							URL u = new URL( url );
							URLConnection uc = u.openConnection();
							String sstr;
							BufferedReader rd = new BufferedReader( new InputStreamReader( uc.getInputStream() ) );
							ArrayList<String> fls = new ArrayList<String>();

							while( ( sstr = rd.readLine() ) != null ) {
								fls.add(sstr);
								if( log.isLoggable(Level.FINE) )
									log.fine("sstr: "+sstr );
							}

							for( String str : fls ) {
								synchronized( stopLock ) {
									if( stopping || map.lastAnim != AnimLoader.this ) {
										log.info("stopping load");
										return false;
									}
								}
								StringTokenizer st = new StringTokenizer( str, " " );
								String bkgrnd = st.nextToken();
								log.info("bkgrnd: \""+bkgrnd+"\"");
								if( st.hasMoreTokens() == false )
									break;

								String time = st.nextToken("\"");
								log.info("time: \""+time+"\"");

								time = st.nextToken("\"");
								log.info("time: \""+time+"\"");
								RadarParts pts = new RadarParts(time);

								String topo = st.nextToken(" ,");
								log.info("topo: \""+topo+"\"");
								topo = st.nextToken(" ,");
								if(topo.startsWith("overlay=") )
									topo = topo.substring( "overlay=".length() );
								log.info("topo: \""+topo+"\"");
								pts.addTopo( topo );

								String radar = st.nextToken(" ,");
								log.info("radar: \""+radar+"\"");
								pts.addRadar( radar );

								String county = st.nextToken(" ,");
								log.info("county: \""+county+"\"");
								pts.addCounties( county );

								String rivers = st.nextToken(" ,");
								log.info("rivers: \""+rivers+"\"");
								pts.addRivers( rivers );

								String highways = st.nextToken(" ,");
								log.info("highways: \""+highways+"\"");
								pts.addHighways( highways );

								String cities = st.nextToken(" ,");
								log.info("cities: \""+cities+"\"");
								pts.addCities( cities );

								String warnings = st.nextToken(" ,");
								log.info("warnings: \""+warnings+"\"");
								pts.addWarnings( warnings );

								String legend = st.nextToken(" ,");
								log.info("legend: \""+legend+"\"");
								pts.addLegend( legend );
								plist.add(pts);
							}
							log.info("Found: "+plist.size()+" animation frames: "+plist );
							SimpleProgress sp = new SimpleProgress( (JFrame)getTopLevelAncestor(), "Fetching Imagery", plist.size() );
							LinkedBlockingQueue<Runnable> lbq = new LinkedBlockingQueue<Runnable>();
							ThreadPoolExecutor tpe = new ThreadPoolExecutor( 2, 5, 10L, TimeUnit.SECONDS, lbq );
							try {
								for( int i = 0; i < plist.size(); ++i ) {
									RadarParts pts = plist.get(i);
									log.info("loading images for part: "+i );
									sp.setValue(i);
									for( int j = 0; j < pts.parts.length; ++j ) {
										RadarPart p = pts.parts[j];
										if( p == null ) {
											log.warning("no part at index: "+j+" for: "+pts );
											continue;
										}
										String fn = new File(p.url.getURL()).getName();
										log.info("loading \""+fn+"\" next");
										sp.setCurrentEntity(fn);
										synchronized( stopLock ) {
											if( stopping || map.lastAnim != AnimLoader.this ) {
												log.info("Stopping image loading on request");
												return false;
											}
										}
										final RadarPart fp = p;
										Runnable r = new Runnable() {
											public void run() {
												synchronized( stopLock ) {
													if( stopping || closed || map.lastAnim != AnimLoader.this ) {
														log.info("Stopping image loading on request");
														return;
													}
												}
												try {
													log.info("loading part imagery: "+fp );
													fp.loadImage();
													log.info("loaded: "+fp.url.getURL());
												} catch( Exception ex ) {
													log.log( Level.WARNING, ex.toString()+": "+fp );
												}
											}
										};
										tpe.execute( r );
									}
									while( tpe.getActiveCount() > 2 )
										Thread.yield();
								}
							} finally {
								tpe.shutdown();
								tpe.awaitTermination( 60, TimeUnit.SECONDS );
								log.info("closing progress");
								sp.setVisible(false);
							}
							return true;
						} catch( Exception ex ) {
							log.log( Level.SEVERE, ex.toString(), ex );
						}
						return false;
					}
					public void finished() {
						try {
							if( getValue() ) {
								log.info("Scheduling paint loop");
								timer.scheduleAtFixedRate( new TimerTask() {
									int idx = 0;
									public void run() {
										log.info("Checking for stopping ("+stopping+"), closed("+closed+") lastAnim: "+map.lastAnim );
										synchronized( stopLock ) {
											if( stopping || closed || map.lastAnim != AnimLoader.this ) {
												try {
													cancel();
												} finally {
													release();
													try {
														timer.cancel();
													} catch( Exception ex ) {
														log.log( Level.SEVERE, ex.toString(), ex );
													}
													timer = new Timer( true );
													running = false;
													stopLock.notifyAll();
												}
												return;
											}
										}
										if( idx >= plist.size() )
											idx = 0;
										log.info("playing frame: "+idx);
										SwingUtilities.invokeLater( new Runnable() {
											public void run() {
												RadarParts sp = plist.get(idx++);
												for( int i = 0; i < sp.parts.length; ++i ) {
													sp.parts[i].setSelected( sparts.parts[i].isSelected() );
												}
												parts = sp;
												map.repaint();
											}
										});
									}
								}, 1500, 500 );
							} else {
								log.info("aborting loading");
								release();
							}
						} catch( Exception ex ) {
							log.log( Level.SEVERE, ex.toString(), ex );
						} finally {
							super.finished();
							log.info("notifying all of loading complete");
							synchronized( stopLock ) {
								running = false;
								stopLock.notifyAll();
								log.info("notifyied all of loading complete");
							}
						}
					}
				}.start();
			}
		}

		public void update( Graphics g ) {
			paint(g);
		}

//		public Dimension getMinimumSize() {
//			log.info("parts: "+parts);
//			log.info("parts.parts: "+parts.parts );
//			if( parts.parts != null ) {
//				log.info( "parts.parts[0].img: "+parts.parts[0].img );
//			}
//			return new Dimension( parts.parts[0].img.getWidth(this), parts.parts[0].img.getHeight(this) );
//		}
//
//		public Dimension getPreferredSize() {
//			log.info("parts: "+parts);
//			log.info("parts.parts: "+parts.parts );
//			if( parts.parts != null ) {
//				log.info( "parts.parts[0].img: "+parts.parts[0].img );
//			}
//			return new Dimension( parts.parts[0].img.getWidth(this), parts.parts[0].img.getHeight(this) );
//		}

		public void paintComponent( Graphics gr ) {
			Image offi = createImage( getSize().width, getSize().height );
			Graphics g = offi.getGraphics();
			if( part(0).isSelected() == false ) {
				g.setColor( Color.black );
				g.fillRect(0,0,getSize().width,getSize().height);
			}
			for( int i = 0 ; i < parts.parts.length; ++i ) {
				if( part(i).isSelected() ) {
					if( part(i).img != null ) {
						g.drawImage( part(i).img, 0, 0,  getSize().width, getSize().height, this );
					}
				} else {
					log.finer("Not painting unselected: "+part(i) );
				}
			}
			gr.drawImage( offi, 0, 0, this );
		}
	}

	public interface ImageURL {
		public String getURL();
	}

	RadarPart[]parts;
	boolean[]lastParts;
	Hashtable<String,String[]> navtbl = new Hashtable<String,String[]>();

	public void addNavigationEntry( String site, String[] sites ) {
		navtbl.put( site, sites );
	}

	public RadarPane( final String site ) throws IOException {
		this( site, null );
	}
	
	private JButton[]navbtns = null;
	private JCheckBox anim;
	public RadarPane( final String site, Hashtable<String,String[]> navtbl ) throws IOException {
		if( navtbl != null )
			this.navtbl = navtbl;
		navbtns = new JButton[9];
		boolean[]def = new boolean[] {true,true,true,false,true,true,true,true};
//		Packer pk = new Packer(this);
		int y = -1;

		URL u =new URL( getRadar( site ) );
		final ImageIcon ic = new ImageIcon( u );
		parts = new RadarPart[] {
			new RadarPart( "Topo", new ImageURL() {
				public String getURL(){ return getTopo(site);}}, 0 ),
			new RadarPart( "Radar", new ImageURL() {
				public String getURL(){ return getRadar(site);}}, 1 ),
			new RadarPart( "Counties", new ImageURL() {
				public String getURL(){ return getCounty(site);}}, 2 ),
			new RadarPart( "Rivers", new ImageURL() {
				public String getURL(){ return getRivers(site);}}, 3 ),
			new RadarPart( "Highways", new ImageURL() {
				public String getURL(){ return getHighways(site);}}, 4 ),
			new RadarPart( "Cities", new ImageURL() {
				public String getURL(){ return getCities(site);}}, 5 ),
			new RadarPart( "Warnings", new ImageURL() {
				public String getURL(){ return getWarnings(site);}}, 6 ),
			new RadarPart( "Legend", new ImageURL() {
				public String getURL(){ return getLegend(site);}}, 7 ),
		};
		lastParts = new boolean[ parts.length ];
		map = new RadarImage(parts) {
			public Dimension getPreferredSize() {
				return new Dimension(ic.getIconWidth(),ic.getIconHeight());
			}
		};

		int sec = 10;
		int stime = 300;
		top = new JPanel();
		bot = new JPanel();
		Packer botpk = new Packer( bot );
		for( int i = 0; i < parts.length; ++i ) {
			final RadarPart pt = parts[i];
			pt.setSelected( def[i] );
			lastParts[i] = def[i];
			botpk.pack( pt ).fillx().gridx(i).gridy(0);
			final int fidx = i;
			pt.addActionListener( new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					map.repaint();
					lastParts[ fidx ] = pt.isSelected();
				}
			});
			pt.setInterval( stime+(sec += 2) );
		}
		Packer tpk = new Packer( top );
		tpk.pack( map ).fillboth().gridx(0).gridy(0);
		final JComboBox prods = new JComboBox();
		for( String pr : productDesc ) {
			prods.addItem(pr);
		}
		final JCheckBox range = new JCheckBox("Long Range");
		prods.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				product = products[ prods.getSelectedIndex() ];
				Container c = RadarPane.this.getTop().getTopLevelAncestor();
				JFrame f = null;
				if( c instanceof JFrame )
					f = (JFrame)c;
				final SimpleProgress sp = new SimpleProgress( f,
					"Loading Products", parts.length );
				new ComponentUpdateThread( prods, range, map ) {
					public Object construct() {
						for( int i = 0; i < parts.length; ++i ) {
							sp.setValue(i);
							sp.setCurrentEntity( new File( parts[i].url.getURL() ).getName() );
							parts[i].loadImage();
						}
						return null;
					}
					public void finished() {
						try {
							map.repaint();
						} finally {
							super.finished();
							sp.setVisible(false);
						}
					}
				}.start();
			}
		});
		++y;
		JPanel btp = new JPanel();
		Packer btpk = new Packer( btp );

		botpk.pack( new JSeparator() ).gridx(0).gridy(++y).gridw(parts.length).fillx().inset(4,4,4,4);
		botpk.pack( btp ).gridx(0).gridy(++y).gridw(8).fillx();
		JPanel ppan = new JPanel();
		Packer pppk = new Packer( ppan);
		//pppk.pack( new JLabel("Product: ") ).gridx(0).gridy(0).west();
		ppan.setBorder(BorderFactory.createTitledBorder("Product"));
		pppk.pack( prods ).gridx(0).gridy(1).fillx();
		pppk.pack( range ).gridx(0).gridy(2).fillx();
		//ppan.setMinimumSize(new Dimension(360, 100));
		//ppan.setBorder(BorderFactory.createEtchedBorder());
		btpk.pack( ppan ).gridx(0).gridy(0).fillboth(0,0).gridh(3);
		pppk.pack( anim = new JCheckBox("Animate") ).gridx(0).gridy(3).fillx();
		anim.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent ev ) {
				try {
					map.setModeAnim( anim.isSelected(), cursite, getAnims(cursite) );
				} catch( Exception ex ) {
					log.log( Level.SEVERE, ex.toString(), ex );
				}
			}
		});
		JPanel ctl = new JPanel();
		Packer cpk = new Packer( ctl );
		int bi = 0;
		cpk.pack( navbtns[bi++] = iconButton( "images/nw.gif", "/", 0) ).gridx(0).gridy(0).inset(0,0,0,0);
		cpk.pack( navbtns[bi++] = iconButton( "images/n.gif", "^", 1) ).gridx(1).gridy(0).inset(0,0,0,0);
		cpk.pack( navbtns[bi++] = iconButton( "images/ne.gif", "\\", 2) ).gridx(2).gridy(0).inset(0,0,0,0);
		cpk.pack( navbtns[bi++] = iconButton( "images/w.gif", "<", 3) ).gridx(0).gridy(1).inset(0,0,0,0);
		++bi; // skip label slot.
		cpk.pack( slab = new JLabel("", JLabel.CENTER) ).gridx(1).gridy(1).fillboth(0,0);
		slab.setFont( new Font( "serif", Font.BOLD, 16 ) );
		cpk.pack( navbtns[bi++] = iconButton( "images/e.gif", ">", 5) ).gridx(2).gridy(1).inset(0,0,0,0);
		cpk.pack( navbtns[bi++] = iconButton( "images/sw.gif", "\\", 6) ).gridx(0).gridy(2).inset(0,0,0,0);
		cpk.pack( navbtns[bi++] = iconButton( "images/s.gif", "v", 7) ).gridx(1).gridy(2).inset(0,0,0,0);
		cpk.pack( navbtns[bi++] = iconButton( "images/se.gif", "/", 8 ) ).gridx(2).gridy(2).inset(0,0,0,0);

		btpk.pack( ctl ).gridx(1).gridy(0).gridh(3).gridw(1);

		final JSlider sld = new JSlider( 30, 600 );
		JPanel slp = new JPanel();
		slp.setBorder( BorderFactory.createTitledBorder("Refresh Interval") );
		Packer slpk = new Packer( slp );
		slpk.pack( sld ).gridx(0).gridy(0).fillx();
		final JLabel slval;
		slp.setPreferredSize( new Dimension( 240, 40));
		slpk.pack( slval = new JLabel("  0") ).gridx(1).gridy(0);
		slpk.pack( new JLabel("mins") ).gridx(2).gridy(0).inset(0,3,0,0);
		sld.addChangeListener( new ChangeListener() {
			public void stateChanged( ChangeEvent ev ) {
				int vl = (int)((sld.getValue()/60.0)*10);
				int r = vl % 10;
				int v = vl/10;
				String vs = v+"";
				if( v < 10 )
					vs = "0"+vs;
				slval.setText( vs+"."+r );
				
			for( int i = 0; i < parts.length; ++i ) {
				RadarPart pt = parts[i];
				pt.setInterval( sld.getValue() );
			}
			}
		});
		
		sld.setValue( stime );
		int vl = (int)((sld.getValue()/60.0)*10);
		int r = vl % 10;
		int v = vl/10;
		slval.setText( v+"."+r );
		btpk.pack( slp ).gridx(2).gridy(0).gridh(3).fillboth().inset(4,4,4,4).gridh(2);
		sld.setPaintLabels(true);
		sld.setMinorTickSpacing( 30 );
		sld.setPaintTicks( true );
		range.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent ev ) {
				which = range.isSelected() ? "Long" : "Short";
				prods.setEnabled( range.isSelected() == false );
				if( range.isSelected() ) {
					product = "N0Z";
					which = "Long";
				} else {
					which = "Short";
					product = products[ prods.getSelectedIndex() ];
				}
				Container c = RadarPane.this.getTop().getTopLevelAncestor();
				JFrame f = null;
				if( c instanceof JFrame )
					f = (JFrame)c;
				final SimpleProgress sp = new SimpleProgress( f,
					"Loading Products", parts.length );
				new ComponentUpdateThread( range, prods, map ) {
					public Object construct() {
						for( int i = 0; i < parts.length; ++i ) {
							sp.setValue(i);
							sp.setCurrentEntity( new File( parts[i].url.getURL() ).getName() );
							exec.remove( parts[i] );
							exec.submit( parts[i] );
						}
						return null;
					}
					public void finished() {
						try {
							map.repaint();
						} finally {
							super.finished();
							sp.setVisible(false);
							prods.setEnabled( range.isSelected() == false );			
						}
					}
				}.start();
			}
		});

		cursite = site;
		if( cursite.length() == 4 ) {
			cursite = cursite.substring(1).toLowerCase();
		} else {
			cursite = cursite.toLowerCase();
		}
		if( cursite != null && navtbl != null ) {
			String ent[] = navtbl.get(cursite);
			if( ent != null ) {
				for( int i = 0; i < navbtns.length; ++i ) {
					if( navbtns[i] != null ) {
						log.info("checking navigation to: \""+ent[i]+"\", tbl has: "+
								(ent[i] != null ? navtbl.get(ent[i]) : "<null>"));
						navbtns[i].setEnabled( ent[i] != null && navtbl.get( ent[i] ) != null );
						navbtns[i].setToolTipText( navbtns[i].isEnabled() ? ent[i] : null );
					}
				}
			}
			slab.setText( cursite.toUpperCase() );
		}
	}
	
	protected void gotoSite( int idx ) {
		log.info("navtbl: "+navtbl);
		log.info("Going to site["+idx+"] of "+Arrays.toString( navtbl.get(cursite) ) );
		final String[]ents = navtbl.get( cursite );
		if( ents == null ) {
			return; //sthrow new NullPointerException( "No navigation for idx="+idx+" from "+cursite );
		}
		final String site = ents[idx].toUpperCase();
		final String[]ent = navtbl.get( ents[idx] );
		final boolean was[] = new boolean[parts.length];
		for( int i = 0; i < was.length; ++i ) {
			was[i] = parts[i].isSelected();
		}
		parts[0].url = new ImageURL() {
			public String getURL(){ return getTopo(site);}};

		parts[1].url = new ImageURL() {
			public String getURL(){ return getRadar(site);}};
		parts[2].url = new ImageURL() {
			public String getURL(){ return getCounty(site);}};
		parts[3].url = new ImageURL() {
			public String getURL(){ return getRivers(site);}};
		parts[4].url = new ImageURL() {
			public String getURL(){ return getHighways(site);}};
		parts[5].url = new ImageURL() {
			public String getURL(){ return getCities(site);}};
		parts[6].url = new ImageURL() {
			public String getURL(){ return getWarnings(site);}};
		parts[7].url = new ImageURL() {
			public String getURL(){ return getLegend(site);}};

		log.info("parts updated with new URLs");
		new ComponentUpdateThread() {
			public void setup() {
				super.setup();
				log.info("deactivating navigation while loading images");
				cursite = site.toLowerCase();
				for( int i = 0; i < navbtns.length; ++i ) {
					if( navbtns[i] != null )
						navbtns[i].setEnabled( false );
				}
				for( int i = 0; i < parts.length; ++i ) {
					parts[i].setSelected(false);
				}
				map.repaint();
				slab.setText( site );
			}
	
			public Object construct() {
				log.info("Loading "+parts.length+" images");
				LinkedBlockingQueue<Runnable> lbq = new LinkedBlockingQueue<Runnable>();
				ThreadPoolExecutor tpe = new ThreadPoolExecutor( 2, 8, 10L, TimeUnit.SECONDS, lbq );
				final SimpleProgress sp = new SimpleProgress((JFrame)getTop().getTopLevelAncestor(),"Loading Static Imagery", parts.length );
				final int cnts[] = new int[1];
				final Object lock = new Object();
				for( int i = 0; i < parts.length; ++i ) {
					final RadarPart p = parts[i];
					final int fi = i;
					Runnable r = new Runnable() {
						public void run() {
							try {
								log.info("loading image for["+fi+"]");
								p.loadImage();
								synchronized( lock ) {
									cnts[0]++;
								}
								sp.setValue( cnts[0] );
								sp.setCurrentEntity( new File( p.url.getURL() ).getName() );
							} catch( Exception ex ) {
								log.log( Level.WARNING, ex.toString(), ex );
							}

						}
					};
					tpe.execute( r );
				}
				try {
					tpe.shutdown();
					tpe.awaitTermination( 60, TimeUnit.SECONDS );
				} catch( Exception ex ) {
					log.log( Level.FINER, ex.toString(), ex );
				} finally {
					sp.setVisible(false);
					SwingUtilities.invokeLater( new Runnable() {
						public void run() {
							map.repaint();
						}
					});
				}
				return null;
			}
			
			Object animSwitchLock = new Object();
			public void finished() {
				try {
					for( int i = 0; i < navbtns.length; ++i ) {
						if( navbtns[i] != null ) {
							log.info("checking navigation["+i+"] to: "+ent[i]+", tbl has: "+
									( ent[i] != null ? Arrays.toString( navtbl.get(ent[i]) ) : "<null>"));
							navbtns[i].setEnabled( ent[i] != null && navtbl.get( ent[i] ) != null );
							navbtns[i].setToolTipText( navbtns[i].isEnabled() ? ent[i] : null );
						}
					}

					for( int i = 0; i < parts.length; ++i ) {
						parts[i].setSelected(was[i]);
					}

					if( map.lastAnim != null ) {
						new Thread() {
							public void run() {
								log.info( "Animation on, stopping and restarting" );
								try {
									// Make sure these happen atomically
									synchronized( animSwitchLock ) {
										map.setModeAnim( false, cursite, "" );
										map.setModeAnim( true, cursite, getAnims( cursite ) );
									}
								} catch( Exception ex ) {
									log.log( Level.SEVERE, ex.toString(), ex );
								}
							}
						}.start();
					}
				} finally {
					super.finished();
				}
			}
		}.start();
	}

	volatile String cursite;
	public JButton iconButton( String url, String def, final int idx ) {
		JButton b = iconButton( url, def );
		log.info("using icon for "+def+" button: "+url);
		b.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent ev ) {
				gotoSite( idx );
			}
		});
		b.setPreferredSize(new Dimension( 40, 30));
		b.setEnabled(false);
		b.setToolTipText("Function Not Available");
		return b;
	}

	/**
	 * Build the button object for this icon image and default text description.
	 * @param url the location of the image for the button
	 * @param def the default text label to use of the image can't be loaded.
	 */
	public JButton iconButton( String url, String def ) {
		URL u = getClass().getResource( url );
		JButton b;
		if( u == null ) {
			b = new JButton( def );
		} else {
			b = new JButton( new ImageIcon( u ) );
		}
		//b.setMargin( new Insets( 0,0,0,0 ) );
		return b;
	}

	// The names of all sites as listed in the directory structure.  Should create a function to
	// connect and enumerate these.
	private String[]radars = {
		"ABC", "ABR", "ABX", "ACG", "AEC", "AHG", "AIH", "AKC", "AKQ", "AMA", "AMX", "APD", "APX", "ARX", "ATX",
		"BBX", "BGM", "BHX", "BIS", "BLX", "BMX", "BOX", "BRO", "BUF", "BYX", "CAE", "CBW", "CBX", "CCX", "CLE",
		"CLX", "CRP", "CXX", "CYS", "DAX", "DDC", "DFX", "DGX", "DIX", "DLH", "DMX", "DOX", "DTX", "DVN", "DYX",
		"EAX", "EMX", "ENX", "EOX", "EPZ", "ESX", "EVX", "EWX", "EYX", "FCX", "FDR", "FDX", "FFC", "FSD", "FSX",
		"FTG", "FWS", "GGW", "GJX", "GLD", "GRB", "GRK", "GRR", "GSP", "GUA", "GWX", "GYX", "HDX", "HGX", "HKI",
		"HKM", "HMO", "HNX", "HPX", "HTX", "HWA", "ICT", "ICX", "ILN", "ILX", "IND", "INX", "IWA", "IWX", "JAX",
		"JGX", "JKL", "JUA", "LBB", "LCH", "LIX", "LNX", "LOT", "LRX", "LSX", "LTX", "LVX", "LWX", "LZK", "MAF",
		"MAX", "MBX", "MHX", "MKX", "MLB", "MOB", "MPX", "MQT", "MRX", "MSX", "MTX", "MUX", "MVX", "MXX", "NKX",
		"NQA", "OAX", "OHX", "OKX", "OTX", "PAH", "PBZ", "PDT", "POE", "PUX", "RAX", "RGX", "RIW", "RLX", "RTX",
		"SFX", "SGF", "SHV", "SJT", "SOX", "SRX", "TBW", "TFX", "TLH", "TLX", "TWX", "TYX", "UDX", "UEX", "VAX",
		"VBX", "VNX", "VTX", "VWX", "YUX",
	};
}
