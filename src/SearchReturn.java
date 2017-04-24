import java.util.concurrent.atomic.AtomicStampedReference;

public class SearchReturn {
	InternalNode grandparent;
	InternalNode parent;
	Node leaf;
	AtomicStampedReference<Info> parentUpdate;
	AtomicStampedReference<Info> grandparentUpdate;

	public void moveDown() {
		grandparent = parent;
		grandparentUpdate = parentUpdate;
		parent = (InternalNode) leaf;
		grandparentUpdate = parentUpdate;
		parentUpdate = parent.update;
	}

}