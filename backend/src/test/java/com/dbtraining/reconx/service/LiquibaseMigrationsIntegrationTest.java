package com.dbtraining.reconx.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TICKET-ADV079 — Verify Liquibase runs cleanly on a fresh Postgres and seed
 * data lands.
 *
 * Note: unlike the ticket's original assumption, `trades` are NOT seeded via
 * Liquibase — db/seed_data.sql (500 trades) is a standalone script, not
 * wired into the changelog (see TICKET-ADV017). So this test asserts on
 * counterparties and users instead, which ARE loaded by 008-seed.xml.
 */
@SpringBootTest
@Testcontainers
class LiquibaseMigrationsIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("reconx")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void liquibaseAppliedAllChangesetsOnFreshDatabase() {
        Integer changesetCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM databasechangelog", Integer.class);

        assertThat(changesetCount).isGreaterThanOrEqualTo(13);
    }

    @Test
    void seedDataLandedForCounterpartiesAndUsers() {
        Integer counterpartyCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM counterparties", Integer.class);
        Integer userCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users", Integer.class);

        assertThat(counterpartyCount).isEqualTo(10);
        assertThat(userCount).isEqualTo(4);
    }
}
