/*
Name: Alex James Waddell
Student Number: C3330987
Description: the node class, acts as an object representing a node in a tree
and holding up to 3 children
 */

public class Node {



    private Node leftSubTree;
    private Node middleSubTree;
    private Node rightSubTree;
    private String symbolValue;
    private String symbolType;
    private String nodeValue;

    public Node () {

    }

    public Node(String nodeValue_) {
        nodeValue = nodeValue_;
        leftSubTree = null;
        middleSubTree = null;
        rightSubTree = null;
        symbolValue = null;
        symbolType = null;
    }

    public Node(String nodeValue_, Node leftSubTree_) {
        nodeValue = nodeValue_;
        leftSubTree = leftSubTree_;
        middleSubTree = null;
        rightSubTree = null;
        symbolValue = null;
        symbolType = null;
    }

    public Node(String nodeValue_, Node leftSubTree_, Node rightSubTree_) {
        nodeValue = nodeValue_;
        leftSubTree = leftSubTree_;
        rightSubTree = rightSubTree_;
        middleSubTree = null;
        symbolValue = null;
        symbolType = null;
    }

    public Node(String nodeValue_, Node leftSubTree_, Node middleSubTree_,Node rightSubTree_) {
        nodeValue = nodeValue_;
        leftSubTree = leftSubTree_;
        rightSubTree = rightSubTree_;
        middleSubTree = middleSubTree_;
        symbolValue = null;
        symbolType = null;
    }

    public void setLeftSubTree(Node leftSubTree) {
        this.leftSubTree = leftSubTree;
    }

    public void setMiddleSubTree(Node middleSubTree) {
        this.middleSubTree = middleSubTree;
    }

    public void setNodeValue(String nodeValue) {
        this.nodeValue = nodeValue;
    }

    public void setRightSubTree(Node rightSubTree) {
        this.rightSubTree = rightSubTree;
    }

    public void setSymbolType(String symbolType) {
        this.symbolType = symbolType;
    }

    public String getSymbolType() {
        return symbolType;
    }

    public void setSymbolValue(String symbolValue) {
        this.symbolValue = symbolValue;
    }

    public Node getLeftSubTree() {
        return leftSubTree;
    }

    public Node getMiddleSubTree() {
        return middleSubTree;
    }

    public Node getRightSubTree() {
        return rightSubTree;
    }

    public String getSymbolValue() {
        return symbolValue;
    }

    public String getNodeValue() {
        return nodeValue;
    }
}
