/**
 * Copyright (C) 2014, GIAYBAC.COM
 *
 * Released under the MIT license
 */
package com.giaybac.traprange.service;

import com.giaybac.traprange.entity.Line;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author THO Q LUONG Jul 16, 2014 11:21:10 AM
 */
public interface ExtractService {
    //--------------------------------------------------------------------------
    //  Members
    //--------------------------------------------------------------------------
    //  Initialization
    //--------------------------------------------------------------------------
    //  Getter N Setter
    //--------------------------------------------------------------------------
    //  Method binding
    public ExtractorBuilder newBuilder();
    //--------------------------------------------------------------------------
    //  Implement N Override
    //--------------------------------------------------------------------------
    //  Utils
    //--------------------------------------------------------------------------
    //  Inner class
    public interface ExtractorBuilder {

        /**
         * Configure file input
         *
         * @param filePath
         * @return 
         */
        public ExtractorBuilder setPath(String filePath);

        /**
         * Add optional configuration before extracting
         *
         * @param properties
         * @return 
         */
        public ExtractorBuilder setConfiguration(Properties properties);

        /**
         * After entire line was created successfully, CustomProcess.clean(...)
         * will be invoked to make line data more exactly.
         *
         * After each time a cell or line was created, extractService will
         * invoke CustomProcess.verify(...) to validate this cell or line
         *
         * Sequence of process:<br/>
         * 1. clean<br/>
         * 2. verify cells<br/>
         * 3. verify line<br/>
         *
         * @param customProcess
         * @return 
         */
        public ExtractorBuilder setCustomProcess(CustomProcess customProcess);

        public ExtractorBuilder extract();

        /**
         * Get extracting result
         *
         * @return
         */
        public List<? extends Line> getResult();
    }
}
