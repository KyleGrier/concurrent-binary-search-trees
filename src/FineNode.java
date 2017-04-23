/**
 * Created by kyle on 4/22/2017.
 */
import java.util.concurrent.locks.ReentrantLock;

public class FineNode<T extends Comparable> {
    private T value;
    private FineNode parent;
    private FineNode left;
    private FineNode right;
    private ReentrantLock lock;

    public FineNode(T value){
        this.value = value;
        lock = new ReentrantLock();
        left = null;
        right = null;
    }

    public void setLeft(T value){
        this.left = new FineNode(value);
    }

    public void setRight(T value){
        this.right = new FineNode(value);
    }

    public void setParent(T value){
        this.parent = new FineNode(value);
    }
    public void setValue(T value){
        this.value = value;
    }

    public void removeLeft(){
        this.right = null;
    }

    public T getValue(){
        return this.value;
    }

    public void setLock(){
        lock.lock();
    }

    public void unlock(){
        lock.unlock();
    }

    public FineNode getRight(){
        return this.right;
    }

    public FineNode getLeft(){
        return this.left;
    }
}
