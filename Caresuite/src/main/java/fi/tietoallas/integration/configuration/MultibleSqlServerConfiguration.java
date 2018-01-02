package fi.tietoallas.integration.configuration;


import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import sun.security.ssl.HandshakeInStream;

import javax.sql.DataSource;

@Configuration
public class MultibleSqlServerConfiguration {

    public static final String MSSQL_DB = "caresuiteMssqlDb";
    public static final String MSSQL_JDBC_TEMPLATE = "caresuiteMssqlJdbcTemplate";

    @Autowired
    Environment environment;

    @Bean(name = MSSQL_DB)
    public DataSource msSqlDatasource() {
       try {
           HikariDataSource hikariDataSource = new HikariDataSource();
           hikariDataSource.setConnectionTimeout(600000);
           hikariDataSource.setLoginTimeout(600000);
           hikariDataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
           hikariDataSource.setJdbcUrl(environment.getProperty("fi.datalake.sourcedb.url"));
           hikariDataSource.setUsername(environment.getProperty("fi.datalake.sourcedb.username"));
           hikariDataSource.setPassword(environment.getProperty("fi.datalake.sourcedb.password"));
           return hikariDataSource;
       } catch (Exception e){
           throw new RuntimeException(e);
       }
    }

    @Bean(name = MSSQL_JDBC_TEMPLATE)
    public JdbcTemplate jdbcTemplate(@Qualifier(MSSQL_DB) DataSource dataSource)
    {
        return new JdbcTemplate(dataSource);
    }



}
