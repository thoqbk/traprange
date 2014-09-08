/**
 * Copyright (C) 2014, GIAYBAC.COM
 *
 * Released under the MIT license
 */
package com.giaybac.traprange.service.impl;

import com.giaybac.traprange.service.ExtractService;
import com.giaybac.traprange.entity.Line;
import com.giaybac.traprange.service.impl.entity.PortableCell;
import com.giaybac.traprange.service.impl.entity.PortableLine;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.graphics.text.LineText;
import org.icepdf.core.pobjects.graphics.text.PageText;
import org.icepdf.core.pobjects.graphics.text.WordText;
import org.slf4j.LoggerFactory;

/**
 *
 * @author THO Q LUONG
 *
 * Jul 16, 2014 11:27:17 AM
 */
public class PortableExtractorBuilderImpl extends AbstractExtractorBuilder {

    //--------------------------------------------------------------------------
    //  Members
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(PortableExtractorBuilderImpl.class);

    private final List<PortableLine> lines = new ArrayList<>();

    private Document document;
    private final Set<Integer> cellXs = new TreeSet<>();
    private final Set<Integer> rightCellXs = new TreeSet<>();

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
        try {
            document = new Document();
            logger.info("Begin loading file");
            document.setFile(filePath);
            logger.info("Loaded file successfully. File path: " + filePath);
            //extract cellXs
            extractCellXs();
            //parse pages
            int lineHeightThreshold = Integer.parseInt(properties.getProperty("lineHeightThreshold"));
            int columnsCount = Integer.parseInt(properties.getProperty("columnsCount"));
            int maxNullableFieldsCount = Integer.parseInt(properties.getProperty("maxNullableFieldsCount"));
            for (int pageIdx = 0; pageIdx < document.getNumberOfPages(); pageIdx++) {
                PageText pageText = document.getPageText(pageIdx);
                logger.info("Begin reading page: " + pageIdx);
                //ELSE: read lines:
                int lineIdx = 0;
                int baselineY = 0;
                PortableLine currentLine = null;
                for (LineText lineText : pageText.getPageLines()) {
                    for (WordText wordText : lineText.getWords()) {
                        if (wordText.getText().trim().isEmpty()) {
                            continue;
                        }
                        //ELSE:
                        int x = (int) wordText.getBounds().getX();
                        int y = (int) wordText.getBounds().getY();
                        if (y - baselineY >= lineHeightThreshold) {
                            baselineY = y;
                            if (currentLine != null && columnsCount - currentLine.getCells().size() <= maxNullableFieldsCount) {
                                //clean and verify line
                                if (customProcess != null) {
                                    customProcess.clean(currentLine);
                                    try {
                                        currentLine.getCells().forEach(cell -> customProcess.verify(cell));
                                        customProcess.verify(currentLine);
                                        lines.add(currentLine);
                                    } catch (Exception e) {
                                        logger.error("Invalid data: ", e);
                                    }
                                } else {
                                    lines.add(currentLine);
                                }
                                //debug
                                logger.debug("Finish extracting a new line. Cells count: " + currentLine.getCells().size());
                                //debug
                                currentLine.toString();//to sort
                                List<Integer> cellPositions = new ArrayList<>();
                                for (PortableCell cell : currentLine.getCells()) {
                                    cellPositions.add((int) cell.getX());
                                }
                                logger.debug("Cell positions: " + cellPositions);
                            } else {
                                logger.info("Invalid line: " + lineIdx + " in page: " + pageIdx);
                            }
                            currentLine = new PortableLine();
                            currentLine.setIdx(lineIdx);
                            lineIdx++;
                        }
                        PortableCell cell = getOrCreateCellByX(currentLine, x);
                        cell.setContent(cell.getContent() + wordText.getText());
                    }
                }
                //debug
                logger.info("Finish extracting page: " + pageIdx + "; total lines: " + lines.size());
            }
        } catch (PDFException | PDFSecurityException | IOException | InterruptedException ex) {
            logger.error("", ex);
        }
        return this;
    }

    @Override
    public List<? extends Line> getResult() {
        return this.lines;
    }
    //--------------------------------------------------------------------------
    //  Utils

    /**
     * Sampling data
     *
     * @throws InterruptedException
     */
    private void extractCellXs() throws InterruptedException {
        int samplingPagesCount = Integer.parseInt(properties.getProperty("sampling.pagesCount"));
        int samplingWeightThreshold = Integer.parseInt(properties.getProperty("sampling.weightThreshold"));
        int minColumnWidth = 100;
        if (properties.getProperty("minColumnWidth") != null) {
            minColumnWidth = Integer.parseInt(properties.getProperty("minColumnWidth"));
        }
        //begin sampling
        Map<Integer, Integer> cellXNWeightMap = new HashMap<>();
        Map<Integer, Integer> cellXNRightWeightMap = new TreeMap<>();
        int pageIdx = 0;
        while (pageIdx < document.getNumberOfPages() && pageIdx < samplingPagesCount) {
            PageText pageText = document.getPageText(pageIdx);
            logger.info("Begin sampling page: " + pageIdx);
            for (LineText lineText : pageText.getPageLines()) {
                for (WordText wordText : lineText.getWords()) {
                    if (wordText.getText().trim().isEmpty()) {
                        continue;
                    }
                    //ELSE:
                    //Left
                    int x = (int) wordText.getBounds().getX();
                    int weight = 0;
                    if (cellXNWeightMap.containsKey(x)) {
                        weight = cellXNWeightMap.get(x);
                    }
                    weight++;
                    cellXNWeightMap.put(x, weight);
                    //Right
                    int maxX = (int) wordText.getBounds().getMaxX();
                    int rightWeight = 0;
                    if (cellXNRightWeightMap.containsKey(maxX)) {
                        rightWeight = cellXNRightWeightMap.get(maxX);
                    }
                    rightWeight++;
                    cellXNRightWeightMap.put(maxX, rightWeight);
                }
            }
            pageIdx++;
        }
        if (pageIdx < samplingPagesCount) {
            throw new RuntimeException("Number of pages is not enough for sampling. Expected: " + samplingPagesCount + ";actual: " + pageIdx);
        }
        //identify cellXs
        cellXs.clear();
        for (Integer x : cellXNWeightMap.keySet()) {
            int weight = cellXNWeightMap.get(x);
            if (weight >= samplingWeightThreshold) {
                cellXs.add(x);
                //debug
                logger.info("Identified X: " + x);
            }
        }
        //remove too close cells
        int previousCellX = 0;
        Iterator<Integer> iteratorCellX = cellXs.iterator();
        while (iteratorCellX.hasNext()) {
            int cellX = iteratorCellX.next();
            if (cellX - previousCellX < minColumnWidth) {
                iteratorCellX.remove();
            }
            //debug
            logger.debug("Space between cells: " + (cellX - previousCellX));
            previousCellX = cellX;
        }
        //identify cellXs depend on right alignment
        List<Integer> sortedRightWeights = new ArrayList<>(cellXNRightWeightMap.values());
        sortedRightWeights.sort((Integer o1, Integer o2) -> {
            int retVal = 0;
            if (o1 > o2) {
                retVal = -1;
            } else if (o1 < o2) {
                retVal = 1;
            }
            return retVal;
        });
        logger.debug("Identified candidates: " + sortedRightWeights);
        //top "columnCountCell"
        int rightWeightsCount = 0;
        int stdWeight = sortedRightWeights.get(0);
        for (Integer tempRightWeight : sortedRightWeights) {
            if (stdWeight - tempRightWeight * 110 / 100 > 0) {//20%
                logger.debug("Found invalid column weight: " + tempRightWeight + "; stdWeight: " + stdWeight);
                break;
            }
            rightWeightsCount++;
        }
        List<Integer> topRightWeights = sortedRightWeights.subList(0, rightWeightsCount);
        rightCellXs.clear();
        topRightWeights.forEach(rightWeight -> {
            cellXNRightWeightMap.forEach((cellX, tempRightWeight) -> {
                if (Objects.equals(rightWeight, tempRightWeight)) {
                    rightCellXs.add(cellX);
                }
            });
        });
        //debug
        logger.debug("Identified " + rightCellXs.size() + " Cell X depending on right alignment; values: " + rightCellXs);
        //debug
        logger.debug("Identified CellXs: " + cellXs + "; count: " + cellXs.size());
    }

    private PortableCell getOrCreateCellByX(PortableLine line, int x) {
        int stdX = 0;
        boolean foundCellX = false;
        int previousCellX = 0;
        for (Integer cellX : cellXs) {
            if (x == cellX) {
                foundCellX = true;
                stdX = cellX;
                break;
            } else if (cellX > x) {
                break;
            }
            previousCellX = cellX;
        }
        if (!foundCellX) {
            stdX = previousCellX;
        }
        int cellIdx = -1;
        for (Integer cellX : cellXs) {
            cellIdx++;
            if (cellX == stdX) {
                break;
            }
        }
        PortableCell retVal = line.getCellByX(stdX);
        if (retVal == null) {
            retVal = new PortableCell();
            retVal.setX(stdX);
            retVal.setIdx(cellIdx);
            retVal.setLineIdx(line.getIdx());
            line.addCell(retVal);
        }
        //ELSE:
        return retVal;
    }
    //--------------------------------------------------------------------------
    //  Inner class

}
