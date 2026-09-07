package com.muyue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muyue.common.config.JacksonLongConfig;
import com.muyue.common.config.JacksonTimeConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 序列化契约测试：前后端联调依赖这两个全局规则，回归破坏会导致雪花 ID 精度丢失 / 时间展示错乱
 */
class JacksonSerializationTest {

    private ObjectMapper buildMapper(Jackson2ObjectMapperBuilderCustomizer... customizers) {
        Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json();
        for (Jackson2ObjectMapperBuilderCustomizer c : customizers) {
            c.customize(builder);
        }
        return builder.build();
    }

    @Test
    void longBeyondJsSafeRangeIsSerializedAsString() throws Exception {
        ObjectMapper mapper = buildMapper(new JacksonLongConfig().longToStringCustomizer());
        // 雪花 ID 超出 2^53，必须输出字符串
        assertEquals("\"2096828404530825473\"", mapper.writeValueAsString(2096828404530825473L));
        // 小数值仍输出数字（total 等字段不能变成字符串）
        assertEquals("351", mapper.writeValueAsString(351L));
    }

    @Test
    void localDateTimeUsesCompactPattern() throws Exception {
        ObjectMapper mapper = buildMapper(new JacksonTimeConfig().localDateTimeCustomizer());
        String json = mapper.writeValueAsString(LocalDateTime.of(2026, 9, 7, 13, 5, 9));
        assertEquals("\"2026-09-07 13:05:09\"", json);
    }

    @Test
    void localDateTimeRoundTrip() throws Exception {
        ObjectMapper mapper = buildMapper(new JacksonTimeConfig().localDateTimeCustomizer());
        LocalDateTime parsed = mapper.readValue("\"2026-09-07 13:05:09\"", LocalDateTime.class);
        assertEquals(LocalDateTime.of(2026, 9, 7, 13, 5, 9), parsed);
    }

    @Test
    void localDateUsesDateOnlyPattern() throws Exception {
        ObjectMapper mapper = buildMapper(new JacksonTimeConfig().localDateTimeCustomizer());
        assertEquals("\"2026-09-07\"", mapper.writeValueAsString(LocalDate.of(2026, 9, 7)));
    }
}
