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
 * Jul 21, 2014 5:12:12 PM
 */
public class CSVCustomeProcess implements CustomProcess{
    //--------------------------------------------------------------------------
    //  Members
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
    }

    @Override
    public void verify(Cell cell) {
    }

    @Override
    public void clean(Line line) {
    }
    //--------------------------------------------------------------------------
    //  Utils
    //--------------------------------------------------------------------------
    //  Inner class
}