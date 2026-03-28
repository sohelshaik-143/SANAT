package com.civicguard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDB configuration for CivicGuard.
 * Sets up geo-spatial indexing for location-based complaint queries,
 * removes _class field from documents, and configures converters.
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.civicguard.repository")
public class MongoConfig {

    /**
     * Custom MongoTemplate that removes the _class field from MongoDB documents.
     * This keeps the database clean and avoids coupling to Java class names.
     */
    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory,
                                       MongoMappingContext context) {
        MappingMongoConverter converter = new MappingMongoConverter(
            new DefaultDbRefResolver(mongoDbFactory), context);
        
        // Remove _class field from all documents
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        converter.afterPropertiesSet();
        
        return new MongoTemplate(mongoDbFactory, converter);
    }
}
