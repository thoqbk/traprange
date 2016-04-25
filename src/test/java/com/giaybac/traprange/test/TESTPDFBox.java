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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.junit.Test;

/**
 *
 * @author THOQ LUONG Mar 21, 2015 11:23:40 PM
 */
public class TESTPDFBox extends PDFTextStripper {

    //--------------------------------------------------------------------------
    //  Members
    private final List<Range<Integer>> ranges = new ArrayList<>();

    private TrapRangeBuilder trapRangeBuilder = new TrapRangeBuilder();

    //--------------------------------------------------------------------------
    //  Initialization and releasation
    public TESTPDFBox() throws IOException {
        super.setSortByPosition(true);
    }

    //--------------------------------------------------------------------------
    //  Getter N Setter
    //--------------------------------------------------------------------------
    //  Method binding
    @Test
    public void test() throws IOException {
        String filePath = "D:\\traprange\\_Docs\\TK0976-AB5-0-2014042211.pdf";
        //String filePath = "C:\\Users\\ThoLuong\\Downloads\\Download\\1986 NL Batting - Sheet1.pdf";
        File pdfFile = new File(filePath);
        PDDocument pdDocument = PDDocument.load(pdfFile);
        //PrintTextLocations printer = new PrinTextLocations();
        PDPage page = pdDocument.getPage(0);

        this.processPage(page);
        //Print out all text
        ranges.sort(new Comparator<Range>() {
            @Override
            public int compare(Range o1, Range o2) {
                return o1.lowerEndpoint().compareTo(o2.lowerEndpoint());
            }
        });
        for (Range range : ranges) {
            System.out.println("> " + range);
        }
        //Print out all ranges
        List<Range<Integer>> trapRanges = trapRangeBuilder.build();
        for (Range trapRange : trapRanges) {
            System.out.println("TrapRange: " + trapRange);
        }
    }

    //--------------------------------------------------------------------------
    //  Implement N Override
    @Override
    protected void processTextPosition(TextPosition text) {
        Range range = Range.closed((int) text.getY(), (int) (text.getY() + text.getHeight()));
        System.out.println("Text: " + text.getUnicode());
        trapRangeBuilder.addRange(range);
    }
    //--------------------------------------------------------------------------
    //  Utils
    //--------------------------------------------------------------------------
    //  Inner class
}
