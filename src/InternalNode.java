import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;
public class InternalNode<T extends Comparable> extends Node {
	AtomicStampedReference<Info> update;
	AtomicReference<Node> left;
	AtomicReference<Node> right;

	public InternalNode(T value, Node left, Node right, AtomicStampedReference<Info> update) {
		super(value);
		this.left = new AtomicReference(left);
		this.right = new AtomicReference(right);
		this.update = update;

	}

	//left, right, key are stored in Node
}