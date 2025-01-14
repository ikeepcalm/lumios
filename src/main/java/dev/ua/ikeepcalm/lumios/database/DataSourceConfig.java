package dev.ua.ikeepcalm.lumios.database;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    private final Environment environment;

    public DataSourceConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl("jdbc:" + environment.getProperty("MYSQL_URL", "jdbc:mysql://localhost:3306/default_db"));
//        dataSource.setUsername(environment.getProperty("MYSQLUSER", "default_user"));
//        dataSource.setPassword(environment.getProperty("MYSQLPASSWORD", "default_password"));
//        dataSource.setDriverClassName(environment.getProperty("spring.datasource.driver-class-name", "com.mysql.cj.jdbc.Driver"));
        return dataSource;
    }
}
