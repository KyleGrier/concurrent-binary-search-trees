import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

class TestHelper {
  private static final int NUM_THREADS = 2;
  private static final int NUM_OPERATIONS = 10;
  private static final int RANDOM_ADDS = 5;

  private final ExecutorService service = Executors.newFixedThreadPool(NUM_THREADS);

  /**
   * Performs the required operations on the tree.
   *
   * @param tree The tree to perform the searches on.
   * @param operation The type of operation to perform.
   *
   * @return The time taken to perform the searches in nanoseconds
   */
  Long performOperations(Tree<Integer> tree,
                         OperationType operation) {
    if (operation == null) {
      return performMixedOperations(tree);
    } else if(operation.equals(OperationType.INSERT)) {
      return performInserts(tree);
    } else if (operation.equals(OperationType.SEARCH)) {
      return performSearches(tree);
    } else if (operation.equals(OperationType.DELETE)) {
      return performDeletes(tree);
    }

    // Unknown operation
    System.out.println("An error occurred: Unknown Operation");
    return null;
  }

  /**
   * Verifies that the given tree is a binary search tree.
   *
   * @param tree The tree to verify.
   *
   * @return True if the tree is a BST, false otherwise.
   */
  static boolean verifyIntegerTree(Tree<Integer> tree) {
    if (tree instanceof FineGrainBST) {
      FineNode<Integer> root = (FineNode<Integer>) tree.getRoot();

      return verifyFineGrainedBST(root, Integer.MIN_VALUE, Integer.MAX_VALUE);
    } else if (tree instanceof LockFreeBST) {
      InternalNode<Integer> root = (InternalNode<Integer>) tree.getRoot();

      return verifyLockFreeBST(root);
    } else if (tree instanceof ILockFreeBST) {
      INode<Integer> root = (INode<Integer>) tree.getRoot();

      return verifyILockFreeBST(root);
    }

    // If unknown type of tree, return false
    return false;
  }

  /**
   * A helper method to verify the BST property.
   *
   * @param node The root node.
   * @param min The minimum value that this node can contain.
   * @param max The maximum value that this node can contain.
   *
   * @return True if the tree starting the the root node is a BST, false otherwise.
   */
  private static boolean verifyFineGrainedBST(FineNode<Integer> node, int min, int max) {
    if (node == null) {
      return true;
    }

    if (node.getValue() < min || node.getValue() > max) {
      return false;
    }

    // Recursively check
    return verifyFineGrainedBST(node.getLeftNoLock(), min, node.getValue() - 1)
        && verifyFineGrainedBST(node.getRightNoLock(), node.getValue() + 1, max);
  }

  /**
   * A helper method to verify the BST property.
   *
   * @param node The root node.
   *
   * @return True if the tree starting the the root node is a BST, false otherwise.
   */
  private static boolean verifyLockFreeBST(Node<Integer> node) {
    if (node == null) {
      return true;
    }

    List<Integer> values = new ArrayList<>();
    buildInOrderList(node, values);

    return values.stream().sorted().collect(Collectors.toList()).equals(values);
  }

  /**
   * A helper method to verify the BST property.
   *
   * @param node The root node.
   *
   * @return True if the tree starting the the root node is a BST, false otherwise.
   */
  private static boolean verifyILockFreeBST(INode<Integer> node) {
    if (node == null) {
      return true;
    }

    List<Integer> values = new ArrayList<>();
    buildInOrderList(node, values);

    return values.stream().sorted().collect(Collectors.toList()).equals(values);
  }

  /**
   * Builds a list of values using in-order traversal.
   *
   * @param node The root node to begin building at.
   * @param values The list to insert values to.
   */
  private static void buildInOrderList(Node<Integer> node, List<Integer> values) {
    if (node == null) {
      return;
    }

    // If it's a leaf, we care about the value
    if (node instanceof Leaf) {
      values.add(node.getValue());

      //System.out.println(node.getValue() + " ");
    } else if (node instanceof InternalNode) {
      Node<Integer> left = (Node<Integer>) ((InternalNode) node).left.get();
      Node<Integer> right = (Node<Integer>) ((InternalNode) node).right.get();

      buildInOrderList(left, values);
      buildInOrderList(right, values);
    } else {
      System.out.println("Something went horribly wrong.");
    }
  }

  /**
   * Builds a list of values using in-order traversal.
   *
   * @param node The root node to begin building at.
   * @param values The list to insert values to.
   */
  private static void buildInOrderList(INode<Integer> node, List<Integer> values) {
    if (!((node.child[0].getStamp() & INode.NULL_BIT) == INode.NULL_BIT)) {
      // Left child
      buildInOrderList(node.child[0].getReference(), values);
    }

    values.add(node.mKey.getReference());
//    System.out.println(node.mKey.getReference());

    if (! ((node.child[1].getStamp() & INode.NULL_BIT) == INode.NULL_BIT) ) {
      // Right child
      buildInOrderList(node.child[1].getReference(), values);
    }

  }

  /* ********** PERFORM OPERATION METHODS ********** */

