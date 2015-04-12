/**
* Copyright (C) 2015, GIAYBAC
*
* Released under the MIT license
*/
package com.giaybac.traprange.entity;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author THO Q LUONG Jul 16, 2014 11:20:12 AM
 */
public class TableRow {

    //--------------------------------------------------------------------------
    //  Members
    private final int idx;
    private final List<TableCell> cells = new ArrayList<>();

    //--------------------------------------------------------------------------
    //  Initialization
    public TableRow(int idx) {
        this.idx = idx;
    }

    //--------------------------------------------------------------------------
    //  Getter N Setter
    public int getIdx() {
        return idx;
    }

    public List<TableCell> getCells() {
        return cells;
    }

    //--------------------------------------------------------------------------
    //  Method binding
    //--------------------------------------------------------------------------
    //  Implement N Override    
    @Override
    public String toString() {
        StringBuilder retVal = new StringBuilder();
        int lastCellIdx = -1;
        for (TableCell cell : cells) {
            for (int idx = lastCellIdx; idx < cell.getIdx() - 1; idx++) {
                retVal.append(";");
            }
            if (retVal.length() > 0) {
                retVal.append(";");
            }
            retVal.append(cell.getContent());
            lastCellIdx = cell.getIdx();
        }
        //return
        return retVal.toString();
    }
    //--------------------------------------------------------------------------
    //  Utils
    //--------------------------------------------------------------------------
    //  Inner class
}
