import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class DeleteTest {
  private final TestHelper helper = new TestHelper();

  @Test
  public void testTreeSet() {
    // This tests the Java TreeSet implementation. No verification is done,
    // this is simply for timing purposes.
    Tree<Integer> tree = new JavaTree();
    long time = helper.performOperations(tree, OperationType.DELETE);

    System.out.println("Time taken to perform deletes (Java TreeSet): " + ((double) time / 1000000) + " ms");
  }

  @Test
  public void testFineGrainedInsert() {
    Tree<Integer> tree = new FineGrainBST<>(1);
    long time = helper.performOperations(tree, OperationType.DELETE);

    assertTrue(TestHelper.verifyIntegerTree(tree));

    System.out.println("Time taken to perform deletes (Fine-Grained): " + ((double) time / 1000000) + " ms");
  }

  @Test
  public void testLockFreeInsert() {
    Tree<Integer> tree = new LockFreeBST<>(1, 200);
    long time = helper.performOperations(tree, OperationType.DELETE);

    assertTrue(TestHelper.verifyIntegerTree(tree));

    System.out.println("Time taken to perform deletes (Lock Free): " + ((double) time / 1000000) + " ms");
  }

  @Test
  public void testILockFreeInsert() {
    Tree<Integer> tree = new ILockFreeBST<>(Integer.MAX_VALUE - 2, Integer.MAX_VALUE - 1, Integer.MAX_VALUE);
    long time = helper.performOperations(tree, OperationType.DELETE);

    assertTrue(TestHelper.verifyIntegerTree(tree));

    System.out.println("Time taken to perform deletes (Internal Lock Free): " + ((double) time / 1000000) + " ms");
  }
}