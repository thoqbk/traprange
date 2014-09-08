/**
 * Copyright (C) 2014, GIAYBAC.COM
 *
 * Released under the MIT license
 */
package com.giaybac.traprange.service.impl.entity;

import com.giaybac.traprange.entity.Line;
import java.util.Iterator;

/**
 *
 * @author THO Q LUONG Jul 16, 2014 11:24:57 AM
 */
public class PortableLine extends Line<PortableCell> {

    //--------------------------------------------------------------------------
    //  Members
    private Status status;
    private int minY;
    private int maxY;
    private int preferredY;

    //--------------------------------------------------------------------------
    //  Initialization
    public PortableLine() {
    }
    //--------------------------------------------------------------------------
    //  Getter N Setter

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getMinY() {
        return minY;
    }

    public void setMinY(int minY) {
        this.minY = minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    public int getPreferredY() {
        return preferredY;
    }

    public void setPreferredY(int preferredY) {
        this.preferredY = preferredY;
    }

    public PortableCell getCellByX(int x) {
        PortableCell retVal = null;
        for (PortableCell portableCell : getCells()) {
            if (portableCell.getX() == x) {
                retVal = portableCell;
                break;
            }
        }
        //return
        return retVal;
    }

    //--------------------------------------------------------------------------
    //  Method binding

    public PortableCell removeCell(int idx) {
        PortableCell retVal = null;
        Iterator<PortableCell> iteratorCell = cells.iterator();
        while (iteratorCell.hasNext()) {
            PortableCell tempCell = iteratorCell.next();
            if (tempCell.getIdx() == idx) {
                iteratorCell.remove();
                retVal = tempCell;
            }
            if (tempCell.getIdx() > idx) {
                tempCell.setIdx(tempCell.getIdx() - 1);
            }
        }
        return retVal;
    }
    //--------------------------------------------------------------------------
    //  Implement N Override
    //--------------------------------------------------------------------------
    //  Utils
    //--------------------------------------------------------------------------
    //  Inner class
    public enum Status {

        EXTRACTING, FINISHED, FAIL, NA
    }
}
