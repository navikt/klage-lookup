package no.nav.klage.lookup.config

import org.springframework.context.annotation.Configuration
import org.springframework.resilience.annotation.EnableResilientMethods

@Configuration
@EnableResilientMethods
class RetryConfiguration