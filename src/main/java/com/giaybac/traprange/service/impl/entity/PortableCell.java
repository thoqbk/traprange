/**
 * Copyright (C) 2014, GIAYBAC.COM
 *
 * Released under the MIT license
 */

package com.giaybac.traprange.service.impl.entity;

import com.giaybac.traprange.entity.Cell;

/**
 *
 * @author THO Q LUONG
 * Jul 16, 2014 11:23:56 AM
 */
public class PortableCell extends Cell{
    //--------------------------------------------------------------------------
    //  Members
    private int x;
    private int y;
    
    private int maxX;
    //--------------------------------------------------------------------------
    //  Method binding
    //--------------------------------------------------------------------------
    //  Implement N Override
    @Override
    public String toString(){
        return this.getContent();
    }
    //--------------------------------------------------------------------------
    //  Utils
    //--------------------------------------------------------------------------
    //  Inner class
    //--------------------------------------------------------------------------
    //  Initialization
    //--------------------------------------------------------------------------
    //  Getter N Setter
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getMaxX() {
        return maxX;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }    
}