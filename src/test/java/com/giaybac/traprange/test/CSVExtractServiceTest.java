/**
 * Copyright (C) 2014, GIAYBAC.COM
 *
 * Released under the MIT license
 */
package com.giaybac.traprange.test;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.giaybac.traprange.service.ExtractService;
import com.giaybac.traprange.service.ExtractServiceFactory;
import com.giaybac.traprange.entity.Line;
import com.giaybac.traprange.service.impl.CSVExtractorBuilderImpl;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
 * @author THO Q LUONG
 *
 * Jul 21, 2014 3:57:38 PM
 */
public class CSVExtractServiceTest {

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
    @Test
    public void testTrapWindowsExtractServiceImpl() throws IOException {
        ExtractService extractService = ExtractServiceFactory.getExtractService("csv");
        ExtractService.ExtractorBuilder builder = extractService.newBuilder().setPath("E:\\Dropbox\\Portable Extractor\\_Docs\\20140614.csv")
                //extractService.setPath("C:\\Users\\tholq6505\\Desktop\\trap-result.txt");
                //extractService.setCustomProcess(new TrapWindowsCustomProcess());
                .extract();

        String outputFilePath = "C:\\Users\\tholq6505\\Desktop\\csv-result.csv";
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

    //@Test
    public void testEncoding() throws IOException {
        //String inputFilePath = "C:\\Users\\tholq6505\\Desktop\\trap-result - Copy.txt";
        String inputFilePath = "C:\\Users\\tholq6505\\Desktop\\20140614.csv";
        String outputFilePath = "C:\\Users\\tholq6505\\Desktop\\copied-csv.txt";
        try (InputStream inputStream = new FileInputStream(inputFilePath);
                Writer writer = new OutputStreamWriter(new FileOutputStream(outputFilePath), "UTF-8")) {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            CharsetDetector detector = new CharsetDetector();
            detector.setText(bufferedInputStream);

            CharsetMatch match = detector.detect();
            if (match != null) {
                logger.info("Charset: " + match.getName());
            }
            byte[] bytes = new byte[1014000];
            int readBytes = 0;
            int byteInInt;
            while ((byteInInt = inputStream.read()) != -1) {
                bytes[readBytes++] = (byte) byteInInt;
            }
            String content = new String(bytes, 0, readBytes, "Shift_JIS");
            writer.append(content);
        }
    }

    //--------------------------------------------------------------------------
    //  Implement N Override
    //--------------------------------------------------------------------------
    //  Utils
    //--------------------------------------------------------------------------
    //  Inner class
}
