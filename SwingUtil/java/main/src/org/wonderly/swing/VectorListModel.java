package org.wonderly.swing;

import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.util.Collections;
import java.util.List;
import java.util.AbstractList;

/**
 *  This is an {@link AbstractListModel} subclass that also implements
 *  the {@link List} interface.  This allows the program to employ
 *  {@link Collections} provided operations such as <code>sort(List)</code>
 *  on the model to manage the ordering of the elements.  It also includes
 *  a {@link #setContents(Vector)} method that will allow the list contents
 *  to be replaced instantaneously so that large list manipulations don't
 *  show the user a bunch of consecutive insert and delete operations
 *  that make the display flash.
 *  @author Gregg Wonderly <a href="mailto:gregg.wonderly@pobox.com">gregg.wonderly@pobox.com</a>
 *  @see CircularListModel
 */
public class VectorListModel extends AbstractListModel 
	implements List,ComboBoxModel {

	protected Vector delegate = new Vector();

	public int getSize() {
		return delegate.size();
	}
	
	public VectorListModel( Vector conts ) {
		super();
		delegate = conts;
	}
	
	public VectorListModel() {
	}

	public Object getElementAt(int index) {
		if( index >= delegate.size() )
			return null;
		return delegate.elementAt(index);
	}

	public void copyInto(Object anArray[]) {
		delegate.copyInto(anArray);
	}

	public void trimToSize() {
		delegate.trimToSize();
	}

	public void ensureCapacity(int minCapacity) {
		delegate.ensureCapacity(minCapacity);
	}

	public void setSize(int newSize) {
		int oldSize = delegate.size();
		delegate.setSize(newSize);
		if (oldSize > newSize) {
			fireIntervalRemoved(this, newSize, oldSize-1);
		}
		else if (oldSize < newSize) {
			fireIntervalAdded(this, oldSize, newSize-1);
		}
	}
	
	public Object getSelectedItem() {
		return comboSel;
	}

	Object comboSel;
	public void setSelectedItem( Object obj ) {
		comboSel = obj;
	}

	public void setContents( Vector v ) {
		delegate = v;
		fireContentsChanged( this, 0, v.size()-1);
	}

	public Vector getContents() {
		return delegate;
	}

	public int capacity() {
		return delegate.capacity();
	}

	public int size() {
		return delegate.size();
	}

	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	public Enumeration elements() {
		return delegate.elements();
	}

	public boolean contains(Object elem) {
		return delegate.contains(elem);
	}

	public int indexOf(Object elem) {
		return delegate.indexOf(elem);
	}

	public int indexOf(Object elem, int index) {
		return delegate.indexOf(elem, index);
	}

	public int lastIndexOf(Object elem) {
		return delegate.lastIndexOf(elem);
	}

	public int lastIndexOf(Object elem, int index) {
		return delegate.lastIndexOf(elem, index);
	}

	public Object elementAt(int index) {
		return delegate.elementAt(index);
	}

	public Object firstElement() {
		return delegate.firstElement();
	}

	public Object lastElement() {
		return delegate.lastElement();
	}

	public void setElementAt(Object obj, int index) {
		delegate.setElementAt(obj, index);
		fireContentsChanged(this, index, index);
	}

	public void removeElementAt(int index) {
		delegate.removeElementAt(index);
		fireIntervalRemoved(this, index, index);
	}

	public void insertElementAt(Object obj, int index) {
		delegate.insertElementAt(obj, index);
		fireIntervalAdded(this, index, index);
	}

	public void addElement(Object obj) {
		int index = delegate.size();
		delegate.addElement(obj);
		fireIntervalAdded(this, index, index);
	}

	public boolean removeElement(Object obj) {
		int index = indexOf(obj);
		boolean rv = delegate.removeElement(obj);
		if (index > 0) {
			fireIntervalRemoved(this, index, index);
		}
		return rv;
	}


	public void removeAllElements() {
		int index1 = delegate.size()-1;
		delegate.removeAllElements();
		if (index1 >= 0) {
			fireIntervalRemoved(this, 0, index1);
		}
	}

	public Object[] toArray() {
		Object[] rv = new Object[delegate.size()];
		delegate.copyInto(rv);
		return rv;
	}

	public Object get(int index) {
		return delegate.elementAt(index);
	}

	public Object set(int index, Object element) {
		Object rv = delegate.elementAt(index);
		delegate.setElementAt(element, index);
		fireContentsChanged(this, index, index);
		return rv;
	}

	public void add(int index, Object element) {
		delegate.insertElementAt(element, index);
		fireIntervalAdded(this, index, index);
	}

	public Object remove(int index) {
		Object rv = delegate.elementAt(index);
		delegate.removeElementAt(index);
		fireIntervalRemoved(this, index, index);
		return rv;
	}

	public void clear() {
		int index1 = delegate.size()-1;
		delegate.removeAllElements();
		if (index1 >= 0) {
			fireIntervalRemoved(this, 0, index1);
		}
	}

	public void removeRange(int fromIndex, int toIndex) {
		for(int i = toIndex; i >= fromIndex; i--) {
			delegate.removeElementAt(i);
		}
		fireIntervalRemoved(this, fromIndex, toIndex);
	}
	public String toString() {
		return delegate.toString();
	}
	// List implementation follows...
//	public void add(int index, Object element) {
//		delegate.add( index, element );
//	}
	public boolean add(Object o) {
		return delegate.add(o);
	}
	public boolean addAll(Collection c) {
		return delegate.addAll(c);
	}
	public boolean addAll(int index, Collection c) {
		return delegate.addAll( index, c );
	}
//	public void clear() {
//		delegate.clear();
//	}
//	public boolean contains(Object o) {
//		return delegate.contains(o);
//	}
	public boolean containsAll(Collection c) {
		return delegate.containsAll(c);
	}
	public boolean equals(Object o) {
		return delegate.equals(o);
	}
//	public Object get(int index) {
//		return delegate.get(index);
//	}
	public int hashCode() {
		return delegate.hashCode();
	}
//	public int indexOf(Object o) {
//		return delegate.indexOf(o);
//	}
//	public boolean isEmpty() {
//		return delegate.isEmpty();
//	}
	public Iterator iterator() {
		return delegate.iterator();
	}
//	public int lastIndexOf(Object o) {
//		return delegate.lastIndexOf(o);
//	}
	public ListIterator listIterator() {
		return delegate.listIterator();
	}
	public ListIterator listIterator(int index) {
		return delegate.listIterator( index );
	}
//	public Object remove(int index) {
//		return delegate.remove(index);
//	}
	public boolean remove(Object o) {
		return delegate.remove(o);
	}
	public boolean removeAll(Collection c) {
		return delegate.removeAll(c);
	}
	public boolean retainAll(Collection c) {
		return delegate.retainAll(c);
	}
//	public Object set(int index, Object element) {
//		return delegate.set(index,element);
//	}
//	public int size() {
//		return delegate.size();
//	}
	public List subList(int fromIndex, int toIndex) {
		return delegate.subList( fromIndex, toIndex );
	}
//	public Object[] toArray() {
//		return delegate.toArray();
//	}
	public Object[] toArray(Object[] a) {
		return delegate.toArray(a);
	}
}