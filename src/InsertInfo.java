import java.util.concurrent.atomic.AtomicStampedReference;

public class InsertInfo extends Info{
	InternalNode parent;
	InternalNode newInternal;
	Leaf leaf;
	boolean insertLeft;
	//dummy interface for Info Type

	public InsertInfo(InternalNode parent, InternalNode newInternal, Leaf leaf, boolean insertLeft) {
		this.parent = parent;
		this.newInternal = newInternal;
		this.leaf = leaf;
		this.insertLeft = insertLeft;
	}
}