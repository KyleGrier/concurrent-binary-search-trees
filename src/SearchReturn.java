import java.util.concurrent.atomic.AtomicStampedReference;

public class SearchReturn {
	InternalNode grandparent;
	InternalNode parent;
	Node leaf;
	AtomicStampedReference<Info> parentUpdate;
	AtomicStampedReference<Info> grandparentUpdate;

	public SearchReturn(InternalNode grandparent, InternalNode parent, Node leaf, AtomicStampedReference<Info> parentUpdate, AtomicStampedReference<Info> grandparentUpdate) {
		this.grandparent = grandparent;
		this.parent = parent;
		this.leaf = leaf;
		this.parentUpdate = parentUpdate;
		this.grandparentUpdate = grandparentUpdate;
	}

}