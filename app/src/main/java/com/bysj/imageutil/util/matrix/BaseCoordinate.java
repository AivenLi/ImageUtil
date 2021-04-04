package com.bysj.imageutil.util.matrix;

/**
 * 坐标类
 *
 * Create on 2021-4-4
 */

public abstract class BaseCoordinate {

    public int x;
    public int y;

    public BaseCoordinate(int x, int y) {

        this.x = x;
        this.y = y;
    }

    public void setX(int x) {

        this.x = x;
    }

    public void setY(int y) {

        this.y = y;
    }

    public int getY() {

        return this.y;
    }

    protected abstract BaseCoordinate getCoordinate();
    protected abstract double getCoordinateDistance(BaseCoordinate to);
}
