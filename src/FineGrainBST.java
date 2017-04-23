/**
 * Created by kyle on 4/22/2017.
 */
public class FineGrainBST<T extends Comparable> implements Tree<T> {
    FineNode root;
    public FineGrainBST(T root){
        this.root = new FineNode(root);
    }

    public boolean insert(T node){
        root.setLock();
        T rValue = (T) root.getValue();
        if(node.compareTo(rValue) < 0){

        }
    }

    public boolean delete(T node){

    }

    public boolean search(T value){

    }

    public boolean insertRec(T node, FineNode current, FineNode parent){

    }


}
