/**
 * Copyright (C) 2015, GIAYBAC
 *
 * Released under the MIT license
 */
package com.giaybac.traprange.test;

import com.giaybac.traprange.TrapRangeBuilder;
import com.google.common.collect.Range;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.junit.Test;

/**
 *
 * @author Tho Mar 21, 2015 11:23:40 PM
 */
public class TESTPDFBox extends PDFTextStripper {

    //--------------------------------------------------------------------------
    //  Members
    private final List<Range<Integer>> ranges = new ArrayList<>();

    private final TrapRangeBuilder trapRangeBuilder = new TrapRangeBuilder();

    public TESTPDFBox() throws IOException {
        super.setSortByPosition(true);
    }

    @Test
    public void test() throws IOException {
        String homeDirectory = System.getProperty("user.dir");
        String filePath = Paths.get(homeDirectory, "_Docs", "sample-1.pdf").toString();
        File pdfFile = new File(filePath);
        PDDocument pdDocument = PDDocument.load(pdfFile);
        PDPage page = pdDocument.getPage(0);

        this.processPage(page);
        //Print out all text    
        Collections.sort(ranges, new Comparator<Range<Integer>>() {
            @Override
            public int compare(Range<Integer> o1, Range<Integer> o2) {
                return o1.lowerEndpoint().compareTo(o2.lowerEndpoint());
            }
        });
        for (Range<Integer> range : ranges) {
            System.out.println("> " + range);
        }
        //Print out all ranges
        List<Range<Integer>> trapRanges = trapRangeBuilder.build();
        for (Range<Integer> trapRange : trapRanges) {
            System.out.println("TrapRange: " + trapRange);
        }
    }

    @Override
    protected void processTextPosition(TextPosition text) {
        Range<Integer> range = Range.closed((int) text.getY(), (int) (text.getY() + text.getHeight()));
        System.out.println("Text: " + text.getUnicode());
        trapRangeBuilder.addRange(range);
    }
}
