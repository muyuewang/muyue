package com.muyue.common.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Jackson 序列化配置：超出 JS 安全整数范围（2^53）的 Long 输出为字符串，
 * 避免浏览器 JSON.parse 精度丢失导致雪花 ID 查询 404。
 *
 * @author muyue
 */
@Configuration
public class JacksonLongConfig {

    private static final long MAX_SAFE_INTEGER = 9007199254740991L;

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longToStringCustomizer() {
        JsonSerializer<Long> serializer = new JsonSerializer<Long>() {
            @Override
            public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                if (value == null) {
                    gen.writeNull();
                } else if (value > MAX_SAFE_INTEGER || value < -MAX_SAFE_INTEGER) {
                    gen.writeString(String.valueOf(value));
                } else {
                    gen.writeNumber(value);
                }
            }
        };
        return builder -> builder
                .serializerByType(Long.class, serializer)
                .serializerByType(Long.TYPE, serializer);
    }
}
