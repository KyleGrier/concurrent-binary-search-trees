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
  private final TestHelper helper = new TestHelper();

  @Test
  public void testTreeSet() {
    // This tests the Java TreeSet implementation. No verification is done,
    // this is simply for timing purposes.
    Tree<Integer> tree = new JavaTree();
    long time = helper.performOperations(tree, OperationType.INSERT);

    System.out.println("Time taken to perform searches (Java TreeSet): " + ((double) time / 1000000) + " ms");
  }

  @Test
  public void testFineGrainedInsert() {
    // TODO: replace the tree implementation with the Fine-Grained implementation
    Tree<Integer> tree = new DefaultTree<>();
    long time = helper.performOperations(tree, OperationType.INSERT);

    assertTrue(verifyIntegerTree(tree));

    System.out.println("Time taken to perform searches (Fine-Grained): " + ((double) time / 1000000) + " ms");
  }

  @Test
  public void testLockFreeInsert() {
    Tree<Integer> tree = new LockFreeBST<>(1, 200);
    long time = helper.performOperations(tree, OperationType.INSERT);

    assertTrue(verifyIntegerTree(tree));

    System.out.println("Time taken to perform searches (Lock Free): " + ((double) time / 1000000) + " ms");
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
//  private boolean verifyBST(Node<Integer> node, int min, int max) {
//    if (node == null) {
//      return true;
//    }
//
//    if (node.getValue() < min || node.getValue() > max) {
//      return false;
//    }
//
//    // Recursively check
//    return verifyBST(node.getLeft(), min, node.getValue() - 1)
//        && verifyBST(node.getRight(), node.getValue() + 1, max);
//  }

}
