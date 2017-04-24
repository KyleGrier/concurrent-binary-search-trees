import java.util.concurrent.atomic.AtomicStampedReference;

public class DeleteInfo extends Info{
	InternalNode parent;
	InternalNode grandparent;
	Leaf leaf;
	AtomicStampedReference<Info> parentUpdate;
	//dummy interface for Info Type

	public DeleteInfo(InternalNode parent, InternalNode grandparent, Leaf leaf, AtomicStampedReference<Info> parentUpdate) {
		this.parent = parent;
		this.grandparent = grandparent;
		this.leaf = leaf;
		this.parentUpdate = parentUpdate;
	}
}