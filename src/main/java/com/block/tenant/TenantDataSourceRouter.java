package com.block.tenant;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Routes every JDBC connection to the clinic-specific datasource
 * determined by {@link TenantContext}.
 *
 * Extends AbstractDataSource (Spring JDBC) rather than AbstractRoutingDataSource
 * so that pool creation is fully lazy — no upfront map of datasources needed.
 */
@RequiredArgsConstructor
public class TenantDataSourceRouter extends AbstractDataSource {

    private final TenantDataSourceManager manager;

    @Override
    public Connection getConnection() throws SQLException {
        return resolve().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return resolve().getConnection(username, password);
    }

    private DataSource resolve() {
        Long clinicId = TenantContext.getClinicId();
        if (clinicId == null) {
            throw new IllegalStateException(
                    "No tenant in TenantContext — cannot route datasource. Is the request authenticated?");
        }
        return manager.getDataSourceForClinic(clinicId);
    }
}
