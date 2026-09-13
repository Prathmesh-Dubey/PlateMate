package com.mess.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AppApplication implements CommandLineRunner {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public static void main(String[] args) {
		SpringApplication.run(AppApplication.class, args);
	}

	@Override
	public void run(String... args) {
		try {
			Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
			System.out.println("========================================");
			System.out.println("✅ DATABASE CONNECTION SUCCESSFUL!");
			System.out.println("✅ Query Result: " + result);
			System.out.println("========================================");
		} catch (Exception e) {
			System.err.println("========================================");
			System.err.println("❌ DATABASE CONNECTION FAILED!");
			System.err.println("❌ Error: " + e.getMessage());
			System.err.println("========================================");

			// Check if it's a connection pool issue
			if (e.getMessage().contains("closed")) {
				System.err.println("⚠️ HINT: The connection was closed. Check your Supabase URL format.");
				System.err.println(
						"Try using: jdbc:postgresql://db.wocllxphpjtoszlnvcsi.supabase.co:5432/postgres?ssl=true&sslmode=require");
			}
		}
	}
}