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
        lock = new ReentrantLock(true);
        left = null;
        right = null;
    }
    public FineNode(T value, FineNode parent){
        this.value = value;
        lock = new ReentrantLock(true);
        left = null;
        right = null;
        this.parent = parent;
    }

    public void setLeft(T value){
        this.left = new FineNode(value, this);
    }

    public void setRight(T value){
        this.right = new FineNode(value,this);
    }
    public void setLeft(FineNode newBaby){
        this.left = newBaby;
    }

    public void setRight(FineNode newBaby){
        this.right = newBaby;
    }

    public void setParent(FineNode newMama){
        this.parent = newMama;
    }

    public void setValue(T value){
        this.value = value;
    }

    public void removeLeft(){
        this.left = null;
    }

    public void removeRight(){
        this.right = null;
    }

    public T getValue(){
        return this.value;
    }

    public void setLock(){
        if(lock.isLocked()){
            System.out.println(" is blocking right now");
        }
        lock.lock();
    }

    public Boolean isLocked(){
        return lock.isLocked();
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
            return true;
        }
        return false;
    }
    public Boolean whichChild(FineNode child){
        if(child == this.left){
            return false;
        }else return true;
    }
    public FineNode getParent(){
        return this.parent;
    }

    public Boolean hasChild(){
        if(this.left == null && this.right == null){
            return false;
        }
        return true;
    }

}
