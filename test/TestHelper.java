import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

class TestHelper {
  private static final int NUM_THREADS = 8;
  private static final int NUM_OPERATIONS = 1000;

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
      return 2L;
    } else {
      return 3L;
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
   * A Thread to perform deletes on a tree.
   */
  private class DeleteThread implements Callable<Void> {
    private final Tree<Integer> tree;
    private final int deletes;

    DeleteThread(Tree<Integer> tree, int deletes) {
      this.tree = tree;
      this.deletes = deletes;
    }

    @Override
    public Void call() {
      for (int i = 0; i < deletes; i++) {
        int number = ThreadLocalRandom.current().nextInt();

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
