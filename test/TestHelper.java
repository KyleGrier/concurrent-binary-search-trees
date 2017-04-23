import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

class TestHelper {
  private static final int NUM_THREADS = 8;
  private static final int NUM_OPERATIONS = 5000;

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
    if (operation.equals(OperationType.INSERT)) {
      return performInserts(tree);
    } else if (operation.equals(OperationType.SEARCH)) {
      return 1L;
    } else if (operation.equals(OperationType.DELETE)) {
      return performDeletes(tree);
    } else {
      return 3L;
    }
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

    return true;
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
    return verifyFineGrainedBST(node.getLeft(), min, node.getValue() - 1)
        && verifyFineGrainedBST(node.getRight(), node.getValue() + 1, max);
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

  private static void buildInOrderList(Node<Integer> node, List<Integer> values) {
    if (node == null) {
      return;
    }

    // If it's a leaf, we care about the value
    if (node instanceof Leaf) {
      values.add(node.getValue());
    } else if (node instanceof InternalNode) {
      Node<Integer> left = (Node<Integer>) ((InternalNode) node).left.get();
      Node<Integer> right = (Node<Integer>) ((InternalNode) node).right.get();

      buildInOrderList(left, values);
      buildInOrderList(right, values);
    } else {
      System.out.println("Something went horribly wrong.");
      return;
    }
  }

  private static void buildInOrderList(INode<Integer> node, List<Integer> values) {
    if (!((node.child[0].getStamp() & INode.NULL_BIT) == INode.NULL_BIT)) {
      // Left child
      buildInOrderList(node.child[0].getReference(), values);
    }

    values.add(node.mKey.getReference());

    if (!((node.child[1].getStamp() & INode.NULL_BIT) == INode.NULL_BIT)) {
      // Right child
      buildInOrderList(node.child[1].getReference(), values);
    }

  }

  private Long performInserts(Tree<Integer> tree) {
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
      }
    }
    long endTime = System.nanoTime();

    // Return time taken to insert
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
      }
    }
    long endTime = System.nanoTime();

    System.out.println("Done deleting.");

    // Return time taken to insert
    return endTime - startTime;
  }

  /**
   * A Thread to perform searches on a tree.
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
   * A Thread to perform searches on a tree.
   */
  private class SearchThread implements Callable<Void> {
    private final Tree<Integer> tree;
    private final int searches;

    private final List<Integer> found;
    private final List<Integer> notFound;

    SearchThread(Tree<Integer> tree, int searches) {
      this.tree = tree;
      this.searches = searches;

      this.found = new ArrayList<>();
      this.notFound = new ArrayList<>();
    }

    @Override
    public Void call() {
      for (int i = 0; i < searches; i++) {
        int number = ThreadLocalRandom.current().nextInt();

        if (tree.search(number)) {
          found.add(number);
        } else {
          notFound.add(number);
        }
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
        tree.insert(number);
      }

      return values;
    }
  }

  /**
   * A Thread to perform deletes on a tree.
   */
  private class DeleteThread implements Callable<Void> {
    private final Tree<Integer> tree;
    private final List<Integer> values;

    DeleteThread(Tree<Integer> tree, List<Integer> values) {
      this.tree = tree;
      this.values = values;
    }

    @Override
    public Void call() {
      for (Integer number : values) {
        tree.delete(number);
      }

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
        int number = ThreadLocalRandom.current().nextInt();

        int index = ThreadLocalRandom.current().nextInt(inserted.size());

        switch (type) {
          case INSERT:
            tree.insert(number);

            inserted.add(number);
            break;

          case SEARCH:
            tree.search(inserted.get(index));
            break;

          case DELETE:
            tree.delete(inserted.get(index));

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
