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
package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.cdap.wrangler.api.annotations.PublicEvolving;

/**
 * The Bool class wraps the primitive type {@code Boolean} in a object.
 * An object of type {@code Bool} contains the value in primitive type
 * as well as the type of the token this class represents.
 */
@PublicEvolving
public class TimeDuration implements Token {
    private final String value;
    private final double number;
    private final String unit;

    public TimeDuration(String value) {
        this.value = value;
        this.number = extractNumber(value);
        this.unit = extractUnit(value);
    }

    private static double extractNumber(String value) {
        return Double.parseDouble(value.replaceAll("[^0-9.]", ""));
    }
    
    private static String extractUnit(String value) {
        return value.replaceAll("[0-9.]", "");
    }

    public long getNanoSeconds() {
        String u = unit.toLowerCase();
        if (u.equals("us")) {
            return (long) (number * 1000);
        }
        if (u.equals("ms")) {
            return (long) (number * 1_000_000);
        }
        if (u.equals("s")) {
            return (long) (number * 1_000_000_000);
        }
        if (u.equals("hr")) {
            return (long) (number * 3_600_000_000_000L);
        }
        throw new IllegalArgumentException("Unknown unit: " + unit);
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public TokenType type() {
        return TokenType.TIME_DURATION;
    }

    @Override
    public JsonElement toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", TokenType.TIME_DURATION.name());
        object.addProperty("value", value);
        return object;
    }
}
