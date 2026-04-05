package com.blockick.app.data.parsers

import java.util.Locale

class DomainListParser {
    /**
     * Parses a simple domain-per-line file.
     * More robust: handles comments and common non-domain patterns.
     */
    fun parse(content: String): List<String> {
        return content.lineSequence()
            .map { it.trim() }
            // Ignore comments and markers
            .filter { it.isNotEmpty() && !it.startsWith("#") && !it.startsWith("!") && !it.startsWith("/") && !it.startsWith("[") }
            .map { line ->
                // Remove inline comments if any
                line.split("#")[0].split("!")[0].trim()
            }
            .filter { it.isNotEmpty() && it.contains(".") && !it.contains(" ") }
            .map { it.lowercase(Locale.ROOT).trimEnd('.') }
            // Basic domain validation
            .filter { it.length > 3 && !it.startsWith("-") && !it.endsWith("-") }
            .distinct()
            .toList()
    }
}

