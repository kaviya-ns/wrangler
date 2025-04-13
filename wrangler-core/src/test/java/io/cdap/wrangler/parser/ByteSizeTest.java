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

import io.cdap.wrangler.api.parser.ByteSize;

public class ByteSizeTest {
    
    @Test
    public void testByteSizeParsing() {
        // Test valid byte size conversions
        assertEquals(1024L, new ByteSize("1kb").getBytes());          // 1 KB → 1024 bytes
        assertEquals(1536L, new ByteSize("1.5kb").getBytes());        // 1.5 KB → 1536 bytes
        assertEquals(1048576L, new ByteSize("1mb").getBytes());       // 1 MB → 1024^2 bytes
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidUnit() {
        new ByteSize("10invalid").getBytes();  // Force evaluation
    }


    @Test(expected = NumberFormatException.class)
    public void testInvalidNumberFormat() {
        new ByteSize("abcMB").getBytes();  // Force number parsing
    }

    
}
