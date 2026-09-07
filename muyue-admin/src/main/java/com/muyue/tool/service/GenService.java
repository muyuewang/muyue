package com.muyue.tool.service;

import com.muyue.common.exception.ServiceException;
import com.muyue.tool.domain.GenColumn;
import com.muyue.tool.domain.GenTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 代码生成 - 读取数据库表结构（支持 SQLite / Oracle）
 *
 * @author muyue
 */
@Service
@RequiredArgsConstructor
public class GenService {

    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    private final DataSource dataSource;

    /** 数据源类型缓存（连接池较小，避免重复建连） */
    private volatile Boolean sqlite;

    /** 查询所有业务表（先取表名列表，再逐个取列，避免嵌套占用连接） */
    public List<GenTable> listTables(String tableNameLike) {
        String sql = isSqlite()
                ? "SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%' ORDER BY name"
                : "SELECT table_name FROM user_tables ORDER BY table_name";
        List<String> names = jdbcQuery(sql, rs -> rs.getString(1));
        List<GenTable> tables = new ArrayList<>();
        for (String tableName : names) {
            if (tableNameLike == null || tableNameLike.isBlank()
                    || tableName.toLowerCase(Locale.ROOT).contains(tableNameLike.toLowerCase(Locale.ROOT))) {
                tables.add(getTable(tableName));
            }
        }
        return tables;
    }

    /** 查询单表元数据（含列） */
    public GenTable getTable(String tableName) {
        if (tableName == null || !TABLE_NAME_PATTERN.matcher(tableName).matches()) {
            throw new ServiceException("非法表名：" + tableName);
        }
        GenTable table = new GenTable();
        table.setTableName(tableName);
        String[] parts = tableName.split("_");
        table.setModuleName(parts.length > 1 ? parts[0].toLowerCase(Locale.ROOT) : "system");
        table.setClassName(camelCase(tableName));
        table.setBusinessName(parts.length > 1
                ? camelCase(tableName.substring(tableName.indexOf('_') + 1))
                : tableName.toLowerCase(Locale.ROOT));
        table.setFunctionName(tableName);

        List<Map<String, Object>> rows;
        if (isSqlite()) {
            rows = jdbcQueryMap("PRAGMA table_info('" + tableName + "')");
        } else {
            rows = jdbcQueryMap(
                    "SELECT c.column_name, c.data_type, cc.comments "
                            + "FROM user_tab_columns c "
                            + "LEFT JOIN user_col_comments cc ON cc.table_name = c.table_name AND cc.column_name = c.column_name "
                            + "WHERE c.table_name = '" + tableName.toUpperCase(Locale.ROOT) + "' "
                            + "ORDER BY c.column_id");
        }
        for (Map<String, Object> row : rows) {
            GenColumn column = new GenColumn();
            column.setColumnName(str(row.get("name") != null ? row.get("name") : row.get("column_name")));
            column.setDataType(str(row.get("type") != null ? row.get("type") : row.get("data_type")));
            Object comment = row.get("comment") != null ? row.get("comment") : row.get("comments");
            column.setColumnComment(str(comment));
            Object pk = row.get("pk");
            column.setIsPk(pk != null && "1".equals(str(pk)) ? "1" : "0");
            column.setJavaField(lowerCamel(column.getColumnName()));
            column.setJavaType(javaType(column.getDataType()));
            table.getColumns().add(column);
            if ("1".equals(column.getIsPk())) {
                table.setPkColumn(column);
            }
        }
        // Oracle 主键补充判断
        if (!isSqlite() && table.getPkColumn() == null) {
            List<Map<String, Object>> cons = jdbcQueryMap(
                    "SELECT cols.column_name FROM user_constraints cons "
                            + "JOIN user_cons_columns cols ON cols.constraint_name = cons.constraint_name "
                            + "WHERE cons.constraint_type = 'P' AND cons.table_name = '" + tableName.toUpperCase(Locale.ROOT) + "'");
            if (!cons.isEmpty()) {
                String pkName = str(cons.get(0).get("column_name"));
                for (GenColumn column : table.getColumns()) {
                    if (column.getColumnName().equalsIgnoreCase(pkName)) {
                        column.setIsPk("1");
                        table.setPkColumn(column);
                        break;
                    }
                }
            }
        }
        return table;
    }

    /** 是否 SQLite 数据源（结果缓存） */
    private boolean isSqlite() {
        Boolean cached = sqlite;
        if (cached != null) {
            return cached;
        }
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            boolean result = metaData.getURL() != null && metaData.getURL().startsWith("jdbc:sqlite:");
            sqlite = result;
            return result;
        } catch (Exception e) {
            throw new ServiceException("读取数据源失败：" + e.getMessage());
        }
    }

    /** 通用查询（取第一列并映射） */
    private <T> List<T> jdbcQuery(String sql, RowMapper<T> mapper) {
        List<T> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             var statement = connection.createStatement();
             var rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                T row = mapper.map(rs);
                if (row != null) {
                    result.add(row);
                }
            }
        } catch (Exception e) {
            throw new ServiceException("读取表结构失败：" + e.getMessage());
        }
        return result;
    }

    /** 通用查询（列名统一转小写） */
    private List<Map<String, Object>> jdbcQueryMap(String sql) {
        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             var statement = connection.createStatement();
             var rs = statement.executeQuery(sql)) {
            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(rs.getMetaData().getColumnLabel(i).toLowerCase(Locale.ROOT), rs.getObject(i));
                }
                result.add(row);
            }
        } catch (Exception e) {
            throw new ServiceException("读取列信息失败：" + e.getMessage());
        }
        return result;
    }

    private String str(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    /** biz_order_item -> OrderItem */
    private String camelCase(String tableName) {
        String[] parts = tableName.split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1).toLowerCase(Locale.ROOT));
        }
        return builder.toString();
    }

    /** create_time -> createTime */
    private String lowerCamel(String columnName) {
        String camel = camelCase(columnName);
        return Character.toLowerCase(camel.charAt(0)) + camel.substring(1);
    }

    /** 数据库类型 -> Java 类型 */
    private String javaType(String dbType) {
        String type = dbType == null ? "" : dbType.toUpperCase(Locale.ROOT);
        if (type.contains("DECIMAL") || type.contains("NUMERIC") || type.contains("REAL") || type.contains("FLOAT")) {
            return "BigDecimal";
        }
        if (type.contains("DATETIME") || type.contains("TIMESTAMP") || type.contains("DATE") || type.contains("TIME")) {
            return "LocalDateTime";
        }
        if (type.contains("NUMBER") || type.contains("BIGINT") || type.contains("INT")) {
            return "Long";
        }
        return "String";
    }

    /** 行映射函数式接口 */
    @FunctionalInterface
    public interface RowMapper<T> {
        T map(java.sql.ResultSet rs) throws java.sql.SQLException;
    }
}
