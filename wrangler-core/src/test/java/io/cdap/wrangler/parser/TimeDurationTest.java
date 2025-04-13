/*
 * Copyright © 2021 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import io.cdap.wrangler.api.parser.TimeDuration;

public class TimeDurationTest {
    
    @Test
    public void testTimeDurationParsing() {
        // Test valid time conversions
        assertEquals(1000000L, new TimeDuration("1ms").getNanoSeconds());  // 1 ms → 1,000,000 ns
        assertEquals(1500000L, new TimeDuration("1.5ms").getNanoSeconds()); // 1.5 ms → 1,500,000 ns
        assertEquals(1000000000L, new TimeDuration("1s").getNanoSeconds()); // 1 sec → 1,000,000,000 ns
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTimeUnit() {
        new TimeDuration("10xyz");  // Should throw exception
    }

    @Test(expected = NumberFormatException.class)
    public void testInvalidNumberFormat() {
        new TimeDuration("abcms");  // Should throw exception
    }

    @Test
    public void testBoundaryValues() {
        assertEquals(0L, new TimeDuration("0ms").getNanoSeconds());  
        assertEquals(3600000000000L, new TimeDuration("1hr").getNanoSeconds());  // 1 hour → 3.6e+12 ns
    }
}
