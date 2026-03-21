package com.auction.backend.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@JsonComponent
public class StringTrimModule extends StdScalarDeserializer<String> {

    public StringTrimModule() {
        super(String.class);
    }

    @Override
    public String deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        // Bước 1: jp (JsonParser) sẽ bóc tách JSON và lấy ra giá trị gốc (bao gồm cả khoảng trắng thừa)
        String value = jp.getValueAsString();

        // Bước 2: Toán tử ba ngôi (Ternary operator) kiểm tra an toàn
        return (value != null) ? value.trim() : null;
    }
}