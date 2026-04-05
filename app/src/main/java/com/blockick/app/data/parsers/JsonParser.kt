package com.blockick.app.data.parsers

import org.json.JSONObject
import java.util.Locale

class JsonParser {
    /**
     * Parses JSON content. 
     * Specifically handles Exodus Privacy format and general string extraction.
     */
    fun parse(content: String): List<String> {
        val domains = mutableSetOf<String>()
        try {
            val json = JSONObject(content)
            
            // Exodus Privacy logic
            if (json.has("trackers")) {
                val trackers = json.getJSONObject("trackers")
                val keys = trackers.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val tracker = trackers.getJSONObject(key)
                    if (tracker.has("network_signature")) {
                        val signature = tracker.getString("network_signature")
                        // Signatures are often regex like "domain\\.com|other\\.com"
                        signature.split("|").forEach { part ->
                            val clean = part.replace("\\", "").trim()
                            if (isValidDomain(clean)) {
                                domains.add(clean.lowercase(Locale.ROOT))
                            }
                        }
                    }
                }
            }
            
            // Fallback: Generic string extraction from any JSON values
            // (Simple implementation for now)
            extractStrings(json, domains)

        } catch (e: Exception) {
            // If not a valid JSON object, try generic extraction from the raw text
            val regex = Regex("[a-zA-Z0-9][a-zA-Z0-9-]{1,61}[a-zA-Z0-9]\\.[a-zA-Z]{2,}")
            regex.findAll(content).forEach { match ->
                domains.add(match.value.lowercase(Locale.ROOT))
            }
        }
        return domains.toList()
    }

    private fun extractStrings(obj: Any, domains: MutableSet<String>) {
        when (obj) {
            is JSONObject -> {
                val keys = obj.keys()
                while (keys.hasNext()) {
                    extractStrings(obj.get(keys.next()), domains)
                }
            }
            is String -> {
                if (isValidDomain(obj)) {
                    domains.add(obj.lowercase(Locale.ROOT))
                }
            }
        }
    }

    private fun isValidDomain(domain: String): Boolean {
        return domain.contains(".") && 
               !domain.startsWith(".") && 
               !domain.endsWith(".") &&
               domain.length > 3 &&
               !domain.contains(" ")
    }
}

