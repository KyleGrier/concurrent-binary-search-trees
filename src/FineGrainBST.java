import com.sun.org.apache.xpath.internal.operations.Bool;

/**
 * Created by kyle on 4/22/2017.
 */
public class FineGrainBST<T extends Comparable> implements Tree<T> {
    private final Boolean LEFT = false;
    private final Boolean RIGHT = true;
    FineNode root;
    public FineGrainBST(T root){
        this.root = new FineNode(root);
    }
    //returns false if the node is already in tree
    //returns true if the node is now a newly inserted member of the tree
    public boolean insert(T node){
        root.setLock();
        T rValue = (T) root.getValue();
        if(node.compareTo(rValue) < 0){
            return insertRec(node, root.getLeft(), root, LEFT);
        }else if(node.compareTo(rValue) > 0){
            return insertRec(node, root.getRight(), root, RIGHT);
        }else{ // case where the value is the same as the root
            return false;
        }
    }

    public boolean delete(T node){

    }

    public boolean search(T value){

    }
    // node is value to be inserted, current is the the node currently being explored, parent is previous node,
    // pside is LEFT or RIGHT depending on where current is in relation to parent.
    // If the thread is in the method it is assumed that the thread has the parents lock.
    public boolean insertRec(T node, FineNode current, FineNode parent, Boolean pside){
        //there is nothing on the pside of the parent so the node can be made here
        if(current == null) {
            doInsert(node, parent, pside);
        }
        current.setLock();
        //current is now the new locked node
        parent.unlock();
        T rValue = (T) current.getValue();
        if(node.compareTo(rValue) < 0){
            return insertRec(node, current.getLeft(), current, LEFT);
        }else if(node.compareTo(rValue) > 0){
            return insertRec(node, current.getRight(), current, RIGHT);
        }else{ // case where the value is the same as the root
            return false;
        }
    }

    private void doInsert(T node, FineNode parent, Boolean pside){
        if(pside == LEFT){
            parent.setLeft(node);
        }else if(pside == RIGHT){
            parent.setRight(node);
        }
    }


}
