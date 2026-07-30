package com.dbtraining.reconx.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * TICKET-ADV050 — @CreatedDate / @LastModifiedDate auditing.
 *
 * Kept off {@code ReconxApplication} on purpose: {@code @EnableJpaAuditing}
 * on the {@code @SpringBootApplication} class gets pulled into every
 * {@code @WebMvcTest} slice (which uses the app class as its root config),
 * crashing with "JPA metamodel must not be empty" since slice tests don't
 * load a real EntityManagerFactory. A separate @Configuration class isn't
 * picked up by slice tests, so it stays out of their way.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
