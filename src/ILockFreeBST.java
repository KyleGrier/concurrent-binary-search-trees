
import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * Implementation of internal lock-free binary search tree based on Ramachandran & Mittal's
 *  'A Fast Lock-Free Internal Binary Search Tree' (2015)
 */
public class ILockFreeBST<T extends Comparable> implements Tree<T>{

    private INode<T> R;
    private INode<T> S;
    private INode<T> U;
    private long numOfNodes;
    T INF_0;
    T INF_1;
    T INF_2;

    /**
     * To easily deal with edge cases, set INF_R, INF_S, INF_U s.t INF_R < INF_S < INF_U, where INF_U is the
     * largest value in the domain.
     */
    public ILockFreeBST(T INF_R, T INF_S, T INF_U) {

        U = new INode<>(INF_U, null, null);
        S = new INode<>(INF_S, U, null);
        R = new INode<>(INF_R, S, null);
        INF_0 = INF_R;
        INF_1 = INF_S;
        INF_2 = INF_U;



    }

    private ISeekRecord<T> seek(T key) {
        IAnchorRecord<T> pAnchorRecord = new IAnchorRecord<>(S, INF_1);
        ISeekRecord<T> seekRecord;
        ISeekRecord<T> pSeekRecord = null;
        while(true) {
            IEdge<T> pLastEdge = new IEdge<>(R, S, IEdge.RIGHT);
            IEdge<T> lastEdge = new IEdge<>(S, U, IEdge.RIGHT);
            INode<T> curr = U;
            IAnchorRecord<T> anchorRecord = new IAnchorRecord<>(S, INF_1);
            while(true) {
                T cKey = curr.mKey.getReference();
                int which = key.compareTo(cKey) < 0 ? IEdge.LEFT:IEdge.RIGHT;
                int[] stamp = new int[1];
                INode<T> next = curr.child[which].get(stamp);
                if((stamp[0] & INode.NULL_BIT ) == INode.NULL_BIT ||
                        key.equals(cKey)) {
                    //either key found or no next edge
                    IEdge<T> injectionEdge = new IEdge(curr, next, which);
                    seekRecord = new ISeekRecord<>(pLastEdge, lastEdge, injectionEdge);
                    if(key.equals(cKey)) {
                        return seekRecord;
                    } else {
                        break;
                    }
                }

                if(which == IEdge.RIGHT) {
                    anchorRecord.update(curr, cKey);
                }

                //go to next edge
                pLastEdge = lastEdge;
                lastEdge.update(curr, next, which);
                curr = next;

            }

            //key not found
            /*int anchorStamp = anchorRecord.node.child[IEdge.RIGHT].getStamp();
            if(!((anchorStamp & INode.DELETE_BIT ) == INode.DELETE_BIT) &&
                !((anchorStamp & INode.PROMOTE_BIT ) == INode.PROMOTE_BIT)) {
                //still part of tree
                if (anchorRecord.key.equals(anchorRecord.node.mKey)) { //check if key hasn't changed
                    return seekRecord;
                }

            }

            else {
                //check if anchor record matches that of previous traversal
                if(pAnchorRecord.equals(anchorRecord) && pSeekRecord != null) {
                    seekRecord = pSeekRecord;
                    return seekRecord;
                }
            } */

            if(anchorRecord.key.equals(anchorRecord.node.mKey.getReference())) {
                int anchorStamp = anchorRecord.node.child[IEdge.RIGHT].getStamp();
                if(!((anchorStamp & INode.DELETE_BIT ) == INode.DELETE_BIT) &&
                        !((anchorStamp & INode.PROMOTE_BIT ) == INode.PROMOTE_BIT)) {
                    return seekRecord;

                }
                if(pAnchorRecord.equals(anchorRecord) && pSeekRecord != null) {
                    seekRecord = pSeekRecord;
                    return seekRecord;
                }

            }

            pSeekRecord = seekRecord;
            pAnchorRecord = anchorRecord;

        }
    }

    @Override
    public boolean insert(T key) {
        while(true) {
            ISeekRecord<T> targetRecord = seek(key);
            IEdge<T> targetEdge = targetRecord.lastEdge;
            INode<T> node = targetEdge.child;
            if(node.mKey.equals(key)) {
                return false;
            }

            INode<T> newNode = new INode<>(key, null, null);
            int which = targetRecord.injectionEdge.which;
            INode<T> address = targetRecord.injectionEdge.child;
            boolean result = node.child[which].compareAndSet(address, newNode, INode.NULL_BIT, 0);
            if(result) {
                return true;
            }

            //help if needed
            int stamp = node.child[which].getStamp();
            if ((stamp & INode.DELETE_BIT ) == INode.DELETE_BIT) {
                helpTargetNode(targetEdge);
            } else if ((stamp & INode.PROMOTE_BIT ) == INode.PROMOTE_BIT) {
                helpSuccesorNode(targetEdge);
            }
        }
    }

