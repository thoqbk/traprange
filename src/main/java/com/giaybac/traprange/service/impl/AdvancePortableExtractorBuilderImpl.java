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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.graphics.text.LineText;
import org.icepdf.core.pobjects.graphics.text.PageText;
import org.icepdf.core.pobjects.graphics.text.WordText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author THO Q LUONG Jul 16, 2014 6:12:04 PM
 */
public class AdvancePortableExtractorBuilderImpl extends AbstractExtractorBuilder {

    //--------------------------------------------------------------------------
    //  Members
    private final Logger logger = LoggerFactory.getLogger(AdvancePortableExtractorBuilderImpl.class);

    private final List<PortableLine> lines = new ArrayList<>();

    private Document document;
    private final Map<Integer, PortableColumn> cellXNColumnMap = new TreeMap<>();
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
            extractColumns();
            //parse pages
            extractLines();
        } catch (PDFException | PDFSecurityException | IOException | InterruptedException ex) {
            java.util.logging.Logger.getLogger(PortableExtractorBuilderImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return this;
    }

    @Override
    public List<? extends Line> getResult() {
        return lines;
    }
    //--------------------------------------------------------------------------
    //  Utils
    private void extractLines() throws InterruptedException {
        int lineHeightThreshold = Integer.parseInt(properties.getProperty("lineHeightThreshold"));
        int minValidCellsCount = Integer.parseInt(properties.getProperty("minValidCellsCount"));
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
                    //debug
                    logger.debug("Processing word: " + wordText.getText());
                    //ELSE:
                    int x = (int) wordText.getBounds().getX();
                    int maxX = (int) wordText.getBounds().getMaxX();
                    int y = (int) wordText.getBounds().getY();
                    if (y - baselineY >= lineHeightThreshold) {//new line
                        baselineY = y;
                        if (currentLine != null && currentLine.getCells().size() >= minValidCellsCount) {
                            lines.add(currentLine);
                            //debug
                            logger.debug("Finish extracting a new line. Cells count: " + currentLine.getCells().size());
                            //debug
                            currentLine.toString();//to sort
                            List<Integer> cellPositions = new ArrayList<>();
                            for (PortableCell cell : currentLine.getCells()) {
                                cellPositions.add((int) cell.getX());
                            }
                            //debug
                            logger.debug("Cell positions: " + cellPositions);
                        } else {
                            logger.info("Found an invalid line: " + lineIdx + " in page: " + pageIdx);
                        }
                        currentLine = new PortableLine();
                        lineIdx++;
                    }
                    PortableCell cell = getOrCreateCellByX(currentLine, x, maxX);
                    if (cell != null) {
                        cell.setContent(cell.getContent() + wordText.getText());
                    } else {
                        logger.debug("Cell not found for line: " + lineIdx + "; x: " + x + "; maxX: " + maxX);
                    }
                }
            }
            //debug
            logger.info("Finish extracting page: " + pageIdx + "; total lines: " + lines.size());
        }
    }

    private void extractColumns() throws InterruptedException {
        int samplingPagesCount = Integer.parseInt(properties.getProperty("sampling.pagesCount"));
        //begin sampling
        Map<Integer, PortableColumn> tempCellXNColumnMap = new HashMap<>();
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
                    //debug
                    logger.debug("Processing word: " + wordText.getText());
                    //left alignment
                    int x = (int) wordText.getBounds().getX();
                    PortableColumn leftAlignmentColumn = getOrCreateColumn(tempCellXNColumnMap, x);
                    if (leftAlignmentColumn != null) {
                        leftAlignmentColumn.setLeftWeight(leftAlignmentColumn.getLeftWeight() + 1);
                    }
                    //right alignment
                    int maxX = (int) wordText.getBounds().getMaxX();
                    PortableColumn rightAlignmentColumn = getOrCreateColumn(tempCellXNColumnMap, maxX);
                    if (rightAlignmentColumn != null) {
                        rightAlignmentColumn.setRightWeight(rightAlignmentColumn.getRightWeight() + 1);
                    }
                }
            }
            pageIdx++;
        }
        if (pageIdx < samplingPagesCount) {
            throw new RuntimeException("Number of pages is not enough for sampling. Expected: " + samplingPagesCount + "; actual: " + pageIdx);
        }
        //identify cellXs
        List<PortableColumn> sortedColumns = new ArrayList<>(tempCellXNColumnMap.values());
        sortedColumns.sort((PortableColumn o1, PortableColumn o2) -> {
            int retVal = 0;
            if (o1.getWeight() > o2.getWeight()) {
                retVal = -1;
            } else if (o1.getWeight() < o2.getWeight()) {
                retVal = 1;
            }
            return retVal;
        });
        logger.debug("Identified candidates: " + tempCellXNColumnMap.keySet());
        //top "columnCountCell"
        int columnsCount = 0;
        int stdWeight = sortedColumns.get(0).getWeight();
        for (PortableColumn tempColumn : sortedColumns) {
            if (stdWeight - tempColumn.getWeight() * 150 / 100 > 0) {//20%
                logger.debug("Found invalid column weight: " + tempColumn.getWeight() + "; stdWeight: " + stdWeight);
                break;
            }
            columnsCount++;
        }

        cellXNColumnMap.clear();
        List<PortableColumn> topColumns = sortedColumns.subList(0, columnsCount);
        topColumns.forEach(topColumn -> {
            cellXNColumnMap.put(topColumn.getX(), topColumn);
        });
        //debug
        logger.debug("Identified Cell X: " + cellXNColumnMap.size() + "; values: " + cellXNColumnMap.keySet());
    }

    private PortableColumn getOrCreateColumn(Map<Integer, PortableColumn> tempCellXNColumnMap, int x) {
        PortableColumn retVal = tempCellXNColumnMap.get(x);
        if (retVal == null) {
            retVal = new PortableColumn();
            retVal.setX(x);
            //save
            tempCellXNColumnMap.put(x, retVal);
        }
        //return
        return retVal;
    }

    private PortableCell getOrCreateCellByX(PortableLine line, int x, int maxX) {
        PortableCell retVal = null;
        PortableColumn column = cellXNColumnMap.get(x);
        if (column == null) {
            //get previous and next column to identify column: previous < column < next
            PortableColumn previousColumn = null;
            PortableColumn nextColumn = null;
            for (int cellX : cellXNColumnMap.keySet()) {
                PortableColumn tempColumn = cellXNColumnMap.get(cellX);
                if (cellX < x) {
                    previousColumn = tempColumn;
                } else if (x < cellX) {
                    nextColumn = tempColumn;
                    break;
                } else {
                    throw new RuntimeException("Invalid X. Expect this case never occur");
                }
            }
            //save
            if (previousColumn == null) {
                column = nextColumn;
            } else if (nextColumn == null) {
                column = previousColumn;
            } else {
                if (previousColumn.getTextAlignment() == TextAlignment.LEFT && nextColumn.getTextAlignment() == TextAlignment.LEFT) {//example "? in previous" |previous ?  |next
                    column = previousColumn;
                } else if (previousColumn.getTextAlignment() == TextAlignment.RIGHT && nextColumn.getTextAlignment() == TextAlignment.RIGHT) {//example "? in next" previous| ? next|
                    column = nextColumn;
                } else if (previousColumn.getTextAlignment() == TextAlignment.LEFT && nextColumn.getTextAlignment() == TextAlignment.RIGHT) {
                    column = (x - previousColumn.getX() >= nextColumn.getX() - maxX) ? nextColumn : previousColumn;
                }
            }
        }
        if (column != null) {
            retVal = line.getCellByX(column.getX());
            if (retVal == null) {
                retVal = new PortableCell();
                retVal.setX(x);
                line.addCell(retVal);
            }
        }
        return retVal;
    }

    //--------------------------------------------------------------------------
    //  Inner class
    public static class PortableColumn {

        private int x;
        private int leftWeight = 0;
        private int rightWeight = 0;

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public TextAlignment getTextAlignment() {
            return leftWeight > rightWeight ? TextAlignment.LEFT : TextAlignment.RIGHT;
        }

        public int getLeftWeight() {
            return leftWeight;
        }

        public void setLeftWeight(int leftWeight) {
            this.leftWeight = leftWeight;
        }

        public int getRightWeight() {
            return rightWeight;
        }

        public void setRightWeight(int rightWeight) {
            this.rightWeight = rightWeight;
        }

        public int getWeight() {
            return leftWeight + rightWeight;
        }
    }

    public enum TextAlignment {

        LEFT, RIGHT, NA
    }
}
