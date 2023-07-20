/**
 * Copyright (C) 2015, GIAYBAC
 *
 * Released under the MIT license
 */
package com.giaybac.traprange;

import com.google.common.collect.Range;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Tho Mar 19, 2015 10:43:22 PM
 */
public class TrapRangeBuilder {
    private final Logger logger = LoggerFactory.getLogger(TrapRangeBuilder.class);
    private final List<Range<Integer>> ranges = new ArrayList<>();

    public TrapRangeBuilder addRange(Range<Integer> range) {
        ranges.add(range);
        return this;
    }

    /**
     * The result will be ordered by lowerEndpoint ASC
     *
     * @return
     */
    public List<Range<Integer>> build() {
        List<Range<Integer>> retVal = new ArrayList<>();
        //order range by lower Bound
        Collections.sort(ranges, (Comparator<Range>) (o1, o2) -> o1.lowerEndpoint().compareTo(o2.lowerEndpoint()));

        for (Range<Integer> range : ranges) {
            if (retVal.isEmpty()) {
                retVal.add(range);
            } else {
                Range<Integer> lastRange = retVal.get(retVal.size() - 1);
                if (lastRange.isConnected(range)) {
                    Range newLastRange = lastRange.span(range);
                    retVal.set(retVal.size() - 1, newLastRange);
                } else {
                    retVal.add(range);
                }
            }
        }
        //debug
        logger.debug("Found " + retVal.size() + " trap-range(s)");
        //return
        return retVal;
    }
}
