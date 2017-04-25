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
        if(root == null){
            return false;
        }
        root.setLock();
        T rValue = (T) root.getValue();
        FineNode find;
        //the path moves towards the left side of the tree
        //root lock isn't needed and will be unlocked in searchDelete
        if(node.compareTo(rValue) < 0){
            find = searchDelete2(node, root.getLeft(), root);
            // the path moves towards the left side of the tree
            //root lock isn't needed and will be unlocked in searchDelete
        }else if(node.compareTo(rValue) > 0){
            find = searchDelete2(node, root.getRight(), root);


            //case where the node to be deleted is the same as root
            //couple different situation
            //1)root has no child
            //2)root has right
        }else{ // case where the value is the same as the root
            if(!root.hasChild()){//case where root has no children so the BST has nothing in it.
                root = null;
                return true;
            }
            //Gets right to see if the root has a successor
            FineNode theRight = root.getRight();
            //if the Right is null then root has a left node
            //because the function has already moved passed the
            //conditional check for no children
            if(theRight == null){//case where the node being removed only has a left child
                FineNode theLeft = root.getLeft();
                FineNode theLeftLeft = theLeft.getLeft();
                FineNode theRightLeft = theLeft.getRight();
                this.root.setValue(theLeft.getValue());
                this.root.setLeft(theLeftLeft);
                this.root.setRight(theRightLeft);
                if(theRightLeft != null){
                    theRightLeft.setParent(this.root);
                    theRightLeft.unlock();
                }
                if(theLeftLeft != null){
                    theLeftLeft.setParent(this.root);
                    theLeftLeft.unlock();
                }
                this.root.unlock();
                return true;
                //case where the node being removed has a right child so there is a successor
            }else{
                //get successor should keep lock on root
                //check to make sure the root gets unlocked
                FineNode theSuc = getSuccessor(theRight,root, root);
                //There is a chance where parentSuc is root
                FineNode parentSuc = theSuc.getParent();

                if(theSuc.hasChild()){
                    FineNode rightSuc = theSuc.getRight();
                    rightSuc.setParent(parentSuc);
                    Boolean side = parentSuc.whichChild(theSuc);
                    if(side == LEFT){
                        parentSuc.setLeft(rightSuc);
                    }else{
                        parentSuc.setRight(rightSuc);
                    }
                    rightSuc.unlock();
                }else{
                    //I think this is always left though
                    //Might come back and change later.
                    Boolean side = parentSuc.whichChild(theSuc);
                    if(side == LEFT){
                        parentSuc.removeLeft();
                    }else{
                        parentSuc.removeRight();
                    }
                }
                //theSuc is the last reference to this node
                //No other thread is being blocked on theSuc
                //because the thread who has derferenced theSuc had the lock
                //to theSuc's parent for the whole process of dereferencing
                root.setValue(theSuc.getValue());
                //case where parentSuc is the same as root
                //this conditional protects against illegalmonitor exceptions
                if(parentSuc != root){
                    parentSuc.unlock();
                }
                root.unlock();
                return true;
            }
        }
        if(find == null){
            return false;
        }
        //parentalUnit Lock is still held by this thread
        FineNode parentalUnit = find.getParent();

        //when the node to be deleted has no children
        if(!find.hasChild()){
            Boolean side = parentalUnit.whichChild(find);
            if(side == LEFT){
                parentalUnit.removeLeft();
            }else{
                parentalUnit.removeRight();
            }
            parentalUnit.unlock();
            find.unlock();
            return true;
        }
        FineNode theRight = find.getRight();
        if(theRight == null){
            FineNode theLeft = find.getLeft();
            theLeft.setParent(parentalUnit);
            Boolean side = parentalUnit.whichChild(find);
            if(side == LEFT){
                parentalUnit.setLeft(theLeft);
            }else{
                parentalUnit.setRight(theLeft);
            }
            parentalUnit.unlock();
            find.unlock();
            theLeft.unlock();
            return true;
        }else{
            FineNode theSuc = getSuccessor(theRight,find, find);
            FineNode parentSuc = theSuc.getParent();
            if(theSuc.hasChild()){
                FineNode rightSuc = theSuc.getRight();
                rightSuc.setParent(parentSuc);
                Boolean side = parentSuc.whichChild(theSuc);
                if(side == LEFT){
                    parentSuc.setLeft(rightSuc);
                }else{
                    parentalUnit.setRight(rightSuc);
                }
                rightSuc.unlock();
            }else{
                Boolean side = parentSuc.whichChild(theSuc);
                if(side == LEFT){
                    parentSuc.removeLeft();
                }else{
                    parentSuc.removeRight();
                }
            }
            find.setValue(theSuc.getValue());
            if(parentSuc != find){
                parentSuc.unlock();
            }
            parentalUnit.unlock();
            theSuc.unlock();

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
            root.unlock();
            return true;
        }

        //Decide the output based on the find
        if(find != null){
            return true;
        }else{
            return false;
        }
    }

    public Object getRoot() {
        return root;
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
    //the node that is being deleted needs to be unlocked or just not made a aprt of the overall function
    private FineNode getSuccessor(FineNode searcher, FineNode parent, FineNode toSucceed){
        //Sees if searcher has a left child

        if(searcher.isSuccessor()){
            return searcher;
        }else{
            if(parent != toSucceed){
                parent.unlock();
            }
            return getSuccessor(searcher.getLeft(), searcher, toSucceed);
        }


    }
    //finds the node to delete and keeps the lock of its parent
    public FineNode searchDelete(T value, FineNode current, FineNode parent){
        while(true){
            if(current == null){
                parent.unlock();
                return null;
            }
            T rValue = (T) current.getValue();
            if(value.compareTo(rValue) < 0){
                //release the previous parents lock
                parent.unlock();
                parent = current;
                current = current.getLeft();
            }else if(value.compareTo(rValue) > 0){
                //release the previous parents lock
                parent.unlock();
                parent = current;
                current = current.getRight();
            }else if(value.compareTo(rValue) == 0){ // case where value is equal to the currently accessed node
                //parents lock is still held by this thread
                return current;
            }
        }
    }

    public FineNode searchDelete2(T value, FineNode current, FineNode parent){
        if(current == null) {
            parent.unlock();
            return null;
        }
        //current is now the new locked node
        T rValue = (T) current.getValue();
        if(value.compareTo(rValue) < 0){
            parent.unlock();
            return searchDelete2(value, current.getLeft(), current);
        }else if(value.compareTo(rValue) > 0){
            parent.unlock();
            return searchDelete2(value, current.getRight(), current);
        }else{ // case where the value is the same as the current node
            if(parent.isLocked()){
                System.out.println("Is Locked");
            }
            return current;
        }
    }
    public static void main(String[] args){
        FineGrainBST practice = new FineGrainBST(7);
        practice.insert(8);
        practice.insert(4);
        practice.insert(2);
        practice.insert(1);
        practice.insert(24);
        practice.delete(7);
        practice.delete(1);
    }
}
