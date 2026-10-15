plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.diffplug.spotless") version "6.25.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
}

/**
 * Phase 12: deterministic source whitespace formatter. This is intentionally
 * scoped to whitespace and EOF only; it does not rewrite approved Kotlin APIs.
 * A real negative fixture is exercised in CI so this task must reject drift.
 */
spotless {
    format("kotlinAndGradleWhitespace") {
        target("app/src/**/*.kt", "app/src/**/*.kts", "*.gradle.kts")
        targetExclude("**/build/**")
        trimTrailingWhitespace()
        indentWithSpaces(4)
        endWithNewline()
    }
}

/**
 * Phase 12: real Detekt Kotlin static analyzer on production code only.
 * Explicit rules live in config/detekt/detekt.yml; test code is still
 * checked by the Kotlin compiler and its JUnit contract suites.
 */
detekt {
    buildUponDefaultConfig = false
    ignoreFailures = false
    parallel = true
    source.setFrom(files("app/src/main/java"))
    config.setFrom(files("config/detekt/detekt.yml"))
}
