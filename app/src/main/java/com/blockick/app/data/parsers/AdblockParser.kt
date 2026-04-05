package com.blockick.app.data.parsers

import java.util.Locale

class AdblockParser {
    /**
     * Minimal adblock syntax parser for domain blocking.
     * Pattern: ||domain.com^
     * Also handles simple domain lines.
     * Used for Goodbye Ads.
     */
    fun parse(content: String): List<String> {
        return content.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("!") && !it.startsWith("[") }
            .mapNotNull { line ->
                var domain = line
                if (domain.startsWith("||")) {
                    domain = domain.substring(2)
                }
                if (domain.endsWith("^")) {
                    domain = domain.substring(0, domain.length - 1)
                }
                
                // Remove any paths or options (e.g., domain.com/path or domain.com$option)
                domain = domain.split("/")[0].split("$")[0]
                
                val finalDomain = domain.lowercase(Locale.ROOT)
                if (finalDomain.contains(".") && !finalDomain.startsWith("*")) {
                    finalDomain
                } else {
                    null
                }
            }
            .distinct()
            .toList()
    }
}

