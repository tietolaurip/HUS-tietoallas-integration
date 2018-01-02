package fi.tietoallas.integration.clinisoft.configuration;


import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.SQLDataException;
import java.sql.SQLException;

@Configuration
public class SourceDbConfiguration {

    public static final String SOURCE_DB = "sourceDb";
    public static final String SOURCE_JDBC_TEMPLATE = "clinisoftJdbcTemplate";

    private static Logger logger = LoggerFactory.getLogger(SourceDbConfiguration.class);

    @Autowired
    private Environment environment;

    @Bean(name =SOURCE_DB)
    public DataSource datasource() {
        HikariDataSource hikariDataSource = new HikariDataSource();
                hikariDataSource.setDriverClassName("com.sybase.jdbc4.jdbc.SybDriver");
                hikariDataSource.setJdbcUrl(environment.getProperty("fi.datalake.sourcedb.url"));
                logger.info("===========sybase URL "+environment.getProperty("fi.datalake.sourcedb.url"));
                hikariDataSource.setUsername(environment.getProperty("fi.datalake.sourcedb.username"));
                logger.info("===========sybase ursername "+environment.getProperty("fi.datalake.sourcedb.username"));
                hikariDataSource.setPassword(environment.getProperty("fi.datalake.sourcedb.password"));
                logger.info("============sybase password "+environment.getProperty("fi.datalake.sourcedb.password"));
                return hikariDataSource;

    }
    @Bean(name =SOURCE_JDBC_TEMPLATE)
    public JdbcTemplate jdbcTemplate(){
        JdbcTemplate jdbcTemplate = new JdbcTemplate(datasource());
        jdbcTemplate.setQueryTimeout(0);
        return jdbcTemplate;
    }



}
