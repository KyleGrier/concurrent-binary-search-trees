import java.util.TreeSet;

public class JavaTree implements Tree<Integer> {
  TreeSet<Integer> tree = new TreeSet<>();

  public synchronized boolean insert(Integer value) {
    return tree.add(value);
  }

  public synchronized boolean delete(Integer value) {
    return tree.remove(value);
  }

  public synchronized boolean search(Integer value) {
    return tree.contains(value);
  }

  public synchronized Object getRoot() {
    return tree.first();
  }
}
