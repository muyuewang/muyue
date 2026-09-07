package com.muyue.common.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson 时间序列化配置：LocalDateTime/LocalDate 统一输出为
 * "yyyy-MM-dd HH:mm:ss" / "yyyy-MM-dd"，避免默认 ISO 格式（含 T 与纳秒）展示混乱。
 *
 * @author muyue
 */
@Configuration
public class JacksonTimeConfig {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer localDateTimeCustomizer() {
        JsonSerializer<LocalDateTime> dateTimeSerializer = new JsonSerializer<LocalDateTime>() {
            @Override
            public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                if (value == null) {
                    gen.writeNull();
                } else {
                    gen.writeString(value.format(DATE_TIME_FORMATTER));
                }
            }
        };
        JsonDeserializer<LocalDateTime> dateTimeDeserializer = new JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String text = p.getValueAsString();
                if (text == null || text.isBlank()) {
                    return null;
                }
                return LocalDateTime.parse(text.trim(), DATE_TIME_FORMATTER);
            }
        };
        JsonSerializer<LocalDate> dateSerializer = new JsonSerializer<LocalDate>() {
            @Override
            public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                if (value == null) {
                    gen.writeNull();
                } else {
                    gen.writeString(value.format(DATE_FORMATTER));
                }
            }
        };
        JsonDeserializer<LocalDate> dateDeserializer = new JsonDeserializer<LocalDate>() {
            @Override
            public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String text = p.getValueAsString();
                if (text == null || text.isBlank()) {
                    return null;
                }
                return LocalDate.parse(text.trim(), DATE_FORMATTER);
            }
        };
        return builder -> builder
                .serializerByType(LocalDateTime.class, dateTimeSerializer)
                .deserializerByType(LocalDateTime.class, dateTimeDeserializer)
                .serializerByType(LocalDate.class, dateSerializer)
                .deserializerByType(LocalDate.class, dateDeserializer);
    }
}
