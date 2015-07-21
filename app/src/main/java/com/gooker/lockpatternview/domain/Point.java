package com.gooker.lockpatternview.domain;

/**
 * Created by sczhang on 15/7/20. 下午10:08
 * Package Name com.gooker.lockpatternview
 * Project Name LockPatternView
 */
public class Point {

    public int x;
    public int y;
    public int index;
    public PointStatus status ;

    public Point(int x, int y, PointStatus status) {
        this.x = x;
        this.y = y;
        this.status = status;
    }

    public enum PointStatus {
        NORMAL, ERROR, PRESSED,
    }
}
