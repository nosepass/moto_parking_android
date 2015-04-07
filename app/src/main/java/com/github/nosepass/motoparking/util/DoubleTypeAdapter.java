package com.github.nosepass.motoparking.util;

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;


/**
 * Convert double to string in the json so the Rails server does not
 * have rounding floating point errors when parsing the json into BigDecimal.
 */
public class DoubleTypeAdapter extends TypeAdapter<Double> {

    @Override
    public Double read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String json = in.nextString();
        try {
            return Double.parseDouble(json);
        } catch (NumberFormatException e) {
            throw new JsonSyntaxException(json, e);
        }
    }

    @Override
    public void write(JsonWriter out, Double value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(value.toString());
    }
}
