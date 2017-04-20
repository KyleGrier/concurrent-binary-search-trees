import java.util.concurrent.atomic.AtomicStampedReference;
public class LockFreeBST<T extends Comparable> implements Tree<T>{
	InternalNode root;
	public static final int CLEAN = 0;
	public static final int DFLAG = 1;
	public static final int IFLAG = 2;
	public static final int MARK = 3;


	public LockFreeBST(T dummyLeft, T dummyRight) {
		Leaf right = new Leaf(dummyLeft);
		Leaf left = new Leaf(dummyRight);
		AtomicStampedReference<Info> update = new AtomicStampedReference<>(null, CLEAN);
		root = new InternalNode(dummyRight, right, left, update);
	}


	//finds the value, its parent, and the corresponding updates if necessary 
	private SearchReturn searchPrivate(T value) {
		SearchReturn searchInfo = new SearchReturn();
		searchInfo.leaf = root;
		while(searchInfo.leaf instanceof InternalNode) {
			searchInfo.moveDown();
			if (value.compareTo(searchInfo.leaf.value) < 0 ) {
				searchInfo.childIsLeftChildOfParent = true;
				searchInfo.leaf =  (Node) searchInfo.parent.left.get();
			} else {
				searchInfo.childIsLeftChildOfParent = false;
				searchInfo.leaf = (Node) searchInfo.parent.right.get();
			}
		}

		return searchInfo;
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
				InsertInfo insertInfo = new InsertInfo(searchInfo.parent, newInternalNode, (Leaf) searchInfo.leaf, searchInfo.childIsLeftChildOfParent);
				Info expectedOldParentInfo = searchInfo.parentUpdate.getReference();
				boolean success = searchInfo.parent.update.compareAndSet(expectedOldParentInfo, insertInfo, CLEAN, IFLAG);
				if(success) {
					helpInsert(insertInfo);
					return true;
				} else { //someone else got here before you
					help(searchInfo.parentUpdate);
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
						searchInfo.childIsLeftChildOfParent,
						searchInfo.parentUpdate,
						searchInfo.parentIsLeftChildOfGrandparent);
				Info expectedOldGrandparentInfo = searchInfo.grandparentUpdate.getReference();
				boolean success = searchInfo.grandparent.update.compareAndSet(expectedOldGrandparentInfo, deleteInfo, CLEAN, DFLAG);
				if(success) {
					if (helpDelete(deleteInfo)) {
						return true;
					}
				} else { //someone else got here before you
					help(searchInfo.grandparentUpdate);
				}
			}

		}
	}

	private void helpInsert(InsertInfo insertInfo) {
		if (insertInfo != null) {
			if(insertInfo.insertLeft) {
				insertInfo.parent.left.compareAndSet(insertInfo.leaf, insertInfo.newInternal);
			} else {
				insertInfo.parent.right.compareAndSet(insertInfo.leaf, insertInfo.newInternal);
			}

			insertInfo.parent.update.compareAndSet(insertInfo, insertInfo, IFLAG, CLEAN);
		}

	}

	private boolean helpDelete(DeleteInfo deleteInfo) {
		if (deleteInfo != null) {
			Info expectedOldParentInfo = deleteInfo.parentUpdate.getReference();
			boolean success = deleteInfo.parent.update.compareAndSet(expectedOldParentInfo, deleteInfo, CLEAN, MARK); //not sure if this is supposed to be clean
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
			if(deleteInfo.deleteLeft) {
				other = (Node) deleteInfo.parent.right.get();
			} else {
				other = (Node) deleteInfo.parent.left.get();
			}
			if(deleteInfo.parentIsLeftOfGrandparent) {
				deleteInfo.grandparent.left.compareAndSet(deleteInfo.parent, other);
			} else {
				deleteInfo.grandparent.right.compareAndSet(deleteInfo.parent, other);
			}

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