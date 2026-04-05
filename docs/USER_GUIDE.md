# BLOCKICK - Technical Guide & Features

## 🛡️ How It Works
BLOCKICK works by creating a **Local VPN Tunnel** on your device. Unlike traditional VPNs that send your traffic to a remote server, this tunnel stays entirely on your phone.

1.  **DNS Interception**: When an app tries to connect to a domain (e.g., `ads.google.com`), the VPN intercepts the DNS request.
2.  **On-Device Filtering**: The app checks the domain against its high-performance database of millions of blocked domains.
3.  **Blocking**: If a match is found, the app returns a "Does Not Exist" (NXDOMAIN) response. The ad never loads, saving you data and battery.
4.  **Privacy**: No data ever leaves your device. All filtering happens locally in the `BlocklistEngine`.

---

## ✨ Key Features

### 1. Protection Profiles
One-tap presets to balance performance and security:
*   **Minimal**: Core protection with zero impact on speed. Focuses on high-signal domains with nearly zero risk of "breaking" apps. Uses OISD Small and Hagezi Light.
*   **Balanced**: Recommended. Blocks most ads and common trackers using highly curated lists like OISD Big and Hagezi Multi.
*   **Ultra**: Maximum security. Uses aggressive lists including 1Hosts Pro, Goodbye Ads Extended, and Hagezi Pro for deep-level telemetry blocking.

### 2. Custom Blocklists
Add your own favorite filter URLs. Supports:
*   **Hosts format** (`0.0.0.0 domain.com`)
*   **Plain domain lists**
*   **Adblock syntax** (`||domain.com^`)

### 3. Automatic Filter Updates
Keep your blocklists current with automatic background updates. When enabled, BLOCKICK periodically refreshes all your filter lists to ensure protection against the latest known trackers and ad domains.

**Available Update Frequencies:**
*   **Daily**: Recommended for maximum protection
*   **Every 3 days**: Balanced update schedule
*   **Every 5 days**: Less frequent updates
*   **Every 7 days**: Weekly updates

**How It Works:**
1. Updates run in the background using Android's WorkManager
2. Only runs when connected to the internet and battery is not low
3. Respects your selected frequency automatically
4. Shows a notification when updates are in progress or complete
5. Immediately applies new filters to your protection

**Manual Updates:**
You can always manually update your lists by tapping the refresh icon in the Lists tab. This is useful if you want to update immediately without waiting for the next scheduled update.

### 4. Safe Search Enforcement
BLOCKICK automatically enforces **Safe Search** mode on major search engines at the DNS level. This ensures all search queries - regardless of browser or app - are filtered through safe search versions.

#### How It Works
When you enable Safe Search Enforcement in Settings, the app intercepts DNS queries to search engine domains and redirects them to their safe search equivalents:

| Search Engine | Safe Search Redirect | Description |
|---------------|---------------------|-------------|
| **Google** | `forcesafesearch.google.com` (216.239.38.120) | Blocks explicit content, images, and videos |
| **Bing** | `strict.bing.com` (204.79.197.220) | Enforces strict search filtering |
| **DuckDuckGo** | `safe.duckduckgo.com` | Routes through safe search infrastructure |
| **Yandex** | `family.yandex.com` (213.180.193.56) | Family-safe search mode |

#### Supported Subdomains
Safe Search works across all search engine subdomains, including:
- `www.google.com`, `images.google.com`, `maps.google.com`
- `www.bing.com`, `search.bing.com`
- `www.duckduckgo.com`, `html.duckduckgo.com`
- `www.yandex.com`, `images.yandex.com`

#### Benefits
- **Network-Level Enforcement**: Works on all apps and browsers without account setup
- **No Data Collection**: Search queries stay private
- **No Configuration**: Just toggle on - it works automatically
- **Transparent**: Shows in your activity log as "Safe Search" rather than "Blocked"

### 5. DNS Filtering Provider
BLOCKICK uses encrypted **DNS-over-HTTPS (DoH)** to securely forward legitimate (non-blocked) queries to trusted upstream providers. This ensures your DNS lookups remain private and fast.

#### How It Works
When a domain passes the blocklist check, BLOCKICK forwards the DNS query to your selected provider using encrypted HTTPS instead of plain DNS. This happens entirely automatically - you just choose a provider.

#### Available Providers

| Provider | DoH Endpoint | Features |
|----------|--------------|----------|
| **Cloudflare (Recommended)** | `https://1.1.1.1/dns-query` | Fastest, privacy-focused, no logging |
| **Google DNS** | `https://8.8.8.8/dns-query` | Reliable, extensive global network |
| **Quad9** | `https://9.9.9.9/dns-query` | Security-focused, blocks malicious domains |

#### Privacy & Security
- **Encrypted Queries**: All upstream DNS uses HTTPS (DoH), preventing ISP surveillance
- **No Query Logging**: All providers commit to minimal or no logging
- **Fallback Protection**: If your selected provider fails, BLOCKICK returns an error rather than bypassing filters
- **Custom Provider**: Advanced users can enter their own DoH endpoint

#### Selecting a Provider
1. Go to **Settings -> DNS Filtering Provider**
2. Choose from preset options or enter a custom DoH URL
3. The change applies immediately to all new queries

### 6. App Exclusion
If an app (like a banking app or a game) breaks when ad-blocking is active, you can exclude it in **Settings -> App Exclusion**. These apps will bypass the VPN tunnel entirely.

### 7. Security Audit
A diagnostic tool that scans your network for:
*   **Private DNS Leaks**: Detects if Android's system DNS is bypassing your filters.
*   **Upstream Security**: Verifies if your external queries use encrypted DNS-over-HTTPS (DoH).
*   **Connection Health**: Checks VPN status and network encryption.

