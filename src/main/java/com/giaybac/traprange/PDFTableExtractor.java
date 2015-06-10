/**
* Copyright (C) 2015, GIAYBAC
*
* Released under the MIT license
*/
package com.giaybac.traprange;

import com.giaybac.traprange.entity.Table;
import com.giaybac.traprange.entity.TableCell;
import com.giaybac.traprange.entity.TableRow;
import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author THOQ LUONG Mar 22, 2015 3:34:29 PM
 */
public class PDFTableExtractor {

    //--------------------------------------------------------------------------
    //  Members
    private final Logger logger = LoggerFactory.getLogger(PDFTableExtractor.class);
    //contains pages that will be extracted table content. 
    //If this variable doesn't contain any page, all pages will be extracted
    private final List<Integer> extractedPages = new ArrayList<>();
    private final List<Integer> exceptedPages = new ArrayList<>();
    //contains avoided line idx-s for each page, 
    //if this multimap contains only one element and key of this element equals -1 
    //then all lines in extracted pages contains in multi-map value will be avoided
    private final Multimap<Integer, Integer> pageNExceptedLinesMap = HashMultimap.create();

    private InputStream inputStream;
    private PDDocument document;

    //--------------------------------------------------------------------------
    //  Initialization and releasation
    //--------------------------------------------------------------------------
    //  Getter N Setter
    //--------------------------------------------------------------------------
    //  Method binding
    public PDFTableExtractor setSource(InputStream inputStream) {
        this.inputStream = inputStream;
        return this;
    }

