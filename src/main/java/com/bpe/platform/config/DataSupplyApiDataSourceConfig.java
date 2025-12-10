package com.bpe.platform.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * 데이터소스 설정
 * - 기본 데이터소스: 기존 DB (spring.datasource.*) - @Primary로 설정하여 JPA, Security 등이 사용
 * - 데이터 공급 API 데이터소스: 별도 DB (app.data-supply-api.datasource.*) - 데이터 공급 API 관리 페이지만 사용
 */
@Configuration
public class DataSupplyApiDataSourceConfig {

    /**
     * 기본 데이터소스 설정 (기존 DB)
     * @Primary로 설정하여 JPA, Security 등 모든 기본 기능이 이 데이터소스를 사용
     */
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource primaryDataSource(@Qualifier("primaryDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    /**
     * 데이터 공급 API 전용 데이터소스 (별도 DB)
     * app.data-supply-api.datasource.* 설정을 사용
     */
    @Value("${app.data-supply-api.datasource.url}")
    private String apiUrl;

    @Value("${app.data-supply-api.datasource.username}")
    private String apiUsername;

    @Value("${app.data-supply-api.datasource.password}")
    private String apiPassword;

    @Value("${app.data-supply-api.datasource.driver-class-name}")
    private String apiDriverClassName;

    @Bean(name = "dataSupplyApiDataSource")
    public DataSource dataSupplyApiDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(apiDriverClassName);
        dataSource.setUrl(apiUrl);
        dataSource.setUsername(apiUsername);
        dataSource.setPassword(apiPassword);
        return dataSource;
    }

    @Bean(name = "dataSupplyApiJdbcTemplate")
    public JdbcTemplate dataSupplyApiJdbcTemplate(@Qualifier("dataSupplyApiDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * BPE_STAGE 데이터베이스용 JdbcTemplate (기본 데이터소스)
     */
    @Bean(name = "primaryJdbcTemplate")
    @Primary
    public JdbcTemplate primaryJdbcTemplate(@Qualifier("primaryDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}

