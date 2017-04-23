/**
 * Created by Sneha on 4/22/17.
 */
public class IEdge<T extends Comparable> {
    static final int LEFT = 0;
    static final int RIGHT = 1;
    static final int NONE = -1;
    INode<T> parent, child;
    int which;

    public IEdge(INode<T> r, INode<T> s, int direction) {
        parent = r;
        child = s;
        which = direction;
    }

    public void update(INode<T> curr, INode<T> next, int which) {
        parent = curr;
        child = next;
        this.which = which;
    }
}
