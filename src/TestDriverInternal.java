/**
 * Created by Sneha on 4/22/17.
 */
public class TestDriverInternal {
    public static void main(String[] args) {
        int INF_U = Integer.MAX_VALUE;
        int INF_T = Integer.MAX_VALUE - 1;
        int INF_R = Integer.MAX_VALUE - 2;
        final ILockFreeBST<Integer> tree = new ILockFreeBST<>(INF_R, INF_T, INF_U);

        System.out.println(tree.insert(30));
        System.out.println(tree.insert(20));
        System.out.println(tree.insert(40));
        System.out.println(tree.insert(50));
        System.out.println(tree.insert(10));


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

            System.out.println("Searching");

            //should be false
            System.out.println("Following should be false");
            System.out.println(tree.search(20));
            System.out.println(tree.search(40));
            System.out.println(tree.search(51));


            //should be true
            System.out.println("Following should be true");
            System.out.println(tree.search(12));
            System.out.println(tree.search(16));
            System.out.println(tree.search(60));
            System.out.println(tree.search(61));
            System.out.println(tree.search(50));
            System.out.println(tree.search(30));
            System.out.println(tree.search(10));


        } catch (Exception e) {
            System.out.println(e);
        }





    }
}
