import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class InsertTest {
  private static final int NUM_THREADS = 4;
  private static final int NUM_INSERTS = 20;

  private final Tree<Integer> tree = new DefaultTree<>();

  @Test
  public void testDefaultInsert() {
    List<InsertThread> threads = new ArrayList<>();

    // Start threads
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < NUM_THREADS; i++) {
      InsertThread<Integer> thread = new InsertThread<>(tree, NUM_INSERTS);
      threads.add(thread);

      thread.start();
    }

    // Wait for threads to finish
    for (InsertThread thread : threads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        System.out.println("Thread interrupted.");
      }
    }
    long endTime = System.currentTimeMillis();

    // Verify tree is correct
    assertTrue(verifyIntegerTree(tree));

    // Display time taken to insert
    System.out.println("Time taken for insert: " + (endTime - startTime));
  }

  private boolean verifyIntegerTree(Tree<Integer> tree) {
    // TODO: change to call verifyBST(root, Integer.MIN_VALUE, Integer.MAX_VALUE);
    return true;
  }

  private boolean verifyBST(Node<Integer> node, int min, int max) {
    if (node == null) {
      return true;
    }

    if (node.getValue() < min || node.getValue() > max) {
      return false;
    }

    // Recursively check
    return verifyBST(node.getLeft(), min, node.getValue() - 1)
        && verifyBST(node.getRight(), node.getValue() + 1, max);
  }

  private class InsertThread<T> extends Thread {
    private final Tree<T> tree;
    private final int inserts;

    InsertThread(Tree<T> tree, int inserts) {
      this.tree = tree;
      this.inserts = inserts;
    }

    @Override
    public void run() {
      for (int i = 0; i < inserts; i++) {
        Node<Integer> node = new Node<>(i);

        tree.insert(node);
      }
    }
  }
}
