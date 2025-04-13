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
package io.cdap.directives.aggregates;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.cdap.wrangler.TestingRig;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;

public class AggregateStatsDirectiveIntegrationTest {

    private final List<Row> testRows = Arrays.asList(
        new Row()
            .add("data_transfer_size", new ByteSize("10KB"))   // Corrected to uppercase 'KB'
            .add("response_time", new TimeDuration("100ms")),
        new Row()
            .add("data_transfer_size", new ByteSize("1.5MB"))
            .add("response_time", new TimeDuration("250ms"))
    );

    @Test
    public void testBasicAggregationWithUnitConversion() throws Exception {
        String[] recipe = new String[] {
            "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec 'MB' 's'"
        };

        List<Row> results = TestingRig.execute(recipe, testRows);
        Assert.assertEquals(1, results.size());
        Row aggregated = results.get(0);

        double expectedTotalSizeMB = (10 * 1024L + 1.5 * 1024 * 1024) / (1024.0 * 1024);
        Assert.assertEquals(expectedTotalSizeMB,
            Double.parseDouble(aggregated.getValue("total_size_mb").toString().replace("MB", "")),
            0.001);

        double expectedTotalTimeSec = (100 + 250) / 1000.0;
        Assert.assertEquals(expectedTotalTimeSec,
            Double.parseDouble(aggregated.getValue("total_time_sec").toString().replace("s", "")),
            0.001);
    }

    @Test
    public void testAverageTimeCalculation() throws Exception {
        String[] recipe = new String[] {
            "aggregate-stats :data_transfer_size :response_time total_size_mb avg_time_sec 'MB' 's' avg"
        };

        List<Row> results = TestingRig.execute(recipe, testRows);
        Row aggregated = results.get(0);

        double expectedAvgTimeSec = ((100 + 250) / 2.0) / 1000.0;
        Assert.assertEquals(expectedAvgTimeSec,
            Double.parseDouble(aggregated.getValue("avg_time_sec").toString().replace("s", "")),
            0.001);
    }

    @Test
    public void testDifferentOutputUnits() throws Exception {
        String[] recipe = new String[] {
            "aggregate-stats :data_transfer_size :response_time total_size_kb total_time_ms 'KB' 'ms'"
        };

        List<Row> results = TestingRig.execute(recipe, testRows);
        Row aggregated = results.get(0);

        double expectedTotalSizeKB = (10 * 1024L + 1.5 * 1024 * 1024) / 1024.0;
        Assert.assertEquals(expectedTotalSizeKB,
            Double.parseDouble(aggregated.getValue("total_size_kb").toString().replace("KB", "")),
            0.001);

        Assert.assertEquals(350.0,
            Double.parseDouble(aggregated.getValue("total_time_ms").toString().replace("ms", "")),
            0.001);
    }

    @Test(expected = Exception.class)
    public void testInvalidUnitRejection() throws Exception {
        String[] recipe = new String[] {
            "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec 'MB' 'invalid_unit'"
        };
        TestingRig.execute(recipe, testRows);
    }
}
