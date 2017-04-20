import java.util.concurrent.atomic.AtomicStampedReference;

public class DeleteInfo extends Info{
	InternalNode parent;
	InternalNode grandparent;
	Leaf leaf;
	AtomicStampedReference<Info> parentUpdate;
	boolean deleteLeft;
	boolean parentIsLeftOfGrandparent;
	//dummy interface for Info Type

	public DeleteInfo(InternalNode parent, InternalNode grandparent, Leaf leaf, boolean deleteLeft, AtomicStampedReference<Info> parentUpdate, boolean parentIsLeftOfGrandparent) {
		this.parent = parent;
		this.grandparent = grandparent;
		this.leaf = leaf;
		this.deleteLeft = deleteLeft;
		this.parentUpdate = parentUpdate;
		this.parentIsLeftOfGrandparent = parentIsLeftOfGrandparent;
	}
}