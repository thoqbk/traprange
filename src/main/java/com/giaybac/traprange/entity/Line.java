/**
 * Copyright (C) 2014, GIAYBAC.COM
 *
 * Released under the MIT license
 */
package com.giaybac.traprange.entity;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author THO Q LUONG Jul 16, 2014 11:20:12 AM
 * @param <T>
 */
public class Line<T extends Cell> {

    //--------------------------------------------------------------------------
    //  Members
    private int idx;
    private final Set<Integer> cellIdxs = new TreeSet<>();
    protected List<T> cells = ImmutableList.of();

    //--------------------------------------------------------------------------
    //  Getter N Setter
    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public List<T> getCells() {
        return cells;
    }

    public void addCell(T cell) {
        if (cellIdxs.contains(cell.getIdx())) {
            throw new RuntimeException("Cell with idx " + cell.getIdx() + " has already existed");
        }
        List<T> tempCells = new ArrayList<>();
        tempCells.addAll(cells);
        tempCells.add(cell);
        tempCells.sort((T o1, T o2) -> {
            int retVal = 0;
            if (o1.getIdx() > o2.getIdx()) {
                retVal = 1;
            } else if (o1.getIdx() < o2.getIdx()) {
                retVal = -1;
            }
            return retVal;
        });
        ImmutableList.Builder<T> builder = new ImmutableList.Builder<>();
        tempCells.forEach(c -> builder.add(c));
        this.cells = builder.build();
        //cellIdxs
        this.cellIdxs.add(cell.getIdx());
    }

    public int getMaxCellIdx() {
        int retVal = -1;
        for (int cellIdx : cellIdxs) {
            retVal = cellIdx;
        }
        return retVal;
    }

    public T getCell(int idx) {
        T retVal = null;
        for (T cell : cells) {
            if (cell.getIdx() == idx) {
                retVal = cell;
                break;
            }
        }
        return retVal;
    }

    public void mergeCells(int fromIdx, int toIdx, String glue) {
        if (toIdx <= fromIdx) {
            throw new RuntimeException("Invalid fromIdx and,or toIdx. Expect toIdx > fromIdx");
        }
        //build target's cell content
        StringBuilder targetContent = new StringBuilder();
        for (T cell : cells) {
            if (cell.getIdx() >= fromIdx && cell.getIdx() <= toIdx) {
                if (targetContent.length() > 0) {
                    targetContent.append(glue);
                }
                targetContent.append(cell.getContent());
            } else if (cell.getIdx() > toIdx) {
                break;
            }
        }
        cellIdxs.clear();
        //create tempCells, loop through this.cells
        ImmutableList.Builder<T> cellsBuilder = new ImmutableList.Builder<>();
        for (T cell : cells) {
                //1. if cell.idx = fromIdx, update
            //2. if cell.idx > fromIdx && cell.idx < toIdx, discard (do nothing)
            //3. decrease idx of cell that idx > toIdx
            if (cell.getIdx() == fromIdx) {
                cell.setContent(targetContent.toString());
            }
            if (cell.getIdx() <= fromIdx || cell.getIdx() > toIdx) {
                cellsBuilder.add(cell);
            }
            if (cell.getIdx() > toIdx) {
                cell.setIdx(cell.getIdx() - (toIdx - fromIdx));
            }
            //update cellIdxs
            cellIdxs.add(cell.getIdx());
        }
        this.cells = cellsBuilder.build();
    }

    //--------------------------------------------------------------------------
    //  Method binding
    //--------------------------------------------------------------------------
    //  Implement N Override    
    @Override
    public String toString() {
        StringBuilder retVal = new StringBuilder();
        int previousCellIdx = -1;
        for (T cell : cells) {
            if (previousCellIdx != -1) {
                while (previousCellIdx < cell.getIdx()) {
                    retVal.append(",");
                    previousCellIdx++;
                }
            } else {
                previousCellIdx = 0;
            }
            retVal.append(cell.getContent());
        }
        //return
        return retVal.toString();
    }
    //--------------------------------------------------------------------------
    //  Utils
    //--------------------------------------------------------------------------
    //  Inner class
    //--------------------------------------------------------------------------
    //  Initialization
}
