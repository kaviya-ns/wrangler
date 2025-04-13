/*
 *  Copyright © 2017-2019 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

package io.cdap.directives.aggregates;

import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A directive for aggregating byte size and time duration statistics from multiple rows.
 * 
 * <p>This directive calculates totals for byte sizes and averages for time durations,
 * then formats the results according to specified units.</p>
 * 
 * <p>Example usage: aggregate-stats :size :time :total_size :avg_time mb ms</p>
 */
public class AggregateStatsDirective implements Directive {
    private static final String TOTAL_BYTES = "total_bytes";
    private static final String TOTAL_NANOS = "total_nanos";
    private static final String ROW_COUNT = "row_count";

    private String sizeCol;
    private String timeCol;
    private String outputSizeCol;
    private String outputTimeCol;
    private String outputSizeUnit; // 'b' (bytes), 'kb', 'mb', etc.
    private String outputTimeUnit; // 'ns' (nanoseconds), 'ms', 's', etc.

    @Override
    public UsageDefinition define() {
        UsageDefinition.Builder builder = UsageDefinition.builder("aggregate-stats");
        builder.define("size_col", TokenType.COLUMN_NAME);
        builder.define("time_col", TokenType.COLUMN_NAME);
        builder.define("output_size_col", TokenType.COLUMN_NAME);
        builder.define("output_time_col", TokenType.COLUMN_NAME);
        builder.define("size_unit", TokenType.TEXT, true); // optional
        builder.define("time_unit", TokenType.TEXT, true); // optional
        return builder.build();
    }

    @Override
    public void initialize(Arguments args) throws DirectiveParseException {
        this.sizeCol = ((ColumnName) args.value("size_col")).value();
        this.timeCol = ((ColumnName) args.value("time_col")).value();
        this.outputSizeCol = ((ColumnName) args.value("output_size_col")).value();
        this.outputTimeCol = ((ColumnName) args.value("output_time_col")).value();
        
        // Default to canonical units if not specified
        this.outputSizeUnit = args.contains("size_unit") 
            ? ((Text) args.value("size_unit")).value().toLowerCase() 
            : "b"; // bytes
        this.outputTimeUnit = args.contains("time_unit") 
            ? ((Text) args.value("time_unit")).value().toLowerCase() 
            : "ns"; // nanoseconds
    }

    @Override
    public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
        // Using a Map as a simple store implementation
        Map<String, Long> store = new HashMap<>();
        store.put(TOTAL_BYTES, store.getOrDefault(TOTAL_BYTES, 0L));
        store.put(TOTAL_NANOS, store.getOrDefault(TOTAL_NANOS, 0L));
        store.put(ROW_COUNT, store.getOrDefault(ROW_COUNT, 0L));

        // Process each row
        for (Row row : rows) {
            Object sizeVal = row.getValue(sizeCol);
            Object timeVal = row.getValue(timeCol);

            if (sizeVal instanceof ByteSize) {
                store.put(TOTAL_BYTES, store.get(TOTAL_BYTES) + ((ByteSize) sizeVal).getBytes());
            }
            if (timeVal instanceof TimeDuration) {
                store.put(TOTAL_NANOS, store.get(TOTAL_NANOS) + ((TimeDuration) timeVal).getNanoSeconds());
            }
            store.put(ROW_COUNT, store.get(ROW_COUNT) + 1);
        }

        // Final output (simplified - always return results)
        Row result = new Row();
        
        // Convert and add size
        long totalBytes = store.get(TOTAL_BYTES);
        result.add(outputSizeCol, formatBytes(totalBytes, outputSizeUnit));
        
        // Convert and add time (average)
        long totalNanos = store.get(TOTAL_NANOS);
        long count = store.get(ROW_COUNT);
        long avgNanos = count > 0 ? totalNanos / count : 0;
        result.add(outputTimeCol, formatNanos(avgNanos, outputTimeUnit));
        
        return List.of(result);
    }

    private String formatBytes(long bytes, String unit) {
        switch (unit.toLowerCase()) {
            case "kb": return (bytes / 1024.0) + "kb";
            case "mb": return (bytes / (1024.0 * 1024)) + "mb";
            case "gb": return (bytes / (1024.0 * 1024 * 1024)) + "gb";
            case "tb": return (bytes / (1024.0 * 1024 * 1024 * 1024)) + "tb";
            case "pb": return (bytes / (1024.0 * 1024 * 1024 * 1024 * 1024)) + "pb";
            default: return bytes + "b"; // default to bytes
        }
    }

    private String formatNanos(long nanos, String unit) {
        switch (unit.toLowerCase()) {
            case "us": return (nanos / 1000.0) + "us";
            case "ms": return (nanos / (1000.0 * 1000)) + "ms";
            case "s": return (nanos / (1000.0 * 1000 * 1000)) + "s";
            case "hr": return (nanos / (1000.0 * 1000 * 1000 * 3600)) + "hr";
            default: return nanos + "ns"; // default to nanoseconds
        }
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
}
