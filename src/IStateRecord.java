/**
 * Created by Sneha on 4/22/17.
 */
public class IStateRecord<T extends Comparable> {
    static final int INJECTION = 0;
    static final int DISCOVERY = 1;
    static final int CLEANUP = 2;
    static final int SIMPLEX = 9;
    static final int COMPLEX = 10;
    IEdge<T> targetEdge, pTargetEdge;
    T targetKey, currentKey;
    int mode;
    int type;
    ISeekRecord<T> succesorRecord;
}
