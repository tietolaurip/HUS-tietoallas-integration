package fi.tietoallas.integration.common.configuration;

/*-
 * #%L
 * common-java
 * %%
 * Copyright (C) 2017 - 2018 Helsingin ja Uudenmaan sairaanhoitopiiri, Helsinki, Finland
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.zaxxer.hikari.HikariDataSource;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Driver;
import java.util.Properties;

@Configuration
public class DbConfiguration {

    public static final String STATUS_JDBC_TEMPLATE = "statusJdbcTemplate";
    private static final String STATUS_DB = "statusDb";
    private static final String HIVE_DB = "hiveDb";
    public static final String HIVE_JDBC_TEMPLATE = "hiveJdbcTemplate";
    private Environment environment;

    public DbConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Primary
    @Bean(name = STATUS_DB)
    public DataSource msSqlDatasource() {
        if (environment.getProperty("fi.datalake.statusdb.url") != null) {
            HikariDataSource hikariDataSource = new HikariDataSource();
            hikariDataSource.setJdbcUrl(environment.getProperty("fi.datalake.statusdb.url"));
            hikariDataSource.setUsername(environment.getProperty("fi.datalake.statusdb.username"));
            hikariDataSource.setPassword(environment.getProperty("fi.datalake.statusdb.password"));
            if (environment.getProperty("fi.datalake.statusdb.driver") != null) {
                hikariDataSource.setDriverClassName(environment.getProperty("fi.datalake.statusdb.driver"));
            } else {
                hikariDataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            }
            return hikariDataSource;
        } else {
            return DataSourceBuilder.create()
                    .driverClassName("org.h2.Driver")
                    .url("jdbc:h2:~/test")
                    .build();
        }
    }
    @Bean(name = STATUS_JDBC_TEMPLATE)
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(msSqlDatasource());
    }
    public JdbcTemplate testHiveDataSource(){
        return new JdbcTemplate( DataSourceBuilder.create()
                .driverClassName("org.h2.Driver")
                .url("jdbc:h2:~/hive")
                .build());

    }

    @SuppressWarnings("unchecked")
    @Bean(name = HIVE_DB)
    public DataSource hiveDatasource() {
        try {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        if (environment.getProperty("fi.datalake.targedb.driver") != null) {
            dataSource.setDriverClass((Class<? extends Driver>) Class.forName(environment.getProperty("fi.datalake.targedb.driver")));
        } else {
            dataSource.setDriverClass(org.apache.hive.jdbc.HiveDriver.class);
        }
        if (environment.getProperty("fi.datalake.targedb.url") != null){
            dataSource.setUrl(environment.getProperty("fi.datalake.targedb.url"));

        }else {
            dataSource.setUrl("jdbc:hive2://127.0.0.1:10001/;transportMode=http");
        }
        return dataSource;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Bean(name = HIVE_JDBC_TEMPLATE)
    public JdbcTemplate hiveJdbcTemplate(@Qualifier(HIVE_DB) DataSource hiveDatasource) {
        return new JdbcTemplate(hiveDatasource);
    }
    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        if (environment.getProperty("fi.datalake.statusdb.url") != null){
            jpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.SQLServer2012Dialect");
            jpaVendorAdapter.setGenerateDdl(true);
            jpaVendorAdapter.setShowSql(false);
        }
        else {
            jpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.H2Dialect");
            jpaVendorAdapter.setGenerateDdl(true);
            jpaVendorAdapter.setShowSql(true);
        }

        return jpaVendorAdapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {

        LocalContainerEntityManagerFactoryBean lef = new LocalContainerEntityManagerFactoryBean();
        lef.setPackagesToScan("fi.tietoallas.integration.common.domain");
        lef.setDataSource(msSqlDatasource());
        lef.setJpaVendorAdapter(jpaVendorAdapter());

        Properties properties = new Properties();
        properties.setProperty("hibernate.show_sql", "true");
        properties.setProperty("hibernate.jdbc.fetch_size", "100");
        properties.setProperty("hibernate.hbm2ddl.auto", "update");

        lef.setJpaProperties(properties);
        return lef;
    }
    @Bean
    public PlatformTransactionManager transactionManager(){
        return new DataSourceTransactionManager(msSqlDatasource());
    }



}
