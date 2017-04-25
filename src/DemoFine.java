import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by kyle on 4/25/2017.
 */
public class DemoFine {
    public static void main(String args[]) {
        System.out.println("Demoing external lock-free binary search tree...");

        System.out.println("Initializing Tree...");
        final FineGrainBST<Integer> tree = new FineGrainBST<>(20);

        System.out.println("Creating two threads to insert values up to 10000...");
        final ArrayList<Integer> thread1 = new ArrayList<>();
        final ArrayList<Integer> thread2 = new ArrayList<>();


        try {
            Thread t1 = new Thread() {
                public void run() {
                    for(int i = 0; i< 10000; i+=2) {
                        thread1.add(i);
                        tree.insert(i);
                    }

                }
            };

            Thread t2 = new Thread() {
                public void run() {
                    for (int i = 10000; i < 0; i-=3) {
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
        for(Integer e: allValues) {
            //System.out.println(tree.search(e));
            if(tree.search(e)) {
                found++;
            }
        }

        boolean foundAll = true;
        if (allValues.size() - found > 0) {
            foundAll = false;
        }

        System.out.println("Finished searching. Found all? " + foundAll);
        System.out.println("Creating two threads to delete all values in the tree");

        try {
            Thread t1 = new Thread() {
                public void run() {
                    for (Integer e: thread1) {
                        tree.delete(e);
                    }

                }
            };

            Thread t2 = new Thread() {
                public void run() {
                    for (Integer e: thread2) {
                        tree.delete(e);
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

        boolean foundNone = true;
        if (found > 0) {
            foundNone = false;
        }

        System.out.println("Finished searching. All removed? " + foundNone);

        System.out.println("**********************************************");

        /*System.out.println("Demoing internal lock-free binary search tree");

        System.out.println("Initializing Tree...");
        int INF_U = Integer.MAX_VALUE;
        int INF_T = Integer.MAX_VALUE - 1;
        int INF_R = Integer.MAX_VALUE - 2;
        final ILockFreeBST<Integer> ITree = new ILockFreeBST<>(INF_R, INF_T, INF_U);

        System.out.println("Inserting values into tree...");

        final ArrayList<Integer> IThread1 = new ArrayList<Integer>();
        final ArrayList<Integer> IThread2 = new ArrayList<Integer>();


        try {
            Thread t1 = new Thread() {
                public void run() {
                    for (int i = 0; i < 10; i++) {
                        int a = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
                        IThread1.add(a);
                        ITree.insert(a);
                    }

                }
            };

            Thread t2 = new Thread() {
                public void run() {
                    for (int i = 0; i < 10; i++) {
                        int a = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
                        IThread2.add(a);
                        ITree.insert(a);
                    }
                }
            };

            t1.start();
            t2.start();

            t1.join();
            t2.join();

        } catch (InterruptedException e) {

        }
        allValues.clear();
        allValues.addAll(IThread1);
        allValues.addAll(IThread2);

        System.out.println("Finished inserting. Sorted list of inserted values in tree: ");
        Collections.sort(allValues);
        System.out.println(allValues);

        System.out.println("Searching for inserted values...");
        found = 0;
        for(Integer e: allValues) {
            //System.out.println(ITree.search(e));
            if(ITree.search(e)) {
                found++;
            }
        }

        foundAll = true;
        if (allValues.size() - found > 0) {
            foundAll = false;
        }

        System.out.println("Finished searching. Found all? " + foundAll);
        System.out.println("Creating two threads to delete all values in the tree");

        try {
            Thread t1 = new Thread() {
                public void run() {
                    for (Integer e: IThread1) {
                        System.out.print("");
                        ITree.delete(e);
                    }

                }
            };

            Thread t2 = new Thread() {
                public void run() {
                    for (Integer e: IThread2) {
                        System.out.print("");
                        ITree.delete(e);
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
            if(ITree.search(e)) {
                found++;
            }
        }

        foundNone = true;
        if (found > 0) {
            foundNone = false;
        }

        System.out.println("Finished searching. All removed? " + foundNone); */

    }
}

