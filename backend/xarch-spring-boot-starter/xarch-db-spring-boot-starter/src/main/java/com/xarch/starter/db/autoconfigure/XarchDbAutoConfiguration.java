package com.xarch.starter.db.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MybatisFlex and PageHelper auto-configuration
 */
@AutoConfiguration
@Configuration
public class XarchDbAutoConfiguration {

    /**
     * Enable mapper scanning for mybatis-flex
     */
    @Bean
    public com.mybatis.flex.spring.mapper.MapperScanRegistry mapperScanRegistry() {
        return new com.mybatis.flex.spring.mapper.MapperScanRegistry();
    }
}