/**
 * Created by Sneha on 4/22/17.
 */
public class IAnchorRecord<T extends Comparable> {
    INode<T>  node;
    T key;

    public IAnchorRecord(INode<T> n, T k) {
        node = n;
        key = k;
    }

    public void update(INode<T> curr, T cKey) {
        node = curr;
        key = cKey;
    }

    @Override
    public boolean equals(Object o) {
        IAnchorRecord<T> other = (IAnchorRecord<T>) o;
        if(other.key.equals(key) && other.node.equals(node)) {
            return true;
        }
        return false;
    }
}
