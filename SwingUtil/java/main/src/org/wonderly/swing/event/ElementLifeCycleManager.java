package org.wonderly.swing.event;

/**
 *  This interface is used by the {@link org.wonderly.swing.CircularListModel}
 *  to return items that are overwritten in the model to the user of the model.
 *  As the items are removed, <code>freeElement()</code> will be called
 *  to return the object to the free list. 
 *  @author <a href="mailto:gregg.wonderly@pobox.com">Gregg Wonderly</a>.
 */
public interface ElementLifeCycleManager {
	public void freeElement( LifeCycleElement obj );
}