/*
 * DownloadListener.java
 *
 * Created on October 17, 2006, 2:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.wonderly.net.chttp;

import java.net.URL;

/**
 *
 * @author Mike
 */
public interface DownloadListener {
	public void downloadStarted(URL url,long fileLength);
	public void downloadProgress(URL url, long progress);
	public void downloadFinished(URL url );
	
	public void checking(URL url);
	public void checkComplete(URL url, boolean willDownload, Throwable th );
	
}
