import org.junit.AfterClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class DeleteTest {
  private final TestHelper helper = new TestHelper();

  private static final Map<String, String> times = new HashMap<>();

  @Test(timeout = 10000)
  public void testTreeSetDelete() {
    Tree<Integer> tree = new JavaTree();
    long time = helper.performOperations(tree, OperationType.DELETE);

    String result = ((double) time / 1000000) + " ms";

    times.put("Java TreeSet", result);
    System.out.println("Time taken to perform deletes (Java TreeSet): " + result);
  }

  @Test(timeout = 10000)
  public void testSkipListDelete() {
    Tree<Integer> tree = new JavaSkipList();
    long time = helper.performOperations(tree, OperationType.DELETE);

    String result = ((double) time / 1000000) + " ms";

    times.put("Java SkipList", result);
    System.out.println("Time taken to perform deletes (Java SkipList): " + result);
  }

  @Test(timeout = 2000)
  public void testFineGrainedDelete() {
    Tree<Integer> tree = new FineGrainBST<>(1);
    long time = helper.performOperations(tree, OperationType.DELETE);

    assertTrue(TestHelper.verifyIntegerTree(tree));

    String result = ((double) time / 1000000) + " ms";

    times.put("Fine Grained", result);
    System.out.println("Time taken to perform deletes (Fine-Grained): " + result);
  }

  @Test(timeout = 10000)
  public void testLockFreeDelete() {
    Tree<Integer> tree = new LockFreeBST<>(1, 200);
    long time = helper.performOperations(tree, OperationType.DELETE);

    assertTrue(TestHelper.verifyIntegerTree(tree));

    String result = ((double) time / 1000000) + " ms";

    times.put("Lock Free", result);
    System.out.println("Time taken to perform deletes (Lock Free): " + result);
  }

//  @Test(timeout = 10000)
//  public void testILockFreeDelete() {
//    Tree<Integer> tree = new ILockFreeBST<>(Integer.MAX_VALUE - 2, Integer.MAX_VALUE - 1, Integer.MAX_VALUE);
//    long time = helper.performOperations(tree, OperationType.DELETE);
//
//    assertTrue(TestHelper.verifyIntegerTree(tree));
//
//    String result = ((double) time / 1000000) + " ms";
//
//    times.put("Internal Lock Free", result);
//    System.out.println("Time taken to perform deletes (Internal Lock Free): " + result);
//  }

  @AfterClass
  public static void summarizeDeletes() {
    System.out.println("\nDelete Times:\n--------------------");
    for (Map.Entry entry : times.entrySet()) {
      System.out.println(entry.getKey() + ": " + entry.getValue());
    }
    System.out.println();
  }
}
