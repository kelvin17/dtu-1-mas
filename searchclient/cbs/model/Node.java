package searchclient.cbs.model;

/**
 * Model for CBS
 * Node in the conflict tree
 */
public class Node {

    /**
     * If it's the goal Node
     * the solution is the final solution can be used
     * else
     */
    private final Solution solution;

    //Todo
    private float solutionCost;
    /**
     * Every node is created for solving a selectedConflict except the root one
     */
    private final AbstractConflict selectedConflict;

    /**
     * The constraint that was added in this node (without constraints from {@link #parent}).
     */
    private Constraint addedConstraint;

    private final Node parent;

    /**
     * As there are at most of two methods to solve the conflict{@link #addedConstraint}
     * leftChild uses one method with a solution to addedConstraint
     * rightChild uses the other one
     */
    private Node leftChild;
    private Node rightChild;

    public Node(Solution solution, AbstractConflict selectedConflict, Node parent) {
        this.solution = solution;
        this.selectedConflict = selectedConflict;
        this.parent = parent;
    }

    public Node(Solution solution, AbstractConflict selectedConflict, Constraint addedConstraint, Node parent) {
        this.solution = solution;
        this.selectedConflict = selectedConflict;
        this.addedConstraint = addedConstraint;
        this.parent = parent;
    }

    public void setLeftChild(Node leftChild) {
        this.leftChild = leftChild;
    }

    public void setRightChild(Node rightChild) {
        this.rightChild = rightChild;
    }

    public Solution getSolution() {
        return solution;
    }

    public Node getParent() {
        return parent;
    }

    public Node getLeftChild() {
        return leftChild;
    }

    public Node getRightChild() {
        return rightChild;
    }

    public AbstractConflict getSelectedConflict() {
        return selectedConflict;
    }

    public Constraint getAddedConstraint() {
        return addedConstraint;
    }

    public void setAddedConstraint(Constraint addedConstraint) {
        this.addedConstraint = addedConstraint;
    }
}
