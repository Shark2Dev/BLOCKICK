# BLOCKICK - Project Context

## Project Overview
**BLOCKICK** is a privacy-focused Android application that provides system-wide ad-blocking and tracker filtering through a **Local VPN Tunnel**. By intercepting DNS requests directly on the device, it avoids sending traffic to remote servers, ensuring maximum privacy and minimal latency.

### Key Features
*   **Local DNS Filtering:** Uses `VpnService` to intercept and filter DNS queries locally.
*   **Protection Profiles:** Customizable filtering levels (Minimal, Balanced, Ultra).
*   **Custom Blocklists:** Support for Hosts, plain domain lists, and Adblock syntax.
*   **Safe Search:** Network-level enforcement for major search engines.
*   **App Exclusion:** Ability to bypass filtering for specific applications.
*   **Security Audit:** Diagnostics for DNS leaks and connection health.
*   **Automatic Updates:** Scheduled blocklist updates with configurable frequency.
*   **DNS-over-HTTPS:** Encrypted upstream DNS resolution for privacy.

---

## Technical Stack
*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Material 3)
*   **Dependency Injection:** Hilt
*   **Database:** Room (for domain lists and logs)
*   **Preferences:** Jetpack DataStore
*   **Networking:** OkHttp, Retrofit, dnsjava
*   **Background Tasks:** WorkManager (for blocklist synchronization)
*   **Build System:** Gradle (Kotlin DSL)

---

## Architecture & Core Components

### 1. VPN & Engine (`com.blockick.app.vpn`, `com.blockick.app.domain.engine`)
*   `LocalVpnService`: Extends `VpnService` to create the local tunnel and manage packet flow.
*   `DnsPacketProcessor`: Parses UDP/TCP packets to extract DNS queries.
*   `BlocklistEngine`: The core filtering logic that matches domains against local databases with subdomain support.
*   `UpstreamResolver`: Handles DNS-over-HTTPS resolution to trusted providers.

### 2. UI Layer (`com.blockick.app.ui`)
*   Follows **MVVM** pattern.
*   Uses **Jetpack Compose** for a modern, themed interface.
*   Features a Home Screen Widget for quick toggles.
*   Four main screens: Home, Lists, Statistics, Settings

### 3. Data Layer (`com.blockick.app.data`)
*   `BlocklistRepository`: Orchestrates domain list updates and rule management.
*   `DomainDao`: High-performance access to millions of blocked domains stored in Room.

### 4. Background Processing (`com.blockick.app.worker`)
*   `BlocklistUpdateWorker`: Handles periodic blocklist synchronization.
*   `WorkManagerScheduler`: Manages scheduled work based on user preferences.

---

## Building and Running

### Prerequisites
*   Android Studio Ladybug or newer.
*   JDK 17.

### Key Commands
*   **Build Debug APK:** `./gradlew assembleDebug`
*   **Run Unit Tests:** `./gradlew test`
*   **Run Instrumented Tests:** `./gradlew connectedAndroidTest`
*   **Clean Project:** `./gradlew clean`
*   **Generate Dependency Graph:** (Placeholder - TODO: Add dependency analysis script)

---

## Development Conventions
*   **Asynchronous Work:** Always use Kotlin Coroutines and Flow for data streams.
*   **DI:** All ViewModels and Services must be injected via Hilt.
*   **UI State:** Use `StateFlow` in ViewModels to expose UI state to Compose screens.
*   **Testing:** New features should include unit tests (MockK) and, where applicable, Compose UI tests.
*   **DNS Handling:** Be cautious with `dnsjava` service file conflicts (already handled in `app/build.gradle.kts` packaging options).

---

*Last updated: March 26, 2026*
