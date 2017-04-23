import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * Created by Sneha on 4/22/17.
 */

public class INode<T extends Comparable> {
    static final int NULL_BIT = 8;
    static final int INJECT_BIT = 4;
    static final int DELETE_BIT = 2;
    static final int PROMOTE_BIT = 1;

    static final int ORIGINAL = 0;
    static final int REPLACEMENT = 1;

    AtomicStampedReference<T> mKey;
    AtomicStampedReference<INode<T>>[] child;
    boolean readyToReplace;

    public INode(T value, INode<T> right, INode<T> left) {
        child = new AtomicStampedReference[2];
        mKey = new AtomicStampedReference<>(value, ORIGINAL);
        readyToReplace = false;
        child[IEdge.RIGHT] =  new AtomicStampedReference<>(right, INode.NULL_BIT);
        child[IEdge.LEFT] =  new AtomicStampedReference<>(left, INode.NULL_BIT);

    }
}
