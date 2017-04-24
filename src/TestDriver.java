public class TestDriver {

	public static void main(String[] args) {
		final LockFreeBST<Integer> tree = new LockFreeBST<>(Integer.MAX_VALUE-1, Integer.MAX_VALUE);
		System.out.println(tree.insert(30));
		System.out.println(tree.insert(20));
		System.out.println(tree.insert(40));
		System.out.println(tree.insert(50));
		System.out.println(tree.insert(10));
		//tree.delete(20);

		for(int i = 0; i< 500; i++) {
			tree.insert(i);
		}



		try {
			Thread t1 = new Thread() {
			  public void run() {
				  for(int i = 0; i< 10000; i+=2) {
					  if(i % 50 == 0) {
					  	System.out.println("Inserting " + i);
					  }
					  tree.insert(i);
				  }
			  }
			};

			Thread t2 = new Thread() {
			  public void run() {
				  for(int i = 10000; i< 0; i-=3) {
					  if(i % 60 == 0) {
						  System.out.println("Inserting " + i);
					  }
					  tree.insert(i);
				  }
			  }
			};

			t1.start();
			t2.start();
			
			t1.join();
			t2.join();

			System.out.println("Searching");

			Thread t3 = new Thread() {
				public void run() {
					for(int i = 0; i<10000; i+=2) {
						boolean ret = tree.search(i);
						if(!ret) {
							System.out.println("Couldn't find " + i);
						}
					}
				}
			};

			Thread t4 = new Thread() {
				public void run() {
					for(int i = 10000; i<0; i-=3) {
						boolean ret = tree.search(i);
						if(!ret) {
							System.out.println("Couldn't find " + i);
						}
					}
				}
			};

			t3.start();
			t4.start();

			t3.join();
			t4.join();

			/*for(int i = 0; i<10000; i+=2) {
				boolean ret = tree.search(i);
				if(!ret) {
					System.out.println("Couldn't find " + i);
				}
			}

			for(int i = 10000; i<0; i-=3) {
				boolean ret = tree.search(i);
				if(!ret) {
					System.out.println("Couldn't find " + i);
				}
			} */



		} catch (Exception e) {
			System.out.println(e);
		}




		
	}

}