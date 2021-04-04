package com.bysj.imageutil.util.matrix;

/**
 * 图类。
 * 该图类每个结点包含上下左右
 *
 * Create on 2021-4-4
 */

public class Graph<T> {

    private T node;
    private TwoDimensionalCoordinate coordinate;
    private Graph<T> top;
    private Graph<T> bottom;
    private Graph<T> left;
    private Graph<T> right;

    public Graph(T node, TwoDimensionalCoordinate coordinate) {

        this.node = node;
        this.coordinate = coordinate;
        this.top = null;
        this.bottom = null;
        this.left = null;
        this.right = null;
    }

    public Graph(T node, int x, int y) {

        this(node, new TwoDimensionalCoordinate(x, y));
    }

    public T getNode() {

        return this.node;
    }

    public void insertTop(Graph<T> graph) {

        this.top = graph;
        graph.bottom = this.top;
    }

    public void insertBottom(Graph<T> graph) {

        this.bottom = graph;
        graph.top = bottom;
    }

    public void insertLeft(Graph<T> graph) {

        this.left = graph;
        graph.right = this.left;
    }

    public void insertRight(Graph<T> graph) {

        this.right = graph;
        graph.left = this.right;
    }

    public void setNode(T node) {

        this.node = node;
    }

    public void setTopNode(Graph<T> top) {

        this.top = top;
    }

    public void setBottomNode(Graph<T> bottom) {

        this.bottom = bottom;
    }

    public void setLeftNode(Graph<T> left) {

        this.left = left;
    }

    public void setRightNode(Graph<T> right) {

        this.right = right;
    }

    public Graph<T> getTopNode() {

        return this.top;
    }

    public Graph<T> getBottomNode() {

        return this.bottom;
    }

    public Graph<T> getLeftNode() {

        return this.left;
    }

    public Graph<T> getRightNode() {

        return this.right;
    }

    public void setCoordinate(TwoDimensionalCoordinate coordinate) {

        this.coordinate = coordinate;
    }

    public TwoDimensionalCoordinate getCoordinate() {

        return this.coordinate;
    }

    public double getFromToTopDistance() {

        return getDistance(coordinate, top.coordinate);
    }

    public double getFromToBottomDistance() {

        return getDistance(coordinate, bottom.coordinate);
    }

    public double getFromToLeftDistance() {

        return getDistance(coordinate, left.coordinate);
    }

    public double getFromToRightDistance() {

        return getDistance(coordinate, right.coordinate);
    }

    private double getDistance(TwoDimensionalCoordinate from, TwoDimensionalCoordinate to) {

        return from.getCoordinateDistance(to);
    }
}