    @Override
    public boolean delete(T key) {
        IStateRecord<T> myState = new IStateRecord<>();
        myState.targetKey = key;
        myState.currentKey = key;
        myState.mode = IStateRecord.INJECTION;

        while(true) {
            ISeekRecord<T> targetRecord = seek(myState.currentKey);
            IEdge<T> targetEdge = targetRecord.lastEdge;
            IEdge<T> pTargetEdge = targetRecord.pLastEdge;
            T nKey = targetEdge.child.mKey.getReference();
            if (myState.currentKey != nKey) {
                if (myState.mode == IStateRecord.INJECTION) {
                    return false;
                } else {
                    return true;
                }
            }

            if (myState.mode == IStateRecord.INJECTION) {
                myState.targetEdge = targetEdge;
                myState.pTargetEdge = pTargetEdge;
                inject(myState);
            }

            if(myState.mode != IStateRecord.INJECTION) {
                if(myState.targetEdge.child != targetEdge.child) { //need equals?
                    return true;
                }

                myState.targetEdge = targetEdge;
            }

            if(myState.mode == IStateRecord.DISCOVERY) {
                findAndMarkSuccesor(myState);
            }

            if(myState.mode == IStateRecord.DISCOVERY) {
                removeSuccesor(myState);
            }

            if(myState.mode == IStateRecord.CLEANUP) {
                boolean result = cleanUp(myState);
                if(result) {
                    return true;
                } else {
                    nKey = targetEdge.child.mKey.getReference();
                    myState.currentKey = nKey;
                }
            }


        }
    }

    public Object getRoot() {
        return R;
    }

    private void inject(IStateRecord<T> state) {
        IEdge<T> targetEdge = state.targetEdge;
        INode<T> parent = targetEdge.parent;
        INode<T> node = targetEdge.child;
        int which = targetEdge.which;
        boolean result = parent.child[which].compareAndSet(node, node, 0, INode.INJECT_BIT);
        if(!result) {

            int stamp = parent.child[which].getStamp();

            if((stamp & INode.INJECT_BIT ) == INode.INJECT_BIT) {
                helpTargetNode(targetEdge);
            } else if ((stamp & INode.DELETE_BIT ) == INode.DELETE_BIT) {
                helpTargetNode(state.pTargetEdge);
            } else if ((stamp & INode.PROMOTE_BIT ) == INode.PROMOTE_BIT) {
                helpSuccesorNode(state.pTargetEdge);
            }
            return;
        }

        result = markChildEdge(state, IEdge.LEFT);
        if(!result) {
            return;
        }
        markChildEdge(state, IEdge.RIGHT);
        initializeTypeAndUpdateMode(state);
    }

    private void findAndMarkSuccesor(IStateRecord<T> state) {
       INode<T> node = state.targetEdge.child;
       ISeekRecord<T> seekRecord = state.succesorRecord;

       while(true) {
           int m = node.mKey.getStamp();
           boolean result = findSmallest(state);

           if(m == INode.REPLACEMENT || !result) {
               break;
           }

           IEdge<T> successorEdge = seekRecord.lastEdge;
           INode<T> left = seekRecord.injectionEdge.child;

           m = node.mKey.getStamp();
           if (m == INode.REPLACEMENT) {
               continue;
           }

           result = successorEdge.child.child[IEdge.LEFT].compareAndSet(
                   left, node, INode.NULL_BIT, INode.NULL_BIT & INode.PROMOTE_BIT);

           if(result) {
               break;
           }

           int stamp = successorEdge.child.child[IEdge.LEFT].getStamp();
           if((stamp & INode.NULL_BIT ) == INode.NULL_BIT && (stamp & INode.DELETE_BIT ) == INode.DELETE_BIT) {
                helpTargetNode(successorEdge);
           }
       }

       updateMode(state);
    }

