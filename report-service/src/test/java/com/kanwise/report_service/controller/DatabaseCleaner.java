package com.kanwise.report_service.controller;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.Value;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Service
@ActiveProfiles("test")
@Value
public class DatabaseCleaner {
    Connection connection;
    Connection connectionQuartz;
    Database database;
    Database databaseQuartz;
    Liquibase liquibase;
    Liquibase liquibaseQuartz;


    public DatabaseCleaner() throws SQLException, DatabaseException {
        this.connection = DriverManager.getConnection("jdbc:tc:postgresql://test", "kanwise", "kanwise");
        this.connectionQuartz = DriverManager.getConnection("jdbc:tc:postgresql://test-qrtz", "kanwise", "kanwise");
        this.database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        this.databaseQuartz = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connectionQuartz));
        this.liquibase = new Liquibase("/changelog-test.xml", new ClassLoaderResourceAccessor(), database);
        this.liquibaseQuartz = new Liquibase("/changelog-qrtz.xml", new ClassLoaderResourceAccessor(), databaseQuartz);
    }

    public void setUp() throws LiquibaseException {
        liquibase.dropAll();
        liquibaseQuartz.dropAll();
        liquibase.update(new Contexts());
        liquibaseQuartz.update(new Contexts());
    }
}
