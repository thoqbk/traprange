/**
 * Copyright (C) 2014, GIAYBAC.COM
 *
 * Released under the MIT license
 */
package com.giaybac.traprange.test.base;

import com.giaybac.traprange.service.impl.AdvancePortableExtractorBuilderImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author THO Q LUONG
 *
 * Jul 17, 2014 10:31:30 AM
 */
public class SortedMapTest {

    //--------------------------------------------------------------------------
    //  Members
    private final Logger logger = LoggerFactory.getLogger(SortedMapTest.class);

    //--------------------------------------------------------------------------
    //  Initialization
    @Before
    public void setUp() {
        PropertyConfigurator.configure(SortedMapTest.class.getResource("/com/seta/portable/extractor/log4j.properties"));
    }

    //--------------------------------------------------------------------------
    //  Getter N Setter
    //--------------------------------------------------------------------------
    //  Method binding

    @Test
    public void testSortedMap() {
        //sort map
        Map<Integer, ?> cellXNObjectMap = new TreeMap<>();
        cellXNObjectMap.put(3, null);
        cellXNObjectMap.put(5, null);
        cellXNObjectMap.put(4, null);
        logger.debug("Expected: 3,4,5. Actual order: " + cellXNObjectMap);

        //sort array
        List<AdvancePortableExtractorBuilderImpl.PortableColumn> sortedColumns = new ArrayList<>();

        AdvancePortableExtractorBuilderImpl.PortableColumn column1 = new AdvancePortableExtractorBuilderImpl.PortableColumn();
        column1.setLeftWeight(6);
        sortedColumns.add(column1);

        AdvancePortableExtractorBuilderImpl.PortableColumn column2 = new AdvancePortableExtractorBuilderImpl.PortableColumn();
        column2.setLeftWeight(8);
        sortedColumns.add(column2);

        AdvancePortableExtractorBuilderImpl.PortableColumn column3 = new AdvancePortableExtractorBuilderImpl.PortableColumn();
        column3.setLeftWeight(7);
        sortedColumns.add(column3);
        
        //sort
        sortedColumns.sort((AdvancePortableExtractorBuilderImpl.PortableColumn o1, AdvancePortableExtractorBuilderImpl.PortableColumn o2) -> {
            int retVal = 0;
            if (o1.getWeight() > o2.getWeight()) {
                retVal = -1;
            } else if (o1.getWeight() < o2.getWeight()) {
                retVal = 1;
            }
            return retVal;
        });
        logger.debug("Column order, expect: 8,7,6");
        sortedColumns.forEach(column -> {
            logger.debug("Column Weight: "+ column.getWeight());
        });
    }
    //--------------------------------------------------------------------------
    //  Implement N Override
    //--------------------------------------------------------------------------
    //  Utils
    //--------------------------------------------------------------------------
    //  Inner class
}
