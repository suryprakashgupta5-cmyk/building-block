package com.block.config;


import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import com.block.tenant.TenantDataSourceManager;
import com.block.tenant.TenantDataSourceRouter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * JPA configuration for TENANT databases (medflow_clinic_{id}).
 * Manages: patients, appointments, queue, consultations, billing,
 *          settings, medicines, messaging.
 *
 * The datasource routes to the clinic-specific DB via TenantDataSourceRouter.
 * Marked @Primary so all unannotated @Transactional methods default here.
 */
@Configuration
@EnableJpaRepositories(
        basePackages = {
                "com.block.auth"
                
        },
        entityManagerFactoryRef = "tenantEntityManagerFactory",
        transactionManagerRef   = "tenantTransactionManager")
public class TenantJpaConfig {

    @Primary
    @Bean("tenantDataSource")
    public DataSource tenantDataSource(TenantDataSourceManager manager) {
        return new TenantDataSourceRouter(manager);
    }

    @Primary
    @Bean("tenantEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean tenantEntityManagerFactory(
            @Qualifier("tenantDataSource") DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan(
        		  "com.block.auth.entity"
        );

        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setDatabase(Database.MYSQL);          // prevents JDBC-metadata dialect lookup at startup
        emf.setJpaVendorAdapter(adapter);

        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto",                      "none");
        props.put("hibernate.format_sql",                        "true");
        props.put("hibernate.default_batch_fetch_size",          "20");
        props.put("hibernate.jdbc.batch_size",                   "30");
        props.put("hibernate.order_inserts",                     "true");
        props.put("hibernate.order_updates",                     "true");
        props.put("hibernate.temp.use_jdbc_metadata_defaults",   "false");
        emf.setJpaPropertyMap(props);
        return emf;
    }

    @Primary
    @Bean("tenantTransactionManager")
    public PlatformTransactionManager tenantTransactionManager(
            @Qualifier("tenantEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
