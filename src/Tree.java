public interface Tree<T> {
	public boolean insert(Node node);
	public boolean delete(Node node);
	public boolean search(T value);
}