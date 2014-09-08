/**
 * Copyright (C) 2014, GIAYBAC.COM
 *
 * Released under the MIT license
 */

package com.giaybac.traprange.entity;

/**
 *
 * @author THO Q LUONG
 * Jul 16, 2014 11:19:34 AM
 */
public class Cell {
    //--------------------------------------------------------------------------
    //  Members
    private String content = "";
    private int idx;
    private int lineIdx;
    
    //--------------------------------------------------------------------------
    //  Method binding
    //--------------------------------------------------------------------------
    //  Implement N Override
    //--------------------------------------------------------------------------
    //  Utils
    //--------------------------------------------------------------------------
    //  Inner class
    //--------------------------------------------------------------------------
    //  Initialization
    //--------------------------------------------------------------------------
    //  Getter N Setter
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public int getLineIdx() {
        return lineIdx;
    }

    public void setLineIdx(int lineIdx) {
        this.lineIdx = lineIdx;
    }
}