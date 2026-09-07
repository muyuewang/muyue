package com.muyue.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 当前数据库方言持有者（用于拼接跨库兼容的 SQL 片段）
 * <p>
 * 通过 muyue.db-type 配置切换：oracle / sqlite
 *
 * @author muyue
 */
@Component
public class DbTypeHolder {

    @Getter
    private DbType dbType = DbType.ORACLE;

    @Value("${muyue.db-type:oracle}")
    public void setDbType(String dbType) {
        this.dbType = DbType.getDbType(dbType);
    }

    public boolean isSqlite() {
        return dbType == DbType.SQLITE;
    }

    /**
     * 限制返回一条记录（Oracle 使用 ROWNUM，SQLite 使用 LIMIT）
     */
    public String limitOne() {
        return isSqlite() ? "LIMIT 1" : "AND ROWNUM = 1";
    }
}
