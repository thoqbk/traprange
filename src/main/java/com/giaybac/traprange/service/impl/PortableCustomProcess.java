/**
 * Copyright (C) 2014, GIAYBAC.COM
 *
 * Released under the MIT license
 */
package com.giaybac.traprange.service.impl;

import com.giaybac.traprange.service.CustomProcess;
import com.giaybac.traprange.entity.Cell;
import com.giaybac.traprange.entity.Line;
import com.giaybac.traprange.service.impl.entity.PortableCell;
import com.giaybac.traprange.service.impl.entity.PortableLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author THO Q LUONG
 *
 * Jul 17, 2014 3:41:05 PM
 */
public class PortableCustomProcess implements CustomProcess {

    //--------------------------------------------------------------------------
    //  Members
    private final Logger logger = LoggerFactory.getLogger(PortableCustomProcess.class);
    private int errorLinesCount = 0;

    //--------------------------------------------------------------------------
    //  Initialization
    //--------------------------------------------------------------------------
    //  Getter N Setter
    //--------------------------------------------------------------------------
    //  Method binding
    @Override
    public void verify(Line line) {
        if (line.getCells().size() < 16) {
            errorLinesCount++;
            logger.error("Error line: " + line.getIdx() + " in total error lines:" + errorLinesCount + "; number of cells: " + line.getCells().size() + "; ID: " + ((PortableLine) line).getCell(0).getContent());
        }
    }

    @Override
    public void verify(Cell cell) {
    }

    @Override
    public void clean(Line line) {
        PortableLine portableLine = (PortableLine) line;
        //debug
        logger.debug("Clean line: " + line.getIdx() + "; id: " + portableLine.getCell(0).getContent());
        //Process merge cells: "350028.04" or "RE26.11" should be: "3500", "28.04" or "RE", "26.41"
        PortableCell cell6 = portableLine.getCell(6);
        String cell6Content = cell6.getContent();
        String newCell6Content;
        String newCell7Content;
        if (cell6Content.startsWith("RE")) {
            newCell6Content = "RE";
            newCell7Content = cell6Content.substring(2);
        } else {
            newCell6Content = cell6Content.substring(0, 4);
            newCell7Content = cell6Content.substring(4);
        }
        //update cell 6
        cell6.setContent(newCell6Content);
        //new cell 7
        PortableCell cell7 = new PortableCell();
        cell7.setContent(newCell7Content);
        cell7.setIdx(7);
        portableLine.addCell(cell7);

        //Group cell 12 and 13        
        PortableCell cell12 = portableLine.getCell(12);
        if (cell12 == null) {
            cell12 = new PortableCell();
            cell12.setContent("");
            cell12.setIdx(12);
            portableLine.addCell(cell12);
        }
        PortableCell cell13 = portableLine.getCell(13);
        if (cell13 != null) {
            cell12.setContent(cell12.getContent() + " " + cell13.getContent());
            //delete cell 13
            portableLine.removeCell(13);
        }

        //extract cell 13, 14
        cell13 = portableLine.getCell(13);
        if (cell13 == null) {
            cell13 = new PortableCell();
            cell13.setContent("");
            cell13.setIdx(13);
            portableLine.addCell(cell13);
        }
        String newCell13And14Content = cell13.getContent();
        int maxCellIdx = portableLine.getMaxCellIdx();
        for (int idx = 14; idx < maxCellIdx; idx++) {
            Cell cell = portableLine.getCell(idx);
            if (cell != null) {
                newCell13And14Content += portableLine.getCell(idx).getContent();
            }
        }
        StringBuilder newCell13Content = new StringBuilder();
        newCell13Content.append(newCell13And14Content.charAt(0));
        if (newCell13And14Content.charAt(1) == '.') {
            newCell13Content.append(".");
            newCell13Content.append(newCell13And14Content.charAt(2));
        }
        cell13.setContent(newCell13Content.toString());

        //cell 14
        while (portableLine.getMaxCellIdx() > 15) {
            portableLine.removeCell(14);
        }
        for (int idx = 14; idx < maxCellIdx; idx++) {
            portableLine.removeCell(idx);
        }
        String cell14Content = newCell13And14Content.substring(newCell13Content.length());
        PortableCell cell14 = new PortableCell();
        cell14.setIdx(14);
        cell14.setLineIdx(line.getIdx());
        cell14.setContent(cell14Content);
        portableLine.addCell(cell14);
    }
    //--------------------------------------------------------------------------
    //  Implement N Override
    //--------------------------------------------------------------------------
    //  Utils
    //--------------------------------------------------------------------------
    //  Inner class
}
