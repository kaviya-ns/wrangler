package io.cdap.directives.aggregates;

/*
 * Copyright © 2017-2019 Cask Data, Inc.
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
import static org.junit.Assert.fail;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.Token;
import io.cdap.wrangler.api.parser.TokenType;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Unit tests for {@link AggregateStatsDirective}.
 */
public class AggregateStatsDirectiveTest {
    private AggregateStatsDirective directive;
    private List<Row> testRows;

    @Before
    public void setUp() throws Exception {
        directive = new AggregateStatsDirective();
        Arguments args = new TestArguments(
            new ColumnName("size"),
            new ColumnName("time"),
            new ColumnName("total_size"),
            new ColumnName("avg_time"),
            new Text("mb"),
            new Text("ms")
        );
        directive.initialize(args);

        testRows = Arrays.asList(
            new Row().add("size", new ByteSize("10kb")).add("time", new TimeDuration("100ms")),
            new Row().add("size", new ByteSize("1.5mb")).add("time", new TimeDuration("250ms"))
        );
    }

    @Test
    public void testAggregationLogic() throws Exception {
        List<Row> results = directive.execute(testRows, null);
        assertEquals(1, results.size());
        Row result = results.get(0);
        assertEquals(1.577, Double.parseDouble(result.getValue("total_size").toString().replace("mb", "")), 0.001);
        assertEquals("175.0ms", result.getValue("avg_time").toString());
    }

    @Test
    public void testEmptyInput() throws Exception {
        List<Row> results = directive.execute(Collections.emptyList(), null);
        assertEquals(1, results.size());
        assertEquals("0.0mb", results.get(0).getValue("total_size").toString());
        assertEquals("0.0ms", results.get(0).getValue("avg_time").toString());
    }

    @Test
    public void testInvalidDataTypes() {
        List<Row> invalidRows = Collections.singletonList(
            new Row().add("size", "not_a_byte_size").add("time", new TimeDuration("100ms"))
        );
        try {
            directive.execute(invalidRows, null);
            fail("Expected DirectiveExecutionException");
        } catch (DirectiveExecutionException e) {
            // Expected exception
        }
    }

    /**
     * Test implementation of Arguments interface.
     */
    private static class TestArguments implements Arguments {
        private final Map<String, Token> tokenMap = new HashMap<>();
        private final ColumnName sizeCol;
        private final ColumnName timeCol;
        private final ColumnName outputSizeCol;
        private final ColumnName outputTimeCol;
        private final Text sizeUnit;
        private final Text timeUnit;

        TestArguments(ColumnName sizeCol, ColumnName timeCol, 
                     ColumnName outputSizeCol, ColumnName outputTimeCol,
                     Text sizeUnit, Text timeUnit) {
            this.sizeCol = sizeCol;
            this.timeCol = timeCol;
            this.outputSizeCol = outputSizeCol;
            this.outputTimeCol = outputTimeCol;
            this.sizeUnit = sizeUnit;
            this.timeUnit = timeUnit;
            
            tokenMap.put("size_col", sizeCol);
            tokenMap.put("time_col", timeCol);
            tokenMap.put("output_size_col", outputSizeCol);
            tokenMap.put("output_time_col", outputTimeCol);
            tokenMap.put("size_unit", sizeUnit);
            tokenMap.put("time_unit", timeUnit);
        }

        @Override
        public boolean contains(String key) {
            return tokenMap.containsKey(key);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends Token> T value(String key) {
            return (T) tokenMap.get(key);
        }

        @Override
        public int size() {
            return tokenMap.size();
        }

        @Override
        public TokenType type(String name) {
            Token token = tokenMap.get(name);
            return token != null ? token.type() : null;
        }

        @Override
        public int line() {
            return 1;
        }

        @Override
        public int column() {
            return 1;
        }

        @Override
        public String source() {
            return "test-source";
        }

        @Override
        public JsonElement toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("size_col", sizeCol.value());
            json.addProperty("time_col", timeCol.value());
            json.addProperty("output_size_col", outputSizeCol.value());
            json.addProperty("output_time_col", outputTimeCol.value());
            json.addProperty("size_unit", sizeUnit.value());
            json.addProperty("time_unit", timeUnit.value());
            return json;
        }
    }
}
