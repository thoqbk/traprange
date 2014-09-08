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
import com.giaybac.traprange.service.impl.entity.PortableCell;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
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
 * @author THOQ LUONG Jul 30, 2014 8:51:56 AM
 */
public class TrapRangesExtractorBuilderImpl2 extends TrapRangesExtractorBuilderImpl {
    //--------------------------------------------------------------------------
    //  Members

    private final Logger logger = LoggerFactory.getLogger(TrapRangesExtractorBuilderImpl2.class);

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
            Document document = new Document();
            logger.info("Begin loading file");
            document.setFile(filePath);
            logger.info("Loaded file successfully. File path: " + filePath);
            List<Range<Integer>> windows = calculateRanges(document);
            this.lines = extractRows(document, windows);
        } catch (PDFException | PDFSecurityException | IOException | InterruptedException ex) {
            logger.error("Extract file fail", ex);
            throw new RuntimeException("Extract file fail", ex);
        }
        return this;
    }

    @Override
    protected SortedSetMultimap<Integer, PortableCell> extractLines(PageText page) {
        lineIdxNYRangeMap.clear();
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
                int baselineY = getOrCreateLine(retVal, word);
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
    //  Utils
    private final Map<Integer, Range<Integer>> lineIdxNYRangeMap = new HashMap<>();

    private int getOrCreateLine(SortedSetMultimap<Integer, PortableCell> lineIdxNCellsMap, WordText wordText) {
        int retVal = -1;
        Range<Integer> wordTextYRange = Range.closed((int) wordText.getBounds().getY(), (int) wordText.getBounds().getMaxY());
        for (Integer lineIdx : lineIdxNCellsMap.keySet()) {
            Range<Integer> yRange = lineIdxNYRangeMap.get(lineIdx);
            if (yRange.isConnected(wordTextYRange)) {
                retVal = lineIdx;
                Range<Integer> newYRange = Range.closed(Math.min(yRange.lowerEndpoint(), wordTextYRange.lowerEndpoint()),
                        Math.max(yRange.upperEndpoint(), wordTextYRange.upperEndpoint()));
                lineIdxNYRangeMap.put(lineIdx, newYRange);
                break;
            } else if (yRange.upperEndpoint() < wordTextYRange.lowerEndpoint()) {
                break;
            }
        }
        if (retVal == -1) {
            retVal = lineIdxNCellsMap.size();
            lineIdxNYRangeMap.put(retVal, wordTextYRange);
        }
        //return
        return retVal;
    }

    private PortableCell getCell(SortedSetMultimap<Integer, PortableCell> lineIdxNCellsMap, int lineIdx, WordText word) {
        PortableCell retVal = null;
        SortedSet<PortableCell> cells = lineIdxNCellsMap.get(lineIdx);
        for (PortableCell cell : cells) {
            if (cell.getMaxX() == word.getBounds().getX()) {
                retVal = cell;
                break;
            }
        }
        //return
        return retVal;
    }
    //--------------------------------------------------------------------------
    //  Inner class
}
