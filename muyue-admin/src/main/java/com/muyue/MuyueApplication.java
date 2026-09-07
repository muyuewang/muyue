package com.muyue;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 沐月后台管理脚手架 - 启动类
 *
 * @author muyue
 */
@EnableCaching
@MapperScan("com.muyue.**.mapper")
@SpringBootApplication
public class MuyueApplication {

    /** 与 application-sqlite.yml 中的 spring.datasource.url 保持一致 */
    private static final String DEFAULT_SQLITE_URL = "jdbc:sqlite:./data/muyue.db";

    public static void main(String[] args) {
        // 验证码图片生成需要，避免无图形环境报错
        System.setProperty("java.awt.headless", "true");
        // SQLite 不会自动创建目录，启动时保证数据库文件所在目录存在
        createSqliteDirectory(args);
        SpringApplication.run(MuyueApplication.class, args);
    }

    /**
     * 解析数据源地址，若为 SQLite 文件库则预先创建目录
     */
    private static void createSqliteDirectory(String[] args) {
        String url = System.getProperty("spring.datasource.url");
        String envUrl = System.getenv("SPRING_DATASOURCE_URL");
        if (envUrl != null && !envUrl.isBlank()) {
            url = envUrl;
        }
        for (String arg : args) {
            if (arg.startsWith("--spring.datasource.url=")) {
                url = arg.substring("--spring.datasource.url=".length());
            } else if (arg.startsWith("-Dspring.datasource.url=")) {
                url = arg.substring("-Dspring.datasource.url=".length());
            }
        }
        if (url == null || url.isBlank()) {
            url = DEFAULT_SQLITE_URL;
        }
        if (!url.startsWith("jdbc:sqlite:")) {
            return;
        }
        String path = url.substring("jdbc:sqlite:".length());
        // :memory: / :resource: 等内存或资源库不需要目录
        if (path.startsWith(":")) {
            return;
        }
        try {
            Path directory = Paths.get(path).toAbsolutePath().getParent();
            if (directory != null) {
                Files.createDirectories(directory);
            }
        } catch (Exception e) {
            System.err.println("[muyue] 创建 SQLite 数据库目录失败：" + e.getMessage());
        }
    }
}
