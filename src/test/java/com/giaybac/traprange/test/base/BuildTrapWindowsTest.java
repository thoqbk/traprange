/**
 * Copyright (C) 2014, GIAYBAC.COM
 *
 * Released under the MIT license
 */
package com.giaybac.traprange.test.base;

import com.google.common.collect.Range;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import com.giaybac.traprange.service.impl.entity.PortableCell;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.log4j.PropertyConfigurator;
import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.graphics.text.LineText;
import org.icepdf.core.pobjects.graphics.text.PageText;
import org.icepdf.core.pobjects.graphics.text.WordText;
import org.junit.Test;
import org.slf4j.LoggerFactory;

/**
 *
 * @author THO Q LUONG
 *
 * Jul 18, 2014 10:08:57 AM
 */
public class BuildTrapWindowsTest {

    //--------------------------------------------------------------------------
    //  Members
    private static final int MIN_LINE_HEIGHT = 20;

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(BuildTrapWindowsTest.class);

    //--------------------------------------------------------------------------
    //  Initialization
    public BuildTrapWindowsTest() {
        PropertyConfigurator.configure(IcePDFTest.class.getResource("/com/seta/portable/extractor/log4j.properties"));        
    }

    //--------------------------------------------------------------------------
    //  Getter N Setter
    //--------------------------------------------------------------------------
    //  Method binding
    //@Test
    public void testExtractWindows() throws InterruptedException, PDFException, PDFSecurityException, IOException {
        String filePath = "E:\\Dropbox\\Portable Extractor\\_Docs\\TK0976-AB5-0-2014042211.pdf";
        //String filePath = "C:\\Users\\tholq6505\\Desktop\\sample-02.pdf";
        Document document = new Document();
        logger.info("Begin loading file");
        document.setFile(filePath);
        logger.info("Loaded file successfully. File path: " + filePath);
        List<Range<Integer>> ranges = new ArrayList<>();
        //Test reflect cell to Ox and unions their shadow
        int linesCount = 0;
        for (int pageIdx = 0; pageIdx < document.getNumberOfPages(); pageIdx++) {
            PageText page = document.getPageText(pageIdx);
            SortedSetMultimap<Integer, PortableCell> lineYNCellsMap = extractLines(page);
            logger.info("Begin reading page: " + pageIdx);
            for (Integer lineY : lineYNCellsMap.keySet()) {
                SortedSet<PortableCell> cells = lineYNCellsMap.get(lineY);
                List<Range<Integer>> newRanges = ranges;
                for (PortableCell cell : cells) {
                    Range<Integer> range = Range.closed(cell.getX(), cell.getMaxX());
                    newRanges = union(newRanges, range);
                }
                boolean b = isValidUnion(ranges.size(), newRanges.size());
                if (b) {
                    ranges = newRanges;
                } else if (linesCount < 10) {
                    ranges.clear();
                }
                linesCount++;
                //debug
                logger.debug("Processed lines: " + linesCount);
            }
        }
        //Result
        int rangeIdx = 0;
        for (Range<Integer> range : ranges) {
            logger.debug("Range." + rangeIdx + ": " + range.toString());
            rangeIdx++;
        }
        //print all lines:        
    }

    @Test
    public void testExtractMinLineHeight() throws PDFException, PDFSecurityException, IOException, InterruptedException {
        String filePath = "E:\\Dropbox\\Portable Extractor\\_Docs\\TK0976-AB5-0-2014042211.pdf";
        //String filePath = "C:\\Users\\tholq6505\\Desktop\\sample-02.pdf";
        Document document = new Document();
        logger.info("Begin loading file");
        document.setFile(filePath);
        logger.info("Loaded file successfully. File path: " + filePath);
        //Test reflect cell to Ox and unions their shadow
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
        double minLineHeight = Double.MAX_VALUE;
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
            if (minLineY - previousMaxLineY < minLineHeight) {
                minLineHeight = minLineY - previousMaxLineY;
            }
            previousMaxLineY = maxLineY;
            clusterIdx++;
        }
        logger.debug("Min lineHeight: " + minLineHeight);
    }

    //--------------------------------------------------------------------------
    //  Implement N Override
    //--------------------------------------------------------------------------
    //  Utils
    private int getOrCreateLineY(SortedSetMultimap<Integer, PortableCell> lineYNCellsMap, int y) {
        int retVal = y;
        for (Integer lineY : lineYNCellsMap.keySet()) {
            if (Math.abs(lineY - y) <= MIN_LINE_HEIGHT) {
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

    private List<Range<Integer>> union(List<Range<Integer>> ranges1, Range<Integer> range) {
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

    private boolean isValidUnion(int size1, int size2) {
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

    private SortedSetMultimap<Integer, PortableCell> extractLines(PageText page) {
        SortedSetMultimap<Integer, PortableCell> retVal = TreeMultimap.<Integer, PortableCell>create((Integer o1, Integer o2) -> {
            int retVal1 = 0;
            if (o1 > o2) {
                retVal1 = 1;
            } else if (o1 < o2) {
                retVal1 = -1;
            }
            return retVal1;
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
                logger.debug("Processing word: " + word.getText());
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
        return retVal;
    }

    private void printLines(SortedSetMultimap<Integer, PortableCell> lineYNCellsMap) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        String outputFilePath = "C:\\Users\\ThoLuong\\Desktop\\lines.txt";
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(outputFilePath, false), "UTF-8")) {
            int lineIdx = 0;
            for (Integer lineY : lineYNCellsMap.keySet()) {
                SortedSet<PortableCell> cells = lineYNCellsMap.get(lineY);
                StringBuilder lineContentBuilder = new StringBuilder().append("L")
                        .append(lineIdx)
                        .append(":");
                int wordIdx = 0;
                for (PortableCell cell : cells) {
                    lineContentBuilder.append("\n\t")
                            .append(wordIdx)
                            .append(".");
                    lineContentBuilder.append(String.format(" %-50s x,maxX:%8s,%8s", cell.getContent(), cell.getX(),
                            cell.getMaxX()));
                    wordIdx++;
                }
                writer.append(lineContentBuilder)
                        .append("\n");
                lineIdx++;
            }
        }
    }

    //--------------------------------------------------------------------------
    //  Inner class
    public class LineY implements Clusterable {

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
