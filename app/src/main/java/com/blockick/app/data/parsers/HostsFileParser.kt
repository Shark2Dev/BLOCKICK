package com.blockick.app.data.parsers

import java.util.Locale

class HostsFileParser {
    /**
     * Parses a standard hosts file.
     * Handles: 
     * - 0.0.0.0 domain.com
     * - 127.0.0.1 domain.com
     * - domain.com (fallback to domain parser logic)
     * - Inline comments: 0.0.0.0 domain.com # comment
     */
    fun parse(content: String): List<String> {
        return content.lineSequence()
            .map { it.trim() }
            // Remove full line comments and empty lines
            .filter { it.isNotEmpty() && !it.startsWith("#") && !it.startsWith("!") }
            .mapNotNull { line ->
                // Remove inline comments
                val cleanLine = line.split("#")[0].split("!")[0].trim()
                if (cleanLine.isEmpty()) return@mapNotNull null

                val parts = cleanLine.split(Regex("\\s+"))
                
                when {
                    // Standard hosts: IP domain1 domain2...
                    parts.size >= 2 && (parts[0] == "0.0.0.0" || parts[0] == "127.0.0.1" || parts[0].contains(":")) -> {
                        // Return all domains on this line (except the IP)
                        parts.drop(1).filter { isValidDomain(it) }
                    }
                    // Just a domain on the line
                    parts.size == 1 && isValidDomain(parts[0]) -> {
                        listOf(parts[0])
                    }
                    // Multiple domains without IP (some lists do this)
                    parts.size > 1 && !parts[0].contains(".") && isValidDomain(parts[1]) -> {
                         parts.filter { isValidDomain(it) }
                    }
                    else -> null
                }
            }
            .flatten()
            .map { it.lowercase(Locale.ROOT).trimEnd('.') }
            .distinct()
            .toList()
    }

    private fun isValidDomain(domain: String): Boolean {
        // Must have a dot, not be an IP, and be a reasonable length
        return domain.contains(".") && 
               !domain.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+")) &&
               domain.length > 3 &&
               !domain.startsWith("*")
    }
}

