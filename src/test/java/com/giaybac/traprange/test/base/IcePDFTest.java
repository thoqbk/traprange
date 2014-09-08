/**
 * Copyright (C) 2014, GIAYBAC.COM
 *
 * Released under the MIT license
 */
package com.giaybac.traprange.test.base;

import com.giaybac.traprange.service.ExtractService;
import com.giaybac.traprange.entity.Line;
import com.giaybac.traprange.service.impl.AdvancePortableExtractorBuilderImpl;
import com.giaybac.traprange.service.impl.PortableExtractorBuilderImpl;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.PropertyConfigurator;
import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.graphics.text.LineText;
import org.icepdf.core.pobjects.graphics.text.PageText;
import org.icepdf.core.pobjects.graphics.text.WordText;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tholq6505
 */
public class IcePDFTest {

    private final Logger logger = LoggerFactory.getLogger(IcePDFTest.class);

    public IcePDFTest() {
        PropertyConfigurator.configure(IcePDFTest.class.getResource("/com/seta/portable/extractor/log4j.properties"));
    }

    @Test
    public void testIcePDF() throws PDFException, PDFSecurityException, IOException, InterruptedException {
        String filePath = "E:\\Dropbox\\Portable Extractor\\_Docs\\TK0976-AB5-0-2014042211.pdf";
        String outputFilePath = "C:\\Users\\tholq6505\\Desktop\\parser-result.txt";
        try (Writer resultWriter = new OutputStreamWriter(new FileOutputStream(outputFilePath), "UTF-8")) {
            Document document = new Document();
            logger.info("Loading file: " + filePath);
            document.setFile(filePath);
            logger.info("Finish loading file: " + filePath);
            //Read pages and lines
            for (int pageIdx = 0; pageIdx < document.getNumberOfPages(); pageIdx++) {
                PageText pageText = document.getPageText(pageIdx);
                logger.info("Begin reading page: " + pageIdx);
                if (pageText == null || pageText.getPageLines() == null) {
                    continue;
                }
                //ELSE: read lines:
                int lineIdx = 0;
                for (LineText lineText : pageText.getPageLines()) {
                    StringBuilder lineContent = new StringBuilder();
                    lineContent.append("Line ").append(lineIdx).append(".").append(pageIdx).append(":");

                    for (WordText wordText : lineText.getWords()) {
                        lineContent.append("\n\t")
                                .append(wordText.getText())
                                .append("; x,maxX,y:")
                                .append(wordText.getBounds().getX())
                                .append(",")
                                .append(wordText.getBounds().getMaxX())
                                .append(",")
                                .append(wordText.getBounds().getY());
                    }
                    resultWriter.write(lineContent.toString());

                    resultWriter.write("\n");
                    lineIdx++;
                    //debug
                    logger.info(lineContent.toString());
                }
            }
        }
    }

    //@Test
    public void testPortableExtractService() throws UnsupportedEncodingException, FileNotFoundException, IOException {
        String filePath = "E:\\Dropbox\\Portable Extractor\\_Docs\\TK0976-AB5-0-2014042211.pdf";

        Properties configuration = new Properties();
        configuration.setProperty("sampling.pagesCount", "2");
        configuration.setProperty("sampling.weightThreshold", "80");
        configuration.setProperty("columnsCount", "17");
        configuration.setProperty("maxNullableFieldsCount", "3");
        configuration.setProperty("lineHeightThreshold", "100");

        ExtractService.ExtractorBuilder extractorBuilder = new PortableExtractorBuilderImpl();
        extractorBuilder.setConfiguration(configuration)
                .setPath(filePath);
        //extractService.setCustomProcess(new PortableCustomProcess());
        extractorBuilder.extract();

        //print result
        String outputFilePath = "C:\\Users\\tholq6505\\Desktop\\new-extract-result.txt";
        try (Writer resultWriter = new OutputStreamWriter(new FileOutputStream(outputFilePath), "UTF-8")) {
            List<? extends Line> lines = extractorBuilder.getResult();
            int idx = 0;
            for (Line line : lines) {
                resultWriter.append(line.toString())
                        .append("\n");
                logger.info((idx++) + ". " + line.toString());
            }
        }
    }

    //@Test    
    public void testAdvancePortableExtractService() throws UnsupportedEncodingException, FileNotFoundException, IOException {
        String filePath = "E:\\Portable Extractor\\_Dev\\TK0976-AB5-0-2014042211.pdf";

        Properties configuration = new Properties();
        configuration.setProperty("sampling.pagesCount", "2");
        configuration.setProperty("lineHeightThreshold", "100");
        configuration.setProperty("minValidCellsCount", "14");

        ExtractService.ExtractorBuilder extractorBuilder = new AdvancePortableExtractorBuilderImpl();
        extractorBuilder.setConfiguration(configuration)
                .setPath(filePath)
                .extract();

        //print result
        String outputFilePath = "C:\\Users\\tholq6505\\Desktop\\extract-result.txt";
        try (Writer resultWriter = new OutputStreamWriter(new FileOutputStream(outputFilePath), "UTF-8")) {
            List<? extends Line> lines = extractorBuilder.getResult();
            int idx = 0;
            for (Line line : lines) {
                resultWriter.append(line.toString())
                        .append("\n");
                logger.info((idx++) + ". " + line.toString());
            }
        }
    }
}
