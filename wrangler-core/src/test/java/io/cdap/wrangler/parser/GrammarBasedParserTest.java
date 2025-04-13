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
package io.cdap.wrangler.parser;

import io.cdap.cdap.api.artifact.ArtifactSummary;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.registry.DirectiveInfo;
import io.cdap.wrangler.registry.DirectiveRegistry;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class GrammarBasedParserTest {
    private static final String DEFAULT_NAMESPACE = "default";
    private static final DirectiveRegistry EMPTY_REGISTRY = new DirectiveRegistry() {
        @Override
        public DirectiveInfo get(String namespace, String name) {
            return null;
        }

        @Override
        public List<DirectiveInfo> list(String namespace) {
            return Collections.emptyList();
        }

        @Override
        public ArtifactSummary getLatestWranglerArtifact() {
            return new ArtifactSummary("wrangler", "1.0.0");
        }

        @Override
        public void reload(String namespace) {
            // No-op implementation
        }

        @Override
        public void close() throws IOException {
            // No-op implementation
        }
    };

    @Test
    public void testValidDirectiveParsing() throws Exception {
        String recipe = "aggregate-stats :size :time :out_size :out_time 'mb' 'ms'";
        GrammarBasedParser parser = new GrammarBasedParser(recipe, DEFAULT_NAMESPACE, EMPTY_REGISTRY);
        List<Directive> directives = parser.parse();
        assertTrue(directives.size() > 0);
    }

    @Test(expected = DirectiveParseException.class)
    public void testInvalidDirectiveRejection() throws Exception {
        String invalidRecipe = "aggregate-stats :size";
        GrammarBasedParser parser = new GrammarBasedParser(invalidRecipe, DEFAULT_NAMESPACE, EMPTY_REGISTRY);
        parser.parse();
    }
}
