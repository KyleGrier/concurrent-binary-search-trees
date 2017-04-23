/**
 * Created by kyle on 4/22/2017.
 */
import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.concurrent.locks.ReentrantLock;

public class FineNode<T extends Comparable> {
    private T value;
    private FineNode parent;
    private FineNode left;
    private FineNode right;
    private ReentrantLock lock;
    int numChildren;
    public FineNode(T value){
        this.value = value;
        lock = new ReentrantLock();
        left = null;
        right = null;
        numChildren = 0;
    }

    public void setLeft(T value){
        if(this.left ==null){
            numChildren++;
        }
        this.left = new FineNode(value);
    }

    public void setRight(T value){
        if(this.right ==null){
            numChildren++;
        }
        this.right = new FineNode(value);
    }
    public void setLeft(FineNode newBaby){
        if(this.left ==null){
            numChildren++;
        }
        this.left = newBaby;
    }

    public void setRight(FineNode newBaby){
        if(this.right ==null){
            numChildren++;
        }
        this.right = newBaby;
    }

    public void setParent(T value){
        this.parent = new FineNode(value);
    }
    public void setParent(FineNode newMama){
        this.parent = newMama;
    }
    public void setValue(T value){
        this.value = value;
    }

    public void removeLeft(){
        this.right = null;
        numChildren--;
    }

    public void removeRight(){
        this.right = null;
        numChildren--;
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
        if(this.right == null){
            return this.right;
        }
        this.right.setLock();
        return this.right;
    }

    public FineNode getLeft(){
        if(this.left == null){
            return this.left;
        }
        this.left.setLock();
        return this.left;
    }
    public Boolean isSuccessor(){
        if(this.left == null){
            return false;
        }
        return true;
    }

    public FineNode getParent(){
        return this.parent;
    }
    public Boolean hasChild(){
        if(this.numChildren == 0){
            return false;
        }
        return true;
    }

}
