public class Leaf<T extends Comparable> extends Node<T> {
	//dummy class for marking node type
	//left, right, key are stored in Node
	public Leaf(T value) {
		super(value);
	}
}