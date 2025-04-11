package searchclient.cbs.model;

import java.util.Comparator;
import java.util.Objects;

/**
 * Model for CBS
 * Node in the conflict tree
 */
public class Node implements Comparable<Node> {

    /**
     * Every node is created for solving a selectedConflict with a specific constraint except the root one
     * As the paper analysis in 4.2.4
     */
    private AbstractConflict selectedConflict;
    /**
     * The constraint that was added in this node as one way to solve the selectedConflict{@link #selectedConflict}
     * (without constraints from {@link #parent}).
     */
    private Constraint addedConstraint;
    /**
     * A Set of k paths, one path for each agent.
     * The path for agent a_i is consistent with the constraints of a_i
     */
    private Solution solution;
    /**
     * The cost of the current solution(using SIC) , and it is referred to as the f-value of the node
     */
    private int solutionCost = 0;

    private final Node parent;

    /**
     * As there are at most of two methods to solve the conflict{@link #addedConstraint}
     * leftChild uses one method with a solution to addedConstraint
     * rightChild uses the other one
     */
    private Node leftChild;
    private Node rightChild;

    public Node(Node parent) {
        this.parent = parent;
    }

    public Node(Node parent, AbstractConflict selectedConflict, Constraint addedConstraint) {
        this.parent = parent;
        this.selectedConflict = selectedConflict;
        this.addedConstraint = addedConstraint;
    }

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

    /**
     * the cost of Solution is the sum cost of every agent
     *
     * @return
     */
    public int getSolutionCost() {
        int cost = 0;
        for (SingleAgentPlan singleAgentPlan : solution) {
            cost += singleAgentPlan.getCost();
        }
        solutionCost = cost;
        return solutionCost;
    }

    public void setLeftChild(Node leftChild) {
        this.leftChild = leftChild;
    }

    public void setRightChild(Node rightChild) {
        this.rightChild = rightChild;
    }

    public void setSolution(Solution solution) {
        this.solution = solution;
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

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int compareTo(Node o) {
        return Objects.compare(this, o, Comparator.comparing(Node::getSolutionCost));
    }
}
