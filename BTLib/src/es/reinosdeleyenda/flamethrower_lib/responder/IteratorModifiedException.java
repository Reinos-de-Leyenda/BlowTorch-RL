package es.reinosdeleyenda.flamethrower_lib.responder;

import java.util.ListIterator;

import es.reinosdeleyenda.flamethrower_lib.window.TextTree;

public class IteratorModifiedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8829946663215489331L;

	private ListIterator<TextTree.Line> iterator = null;
	
	public IteratorModifiedException(ListIterator<TextTree.Line> i) {
		setIterator(i);
	}

	public void setIterator(ListIterator<TextTree.Line> iterator) {
		this.iterator = iterator;
	}

	public ListIterator<TextTree.Line> getIterator() {
		return iterator;
	}
	
}
