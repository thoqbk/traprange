/**
 * Copyright (C) 2014, GIAYBAC.COM
 *
 * Released under the MIT license
 */
package com.giaybac.traprange.service.impl;

import com.google.common.collect.Range;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import com.giaybac.traprange.service.ExtractService;
import com.giaybac.traprange.entity.Cell;
import com.giaybac.traprange.entity.Line;
import com.giaybac.traprange.service.impl.entity.PortableCell;
import com.giaybac.traprange.service.impl.entity.PortableLine;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.graphics.text.LineText;
import org.icepdf.core.pobjects.graphics.text.PageText;
import org.icepdf.core.pobjects.graphics.text.WordText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation uses trap windows to separate cells in a row
 *
 * @author THO Q LUONG
 *
 * Jul 18, 2014 3:23:26 PM
 */
public class TrapRangesExtractorBuilderImpl extends AbstractExtractorBuilder {

    //--------------------------------------------------------------------------
    //  Members    
    private final Logger logger = LoggerFactory.getLogger(TrapRangesExtractorBuilderImpl.class);
    protected List<PortableLine> lines = null;
    private int minLineHeight;
    //--------------------------------------------------------------------------
    //  Initialization
    //--------------------------------------------------------------------------
    //  Getter N Setter
    //--------------------------------------------------------------------------
    //  Method binding

    /**
     * Read all rows of table in page
     *
     * @param page
     * @param ranges
     * @return
     */
    protected final List<PortableLine> extractRows(PageText page, List<Range<Integer>> ranges) {
        List<PortableLine> retVal = new ArrayList<>();
        SortedSetMultimap<Integer, PortableCell> lineYNCellsMap = extractLines(page);
        int lineIdx = 0;
        for (Integer lineY : lineYNCellsMap.keySet()) {
            PortableLine line = new PortableLine();
            line.setIdx(lineIdx);
            boolean invalidLine = false;
            SortedSet<PortableCell> cells = lineYNCellsMap.get(lineY);
            for (PortableCell cell : cells) {
                boolean foundCellColumn = false;
                for (int windowIdx = 0; windowIdx < ranges.size(); windowIdx++) {
                    Range<Integer> window = ranges.get(windowIdx);
                    Range<Integer> cellRange = Range.closed(cell.getX(), cell.getMaxX());
                    if (window.encloses(cellRange)) {
                        PortableCell tempCell = line.getCell(windowIdx);
                        if (tempCell != null) {
                            tempCell.setContent(tempCell.getContent() + " " + cell.getContent());
                        } else {
                            cell.setIdx(windowIdx);
                            cell.setLineIdx(lineIdx);
                            line.addCell(cell);
                        }
                        foundCellColumn = true;
                        break;
                    }
                }
                if (!foundCellColumn) {
                    invalidLine = true;
                    break;
                }
            }
            if (!invalidLine) {
                if (customProcess != null) {
                    try {
                        customProcess.clean(line);
                        for (Cell cell : line.getCells()) {
                            customProcess.verify(cell);
                        }
                        customProcess.verify(line);
                    } catch (Exception e) {
                        logger.debug("Verify line fail", e);
                        invalidLine = true;
                    }
                }
            }
            if (invalidLine) {
                logger.debug("Extract line fail. Line content: " + line);
            } else {
                retVal.add(line);
                lineIdx++;
                logger.debug("Extract line " + lineIdx + " successfully");
            }
        }
        //return
        return retVal;
    }
    
    
    protected final List<PortableLine> extractRows(Document document, List<Range<Integer>> ranges) throws InterruptedException {
        List<PortableLine> retVal = new ArrayList<>();
        for (int pageIdx = 0; pageIdx < document.getNumberOfPages(); pageIdx++) {
            PageText page = document.getPageText(pageIdx);
            retVal.addAll(TrapRangesExtractorBuilderImpl.this.extractRows(page, ranges));
        }
        return retVal;
    }
    