    private void removeSuccesor(IStateRecord<T> state) {
        INode<T> node = state.targetEdge.child;
        ISeekRecord<T> seekRecord = state.succesorRecord;
        IEdge<T> succesorEdge = seekRecord.lastEdge;
        int[] stamp = new int[1];
        INode<T> address = succesorEdge.child.child[IEdge.LEFT].get(stamp);
        if((!((stamp[0] & INode.PROMOTE_BIT ) == INode.PROMOTE_BIT)) || address != node) {
            node.readyToReplace = true;
            updateMode(state);
            return;
        }

        markChildEdge(state, IEdge.RIGHT);

        node.mKey = new AtomicStampedReference<>(succesorEdge.child.mKey.getReference(), INode.REPLACEMENT);

        while(true) {
            int dFlag;
            int which;
            if(succesorEdge.parent == node) {
                dFlag = INode.DELETE_BIT;
                which = IEdge.RIGHT;
            } else {
                dFlag = 0;
                which = IEdge.LEFT;
            }
            int stamp2 = succesorEdge.parent.child[which].getStamp();
            int i = stamp2 & INode.INJECT_BIT;
            int[] temp = new int[1];
            INode<T> right = succesorEdge.child.child[IEdge.RIGHT].get(temp);
            int n = temp[0] & INode.NULL_BIT;
            INode<T> oldRef = succesorEdge.child;
            int oldStamp = i | dFlag;
            INode<T> newRef;
            int newStamp;
            AtomicStampedReference<INode<T>> newValue;
            if(n == INode.NULL_BIT) {
                newRef = succesorEdge.child;
                newStamp = INode.NULL_BIT | dFlag;
            } else {
                newRef = right;
                newStamp = dFlag;
            }

            boolean result = succesorEdge.parent.child[which].compareAndSet(oldRef, newRef, oldStamp, newStamp);

            if(result || dFlag == INode.DELETE_BIT) {
                break;
            }

            int tempstamp = succesorEdge.parent.child[which].getStamp();
            int d = tempstamp & INode.DELETE_BIT;
            IEdge<T> pLastEdge = seekRecord.pLastEdge;
            if(d == INode.DELETE_BIT && pLastEdge.parent != null) {
                helpTargetNode(pLastEdge);
            }

            result = findSmallest(state);
            IEdge<T> lastEdge = seekRecord.lastEdge;

            if(!result || lastEdge.child != succesorEdge.child) {
                break; //succesor edge already removed
            } else {
                succesorEdge = seekRecord.lastEdge;
            }
        }

        node.readyToReplace = true;
        updateMode(state);
    }

    private boolean cleanUp(IStateRecord<T> state) {
        INode<T> parent = state.targetEdge.parent;
        INode<T> node = state.targetEdge.child;
        int pWhich = state.targetEdge.which;

        INode<T> oldRef;
        int oldStamp;
        INode<T> newRef;
        int newStamp;

        if(state.type == state.COMPLEX) {
            T nKey = node.mKey.getReference();
            INode<T> newNode = new INode<>(nKey, null, null);
            INode<T> left = node.child[IEdge.LEFT].getReference();
            newNode.child[IEdge.LEFT] = new AtomicStampedReference<>(left, 0);
            int [] temp = new int[1];
            INode<T> right = node.child[IEdge.RIGHT].get(temp);

            int n = temp[0] & INode.NULL_BIT;
            if(n == INode.NULL_BIT) {
                newNode.child[IEdge.RIGHT] = new AtomicStampedReference<>(null, INode.NULL_BIT);
            } else {
                newNode.child[IEdge.RIGHT] = new AtomicStampedReference<>(right, 0);
            }

            oldRef = node;
            oldStamp = INode.INJECT_BIT;
            newRef = newNode;
            newStamp = 0;
        } else {
            //remove node
            int stamp = node.child[IEdge.LEFT].getStamp();
            int nWhich;
            if((stamp & INode.NULL_BIT) == INode.NULL_BIT) {
                nWhich = IEdge.RIGHT;
            } else {
                nWhich = IEdge.LEFT;
            }

            oldRef = node;
            oldStamp = INode.INJECT_BIT;

            int [] temp = new int[1];
            INode<T> address = node.child[nWhich].get(temp);

            int n = temp[0] & INode.NULL_BIT;
            if(n == INode.NULL_BIT) {
                newRef = node;
                newStamp = INode.NULL_BIT;
            } else {
                newRef = address;
                newStamp = 0;
            }
        }

        return parent.child[pWhich].compareAndSet(oldRef, newRef, oldStamp, newStamp);
    }

    private boolean markChildEdge(IStateRecord<T> state, int which) {
        int flag;
        IEdge<T> edge;
        if(state.mode == IStateRecord.INJECTION) {
            edge = state.targetEdge;
            flag = INode.DELETE_BIT;
        } else {
            edge = state.succesorRecord.lastEdge;
            flag = INode.PROMOTE_BIT;
        }
        INode<T> node = edge.child;
        while(true) {
            int[] stamp = new int[1];
            INode<T> address = node.child[which].get(stamp);

            if((stamp[0] & INode.INJECT_BIT) == INode.INJECT_BIT) {
                IEdge<T> helpeeEdge = new IEdge<>(node, address, which);
                helpTargetNode(helpeeEdge);
                continue;
            } else if ((stamp[0] & INode.DELETE_BIT) == INode.DELETE_BIT) {
                if (flag == INode.PROMOTE_BIT) {
                    helpTargetNode(edge);
                    return false;
                } else {
                    return true;
                }
            } else if((stamp[0] & INode.PROMOTE_BIT) == INode.PROMOTE_BIT) {
                if(flag == INode.DELETE_BIT) {
                    helpSuccesorNode(edge);
                    return false;
                } else {
                    return true;
                }
            }

            int oldStamp = stamp[0] & INode.NULL_BIT;
            int newStamp = oldStamp | flag; //??
            boolean result = node.child[which].compareAndSet(address, address, oldStamp, newStamp);
            if(result) {
                break;
            }

        }
        return true;
    }

