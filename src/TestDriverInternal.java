import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Sneha on 4/22/17.
 */
public class TestDriverInternal {
    public static void main(String[] args) {
        int INF_U = Integer.MAX_VALUE;
        int INF_T = Integer.MAX_VALUE - 1;
        int INF_R = Integer.MAX_VALUE - 2;
        final ILockFreeBST<Integer> tree = new ILockFreeBST<>(INF_R, INF_T, INF_U);

        /*System.out.println(tree.insert(30));
        System.out.println(tree.insert(20));
        System.out.println(tree.insert(40));
        System.out.println(tree.insert(50));
        System.out.println(tree.insert(10)); */

        final ArrayList<Integer> thread1 = new ArrayList<Integer>();
        final ArrayList<Integer> thread2 = new ArrayList<Integer>();


        try {
            Thread t1 = new Thread() {
                public void run() {
                    for (int i = 0; i < 50; i++) {
                        int a = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
                        thread1.add(a);
                        tree.insert(a);
                    }

                }
            };

            Thread t2 = new Thread() {
                public void run() {
                    for (int i = 0; i < 50; i++) {
                        int a = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
                        thread2.add(a);
                        tree.insert(a);
                    }
                }
            };

            t1.start();
            t2.start();

            t1.join();
            t2.join();

            System.out.println("Searching");

            //should be false
            System.out.println("Following should be true");

            for(int a: thread1) {
                System.out.println(tree.search(a));
            }

            for (int b: thread2) {
                System.out.println(tree.search(b));
            }


            //should be false
            //System.out.println("Following should be true");

            /*System.out.println(tree.search(20));
            System.out.println(tree.search(40));
            System.out.println(tree.search(51));
            System.out.println(tree.search(60));


            //should be true
            //System.out.println("Following should be true");
            System.out.println(tree.search(12));
            System.out.println(tree.search(16));

            System.out.println(tree.search(50));
            System.out.println(tree.search(30));
            System.out.println(tree.search(10)); */

            System.out.println("Deleting...");


            Thread t3 = new Thread() {
                public void run() {
                    //System.out.println(tree.delete(30));
                    for (int a: thread1) {
                        System.out.println(tree.delete(a) + " " + a);
                    }

                }
            };

            Thread t4 = new Thread() {
                public void run() {
                    for(int a: thread2) {
                        System.out.println(tree.delete(a) + " " + a);
                    }
                }
            };


            t3.start();
            t4.start();

            t3.join();
            t4.join();

            System.out.println("Searching");

            //should be false
            System.out.println("Following should be false");

            for(int a: thread1) {
                System.out.println(tree.search(a));
            }

            for (int b: thread2) {
                System.out.println(tree.search(b));
            }


            /*System.out.println(tree.search(30));



            System.out.println(tree.search(20));
            System.out.println(tree.search(40));
            System.out.println(tree.search(51));
            System.out.println(tree.search(60));


            //should be true
            //System.out.println("Following should be true");
            System.out.println(tree.search(12));
            System.out.println(tree.search(16));

            System.out.println(tree.search(50));
            System.out.println(tree.search(10)); */


        } catch (Exception e) {
            System.out.println(e);
        }





    }
}
