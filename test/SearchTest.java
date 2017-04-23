import org.junit.AfterClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class SearchTest {
  private final TestHelper helper = new TestHelper();

  private static final Map<String, String> times = new HashMap<>();

  @Test(timeout = 10000)
  public void testTreeSetSearch() {
    // This tests the Java TreeSet implementation. No verification is done,
    // this is simply for timing purposes.
    Tree<Integer> tree = new JavaTree();
    long time = helper.performOperations(tree, OperationType.SEARCH);

    String result = ((double) time / 1000000) + " ms";

    times.put("Java TreeSet", result);
    System.out.println("Time taken to perform searches (Java TreeSet): " + result);
  }

  @Test(timeout = 10000)
  public void testFineGrainedSearch() {
    Tree<Integer> tree = new FineGrainBST<>(1);
    long time = helper.performOperations(tree, OperationType.SEARCH);

    assertTrue(TestHelper.verifyIntegerTree(tree));

    String result = ((double) time / 1000000) + " ms";

    times.put("Fine Grained", result);
    System.out.println("Time taken to perform searches (Fine-Grained): " + result);
  }

  @Test(timeout = 10000)
  public void testLockFreeSearch() {
    Tree<Integer> tree = new LockFreeBST<>(1, 200);
    long time = helper.performOperations(tree, OperationType.SEARCH);

    assertTrue(TestHelper.verifyIntegerTree(tree));

    String result = ((double) time / 1000000) + " ms";

    times.put("Lock Free", result);
    System.out.println("Time taken to perform searches (Lock Free): " + result);
  }

  @Test(timeout = 10000)
  public void testILockFreeSearch() {
    Tree<Integer> tree = new ILockFreeBST<>(Integer.MAX_VALUE - 2, Integer.MAX_VALUE - 1, Integer.MAX_VALUE);
    long time = helper.performOperations(tree, OperationType.SEARCH);

    assertTrue(TestHelper.verifyIntegerTree(tree));

    String result = ((double) time / 1000000) + " ms";

    times.put("Internal Lock Free", result);
    System.out.println("Time taken to perform searches (Internal Lock Free): " + result);
  }

  @AfterClass
  public static void summarizeSearches() {
    System.out.println("\nSearch Times:\n--------------------");
    for (Map.Entry entry : times.entrySet()) {
      System.out.println(entry.getKey() + ": " + entry.getValue());
    }
    System.out.println();
  }
}
