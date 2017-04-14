public class TreeThread<T> extends Thread {
  private final Tree<T> tree;

  public TreeThread(Tree<T> tree) {
    this.tree = tree;
  }

  @Override
  public void run() {

  }
}
