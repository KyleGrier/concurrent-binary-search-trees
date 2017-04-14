public interface Tree<T> {
  boolean insert(Node node);

  boolean delete(Node node);

  boolean search(T value);
}