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
 * @author THOQ LUONG Mar 22, 2015 3:49:22 PM
 */
public class Table {

    //--------------------------------------------------------------------------
    //  Members
    private final int pageIdx;
    private final List<TableRow> rows = new ArrayList<>();
    private final int columnsCount;

    //--------------------------------------------------------------------------
    //  Initialization and releasation
    public Table(int idx, int columnsCount) {
        this.pageIdx = idx;
        this.columnsCount = columnsCount;
    }

    //--------------------------------------------------------------------------
    //  Getter N Setter    
    public int getPageIdx() {
        return pageIdx;
    }

    public List<TableRow> getRows() {
        return rows;
    }

    public String toHtml() {
        StringBuilder retVal = new StringBuilder();
        retVal.append("<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<meta charset='utf-8'>")
                .append("</head>")
                .append("<body>");
        retVal.append("<table border='1'>");
        for (TableRow row : rows) {
            retVal.append("<tr>");
            int cellIdx = 0;//pointer of row.cells
            int columnIdx = 0;//pointer of columns
            while (columnIdx < columnsCount) {
                if (cellIdx < row.getCells().size()) {
                    TableCell cell = row.getCells().get(cellIdx);
                    if (cell.getIdx() == columnIdx) {
                        retVal.append("<td>")
                                .append(cell.getContent())
                                .append("</td>");
                        cellIdx++;
                        columnIdx++;
                    } else if (columnIdx < cellIdx) {
                        retVal.append("<td>")
                                .append("</td>");
                        columnIdx++;
                    } else {
                        throw new RuntimeException("Invalid state");
                    }
                }else{
                    break;
                }
            }
            retVal.append("</tr>");
        }
        retVal.append("</table>")
                .append("</body>")
                .append("</html>");
        return retVal.toString();
    }

    //--------------------------------------------------------------------------
    //  Method binding
    //--------------------------------------------------------------------------
    //  Implement N Override
    @Override
    public String toString() {
        StringBuilder retVal = new StringBuilder();
        for (TableRow row : rows) {
            if (retVal.length() > 0) {
                retVal.append("\n");
            }
            retVal.append(row.toString());
        }
        //return
        return retVal.toString();
    }
    //--------------------------------------------------------------------------
    //  Utils
    //--------------------------------------------------------------------------
    //  Inner class

}
