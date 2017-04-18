import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertTrue;

public class InsertTest {
  private static final int NUM_THREADS = 8;
  private static final int NUM_INSERTS = 1000;

  private final ExecutorService service = Executors.newFixedThreadPool(NUM_THREADS);

  @Test
  public void testTreeSet() {
    // This tests the Java TreeSet implementation. No verification is done,
    // this is simply for timing purposes.
    Tree<Integer> tree = new JavaTree();
    long time = performInserts(tree);

    System.out.println("Time taken to perform inserts: " + ((double) time / 1000000) + " ms");
  }

  @Test
  public void testFineGrainedInsert() {
    // TODO: replace the tree implementation with the Fine-Grained implementation
    Tree<Integer> tree = new DefaultTree<>();
    long time = performInserts(tree);

    assertTrue(verifyIntegerTree(tree));

    System.out.println("Time taken to perform inserts: " + ((double) time / 1000000) + " ms");
  }

  @Test
  public void testLockFreeInsert() {
    // TODO: replace the tree implementation with the Lock-Free implementation
    Tree<Integer> tree = new DefaultTree<>();
    long time = performInserts(tree);

    assertTrue(verifyIntegerTree(tree));

    System.out.println("Time taken to perform inserts: " + ((double) time / 1000000) + " ms");
  }

  /**
   * Performs the required inserts for each test.
   *
   * @param tree The tree to perform the inserts on.
   * @return The time taken to perform the inserts in nanoseconds
   */
  private Long performInserts(Tree<Integer> tree) {
    List<Future> threads = new ArrayList<>();

    // Start threads
    long startTime = System.nanoTime();
    for (int i = 0; i < NUM_THREADS; i++) {
      Future<Void> future = service.submit(new InsertThread<>(tree, NUM_INSERTS));
      threads.add(future);
    }

    // Wait for threads to finish
    for (Future thread : threads) {
      try {
        thread.get();
      } catch (ExecutionException | InterruptedException e) {
        System.out.println("Thread interrupted.");
      }
    }
    long endTime = System.nanoTime();

    // Return time taken to insert
    return endTime - startTime;
  }

  /**
   * Verifies that the given tree is a binary search tree.
   *
   * @param tree The tree to verify.
   * @return True if the tree is a BST, false otherwise.
   */
  private boolean verifyIntegerTree(Tree<Integer> tree) {
    // TODO: change to call verifyBST(root, Integer.MIN_VALUE, Integer.MAX_VALUE);
    return true;
  }

  /**
   * A helper method to verify the BST property.
   *
   * @param node The root node.
   * @param min The minimum value that this node can contain.
   * @param max The maximum value that this node can contain.
   * @return True if the tree starting the the root node is a BST, false otherwise.
   */
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

  /**
   * A Thread to perform inserts on a tree.
   *
   * @param <T> The type of nodes that the tree contains.
   */
  private class InsertThread<T> implements Callable<Void> {
    private final Tree<T> tree;
    private final int inserts;

    InsertThread(Tree<T> tree, int inserts) {
      this.tree = tree;
      this.inserts = inserts;
    }

    @Override
    public Void call() {
      for (int i = 0; i < inserts; i++) {
        int number = ThreadLocalRandom.current().nextInt();

        Node<Integer> node = new Node<>(number);
        tree.insert(node);
      }

      return null;
    }
  }
}
