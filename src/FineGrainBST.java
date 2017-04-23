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
        root.setLock();
        T rValue = (T) root.getValue();
        FineNode find;
        if(node.compareTo(rValue) < 0){
            find = searchDelete(node, root.getLeft(), root);
        }else if(node.compareTo(rValue) > 0){
            find = searchDelete(node, root.getRight(), root);
        }else{ // case where the value is the same as the root
            return true;
        }
        if(find == null){
            return false;
        }
        FineNode parentalUnit = find.getParent();
        //when the node to be deleted has no children
        if(!find.hasChild()){
            parentalUnit.removeLeft();
            parentalUnit.unlock();
            return true;
        }
        FineNode theRight = find.getRight();
        if(theRight == null){
            FineNode theLeft = find.getLeft();
            theLeft.setParent(parentalUnit);
            parentalUnit.setLeft(theLeft);
            parentalUnit.unlock();
            theLeft.unlock();
            return true;
        }else{
            FineNode theSuc = getSuccessor(theRight,find);
            FineNode parentSuc = theSuc.getParent();
            if(theSuc.hasChild()){
                FineNode rightSuc = theSuc.getRight();
                rightSuc.setParent(parentSuc);
                parentSuc.setLeft(rightSuc);
            }else{
                parentSuc.removeLeft();
            }
            find.setValue(theSuc.getValue());
            parentalUnit.unlock();
            find.unlock();
            return true;

        }
    }

    public boolean search(T value){
        root.setLock();
        T rValue = (T) root.getValue();
        FineNode find;
        if(value.compareTo(rValue) < 0){
            find = searchIter(value, root.getLeft());
        }else if(value.compareTo(rValue) > 0){
            find = searchIter(value, root.getRight());
        }else{ // case where the value is the same as the root
            return true;
        }

        //Decide the output based on the find
        if(find != null){
            return true;
        }else{
            return false;
        }
    }

    // value is the value to be found, current is the node the value is checked against
    // if through the search current is null then the method return .
    public FineNode searchIter(T value, FineNode current){
        while(current != null){
            current.setLock();
            T rValue = (T) current.getValue();
            if(value.compareTo(rValue) < 0){
                current.unlock();
                current = current.getLeft();
            }else if(value.compareTo(rValue) > 0){
                current.unlock();
                current = current.getRight();
            }else{ // case where the value is the same as the root
                return current;
            }
        }
        return null;
    }
    // node is value to be inserted, current is the the node currently being explored, parent is previous node,
    // pside is LEFT or RIGHT depending on where current is in relation to parent.
    // If the thread is in the method it is assumed that the thread has the parents lock.
    public boolean insertRec(T node, FineNode current, FineNode parent, Boolean pside){
        //there is nothing on the pside of the parent so the node can be made here
        if(current == null) {
            doInsert(node, parent, pside);
            parent.unlock();
            return true;
        }
        current.setLock();
        //current is now the new locked node
        parent.unlock();
        T rValue = (T) current.getValue();
        if(node.compareTo(rValue) < 0){
            return insertRec(node, current.getLeft(), current, LEFT);
        }else if(node.compareTo(rValue) > 0){
            return insertRec(node, current.getRight(), current, RIGHT);
        }else{ // case where the value is the same as the current node
            current.unlock();
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

    private FineNode getSuccessor(FineNode searcher, FineNode parent){
        //Sees if searcher has a left child

        if(searcher.isSuccessor()){
            return searcher;
        }else{
            parent.unlock();
            return getSuccessor(searcher.getLeft(), searcher);
        }


    }
    //finds the node to delete and keeps the lock of its parent
    public FineNode searchDelete(T value, FineNode current, FineNode parent){
        while(true){
            T rValue = (T) current.getValue();
            if(current == null){
                parent.unlock();
                return null;
            }
            if(value.compareTo(rValue) < 0){
                parent.unlock();
                parent = current;
                current = current.getLeft();
            }else if(value.compareTo(rValue) > 0){
                parent.unlock();
                parent = current;
                current = current.getRight();
            }else{ // case where the value is the same as the root
                return current;
            }
        }
    }
}
