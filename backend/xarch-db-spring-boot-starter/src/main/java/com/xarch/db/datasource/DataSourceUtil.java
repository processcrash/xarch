package com.xarch.db.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

import javax.sql.DataSource;

/**
 * DataSource utilities
 */
public class DataSourceUtil {

    public static Class<? extends DataSource> resolveDataSourceClass(String datasourceClass) {
        if (datasourceClass == null || datasourceClass.isEmpty()) {
            return HikariDataSource.class;
        }

        String className = datasourceClass.toLowerCase().trim();
        if (className.contains("druid")) {
            return DruidDataSource.class;
        }
        return HikariDataSource.class;
    }

    public static DataSourceProperties createDataSourceProperties() {
        return new DataSourceProperties();
    }
}