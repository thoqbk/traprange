/**
 * Copyright (C) 2014, GIAYBAC.COM
 *
 * Released under the MIT license
 */
package com.giaybac.traprange.service;

import com.giaybac.traprange.service.impl.CSVCustomeProcess;
import com.giaybac.traprange.service.impl.CSVExtractorBuilderImpl;
import com.giaybac.traprange.service.impl.TrapRangesCustomProcess;
import com.giaybac.traprange.service.impl.TrapRangesExtractorBuilderImpl;
import com.giaybac.traprange.service.impl.TrapRangesExtractorBuilderImpl2;

/**
 *
 * @author THO Q LUONG
 *
 * Jul 21, 2014 5:14:32 PM
 */
public class ExtractServiceFactory {

    //--------------------------------------------------------------------------
    //  Members
    private static final ExtractService.ExtractorBuilder trapWindowsExtractService = new TrapRangesExtractorBuilderImpl();
    private static final ExtractService.ExtractorBuilder csvExtractService = new CSVExtractorBuilderImpl();

    static {
        //set default custom process
        trapWindowsExtractService.setCustomProcess(new TrapRangesCustomProcess());
        csvExtractService.setCustomProcess(new CSVCustomeProcess());
    }

    //--------------------------------------------------------------------------
    //  Initialization
    private ExtractServiceFactory() {
    }

    //--------------------------------------------------------------------------
    //  Getter N Setter
    //--------------------------------------------------------------------------
    //  Method binding
    /**
     * Create ExtractService by name. Supports: <br/>
     * 1. pdf <br/>
     * 2. pdf2 <br/>
     * 3. csv <br/>
     *
     * @param name
     * @return
     */
    public static ExtractService getExtractService(String name) {
        ExtractService retVal;
        ExtractService.ExtractorBuilder builder = null;
        switch (name.toLowerCase()) {
            case "csv": {
                builder = new CSVExtractorBuilderImpl();
                builder.setCustomProcess(new CSVCustomeProcess());
                break;
            }
            case "pdf": {
                builder = new TrapRangesExtractorBuilderImpl();
                builder.setCustomProcess(new TrapRangesCustomProcess());
                break;
            }
            case "pdf2": {
                builder = new TrapRangesExtractorBuilderImpl2();
                builder.setCustomProcess(new TrapRangesCustomProcess());
                break;
            }
        }
        if (builder == null) {
            throw new RuntimeException("Invalid name, expect \"pdf\" or \"csv\" but found " + name);
        }
        retVal = new ExtractorBuilderServiceImpl(builder);
        return retVal;
    }

    //--------------------------------------------------------------------------
    //  Implement N Override
    //--------------------------------------------------------------------------
    //  Utils
    //--------------------------------------------------------------------------
    //  Inner class
    private static class ExtractorBuilderServiceImpl implements ExtractService {

        private final ExtractorBuilder builder;

        private ExtractorBuilderServiceImpl(ExtractorBuilder builder) {
            this.builder = builder;
        }

        @Override
        public ExtractorBuilder newBuilder() {
            return this.builder;
        }
    }
}
