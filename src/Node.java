public class Node<T extends Comparable> {
  T value;

  public Node(T value) {
    this.value = value;

  }

  public Node(T value, Node left, Node right) {
    this.value = value;
    //this.left = left;
    //this.right = right;
  }

  public T getValue() {
    return value;
  }

  public void setValue(T value) {
    this.value = value;
  }

  /*public Node getLeft() {
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
  } */

}