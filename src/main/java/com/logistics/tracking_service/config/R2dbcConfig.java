package com.logistics.tracking_service.config;

import com.logistics.tracking_service.domain.InvalidTrackingNumberVersionException;
import com.logistics.tracking_service.domain.TrackingNumberGenerator;
import com.logistics.tracking_service.domain.TrackingNumberVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import io.r2dbc.spi.ConnectionFactory;

@Configuration
@EnableR2dbcRepositories(basePackages = "com.logistics.tracking_service.repository")
public class R2dbcConfig {

    /**
     * Initializes the database with schema and data SQL scripts
     */
    @Bean
    public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("schema.sql"));
        populator.addScript(new ClassPathResource("data.sql"));
        
        initializer.setDatabasePopulator(populator);
        return initializer;
    }
    
    /**
     * Creates a TrackingNumberGenerator bean with the default version
     */
    @Bean
    public TrackingNumberGenerator trackingNumberGenerator() throws InvalidTrackingNumberVersionException {
        TrackingNumberVersion version = TrackingNumberVersion.of("TP1");
        return new TrackingNumberGenerator(version);
    }
}
