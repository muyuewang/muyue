package com.muyue.screen.service;

import com.muyue.common.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据大屏统计服务（SQLite / Oracle 双方言）
 *
 * @author muyue
 */
@Service
@RequiredArgsConstructor
public class ScreenService {

    private final DataSource dataSource;

    private volatile Boolean sqlite;

    /** 大屏全部统计数据 */
    public Map<String, Object> stats() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", summary());
        result.put("trend", monthlyTrend());
        result.put("statusDist", groupCount("SELECT status, COUNT(*) FROM biz_order GROUP BY status"));
        result.put("payDist", groupCount("SELECT pay_type, COUNT(*) FROM biz_order GROUP BY pay_type"));
        result.put("branchUsers", branchUsers());
        result.put("recentOrders", recentOrders());
        return result;
    }

    /** 汇总指标 */
    private Map<String, Object> summary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("userCount", scalar("SELECT COUNT(*) FROM sys_user WHERE del_flag = '0'"));
        summary.put("branchCount", scalar("SELECT COUNT(*) FROM sys_dept WHERE dept_name LIKE '%公司%'"));
        summary.put("orderCount", scalar("SELECT COUNT(*) FROM biz_order"));
        Object amount = scalar("SELECT IFNULL(SUM(total_amount), 0) FROM biz_order");
        if (!isSqlite()) {
            amount = scalar("SELECT NVL(SUM(total_amount), 0) FROM biz_order");
        }
        summary.put("totalAmount", amount);
        return summary;
    }

    /** 近 12 个月订单量与销售额趋势 */
    private List<Map<String, Object>> monthlyTrend() {
        String sql = isSqlite()
                ? "SELECT strftime('%Y-%m', create_time) ym, COUNT(*) cnt, IFNULL(SUM(total_amount), 0) amt "
                  + "FROM biz_order GROUP BY ym ORDER BY ym"
                : "SELECT TO_CHAR(create_time, 'YYYY-MM') ym, COUNT(*) cnt, NVL(SUM(total_amount), 0) amt "
                  + "FROM biz_order GROUP BY TO_CHAR(create_time, 'YYYY-MM') ORDER BY ym";
        return queryList(sql);
    }

    /** 分公司人员分布 */
    private List<Map<String, Object>> branchUsers() {
        String sql = "SELECT d.dept_name AS name, COUNT(u.user_id) AS cnt "
                + "FROM sys_dept d LEFT JOIN sys_user u ON u.dept_id = d.dept_id AND u.del_flag = '0' "
                + "WHERE d.dept_name LIKE '%公司%' "
                + "GROUP BY d.dept_id, d.dept_name ORDER BY cnt DESC";
        return queryList(sql);
    }

    /** 最新订单 */
    private List<Map<String, Object>> recentOrders() {
        String sql = (isSqlite() ? "SELECT order_no, user_name, total_amount, status, create_time FROM biz_order "
                + "ORDER BY create_time DESC LIMIT 10"
                : "SELECT order_no, user_name, total_amount, status, create_time FROM biz_order "
                + "ORDER BY create_time DESC FETCH FIRST 10 ROWS ONLY");
        return queryList(sql);
    }

    /** 状态/支付方式分布 */
    private List<Map<String, Object>> groupCount(String sql) {
        return queryList(sql);
    }

    private Object scalar(String sql) {
        try (Connection connection = dataSource.getConnection();
             var statement = connection.createStatement();
             var rs = statement.executeQuery(sql)) {
            return rs.next() ? rs.getObject(1) : null;
        } catch (Exception e) {
            throw new ServiceException("大屏统计查询失败：" + e.getMessage());
        }
    }

    private List<Map<String, Object>> queryList(String sql) {
        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             var statement = connection.createStatement();
             var rs = statement.executeQuery(sql)) {
            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(rs.getMetaData().getColumnLabel(i).toLowerCase(), rs.getObject(i));
                }
                result.add(row);
            }
        } catch (Exception e) {
            throw new ServiceException("大屏统计查询失败：" + e.getMessage());
        }
        return result;
    }

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
}