    /**
     * Read all lines in page
     *
     * @param page
     * @return Multimap of line Index and cells in line
     */
    protected SortedSetMultimap<Integer, PortableCell> extractLines(PageText page) {
        SortedSetMultimap<Integer, PortableCell> retVal = TreeMultimap.<Integer, PortableCell>create((Integer o1, Integer o2) -> {
            int retVal2 = 0;
            if (o1 > o2) {
                retVal2 = 1;
            } else if (o1 < o2) {
                retVal2 = -1;
            }
            return retVal2;
        }, (PortableCell o1, PortableCell o2) -> {
            int retVal2 = 0;
            if (o1.getX() > o2.getX()) {
                retVal2 = 1;
            } else if (o1.getX() < o2.getX()) {
                retVal2 = -1;
            }
            return retVal2;
        });
        for (LineText lineText : page.getPageLines()) {
            for (WordText word : lineText.getWords()) {
                if (word.getText().trim().isEmpty()) {
                    continue;
                }
                logger.debug("Processing word: " + word.getText() + "; x: " + word.getBounds().getX() + ", maxX: " + word.getBounds().getMaxX());
                //ELSE:
                int y = (int) word.getBounds().getY();
                int baselineY = getOrCreateLineY(retVal, y);
                PortableCell cell = getCell(retVal, baselineY, word);
                if (cell == null) {
                    cell = new PortableCell();
                    cell.setX((int) word.getBounds().getX());
                    cell.setMaxX((int) word.getBounds().getMaxX());
                    cell.setContent(word.getText());
                    retVal.put(baselineY, cell);
                } else {
                    cell.setContent(cell.getContent() + word.getText());
                    cell.setMaxX(Math.max((int) cell.getMaxX(), (int) word.getBounds().getMaxX()));
                }
            }
        }
        //return
        return retVal;
    }   
    //--------------------------------------------------------------------------
    //  Implement N Override
    @Override
    public ExtractService.ExtractorBuilder extract() {
        try {
            Document document = new Document();
            logger.info("Begin loading file");
            document.setFile(filePath);
            logger.info("Loaded file successfully. File path: " + filePath);
            this.minLineHeight = extractMinLineHeight(document);
            List<Range<Integer>> windows = calculateRanges(document);
            this.lines = extractRows(document, windows);
        } catch (PDFException | PDFSecurityException | IOException | InterruptedException ex) {
            logger.error("Extract file fail", ex);
            throw new RuntimeException("Extract file fail", ex);
        }
        return this;
    }

    @Override
    public List<? extends Line> getResult() {
        return lines;
    }

    //--------------------------------------------------------------------------
    //  Utils
    protected List<Range<Integer>> calculateRanges(Document document) throws InterruptedException {
        List<Range<Integer>> retVal = new ArrayList<>();
        int visitedLinesCount = 0;
        for (int pageIdx = 0; pageIdx < document.getNumberOfPages(); pageIdx++) {
            PageText page = document.getPageText(pageIdx);
            SortedSetMultimap<Integer, PortableCell> lineIdxNCellsMap = extractLines(page);
            List<Integer> blackLineYs = new ArrayList<>();
            logger.info("Begin reading page: " + pageIdx);
            for (Integer lineIdx : lineIdxNCellsMap.keySet()) {
                SortedSet<PortableCell> cells = lineIdxNCellsMap.get(lineIdx);
                List<Range<Integer>> newRanges = TrapRangesExtractorBuilderImpl.this.join(retVal, cells);
                if (newRanges != null) {
                    retVal = newRanges;
                    //debug
                    logger.debug("Join line " + visitedLinesCount + " successfully");
                } else if (visitedLinesCount < 10) {
                    //add visited lines in black list
                    blackLineYs.clear();
                    for (Integer blackLineY : lineIdxNCellsMap.keySet()) {
                        if (blackLineYs.size() < visitedLinesCount + 1) {
                            blackLineYs.add(blackLineY);
                        } else {
                            break;
                        }
                    }
                    retVal.clear();
                    //debug
                    logger.debug("Added first " + (visitedLinesCount + 1) + " line(s) into blacklist and clear ranges");
                } else {
                    blackLineYs.add(lineIdx);
                    //debug
                    logger.debug("Added line " + visitedLinesCount + " into blacklist");
                }
                visitedLinesCount++;
                //debug
                logger.debug("Processed lines: " + visitedLinesCount);
            }
            //re-visit blacklist
            for (Integer lineY : blackLineYs) {
                SortedSet<PortableCell> cells = lineIdxNCellsMap.get(lineY);
                List<Range<Integer>> newRanges = TrapRangesExtractorBuilderImpl.this.join(retVal, cells);
                if (newRanges != null) {
                    retVal = newRanges;
                    //debug
                    logger.debug("Join lineY " + lineY + " in blacklist successfully");
                }
            }
        }
        //debug
        int rangeIdx = 0;
        for (Range<Integer> range : retVal) {
            logger.debug("Range " + rangeIdx + ": " + range.toString());
            rangeIdx++;
        }
        //return
        return retVal;
    }

