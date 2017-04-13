public class Node {
	T value;
	Node left;
	Node right;

	public Node(T value) {
		this.value = value;
		left = null;
		right = null;
	}

	public T getValue() {
		return T;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public Node getLeft() {
		return left;
	}

	public Node getRight() {
		return right;
	}
	
	public void setLeft(Node left) {
		this.left = left;
	}

	public void setRight(Node right) {
		this.right = right;
	}

}