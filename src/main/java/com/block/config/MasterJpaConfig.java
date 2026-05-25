package com.block.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

/**
 * JPA configuration for the MASTER database (medflow_master).
 * Manages: clinics, users, refresh_tokens.
 *
 * Repository package: in.medflowpro.api.auth
 */
@Configuration
@EnableJpaRepositories(
        basePackages = {"in.medflowpro.api.auth", "in.medflowpro.api.subscription",
                        "in.medflowpro.api.platform", "in.medflowpro.api.notification"},
        entityManagerFactoryRef = "masterEntityManagerFactory",
        transactionManagerRef = "masterTransactionManager")
public class MasterJpaConfig {

    @Bean("masterDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean("masterEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean masterEntityManagerFactory(
            @Qualifier("masterDataSource") DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("in.medflowpro.api.auth.entity",
                              "in.medflowpro.api.subscription.entity",
                              "in.medflowpro.api.platform.entity",
                              "in.medflowpro.api.notification.entity");

        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        emf.setJpaVendorAdapter(adapter);
        emf.setJpaPropertyMap(Map.of(
                "hibernate.hbm2ddl.auto",    "none",
                "hibernate.format_sql",      "true",
                "hibernate.jdbc.batch_size", "30",
                "hibernate.order_inserts",   "true",
                "hibernate.order_updates",   "true",
                "hibernate.dialect",         "org.hibernate.dialect.MySQLDialect"
        ));
        return emf;
    }

    @Bean("masterTransactionManager")
    public PlatformTransactionManager masterTransactionManager(
            @Qualifier("masterEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
