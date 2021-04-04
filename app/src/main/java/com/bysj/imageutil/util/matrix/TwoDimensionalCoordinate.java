package com.bysj.imageutil.util.matrix;

public class TwoDimensionalCoordinate extends BaseCoordinate {

    public TwoDimensionalCoordinate(int x, int y) {

        super(x, y);
    }

    public void setX(int x) {

        this.x = x;
    }

    public void setY(int y) {

        this.y = y;
    }

    @Override
    protected BaseCoordinate getCoordinate() {

        return this;
    }

    @Override
    protected double getCoordinateDistance(BaseCoordinate to) {

        double distance;

        if ( to == null ) {

            distance = -1;
        } else {

            double point1 = ( this.x - to.x ) * ( this.x - to.x );
            double point2 = ( this.y - to.y ) * ( this.y - to.y );
            distance = Math.sqrt( point1 + point2 );
        }
        return distance;
    }

}
