/**
 * Copyright (C) 2014, GIAYBAC.COM
 *
 * Released under the MIT license
 */
package com.giaybac.traprange.service.impl;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.giaybac.traprange.service.ExtractService;
import com.giaybac.traprange.entity.Cell;
import com.giaybac.traprange.entity.Line;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author THO Q LUONG
 *
 * Jul 21, 2014 3:32:58 PM
 */
public class CSVExtractorBuilderImpl extends AbstractExtractorBuilder {

    //--------------------------------------------------------------------------
    //  Members
    private final List<Line> lines = new ArrayList<>();
    private final Logger logger = LoggerFactory.getLogger(CSVExtractorBuilderImpl.class);

    //--------------------------------------------------------------------------
    //  Initialization
    //--------------------------------------------------------------------------
    //  Getter N Setter
    //--------------------------------------------------------------------------
    //  Method binding
    //--------------------------------------------------------------------------
    //  Implement N Override    
    @Override
    public ExtractService.ExtractorBuilder extract() {
        String separator = ",";
        if (properties != null && properties.getProperty("separator") != null) {
            separator = properties.getProperty("separator");
        }
        String encoding = null;
        if (properties != null && properties.getProperty("encoding") != null) {
            encoding = properties.getProperty("encoding");
        } else {
            //try detecting encoding
            logger.info("Encoding was not set, try detecting encoding");
            try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(filePath))) {
                CharsetDetector detector = new CharsetDetector();
                detector.setText(bufferedInputStream);
                CharsetMatch match = detector.detect();
                if (match != null) {
                    logger.info("Detected charset: " + match.getName());
                    encoding = match.getName();
                }
            } catch (IOException ex) {
                logger.error("Detect encoding fail", ex);
            }
        }
        logger.info("Begin extracting inputFile with encoding: " + encoding);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), encoding))) {
            String lineText;
            int lineIdx = 0;
            int columnsCount;
            while ((lineText = reader.readLine()) != null) {
                if (lineIdx == 0) {
                    columnsCount = lineText.split(separator).length;
                    logger.debug("Column count: " + columnsCount);
                    lineIdx++;
                    continue;
                }
                //ELSE:
                String[] cellTexts = lineText.split(separator);
                Line<Cell> line = new Line();
                line.setIdx(lineIdx);
                for (int cellIdx = 0; cellIdx < cellTexts.length; cellIdx++) {
                    String cellText = cellTexts[cellIdx];
                    if (!cellText.isEmpty()) {
                        Cell cell = new Cell();
                        cell.setContent(cellText);
                        cell.setLineIdx(line.getIdx());
                        cell.setIdx(cellIdx);
                        line.addCell(cell);
                    }
                }
                boolean isValidLine = false;
                if (!line.getCells().isEmpty()) {
                    if (customProcess != null) {
                        try {
                            customProcess.clean(line);
                            for (Cell cell : line.getCells()) {
                                customProcess.verify(cell);
                            }
                            customProcess.verify(line);
                            isValidLine = true;
                        } catch (Exception e) {
                            logger.error("Verify line fail, line: " + lineText, e);
                        }
                    } else {
                        isValidLine = true;
                    }
                }
                if (isValidLine) {
                    lines.add(line);
                    logger.debug("Parse line: " + lineText + " successfully");
                } else {
                    logger.debug("Parse line fail, line: " + lineText);
                }
                lineIdx++;
            }
        } catch (IOException ex) {
            logger.debug("Extract file error", ex);
        }
        return this;
    }

    @Override
    public List<? extends Line> getResult() {
        return this.lines;
    }
    //--------------------------------------------------------------------------
    //  Utils
    //--------------------------------------------------------------------------
    //  Inner class
}
