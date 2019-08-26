package org.changyeyi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 线段对象
 * @author changyeyi
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Line {
    /**
     * 线端点一
     */
    public Point pointA;
    /**
     * 线端点二
     */
    public Point pointB;


    /**
     * 判断点是否在线上
     */
    public boolean isContainsPoint(Point point){
        double x0=point.getLongitude();
        double y0=point.getLatitude();
        double x1=pointA.getLongitude();
        double y1=pointA.getLatitude();
        double x2=pointB.getLongitude();
        double y2=pointB.getLatitude();
        //判断点是否在，以线段为对角线的长方形范围内，如果不在，则不可能存在于线上
        if((x0-x1)*(x0-x2)>0||(y0-y1)*(y0-y2)>0){return false;}
        //通过斜率判断点是否在线段所在直线上
        if(x2==x1||y2==y1){return true;}
        double ESP=1e-9;
        return Math.abs(multiply(x0, y0, x1, y1, x2, y2)) < ESP;
    }

    private double multiply(double px0 , double py0 , double px1 , double py1 , double px2 , double py2){
        return ((px1 - px0) * (py2 - py0) - (px2 - px0) * (py1 - py0));
    }
    /**
     * 判断两线段是否相交
     * 原理：
     *      线段a相交于线段b所在直线，同时线段b相交于线段a所在直线,
     *      则，线段a相交于线段b
     * @param line
     * @return
     */
    public boolean isIntersect(Line line){
        if(lineIntersectSide(line.getPointA(), line.getPointB(), this.getPointA(), this.getPointB())){return false;}
        if(lineIntersectSide(this.getPointA(), this.getPointB(), line.getPointA(), line.getPointB())){return false;}
        return true;
    }

    /**
     * 判断线段cd是否与直线ab相交
     */
    private boolean lineIntersectSide(Point a,Point b,Point c,Point d){
        //判断点c、d分别在直线ab哪一侧
        double fc=(c.getLatitude()-a.getLatitude())*(a.getLongitude()-b.getLongitude())-(c.getLongitude()-a.getLongitude())*(a.getLatitude()-b.getLatitude());
        double fd=(d.getLatitude()-a.getLatitude())*(a.getLongitude()-b.getLongitude())-(d.getLongitude()-a.getLongitude())*(a.getLatitude()-b.getLatitude());
        //在同一侧表示没有相交，返回false
        return !(fc * fd <= 0);
    }

}
