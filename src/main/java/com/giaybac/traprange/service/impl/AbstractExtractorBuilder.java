/**
 * Copyright (C) 2014, GIAYBAC.COM
 *
 * Released under the MIT license
 */

package com.giaybac.traprange.service.impl;

import com.giaybac.traprange.service.ExtractService;
import com.giaybac.traprange.service.CustomProcess;
import java.util.Properties;

/**
 *
 * @author THO Q LUONG
 * Jul 16, 2014 11:28:01 AM
 */
public abstract class AbstractExtractorBuilder implements ExtractService.ExtractorBuilder{
    //--------------------------------------------------------------------------
    //  Members
    protected String filePath;
    protected Properties properties;
    
    protected CustomProcess customProcess;
    //--------------------------------------------------------------------------
    //  Initialization
    //--------------------------------------------------------------------------
    //  Getter N Setter
    //--------------------------------------------------------------------------
    //  Method binding
    //--------------------------------------------------------------------------
    //  Implement N Override    
    @Override
    public ExtractService.ExtractorBuilder setPath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    @Override
    public ExtractService.ExtractorBuilder setConfiguration(Properties properties) {
        this.properties = properties;
        return this;
    }
    
    
    @Override
    public ExtractService.ExtractorBuilder setCustomProcess(CustomProcess postProcessor) {
        this.customProcess = postProcessor;
        return this;
    }
    //--------------------------------------------------------------------------
    //  Utils
    //--------------------------------------------------------------------------
    //  Inner class
}