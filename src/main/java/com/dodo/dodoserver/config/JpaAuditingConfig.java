package com.dodo.dodoserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// created_at, updated_at
@Configuration
@EnableJpaAuditing
	public class JpaAuditingConfig {
}
