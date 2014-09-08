/**
 * Copyright (C) 2014, GIAYBAC.COM
 *
 * Released under the MIT license
 */
package com.giaybac.traprange.service;

import com.giaybac.traprange.entity.Cell;
import com.giaybac.traprange.entity.Line;

/**
 *
 *
 * After entire line was created successfully, CustomProcess.clean(...) will be
 * invoked to make line data more exactly.
 *
 * After each time a cell or line was created, extractService will invoke
 * CustomProcess.verify(...) to validate this cell or line
 *
 * Sequence of process:<br/>
 * 1. clean<br/>
 * 2. verify cells<br/>
 * 3. verify line<br/>
 *
 * @author THO Q LUONG
 *
 * Jul 17, 2014 3:41:13 PM
 */
public interface CustomProcess {

    //--------------------------------------------------------------------------
    //  Members
    //--------------------------------------------------------------------------
    //  Initialization
    //--------------------------------------------------------------------------
    //  Getter N Setter
    //--------------------------------------------------------------------------
    //  Method binding
    public void verify(Line line);

    public void verify(Cell cell);

    public void clean(Line line);
    //--------------------------------------------------------------------------
    //  Implement N Override
    //--------------------------------------------------------------------------
    //  Utils
    //--------------------------------------------------------------------------
    //  Inner class
}
