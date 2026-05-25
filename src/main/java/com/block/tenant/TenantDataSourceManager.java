package com.block.tenant;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages one HikariCP pool per clinic (tenant).
 * Pools are created lazily on first access and cached indefinitely.
 */
@Component
public class TenantDataSourceManager {

    @Value("${spring.datasource.tenant.url-template}")
    private String urlTemplate;           // e.g. jdbc:mysql://localhost:3306/medflow_clinic_%d?...

    @Value("${spring.datasource.tenant.username}")
    private String username;

    @Value("${spring.datasource.tenant.password}")
    private String password;

    private final Map<Long, DataSource> cache = new ConcurrentHashMap<>();

    public DataSource getDataSourceForClinic(Long clinicId) {
        return cache.computeIfAbsent(clinicId, this::createDataSource);
    }

    private DataSource createDataSource(Long clinicId) {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(String.format(urlTemplate, clinicId));
        cfg.setUsername(username);
        cfg.setPassword(password);
        cfg.setPoolName("MedFlow-Clinic-" + clinicId);
        cfg.setMaximumPoolSize(5);
        cfg.setMinimumIdle(1);
        cfg.setConnectionTimeout(20_000);
        cfg.setIdleTimeout(300_000);
        cfg.setMaxLifetime(1_200_000);
        return new HikariDataSource(cfg);
    }
}
