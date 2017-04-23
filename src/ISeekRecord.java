/**
 * Created by Sneha on 4/22/17.
 */
public class ISeekRecord<T extends Comparable> {
    IEdge<T> lastEdge, pLastEdge, injectionEdge;

    public ISeekRecord(IEdge<T> pLastEdge, IEdge<T> lastEdge, IEdge<T> injectionEdge) {
        this.lastEdge = lastEdge;
        this.pLastEdge = pLastEdge;
        this.injectionEdge = injectionEdge;
    }
}
