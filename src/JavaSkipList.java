import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class JavaSkipList implements Tree<Integer> {
  Set<Integer> tree = new ConcurrentSkipListSet<>();

  public boolean insert(Integer value) {
    return tree.add(value);
  }

  public boolean delete(Integer value) {
    return tree.remove(value);
  }

  public boolean search(Integer value) {
    return tree.contains(value);
  }

  public Object getRoot() {
    return null;
  }
}