  /**
   * Performs concurrent inserts to a tree.
   *
   * @param tree The tree to insert to.
   * @return The time taken to perform the inserts in nanoseconds.
   */
  private Long performInserts(Tree<Integer> tree) {
    System.out.println("Running insert test. Adding values...");

    List<Future> futures = new ArrayList<>();

    // Start threads
    long startTime = System.nanoTime();
    for (int i = 0; i < NUM_THREADS; i++) {
      Future<Void> future = service.submit(new InsertThread(tree, NUM_OPERATIONS));
      futures.add(future);
    }

    // Wait for threads to finish
    for (Future future : futures) {
      try {
        future.get();
      } catch (ExecutionException | InterruptedException e) {
        System.out.println("Thread interrupted.");
        e.printStackTrace();
      }
    }
    long endTime = System.nanoTime();

    System.out.println("Done adding to tree.");

    // Return time taken to insert
    return endTime - startTime;
  }

  private Long performSearches(Tree<Integer> tree) {
    System.out.println("Running Search Test. Adding values first...");

    List<Future<List<Integer>>> futures = new ArrayList<>();
    List<List<Integer>> valueLists = new ArrayList<>();
    List<List<Integer>> inserted = new ArrayList<>();
    Set<Integer> allInserted = new HashSet<>();
    Set<Integer> extraNumbers = new HashSet<>();

    // Start insert threads
    for (int i = 0; i < NUM_THREADS; i++) {
      Future<List<Integer>> future = service.submit(new InsertMemoryThread(tree, NUM_OPERATIONS));
      futures.add(future);
    }

    // Wait for threads to finish
    for (Future<List<Integer>> future : futures) {
      try {
        List<Integer> values = future.get();

        inserted.add(values);
        valueLists.add(values);
        allInserted.addAll(values);
      } catch (ExecutionException | InterruptedException e) {
        System.out.println("Thread interrupted.");
        e.printStackTrace();
      }
    }

    System.out.println("Done adding to tree, adding non-existent values...");
    for (int i = 0; i < RANDOM_ADDS; i++) {
      int number = ThreadLocalRandom.current().nextInt();
      int index = ThreadLocalRandom.current().nextInt(NUM_THREADS);

      if (allInserted.contains(number)) {
        i--;
        continue;
      }

      valueLists.get(index).add(number);
      extraNumbers.add(number);
    }
    System.out.println("Done adding values, now searching...");

    List<Future<SearchResult<Integer>>> searchFutures = new ArrayList<>();

    // Start search threads
    long startTime = System.nanoTime();
    for (int i = 0; i < NUM_THREADS; i++) {
      Future<SearchResult<Integer>> future = service.submit(new SearchThread(tree, valueLists.get(i)));
      searchFutures.add(future);
    }

    // Wait for threads to finish
    for (Future<SearchResult<Integer>> future : searchFutures) {
      try {
        SearchResult<Integer> result = future.get();

        /* Assert that all not found were added by us */
//        System.out.println("Not found: " + result.getNotFound());
//        System.out.println("Found: " + result.getFound());
//        System.out.println("All values added as extra: " + extraNumbers);
//        if (!extraNumbers.containsAll(result.getNotFound())) {
//          System.out.println("ERROR!!!!!!!!!!");
//
//          List<Integer> diff = result.getNotFound();
//          diff.removeAll(extraNumbers);
//          System.out.println("Not Found Extra Elements: " + diff);
//        }
//        System.out.println();

        /* Assert that all found were added by the thread */
//        if (!inserted.containsAll(result.getFound())) {
//          System.out.println("ERROR TWO!!!!!!!");
//        }

      } catch (ExecutionException | InterruptedException e) {
        System.out.println("Thread interrupted.");
        e.printStackTrace();
      }
    }
    long endTime = System.nanoTime();

    System.out.println("Done searching.");

    // Return time taken to search
    return endTime - startTime;
  }

  private Long performDeletes(Tree<Integer> tree) {
    System.out.println("Running Delete Test. Adding values first...");

    List<Future<List<Integer>>> futures = new ArrayList<>();
    List<List<Integer>> valueLists = new ArrayList<>();

    // Start insert threads
    for (int i = 0; i < NUM_THREADS; i++) {
      Future<List<Integer>> future = service.submit(new InsertMemoryThread(tree, NUM_OPERATIONS));
      futures.add(future);
    }

    // Wait for threads to finish
    for (Future<List<Integer>> future : futures) {
      try {
        valueLists.add(future.get());
      } catch (ExecutionException | InterruptedException e) {
        System.out.println("Thread interrupted.");
        e.printStackTrace();
      }
    }

    System.out.println("Done adding to tree, now deleting...");

    List<Future> deleteFutures = new ArrayList<>();

    // Start delete threads
    long startTime = System.nanoTime();
    for (int i = 0; i < NUM_THREADS; i++) {
      Future<Void> future = service.submit(new DeleteThread(tree, valueLists.get(i)));
      deleteFutures.add(future);
    }

    // Wait for threads to finish
    for (Future future : deleteFutures) {
      try {
        future.get();
      } catch (ExecutionException | InterruptedException e) {
        System.out.println("Thread interrupted.");
        e.printStackTrace();
      }
    }
    long endTime = System.nanoTime();

    System.out.println("Done deleting.");

    // Return time taken to insert
    return endTime - startTime;
  }

