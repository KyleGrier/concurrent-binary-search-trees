import org.junit.AfterClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class InsertTest {
  private final TestHelper helper = new TestHelper();

  private static final Map<String, String> times = new HashMap<>();

  @Test(timeout = 10000)
  public void testTreeSetInsert() {
    // This tests the Java TreeSet implementation. No verification is done,
    // this is simply for timing purposes.
    Tree<Integer> tree = new JavaTree();
    long time = helper.performOperations(tree, OperationType.INSERT);

    String result = ((double) time / 1000000) + " ms";

    times.put("Java TreeSet", result);
    System.out.println("Time taken to perform inserts (Java TreeSet): " + result);
  }

  @Test(timeout = 10000)
  public void testSkipListInsert() {
    // This tests the Java TreeSet implementation. No verification is done,
    // this is simply for timing purposes.
    Tree<Integer> tree = new JavaSkipList();
    long time = helper.performOperations(tree, OperationType.INSERT);

    String result = ((double) time / 1000000) + " ms";

    times.put("Java SkipList", result);
    System.out.println("Time taken to perform inserts (Java SkipList): " + result);
  }

  @Test(timeout = 10000)
  public void testFineGrainedInsert() {
    Tree<Integer> tree = new FineGrainBST<>(1);
    long time = helper.performOperations(tree, OperationType.INSERT);

    assertTrue(TestHelper.verifyIntegerTree(tree));

    String result = ((double) time / 1000000) + " ms";

    times.put("Fine-Grained", result);
    System.out.println("Time taken to perform inserts (Fine-Grained): " + result);
  }

  @Test(timeout = 10000)
  public void testLockFreeInsert() {
    Tree<Integer> tree = new LockFreeBST<>(1, 200);
    long time = helper.performOperations(tree, OperationType.INSERT);

    assertTrue(TestHelper.verifyIntegerTree(tree));

    String result = ((double) time / 1000000) + " ms";

    times.put("Lock Free", result);
    System.out.println("Time taken to perform inserts (Lock Free): " + result);
  }

  @Test(timeout = 10000)
  public void testILockFreeInsert() {
    Tree<Integer> tree = new ILockFreeBST<>(Integer.MAX_VALUE - 2, Integer.MAX_VALUE - 1, Integer.MAX_VALUE);
    long time = helper.performOperations(tree, OperationType.INSERT);

    assertTrue(TestHelper.verifyIntegerTree(tree));

    String result = ((double) time / 1000000) + " ms";

    times.put("Internal Lock Free", result);
    System.out.println("Time taken to perform inserts (Internal Lock Free): " + result);
  }

  @AfterClass
  public static void summarizeInserts() {
    System.out.println("\nInsert Times:\n--------------------");
    for (Map.Entry entry : times.entrySet()) {
      System.out.println(entry.getKey() + ": " + entry.getValue());
    }
    System.out.println();
  }

}
