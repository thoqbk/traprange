/**
 * Copyright (C) 2014, GIAYBAC.COM
 *
 * Released under the MIT license
 */
package com.giaybac.traprange.test;

import com.giaybac.traprange.service.ExtractService;
import com.giaybac.traprange.service.ExtractServiceFactory;
import com.giaybac.traprange.entity.Line;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author THOQ LUONG Jul 19, 2014 10:58:10 PM
 */
public class TrapWindowsExtractServiceTest {

    //--------------------------------------------------------------------------
    //  Members
    private final Logger logger = LoggerFactory.getLogger(TrapWindowsExtractServiceTest.class);
    //--------------------------------------------------------------------------
    //  Initialization

    @BeforeClass
    public static void setup() {
        PropertyConfigurator.configure(TrapWindowsExtractServiceTest.class.getResource("/com/seta/portable/extractor/log4j.properties"));
    }

    //--------------------------------------------------------------------------
    //  Getter N Setter
    //--------------------------------------------------------------------------
    //  Method binding
    /**
     * Must check ids: 25714, 25712
     *
     * @throws IOException
     */
    @Test
    public void testTrapWindowsExtractServiceImpl() throws IOException {
        ExtractService extractService = ExtractServiceFactory.getExtractService("pdf2");
        //extractService.setPath("E:\\Dropbox\\Portable Extractor\\_Docs\\TK0976-AB5-0-2014042211.pdf");
        ExtractService.ExtractorBuilder builder = extractService.newBuilder().setPath("D:\\Dropbox\\Portable Extractor\\_Docs\\TK0976-AB5-0-2014042211.pdf")
        //ExtractService.ExtractorBuilder builder = extractService.newBuilder().setPath("C:\\Users\\ThoLuong\\Desktop\\TEAM JP - BCC T7.pdf")
                //extractService.setCustomProcess(new TrapRangesCustomProcess());
                .setCustomProcess(null)
                .extract();

        String outputFilePath = "C:\\Users\\ThoLuong\\Desktop\\trap-result-2.txt";
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(outputFilePath), "UTF-8")) {
            List<? extends Line> lines = builder.getResult();
            int idx = 0;
            for (Line line : lines) {
                writer.append(line.toString())
                        .append("\n");
                logger.info((idx++) + ". " + line.toString());
            }
        }
    }
    //--------------------------------------------------------------------------
    //  Implement N Override
    //--------------------------------------------------------------------------
    //  Utils
    //--------------------------------------------------------------------------
    //  Inner class
}