  private Long performMixedOperations(Tree<Integer> tree) {
    System.out.println("Running mixed test...");

    List<Future> futures = new ArrayList<>();

    // Start threads
    long startTime = System.nanoTime();
    for (int i = 0; i < NUM_THREADS; i++) {
      Future<Void> future = service.submit(new MixedThread(tree, NUM_OPERATIONS));
      futures.add(future);
    }

    // Wait for threads to finish
    for (Future future : futures) {
      try {
        future.get();
      } catch (ExecutionException | InterruptedException e) {
        System.out.println("Thread interrupted.");
        e.printStackTrace();
      }
    }
    long endTime = System.nanoTime();

    System.out.println("Done performing mixed test.");

    // Return time taken to insert
    return endTime - startTime;
  }

  /* ********** THREAD CLASSES ********** */

  /**
   * A Thread to perform inserts on a tree.
   */
  private class InsertThread implements Callable<Void> {
    private final Tree<Integer> tree;
    private final int inserts;

    InsertThread(Tree<Integer> tree, int inserts) {
      this.tree = tree;
      this.inserts = inserts;
    }

    @Override
    public Void call() {
      for (int i = 0; i < inserts; i++) {
        int number = ThreadLocalRandom.current().nextInt();

        tree.insert(number);
      }

      return null;
    }
  }

  /**
   * A Thread to perform inserts on a tree and remember the values.
   */
  private class InsertMemoryThread implements Callable<List<Integer>> {
    private final Tree<Integer> tree;
    private final int inserts;

    private final List<Integer> values;

    InsertMemoryThread(Tree<Integer> tree, int inserts) {
      this.tree = tree;
      this.inserts = inserts;

      this.values = new ArrayList<>();
    }

    @Override
    public List<Integer> call() {
      for (int i = 0; i < inserts; i++) {
        int number = ThreadLocalRandom.current().nextInt();

        values.add(number);
        if (!tree.insert(number)) {
          System.out.println("Failed insert: " + number);
        }
      }

      return values;
    }
  }

  /**
   * A Thread to perform searches on a tree.
   */
  private class SearchThread implements Callable<SearchResult<Integer>> {
    private final Tree<Integer> tree;
    private final List<Integer> values;

    private final List<Integer> found;
    private final List<Integer> notFound;

    SearchThread(Tree<Integer> tree, List<Integer> values) {
      this.tree = tree;
      this.values = values;

      this.found = new ArrayList<>();
      this.notFound = new ArrayList<>();
    }

    @Override
    public SearchResult<Integer> call() {
      for (Integer number : values) {
        if (tree.search(number)) {
          found.add(number);
//          System.out.println("Found: " + number);
        } else {
          notFound.add(number);
//          System.out.println("Not found: " + number);
        }
      }

      return new SearchResult<>(found, notFound);
    }
  }

  /**
   * A Thread to perform deletes on a tree.
   */
  private class DeleteThread implements Callable<Void> {
    private final Tree<Integer> tree;
    private final List<Integer> values;

    private int failures = 0;

    DeleteThread(Tree<Integer> tree, List<Integer> values) {
      this.tree = tree;
      this.values = values;
    }

    @Override
    public Void call() {
      for (Integer number : values) {
        if (!tree.delete(number)) {
//          System.out.println("Unable to delete: " + number);
          failures++;
        }
      }

//      System.out.println("Thread had " + failures + " failures");
      return null;
    }
  }

  /**
   * A Thread to perform tree operations
   */
  private class MixedThread implements Callable<Void> {
    private final Tree<Integer> tree;
    private final int operations;

    private final List<Integer> inserted;

    MixedThread(Tree<Integer> tree, int operations) {
      this.tree = tree;
      this.operations = operations;

      this.inserted = new ArrayList<>();
    }

    @Override
    public Void call() {
      for (int i = 0; i < operations; i++) {
        OperationType type = OperationType.randomType();
        //System.out.println("Performing " + type);

        int number = ThreadLocalRandom.current().nextInt();

        int index;
        int value;

        switch (type) {
          case INSERT:
            tree.insert(number);

            inserted.add(number);
            break;

          case SEARCH:
            if (inserted.size() == 0) continue;

            index = ThreadLocalRandom.current().nextInt(inserted.size());
            value = inserted.get(index);
            if (!tree.search(value)) {
              System.out.println("NOT FOUND: " + value);
            }

            break;

          case DELETE:
            if (inserted.size() == 0) continue;

            index = ThreadLocalRandom.current().nextInt(inserted.size());
            value = inserted.get(index);
            if (!(tree.delete(value))) {
              System.out.println("UNABLE TO DELETE VALUE: " + value);
            }

            inserted.remove(index);
            break;

          default:
            System.out.println("Unknown operation.");
        }
      }

      return null;
    }
  }
}
