package com.mess.app.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Small, idempotent schema fix-ups that Hibernate's ddl-auto=update cannot do on
 * its own. Every statement is safe to re-run and never drops tables or data.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseMigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        log.info("Running database schema migration checks...");

        // Image collection tables must not carry a surrogate id column
        execute("ALTER TABLE stock_item_images DROP COLUMN IF EXISTS id");
        execute("ALTER TABLE staff_images DROP COLUMN IF EXISTS id");

        // Candidate.Status gained the INACTIVE value used by the "Monthly Mess" page.
        // Hibernate creates a CHECK constraint listing the enum values when the table is
        // created, so an existing constraint must be relaxed to accept the new value.
        execute("ALTER TABLE candidates DROP CONSTRAINT IF EXISTS candidates_status_check");

        // Profile image column for candidates (ddl-auto=update adds it as well; this keeps
        // the schema correct even when ddl-auto is turned off).
        execute("ALTER TABLE candidates ADD COLUMN IF NOT EXISTS profile_image_url VARCHAR(2048)");

        log.info("Database schema migration checks completed");
    }

    private void execute(String sql) {
        try {
            jdbcTemplate.execute(sql);
            log.info("Schema migration applied: {}", sql);
        } catch (Exception e) {
            log.warn("Schema migration skipped ({}): {}", sql, e.getMessage());
        }
    }
}