    private int extractMinLineHeight(Document document) throws InterruptedException {
        double retVal = Double.MAX_VALUE;
        List<LineY> lineYs = new ArrayList<>();
        double totalLineHeight = 0;
        for (int pageIdx = 0; pageIdx < document.getNumberOfPages(); pageIdx++) {
            logger.debug("Processing page: " + pageIdx);
            PageText page = document.getPageText(pageIdx);
            for (LineText line : page.getPageLines()) {
                logger.debug("Processing line: " + line + "; y: " + line.getBounds().getY());
                lineYs.add(new LineY(line.getBounds().getY()));
                totalLineHeight += line.getBounds().getHeight();
            }
        }
        double averageWordHeight = totalLineHeight / lineYs.size();
        logger.debug("Average wordHeight: " + averageWordHeight);
        logger.debug("Begin clustering " + lineYs.size() + " points");
        DBSCANClusterer clusterer = new DBSCANClusterer(averageWordHeight / 2, 1);
        List<Cluster<LineY>> clusters = clusterer.cluster(lineYs);
        //Result
        int clusterIdx = 0;
        double previousMaxLineY = 0;
        for (Cluster<LineY> cluster : clusters) {
            logger.debug("Cluster " + clusterIdx);
            List<LineY> points = cluster.getPoints();
            double minLineY = Double.MAX_VALUE;
            double maxLineY = Double.MIN_VALUE;
            for (LineY lineY : points) {
                if (lineY.getPoint()[0] < minLineY) {
                    minLineY = lineY.getPoint()[0];
                }
                if (lineY.getPoint()[0] > maxLineY) {
                    maxLineY = lineY.getPoint()[0];
                }
                logger.debug("\t" + lineY.getPoint()[0]);
            }
            if (minLineY - previousMaxLineY < retVal) {
                retVal = minLineY - previousMaxLineY;
            }
            previousMaxLineY = maxLineY;
            clusterIdx++;
        }
        logger.debug("Min lineHeight: " + retVal);
        //return
        return (int) retVal;
    }

    private int getOrCreateLineY(SortedSetMultimap<Integer, PortableCell> lineYNCellsMap, int y) {
        int retVal = y;
        for (Integer lineY : lineYNCellsMap.keySet()) {
            if (Math.abs(lineY - y) <= minLineHeight) {
                retVal = lineY;
                break;
            }
        }
        return retVal;
    }

    private PortableCell getCell(SortedSetMultimap<Integer, PortableCell> lineYNCellsMap, int y, WordText word) {
        PortableCell retVal = null;
        SortedSet<PortableCell> cells = lineYNCellsMap.get(y);
        for (PortableCell cell : cells) {
            if (cell.getMaxX() == word.getBounds().getX()) {
                retVal = cell;
                break;
            }
        }
        //return
        return retVal;
    }

    /**
     * Join ranges with cells of line. If successful a new ranges will be
     * returned else the result will be NULL
     *
     * @param ranges
     * @param cells
     * @return
     */
    protected List<Range<Integer>> join(List<Range<Integer>> ranges, SortedSet<PortableCell> cells) {
        List<Range<Integer>> newRanges = ranges;
        for (PortableCell cell : cells) {
            Range<Integer> range = Range.closed(cell.getX(), cell.getMaxX());
            newRanges = join(newRanges, range);
        }
        boolean b = isValidJoin(ranges.size(), newRanges.size());
        return b ? newRanges : null;
    }

    protected List<Range<Integer>> join(List<Range<Integer>> ranges1, Range<Integer> range) {
        List<Range<Integer>> retVal = new ArrayList<>();
        List<Range<Integer>> ranges2 = new ArrayList<>();
        ranges2.add(range);
        int idx1 = 0, idx2 = 0;
        while (idx1 < ranges1.size() || idx2 < ranges2.size()) {
            Range<Integer> range1 = indexToRange(ranges1, idx1);
            Range<Integer> range2 = indexToRange(ranges2, idx2);
            Range<Integer> activeRange = indexToRange(retVal, retVal.size() - 1);
            Range<Integer> chosenRange;
            boolean b = range1 != null && (range2 == null || range1.lowerEndpoint() < range2.lowerEndpoint());
            if (b) {
                chosenRange = range1;
                idx1++;
            } else {
                chosenRange = range2;
                idx2++;
            }
            if (chosenRange == null) {
                throw new RuntimeException("Expect this case never occurs");
            }
            //join active range with chosen range
            if (activeRange == null || chosenRange.lowerEndpoint() > activeRange.upperEndpoint()) {
                retVal.add(chosenRange);
            } else {
                retVal.set(retVal.size() - 1, Range.closed(Math.min(activeRange.lowerEndpoint(), chosenRange.lowerEndpoint()), Math.max(activeRange.upperEndpoint(), chosenRange.upperEndpoint())));
            }
        }
        //return
        return retVal;
    }

    private Range<Integer> indexToRange(List<Range<Integer>> ranges, int idx) {
        return ranges.size() > idx && idx >= 0 ? ranges.get(idx) : null;
    }

    private boolean isValidJoin(int size1, int size2) {
        boolean retVal = true;
        int maxDeltaRangesCountPercent = 10;//%
        if (size1 != 0 && size2 != 0) {
            int deltaRangesCount = Math.abs(size1 - size2);
            int deltaRangesCountPercent1 = deltaRangesCount * 100 / size1;
            int deltaRangesCountPercent2 = deltaRangesCount * 100 / size2;
            retVal = deltaRangesCountPercent1 <= maxDeltaRangesCountPercent && deltaRangesCountPercent2 <= maxDeltaRangesCountPercent;
        }
        //return
        return retVal;
    }

    //--------------------------------------------------------------------------
    //  Inner class
    private class LineY implements Clusterable {

        private final double y;

        public LineY(double y) {
            this.y = y;
        }

        @Override
        public double[] getPoint() {
            return new double[]{this.y};
        }

    }
}
