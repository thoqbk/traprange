/**
 * Copyright (C) 2014, GIAYBAC.COM
 *
 * Released under the MIT license
 */
package com.giaybac.traprange.service.impl;

import com.giaybac.traprange.service.CustomProcess;
import com.giaybac.traprange.entity.Cell;
import com.giaybac.traprange.entity.Line;

/**
 *
 * @author THO Q LUONG
 *
 * Jul 21, 2014 11:17:11 AM
 */
public class TrapRangesCustomProcess implements CustomProcess {
    //--------------------------------------------------------------------------
    //  Members
    //private final Logger logger = LoggerFactory.getLogger(TrapRangesCustomProcess.class);
    //--------------------------------------------------------------------------
    //  Initialization
    //--------------------------------------------------------------------------
    //  Getter N Setter
    //--------------------------------------------------------------------------
    //  Method binding
    //--------------------------------------------------------------------------
    //  Implement N Override
    @Override
    public void verify(Line line) {
        if (line.getMaxCellIdx() != 15) {
            throw new RuntimeException("Invalid line: " + line.toString() + ";maxCellIdx: " + line.getMaxCellIdx());
        }
    }

    @Override
    public void verify(Cell cell) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clean(Line line) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        line.mergeCells(13, 18, " ");
        line.mergeCells(8, 9, " ");
    }
    //--------------------------------------------------------------------------
    //  Utils
    //--------------------------------------------------------------------------
    //  Inner class
}
