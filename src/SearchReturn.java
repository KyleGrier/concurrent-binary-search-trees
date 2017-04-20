import java.util.concurrent.atomic.AtomicStampedReference;

public class SearchReturn {
	InternalNode grandparent;
	InternalNode parent;
	Node leaf;
	AtomicStampedReference<Info> parentUpdate;
	AtomicStampedReference<Info> grandparentUpdate;
	boolean parentIsLeftChildOfGrandparent = false;
	boolean childIsLeftChildOfParent = false;

	public void moveDown() {
		grandparent = parent;
		grandparentUpdate = parentUpdate;
		parent = (InternalNode) leaf;
		parentIsLeftChildOfGrandparent = childIsLeftChildOfParent;
		grandparentUpdate = parentUpdate;
		parentUpdate = parent.update;
	}

}