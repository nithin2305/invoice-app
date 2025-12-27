package com.invoice.app.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Database configuration for production environment.
 * Handles Render.com DATABASE_URL format conversion to JDBC URL.
 * 
 * Render.com provides DATABASE_URL in format: postgresql://user:password@host:port/database
 * JDBC requires format: jdbc:postgresql://host:port/database with separate username/password
 */
@Configuration
@Profile("production")
public class DatabaseConfig {

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Bean
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        
        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            try {
                // Parse the Render.com DATABASE_URL format
                URI dbUri = new URI(databaseUrl);
                
                String userInfo = dbUri.getUserInfo();
                if (userInfo == null || !userInfo.contains(":")) {
                    throw new IllegalArgumentException(
                        "DATABASE_URL must contain user credentials in format: postgresql://user:password@host/database");
                }
                
                String[] credentials = userInfo.split(":", 2);
                String username = credentials[0];
                String password = credentials.length > 1 ? credentials[1] : "";
                
                // Build proper JDBC URL
                String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost();
                if (dbUri.getPort() > 0) {
                    jdbcUrl += ":" + dbUri.getPort();
                }
                jdbcUrl += dbUri.getPath();
                
                dataSource.setJdbcUrl(jdbcUrl);
                dataSource.setUsername(username);
                dataSource.setPassword(password);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(
                    "Invalid DATABASE_URL format. Expected: postgresql://user:password@host:port/database", e);
            }
        } else {
            // Fallback to localhost for local development
            dataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/invoicedb");
            dataSource.setUsername("postgres");
            dataSource.setPassword("postgres");
        }
        
        dataSource.setDriverClassName("org.postgresql.Driver");
        
        // Connection pool settings (optimized for free tier)
        dataSource.setMaximumPoolSize(5);
        dataSource.setMinimumIdle(1);
        dataSource.setConnectionTimeout(20000);
        
        return dataSource;
    }
}
