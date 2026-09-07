package com.muyue.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.muyue.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 配置（Oracle 方言 + 自动填充）
 *
 * @author muyue
 */
@Configuration
@MapperScan("com.muyue.**.mapper")
@RequiredArgsConstructor
public class MybatisPlusConfig {

    private final DbTypeHolder dbTypeHolder;

    /**
     * 分页插件（按当前数据库方言自动适配）
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(dbTypeHolder.getDbType()));
        return interceptor;
    }

    /**
     * 公共字段自动填充
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
                String username = SecurityUtils.getUsername();
                if (username != null) {
                    this.strictInsertFill(metaObject, "createBy", String.class, username);
                    this.strictInsertFill(metaObject, "updateBy", String.class, username);
                }
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
                String username = SecurityUtils.getUsername();
                if (username != null) {
                    this.strictUpdateFill(metaObject, "updateBy", String.class, username);
                }
            }
        };
    }
}
