import java.util.concurrent.atomic.AtomicStampedReference;
/**
 * Implementation of external lock-free binary search tree based on Ellen, Fatourou, Ruppert, and Breugel's
 *  'Non-blocking Binary Search Trees' (2010)
 */
public class LockFreeBST<T extends Comparable> implements Tree<T>{
	InternalNode root;
	public static final int CLEAN = 0;
	public static final int DFLAG = 1;
	public static final int IFLAG = 2;
	public static final int MARK = 3;

	/**
	 * To easily deal with edge cases, set dummyLeft s.t. dummyLeft < dummyRight
	 */
	public LockFreeBST(T dummyLeft, T dummyRight) {
		Leaf right = new Leaf(dummyRight);
		Leaf left = new Leaf(dummyLeft);
		AtomicStampedReference<Info> update = new AtomicStampedReference<>(null, CLEAN);
		root = new InternalNode(dummyRight, left, right, update);
	}


	//finds the value, its parent, and the corresponding updates if necessary 
	private SearchReturn searchPrivate(T value) {
		InternalNode grandparent = null;
		InternalNode parent = null;
		Node leaf = root;
		AtomicStampedReference<Info> grandparentUpdate = null;
		AtomicStampedReference<Info> parentUpdate= null;

		while(leaf instanceof InternalNode) {
			grandparent = parent;
			parent = (InternalNode) leaf;
			grandparentUpdate = parentUpdate;
			parentUpdate = parent.update;


			if (value.compareTo(leaf.value) < 0 ) {
				leaf =  (Node) parent.left.get();
			} else {
				leaf = (Node) parent.right.get();
			}
		}

		return new SearchReturn(grandparent, parent, leaf, parentUpdate, grandparentUpdate);
	}


	//Since you have no control over when search runs, if a search happens concurrently w/ insert/delete it just depends on the order 
	@Override 
	public boolean search(T value) {
		SearchReturn searchInfo = searchPrivate(value);
		if (searchInfo.leaf != null && searchInfo.leaf.value != null && searchInfo.leaf.value.equals(value)) {
			return true;
		} 

		return false;
	}

	@Override
	public boolean insert(T value) {

		while(true) {
			SearchReturn searchInfo = searchPrivate(value); //figure out where to insert value
			if (searchInfo.leaf.equals(value)) {
				return false; //duplicate value
			}
			if (searchInfo.parentUpdate.getStamp() != CLEAN) {
				help(searchInfo.parentUpdate);
			} else {
				//Leaf newSibling = new Leaf(searchInfo.leaf.value);
				Comparable internalValue;
				Leaf newLeaf;
				Leaf newSibling;
				if (value.compareTo(searchInfo.leaf.value) > 0 ) { //value is bigger than the current leaf
					internalValue = value;
					newLeaf = new Leaf(searchInfo.leaf.value); //new leaf is the smaller of the two
					newSibling = new Leaf(value);
				} else {
					internalValue = searchInfo.leaf.value;
					newLeaf = new Leaf(value);
					newSibling = new Leaf(searchInfo.leaf.value);
				}
				AtomicStampedReference<Info> newInternalUpdate = new AtomicStampedReference<>(null, CLEAN);
				InternalNode newInternalNode = new InternalNode(internalValue, newLeaf, newSibling, newInternalUpdate);
				InsertInfo insertInfo = new InsertInfo(searchInfo.parent, newInternalNode, (Leaf) searchInfo.leaf);
				int[] stamp = new int[1];
				Info expectedOldParentInfo = searchInfo.parentUpdate.get(stamp);
				boolean success = searchInfo.parent.update.compareAndSet(expectedOldParentInfo, insertInfo, stamp[0], IFLAG);
				if(success) {
					helpInsert(insertInfo);
					return true;
				} else { //someone else got here before you
					help(searchInfo.parent.update);
				}
			}

		}

	}

	@Override
	public boolean delete(T value) {
		while(true) {
			SearchReturn searchInfo = searchPrivate(value);
			if(searchInfo.leaf == null || searchInfo.leaf.value == null || !searchInfo.leaf.value.equals(value)) {
				return false;
			} 

			if (searchInfo.grandparentUpdate.getStamp() != CLEAN) {
				help(searchInfo.grandparentUpdate);
			} else if (searchInfo.parentUpdate.getStamp() != CLEAN) {
				help(searchInfo.parentUpdate);
			} else {
				DeleteInfo deleteInfo = new DeleteInfo(searchInfo.parent,
						searchInfo.grandparent,
						(Leaf) searchInfo.leaf,
						searchInfo.parentUpdate);
				int[] stamp = new int[1];
				Info expectedOldGrandparentInfo = searchInfo.grandparentUpdate.get(stamp);
				boolean success = searchInfo.grandparent.update.compareAndSet(expectedOldGrandparentInfo, deleteInfo, stamp[0], DFLAG);
				if(success) {
					if (helpDelete(deleteInfo)) {
						return true;
					}
				} else { //someone else got here before you
					help(searchInfo.grandparent.update);
				}
			}

		}
	}

	public Object getRoot() {
		return root;
	}

	private void casChild(InternalNode<T> parent, Node oldNode, Node newNode) {
		if(newNode.getValue().compareTo(parent.getValue()) < 0) {
			parent.left.compareAndSet(oldNode, newNode);
		} else {
			parent.right.compareAndSet(oldNode, newNode);
		}
	}

	private void helpInsert(InsertInfo insertInfo) {
		casChild(insertInfo.parent, insertInfo.leaf, insertInfo.newInternal);
		insertInfo.parent.update.compareAndSet(insertInfo, insertInfo, IFLAG, CLEAN);


	}

	private boolean helpDelete(DeleteInfo deleteInfo) {
		if (deleteInfo != null) {
			int[] stamp = new int[1];
			Info expectedOldParentInfo = deleteInfo.parentUpdate.get(stamp);
			boolean success = deleteInfo.parent.update.compareAndSet(expectedOldParentInfo, deleteInfo, stamp[0], MARK); //not sure if this is supposed to be clean
			if (success) {
				helpMarked(deleteInfo);
				return true;
			} else {
				help(deleteInfo.parent.update);
				deleteInfo.grandparent.update.compareAndSet(deleteInfo, deleteInfo, DFLAG, CLEAN);
				return false;
			}

		}
		return true;
	}

	private void helpMarked(DeleteInfo deleteInfo) {
		if(deleteInfo != null) {
			Node other;
			if(deleteInfo.parent.right.get().equals(deleteInfo.leaf)) {
				other = (Node) deleteInfo.parent.left.get();
			} else {
				other = (Node) deleteInfo.parent.right.get();
			}
			casChild(deleteInfo.grandparent, deleteInfo.parent, other);
			deleteInfo.grandparent.update.compareAndSet(deleteInfo, deleteInfo, DFLAG, CLEAN);
			
		}
	}

	private void help(AtomicStampedReference<Info> update) {
		int[] state = new int[1];
		Info info = update.get(state);
		if (state[0] == IFLAG) {
			helpInsert((InsertInfo) info);
		} else if (state[0] == MARK) {
			helpMarked((DeleteInfo) info);
		} else if (state[0] == DFLAG) {
			helpDelete((DeleteInfo) info);
		}
	}
}