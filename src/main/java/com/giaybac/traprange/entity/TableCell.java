/**
* Copyright (C) 2015, GIAYBAC
*
* Released under the MIT license
*/
package com.giaybac.traprange.entity;

/**
 *
 * @author THO Q LUONG Jul 16, 2014 11:19:34 AM
 */
public class TableCell {
    private final String content;
    private final int idx;

    public TableCell(int idx, String content) {
        this.idx = idx;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public int getIdx() {
        return idx;
    }
}