    public PDFTableExtractor setSource(File file) {
        try {
            return this.setSource(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            throw new RuntimeException("Invalid pdf file", ex);
        }
    }

    public PDFTableExtractor setSource(String filePath) {
        return this.setSource(new File(filePath));
    }

    /**
     * This page will be analyze and extract its table content
     *
     * @param pageIdx
     * @return
     */
    public PDFTableExtractor addPage(int pageIdx) {
        extractedPages.add(pageIdx);
        return this;
    }

    public PDFTableExtractor exceptPage(int pageIdx) {
        exceptedPages.add(pageIdx);
        return this;
    }

    /**
     * Avoid a specific line in a specific page. LineIdx can be negative number,
     * -1 is the last line
     *
     * @param pageIdx
     * @param lineIdxs
     * @return
     */
    public PDFTableExtractor exceptLine(int pageIdx, int[] lineIdxs) {
        for (int lineIdx : lineIdxs) {
            pageNExceptedLinesMap.put(pageIdx, lineIdx);
        }
        return this;
    }

    /**
     * Avoid this line in all extracted pages. LineIdx can be negative number,
     * -1 is the last line
     *
     * @param lineIdxs
     * @return
     */
    public PDFTableExtractor exceptLine(int[] lineIdxs) {
        this.exceptLine(-1, lineIdxs);
        return this;
    }

    public List<Table> extract() {
        List<Table> retVal = new ArrayList<>();
        Multimap<Integer, Range<Integer>> pageIdNLineRangesMap = LinkedListMultimap.create();
        Multimap<Integer, TextPosition> pageIdNTextsMap = LinkedListMultimap.create();
        try {
            this.document = PDDocument.load(inputStream);
            for (int pageId = 0; pageId < document.getNumberOfPages(); pageId++) {
                boolean b = !exceptedPages.contains(pageId) && (extractedPages.isEmpty() || extractedPages.contains(pageId));
                if (b) {
                    PDPage pdPage = (PDPage) document.getDocumentCatalog().getAllPages().get(pageId);
                    List<TextPosition> texts = extractTextPositions(pdPage);//sorted by .getY() ASC
                    //extract line ranges
                    List<Range<Integer>> lineRanges = getLineRanges(pageId, texts);
                    //extract column ranges
                    List<TextPosition> textsByLineRanges = getTextsByLineRanges(lineRanges, texts);

                    pageIdNLineRangesMap.putAll(pageId, lineRanges);
                    pageIdNTextsMap.putAll(pageId, textsByLineRanges);
                }
            }
            //Calculate columnRanges
            List<Range<Integer>> columnRanges = getColumnRanges(pageIdNTextsMap.values());
            for (int pageId : pageIdNTextsMap.keySet()) {
                Table table = buildTable(pageId, (List) pageIdNTextsMap.get(pageId), (List) pageIdNLineRangesMap.get(pageId), columnRanges);
                retVal.add(table);
                //debug
                logger.debug("Found " + table.getRows().size() + " row(s) and " + columnRanges.size()
                        + " column(s) of a table in page " + pageId);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Parse pdf file fail", ex);
        }
        //return
        return retVal;
    }

    //--------------------------------------------------------------------------
    //  Implement N Override
    //--------------------------------------------------------------------------
    //  Utils
    /**
     * Texts in tableContent have been ordered by .getY() ASC
     *
     * @param pageIdx
     * @param tableContent
     * @param rowTrapRanges
     * @param columnTrapRanges
     * @return
     */
    private Table buildTable(int pageIdx, List<TextPosition> tableContent,
            List<Range<Integer>> rowTrapRanges, List<Range<Integer>> columnTrapRanges) {
        Table retVal = new Table(pageIdx, columnTrapRanges.size());
        int idx = 0;
        int rowIdx = 0;
        List<TextPosition> rowContent = new ArrayList<>();
        while (idx < tableContent.size()) {
            TextPosition textPosition = tableContent.get(idx);
            Range<Integer> rowTrapRange = rowTrapRanges.get(rowIdx);
            Range<Integer> textRange = Range.closed((int) textPosition.getY(),
                    (int) (textPosition.getY() + textPosition.getHeight()));
            if (rowTrapRange.encloses(textRange)) {
                rowContent.add(textPosition);
                idx++;
            } else {
                TableRow row = buildRow(rowIdx, rowContent, columnTrapRanges);
                retVal.getRows().add(row);
                //next row: clear rowContent
                rowContent.clear();
                rowIdx++;
            }
        }
        //last row
        if (!rowContent.isEmpty() && rowIdx < rowTrapRanges.size()) {
            TableRow row = buildRow(rowIdx, rowContent, columnTrapRanges);
            retVal.getRows().add(row);
        }
        //return
        return retVal;
    }

    /**
     *
     * @param rowIdx
     * @param rowContent
     * @param columnTrapRanges
     * @return
     */
    private TableRow buildRow(int rowIdx, List<TextPosition> rowContent, List<Range<Integer>> columnTrapRanges) {
        TableRow retVal = new TableRow(rowIdx);
        //Sort rowContent
        rowContent.sort(new Comparator<TextPosition>() {
            @Override
            public int compare(TextPosition o1, TextPosition o2) {
                int retVal = 0;
                if (o1.getX() < o2.getX()) {
                    retVal = -1;
                } else if (o1.getX() > o2.getX()) {
                    retVal = 1;
                }
                return retVal;
            }
        });
        int idx = 0;
        int columnIdx = 0;
        List<TextPosition> cellContent = new ArrayList<>();
        while (idx < rowContent.size()) {
            TextPosition textPosition = rowContent.get(idx);
            Range<Integer> columnTrapRange = columnTrapRanges.get(columnIdx);
            Range<Integer> textRange = Range.closed((int) textPosition.getX(),
                    (int) (textPosition.getX() + textPosition.getWidth()));
            if (columnTrapRange.encloses(textRange)) {
                cellContent.add(textPosition);
                idx++;
            } else {
                TableCell cell = buildCell(columnIdx, cellContent);
                retVal.getCells().add(cell);
                //next column: clear cell content
                cellContent.clear();
                columnIdx++;
            }
        }
        if (!cellContent.isEmpty() && columnIdx < columnTrapRanges.size()) {
            TableCell cell = buildCell(columnIdx, cellContent);
            retVal.getCells().add(cell);
        }
        //return
        return retVal;
    }

    private TableCell buildCell(int columnIdx, List<TextPosition> cellContent) {
        cellContent.sort(new Comparator<TextPosition>() {
            @Override
            public int compare(TextPosition o1, TextPosition o2) {
                int retVal = 0;
                if (o1.getX() < o2.getX()) {
                    retVal = -1;
                } else if (o1.getX() > o2.getX()) {
                    retVal = 1;
                }
                return retVal;
            }
        });
        //String cellContentString = Joiner.on("").join(cellContent.stream().map(e -> e.getCharacter()).iterator());
        StringBuilder cellContentBuilder = new StringBuilder();
        for(TextPosition textPosition: cellContent){
            cellContentBuilder.append(textPosition.getCharacter());
        }
        String cellContentString = cellContentBuilder.toString();
        return new TableCell(columnIdx, cellContentString);
    }

    private List<TextPosition> extractTextPositions(PDPage pdPage) throws IOException {
        TextPositionExtractor extractor = new TextPositionExtractor(pdPage);
        return extractor.extract();
    }

    private boolean isExceptedLine(int pageIdx, int lineIdx) {
        boolean retVal = this.pageNExceptedLinesMap.containsEntry(pageIdx, lineIdx)
                || this.pageNExceptedLinesMap.containsEntry(-1, lineIdx);
        return retVal;
    }

    /**
     *
     * Remove all texts in excepted lines
     *
     * TexPositions are sorted by .getY() ASC
     *
     * @param lineRanges
     * @param textPositions
     * @return
     */
    private List<TextPosition> getTextsByLineRanges(List<Range<Integer>> lineRanges, List<TextPosition> textPositions) {
        List<TextPosition> retVal = new ArrayList<>();
        int idx = 0;
        int lineIdx = 0;
        while (idx < textPositions.size() && lineIdx < lineRanges.size()) {
            TextPosition textPosition = textPositions.get(idx);
            Range<Integer> textRange = Range.closed((int) textPosition.getY(),
                    (int) (textPosition.getY() + textPosition.getHeight()));
            Range<Integer> lineRange = lineRanges.get(lineIdx);
            if (lineRange.encloses(textRange)) {
                retVal.add(textPosition);
                idx++;
            } else if (lineRange.upperEndpoint() < textRange.lowerEndpoint()) {
                lineIdx++;
            } else {
                idx++;
            }
        }
        //return
        return retVal;
    }

    /**
     * @param texts
     * @return
     */
    private List<Range<Integer>> getColumnRanges(Collection<TextPosition> texts) {
        TrapRangeBuilder rangesBuilder = new TrapRangeBuilder();
        for (TextPosition text : texts) {
            Range<Integer> range = Range.closed((int) text.getX(), (int) (text.getX() + text.getWidth()));
            rangesBuilder.addRange(range);
        }
        return rangesBuilder.build();
    }

    private List<Range<Integer>> getLineRanges(int pageId, List<TextPosition> pageContent) {
        TrapRangeBuilder lineTrapRangeBuilder = new TrapRangeBuilder();
        for (TextPosition textPosition : pageContent) {
            Range<Integer> lineRange = Range.closed((int) textPosition.getY(),
                    (int) (textPosition.getY() + textPosition.getHeight()));
            //add to builder
            lineTrapRangeBuilder.addRange(lineRange);
        }
        List<Range<Integer>> lineTrapRanges = lineTrapRangeBuilder.build();
        List<Range<Integer>> retVal = removeExceptedLines(pageId, lineTrapRanges);
        return retVal;
    }

    private List<Range<Integer>> removeExceptedLines(int pageIdx, List<Range<Integer>> lineTrapRanges) {
        List<Range<Integer>> retVal = new ArrayList<>();
        for (int lineIdx = 0; lineIdx < lineTrapRanges.size(); lineIdx++) {
            boolean isExceptedLine = isExceptedLine(pageIdx, lineIdx)
                    || isExceptedLine(pageIdx, lineIdx - lineTrapRanges.size());
            if (!isExceptedLine) {
                retVal.add(lineTrapRanges.get(lineIdx));
            }
        }
        //return
        return retVal;
    }

    //--------------------------------------------------------------------------
    //  Inner class
    private static class TextPositionExtractor extends PDFTextStripper {

        private final List<TextPosition> textPositions = new ArrayList<>();
        private final PDPage page;

        private TextPositionExtractor(PDPage page) throws IOException {
            super.setSortByPosition(true);
            this.page = page;
        }

        @Override
        protected void processTextPosition(TextPosition textPosition) {
            textPositions.add(textPosition);
        }

        /**
         * and order by textPosition.getY() ASC
         *
         * @return
         * @throws IOException
         */
        private List<TextPosition> extract() throws IOException {
            this.processStream(page, page.findResources(), page.getContents().getStream());
            //sort
            this.textPositions.sort(new Comparator<TextPosition>() {
                @Override
                public int compare(TextPosition o1, TextPosition o2) {
                    int retVal = 0;
                    if (o1.getY() < o2.getY()) {
                        retVal = -1;
                    } else if (o1.getY() > o2.getY()) {
                        retVal = 1;
                    }
                    return retVal;

                }
            });
            return this.textPositions;
        }
    }
}
