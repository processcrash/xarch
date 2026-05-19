package com.xarch.starter.db.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.mybatis.flex.spring.boot.annotation.EnableFlexConfiguration;
import com.mybatis.flex.spring.boot.mapper.MapperScanRegistry;

/**
 * MybatisFlex and PageHelper auto-configuration
 */
@AutoConfiguration
@EnableFlexConfiguration
@Configuration
public class XarchDbAutoConfiguration {

    /**
     * Enable mapper scanning for mybatis-flex
     */
    @Bean
    public MapperScanRegistry mapperScanRegistry() {
        return new MapperScanRegistry();
    }
}