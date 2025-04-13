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
public class ByteSize implements Token {
    private final String value;
    private final double number;
    private final String unit;

    public ByteSize(String value) {
        this.value = value;
        this.number = extractNumber(value);
        this.unit = extractUnit(value);
    }

    public double extractNumber(String value) {
        return Double.parseDouble(value.replaceAll("[^0-9.]", ""));
    }

    public String extractUnit(String value) {
        return value.replaceAll("[0-9.]", "");
    }

    public long getBytes() {
        String u = unit.toLowerCase();
        if (u.equals("b")) {
            return (long) number;
        }
        if (u.equals("kb")) {
            return (long) (number * 1024);
        }
        if (u.equals("mb")) {
            return (long) (number * 1024 * 1024);
        }
        if (u.equals("gb")) {
            return (long) (number * 1024 * 1024 * 1024);
        }
        if (u.equals("tb")) {
            return (long) (number * 1024L * 1024 * 1024 * 1024);
        }
        if (u.equals("pb")) {
            return (long) (number * 1024L * 1024 * 1024 * 1024 * 1024);
        }
        throw new IllegalArgumentException("Unknown unit: " + unit);
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public TokenType type() {
        return TokenType.BYTE_SIZE;
    }

    @Override
    public JsonElement toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", TokenType.BYTE_SIZE.name());
        object.addProperty("value", value);
        return object;
    }
}
