public interface Tree<T> {
	public boolean insert(T node);
	public boolean delete(T node);
	public boolean search(T value);

	Object getRoot();
}