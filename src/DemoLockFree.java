import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.*;

/**
 * Created by Sneha on 4/24/17.
 */
public class DemoLockFree {
    public static void main(String args[]) {
        doInternalLockFreeDemo();
        doSimpleLockFreeExternalDemo();
        //doLargerLockFreeExternalDemo();

        ExecutorService executor = Executors.newCachedThreadPool();
        Callable<Object> task = new Callable<Object>() {
            public Object call() {
                return doLargerLockFreeExternalDemo();
            }
        };
        Future<Object> future = executor.submit(task);
        try {
            Object result = future.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException ex) {
            // handle the timeout
        } catch (InterruptedException e) {
            // handle the interrupts
        } catch (ExecutionException e) {
            // handle other exceptions
        } finally {
            future.cancel(true); // may or may not desire this
        }

        executor.shutdown();
        System.exit(0);
    }

    public static void doSimpleLockFreeExternalDemo() {
        System.out.println("EXTERNAL LOCK-FREE TREE DEMO 1");

        System.out.println("Demoing external lock-free binary search tree...");

        System.out.println("Initializing Tree...");
        final LockFreeBST<Integer> tree = new LockFreeBST<>(Integer.MAX_VALUE-1, Integer.MAX_VALUE);
        System.out.println("Inserting values with one thread...");
        tree.insert(30);
        tree.insert(20);
        tree.insert(40);
        tree.insert(50);
        tree.insert(10);

        System.out.println("Creating multiple threads to do some mixed inserts and deletes...");

        try {
            Thread t1 = new Thread() {
                public void run() {
                    tree.insert(51);
                    tree.insert(16);
                    tree.insert(60);
                    tree.insert(12);
                    tree.delete(51);
                }
            };

            Thread t2 = new Thread() {
                public void run() {
                    tree.delete(20);
                    tree.delete(40);
                    tree.insert(61);
                }
            };

            t1.start();
            t2.start();

            t1.join();
            t2.join();


        } catch (Exception e) {
            System.out.println(e);
        }

        System.out.println("All threads finished.");
        System.out.println("Using search to see if correct values are in the tree...");

        //should be false
        System.out.println("The following values should be false i.e. not in the tree");
        System.out.println(tree.search(20));
        System.out.println(tree.search(40));
        System.out.println(tree.search(51));



        //should be true
        System.out.println("The following values should be true i.e. in the tree");
        System.out.println(tree.search(12));
        System.out.println(tree.search(16));
        System.out.println(tree.search(60));
        System.out.println(tree.search(61));
        System.out.println(tree.search(50));
        System.out.println(tree.search(30));
        System.out.println(tree.search(10));

        System.out.println("*****************************************************");

    }

    public static boolean doLargerLockFreeExternalDemo() {
        System.out.println("EXTERNAL LOCK-FREE TREE DEMO 2");
        System.out.println("Demoing external lock-free binary search tree with lots of values...");

        System.out.println("Initializing Tree...");
        final LockFreeBST<Integer> tree = new LockFreeBST<>(Integer.MAX_VALUE-1, Integer.MAX_VALUE);

        System.out.println("Creating multiple threads to insert a bunch of values");
        final ArrayList<Integer> thread1 = new ArrayList<>();
        final ArrayList<Integer> thread2 = new ArrayList<>();


        try {
            Thread t1 = new Thread() {
                public void run() {
                    for(int i = 0; i < 5000; i+=7) {
                        thread1.add(i);
                        tree.insert(i);
                    }

                }
            };

            Thread t2 = new Thread() {
                public void run() {
                    for (int i = 10000; i > 5000; i-=10) {
                        thread2.add(i);
                        tree.insert(i);
                    }
                }
            };

            t1.start();
            t2.start();

            t1.join();
            t2.join();
        } catch (InterruptedException e) {

        }
        ArrayList<Integer> allValues = new ArrayList<>();
        allValues.addAll(thread1);
        allValues.addAll(thread2);
        System.out.println("Finished inserting. Sorted list of inserted values in tree: ");
        Collections.sort(allValues);
        System.out.println(allValues);

        System.out.println("Searching for inserted values.");
        int found = 0;
        final ArrayList<Integer> delete = new ArrayList<>();
        for(Integer e: allValues) {
            if (tree.search(e)) {
                delete.add(e);
            }
        }

        System.out.println("Finished searching for all inserted values");
        System.out.println("Creating multiple threads to delete all values in the tree");

        try {
            Thread t1 = new Thread() {
                public void run() {
                    for (Integer e: thread1) {
                        if(delete.contains(e)) {
                            tree.delete(e);
                        }
                    }

                }
            };

            Thread t2 = new Thread() {
                public void run() {
                    for (Integer e: thread2) {
                        if(delete.contains(e)) {
                            tree.delete(e);
                        }
                    }
                }
            };

            t1.start();
            t2.start();

            t1.join();
            t2.join();


        } catch (InterruptedException e) {

        }

        System.out.println("Finished deleting...");
        System.out.println("Searching for deleted values.");
        found = 0;
        for(Integer e: allValues) {
            //System.out.println(tree.search(e));
            if(tree.search(e)) {
                found++;
            }
        }

        if (found == 0) {
            System.out.println("Finished searching. " + found + " values in tree");
        } else {
            System.out.println("Finished searching. Tree is empty");
        }

        System.out.println("**********************************************");
        return true;

    }

    public static void doInternalLockFreeDemo() {
        System.out.println("INTERNAL LOCK-FREE TREE DEMO");

        System.out.println("Demoing Internal lock-free binary search tree");
        System.out.println("Initializing Tree...");
        System.out.println("Initializing Tree...");
        int INF_U = Integer.MAX_VALUE;
        int INF_T = Integer.MAX_VALUE - 1;
        int INF_R = Integer.MAX_VALUE - 2;
        final ILockFreeBST<Integer> ITree = new ILockFreeBST<>(INF_R, INF_T, INF_U);

        System.out.println("Inserting values into tree...");

        final int[] list = {0, 6, 10, 87, 23, 99, 100, -5, 28938, 77, 69, 93, 76};
        final int[] list2 = {3729, 473, 320,10000, 4329, 34379, 32893, -328948, 383, 65, 20, 1887, 987};


        try {
            Thread t1 = new Thread() {
                public void run() {
                    for (int a: list) {
                        //int a = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
                        //IThread1.add(a);
                        ITree.insert(a);
                    }

                }
            };

            Thread t2 = new Thread() {
                public void run() {
                    for (int b: list2) {
                        //int a = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
                        //IThread2.add(a);
                        ITree.insert(b);
                    }
                }
            };

            t1.start();
            t2.start();

            t1.join();
            t2.join();

        } catch (InterruptedException e) {

        }


        System.out.println("Finished inserting values");

        ArrayList<Integer> allValues = new ArrayList<>();

        System.out.println("Searching for inserted values...");
        int found = 0;
        for(Integer e: list) {
            //System.out.println(ITree.search(e));
            if(ITree.search(e)) {
                found++;
            }
        }

        for(Integer e: list2) {
            //System.out.println(ITree.search(e));
            if(ITree.search(e)) {
                found++;
            }
        }

        boolean foundAll = true;
        if (allValues.size() - found > 0) {
            foundAll = false;
        }

        System.out.println("Finished searching. Found all? " + foundAll);
        System.out.println("******************************************");

    }
}
