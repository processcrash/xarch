package com.xarch.db.autoconfigure;

import com.xarch.db.datasource.XarchDataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * DB starter auto-configuration
 */
@AutoConfiguration(
    before = {DataSourceAutoConfiguration.class}
)
public class XarchDbAutoConfiguration {

    public XarchDbAutoConfiguration() {
    }
}