### 8. Home Screen Widget
A modern widget to:
*   Toggle protection on/off instantly.
*   Monitor how many requests were blocked today at a glance.

### 9. Personal Rules Manager
Add individual domains to your personal block or allow list directly from the app.

**How It Works:**
- **Blocked List**: Domains that are always filtered regardless of other settings
- **Allowed List**: Domains that bypass all filtering (whitelist)
- **Easy Management**: Add, remove, and manage domains with a simple interface
- **Instant Effect**: Changes apply immediately to all new DNS queries

**Access:**
Go to **Lists -> Custom Rules Manager** to manage your personal rules.

### 10. Filter List Subscriptions
Subscribe to external filter lists from the community. BLOCKICK supports automatic synchronization with third-party blocklists.

**Supported Formats:**
- **Hosts format** (`0.0.0.0 domain.com`)
- **Plain domain lists** (one domain per line)
- **Adblock syntax** (`||domain.com^`)

**Popular Community Lists:**
- EasyList (Primary ad-blocking rules)
- EasyPrivacy (Tracking protection)
- Fanboy's Enhanced Tracking List
- And many more...

**Access:**
Go to **Lists -> Filter List Subscriptions** to add and manage external lists.

### 11. Bypass Schedule
Schedule automatic bypass periods to temporarily disable ad blocking for time-sensitive tasks like phone backups and notifications.

**How It Works:**
1. **Enable the Schedule**: Toggle on the scheduled bypass feature
2. **Set Time Window**: Choose start and end times (e.g., 02:00 - 03:00)
3. **Select Days**: Choose which days the bypass is active
4. **Automatic Activation**: During the scheduled period, all blocking is bypassed

**Use Cases:**
- **Phone Backups**: Schedule bypass during off-peak hours for backup services
- **App Notifications**: Ensure critical notifications arrive without delay
- **Time-Sensitive Tasks**: Allow necessary network traffic during specific windows

**Example Scenarios:**
- **Nightly Backups**: Set 02:00 - 03:00 every day for automatic backups
- **Weekend Maintenance**: Set 10:00 - 11:00 on weekends for system updates
- **Custom Windows**: Any time window that fits your schedule

**Benefits:**
- **Automatic**: No manual intervention required
- **Flexible**: Choose specific days and times
- **Transparent**: Shows in activity logs as "BYPASS ACTIVE"
- **Safe**: Only affects the scheduled window, not manual toggles

**Configuration:**
Go to **Settings -> Bypass Schedule** to configure your bypass window.

---

## 🔍 Blocklist Selection & Criteria
The blocklists in BLOCKICK are selected based on four key pillars to ensure a high-quality user experience:

### Selection Criteria
1.  **Maintenance Frequency**: We prioritize "living" lists that are updated daily to catch new tracking domains.
2.  **False Positive Rate**: Lists like **OISD** are chosen for their strict curation, ensuring that legitimate services (like banking or system updates) aren't accidentally blocked.
3.  **Mobile Optimization**: Lists like **Goodbye Ads** are prioritized because they are specifically built to target Android-specific ad SDKs and OEM telemetry.
4.  **Tiered Aggression**: We use "Child" lists (e.g., 1Hosts Lite vs. Pro) to allow users to scale their privacy based on their technical comfort level.

### Provider Characteristics
| Provider | Strength | Profile Usage |
| :--- | :--- | :--- |
| **OISD** | Zero false positives; highly curated. | All (Small/Big) |
| **Hagezi** | Industry standard; excellent for telemetry and malware. | All (Light/Multi/Pro) |
| **1Hosts** | Extremely aggressive; targets obscure trackers. | Balanced (Lite) / Ultra (Pro) |
| **Steven Black** | Massive consolidated list of standard ads. | All (Unified) |
| **Goodbye Ads** | Best-in-class for Android app ad-blocking. | Ultra (Extended) |

### Performance vs. Protection Trade-offs
*   **Minimal** profiles use smaller, "high-impact" lists that require less RAM and CPU to process, making them ideal for older devices.
*   **Ultra** profiles load significantly more rules (~500k+), which provides maximum privacy but may require the user to occasionally use **App Exclusion** if a specific app feature (like a sponsored link) is blocked.

---

## 🚀 Pro-Tips for Best Performance

*   **Disable "Private DNS"**: In your Android System Settings (Connections -> More connection settings), set **Private DNS to OFF**. This ensures Android doesn't bypass the local VPN.
*   **Battery Optimization**: For uninterrupted protection, exclude BLOCKICK from Android's battery optimization settings.
*   **Automatic Updates**: Enable automatic filter updates in Settings to keep your protection current without manual intervention.
*   **Manual Refresh**: If you need immediate updates, tap the refresh icon in the Lists tab anytime.
*   **App Exclusion**: If an app breaks, use App Exclusion rather than disabling protection entirely.
*   **Safe Search**: Enable Safe Search Enforcement for an additional layer of content filtering across all search engines.

---

## 📱 Navigation Guide

### Home Screen
- View protection status (Active/Paused)
- Toggle ad-blocking on/off
- See today's blocking statistics
- Access quick settings

### Lists Screen
- Browse and enable/disable filter lists
- Access Custom Blocklists and Personal Rules
- Manage App Exclusions
- Manual update all lists

### Statistics Screen
- View weekly blocking trends
- See most blocked domains
- Monitor filtering activity in real-time
- Access detailed activity logs

### Settings Screen
- Configure DNS Filtering Provider
- Enable Automatic Filter Updates
- Toggle Safe Search Enforcement
- Configure Bypass Schedule
- Run Security Audit
- Access advanced configuration

---

*Created by BLOCKICK AI Assistant - March 2026*