    private boolean findSmallest(IStateRecord<T> state) {
        INode<T> node = state.targetEdge.child;
        int[] temp = new int[1];
        INode<T> right = node.child[IEdge.RIGHT].get(temp);
        if ((temp[0] & INode.NULL_BIT) == INode.NULL_BIT) {
            return false;
        }

        IEdge<T> lastEdge = new IEdge(node, right, IEdge.RIGHT);
        IEdge<T> pLastEdge = new IEdge(node, right, IEdge.RIGHT);
        IEdge<T> injectionEdge;

        while(true) {
            INode<T> curr = lastEdge.child;
            int[] stamp = new int[1];
            INode<T> left = curr.child[IEdge.LEFT].get(stamp);
            if((stamp[0] & INode.NULL_BIT) == INode.NULL_BIT) {
                injectionEdge = new IEdge<>(curr, left, IEdge.LEFT);
                break;
            }

            pLastEdge = lastEdge;
            lastEdge = new IEdge<>(curr, left, IEdge.LEFT);

        }

        state.succesorRecord.lastEdge = lastEdge;
        state.succesorRecord.pLastEdge = pLastEdge;
        state.succesorRecord.injectionEdge = injectionEdge;
        return true;
    }

    private void initializeTypeAndUpdateMode(IStateRecord<T> state) {
        INode<T> node = state.targetEdge.child;
        int leftStamp = node.child[IEdge.LEFT].getStamp();
        int rightStamp = node.child[IEdge.RIGHT].getStamp();
        boolean leftNull = (leftStamp & INode.NULL_BIT) == INode.NULL_BIT;
        boolean rightNull = (rightStamp & INode.NULL_BIT) == INode.NULL_BIT;
        if(leftNull || rightNull) {
            int m = node.mKey.getStamp();
            if(m == INode.REPLACEMENT) {
                state.type = IStateRecord.COMPLEX;
            } else {
                state.type = IStateRecord.SIMPLEX;
            }
        } else {
            state.type = IStateRecord.COMPLEX;
        }

        updateMode(state);

    }

    private void updateMode(IStateRecord<T> state) {
        if(state.type == IStateRecord.SIMPLEX) {
            state.mode = IStateRecord.CLEANUP;
        } else {
            INode<T> node = state.targetEdge.child;
            if(node.readyToReplace) {
                state.mode = IStateRecord.CLEANUP;
            } else {
                state.mode = IStateRecord.DISCOVERY;
            }
        }
    }

    //following two methods help conflicting delete operations
    private void helpTargetNode(IEdge<T> helpeeEdge) {
        IStateRecord<T> state = new IStateRecord<>();
        state.targetEdge = helpeeEdge;
        state.mode = IStateRecord.INJECTION;

        boolean result = markChildEdge(state, IEdge.LEFT);
        if (!result) {
            return;
        }
        markChildEdge(state, IEdge.RIGHT);
        initializeTypeAndUpdateMode(state);

        if(state.mode == IStateRecord.DISCOVERY) {
            findAndMarkSuccesor(state);
        }

        if(state.mode == IStateRecord.DISCOVERY) {
            removeSuccesor(state);
        }

        if(state.mode == IStateRecord.CLEANUP) {
            cleanUp(state);
        }
    }

    private void helpSuccesorNode(IEdge<T> helpeeEdge) {
        INode<T> parent = helpeeEdge.parent;
        INode<T> node = helpeeEdge.child;

        INode<T> left = node.child[IEdge.LEFT].getReference();
        IStateRecord<T> state = new IStateRecord<>();
        state.targetEdge = new IEdge<>(null, left, IEdge.NONE);
        state.mode = IStateRecord.DISCOVERY;
        IEdge<T> pLastEdge = new IEdge(null, parent, IEdge.NONE);
        state.succesorRecord = new ISeekRecord<>(pLastEdge, helpeeEdge, null);
        removeSuccesor(state);
    }



    @Override
    public boolean search(T value) {
        ISeekRecord<T> seekRecord = seek(value);
        if (seekRecord != null && seekRecord.lastEdge != null && seekRecord.lastEdge.child != null) {
            INode<T> node = seekRecord.lastEdge.child;
            if(node.mKey != null && node.mKey.getReference().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
