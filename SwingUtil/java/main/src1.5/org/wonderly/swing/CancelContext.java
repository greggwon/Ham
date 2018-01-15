package org.wonderly.swing;

/**
 *  This interface provides a simple way to check if there is a cancelling context
 *  outside of a looping operation that has been cancelled.  This can be provided
 *  by progress dialogs, cancel buttons on dialogs etc.
 */
public interface CancelContext {
	public boolean isCancelled();
}

