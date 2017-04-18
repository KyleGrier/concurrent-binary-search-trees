import java.util.TreeSet;

public class JavaTree implements Tree<Integer> {
  TreeSet<Integer> tree = new TreeSet<>();

  public synchronized boolean insert(Node node) {
    return tree.add((Integer) node.getValue());
  }

  public synchronized boolean delete(Node node) {
    return tree.remove(node.getValue());
  }

  public synchronized boolean search(Integer value) {
    for (Integer i : tree) {
      if (i.equals(value)) return true;
    }
    return false;
  }
}
