# BLOCKICK

![Android CI](https://github.com/Shark2Dev/blockick/workflows/Android%20CI/badge.svg)
![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-purple.svg)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.12.01-orange.svg)

**BLOCKICK** is a privacy-focused Android ad blocker that uses a **Local VPN Tunnel** to filter DNS requests system-wide. All filtering happens locally on your device - no data is sent to external servers.

## 🛡️ How It Works

1. **DNS Interception**: When an app tries to connect to a domain (e.g., `ads.google.com`), the VPN intercepts the DNS request
2. **On-Device Filtering**: The app checks the domain against a high-performance database of millions of blocked domains
3. **Blocking**: If a match is found, the app returns "Does Not Exist" (NXDOMAIN). The ad never loads
4. **Privacy**: No data ever leaves your device

## ✨ Features

### Core Protection
- **System-wide blocking** across all apps and browsers
- **Local VPN architecture** - no remote servers, complete privacy
- **IPv4/IPv6 dual-stack** support
- **Real-time statistics** with daily and weekly trends

### Smart Protection Profiles
- **Minimal**: Core protection with minimal resource usage
- **Balanced**: Recommended for most users
- **Ultra**: Maximum security with aggressive blocking

### Advanced Features
- **Automatic Filter Updates**: Schedule daily, every 3, 5, or 7 days
- **Safe Search Enforcement**: Force Safe Search on Google, Bing, DuckDuckGo, Yandex
- **DNS-over-HTTPS**: Encrypted upstream DNS resolution
- **Custom Blocklists**: Add your own filter lists (Hosts, Adblock, Domain formats)
- **Personal Rules**: Block or allow specific domains
- **App Exceptions**: Bypass blocking for specific apps
- **Scheduled Bypass**: Temporarily disable blocking for backups and notifications
- **Security Audit**: DNS leak detection and connection diagnostics

### User Experience
- **Modern UI** built with Jetpack Compose and Material Design 3
- **Home Screen Widget** for quick toggle and stats
- **Quick Settings Tile** for instant control
- **Dark Theme** optimized for OLED displays
- **Low Battery Impact** - designed for efficiency

## 📱 Screenshots

*Coming soon - Add screenshots to `docs/SCREENSHOTS/` folder*

## 🚀 Installation

### Build from Source

**Prerequisites:**
- Android Studio Ladybug (2024.2+) or newer
- JDK 17
- Android SDK (Compile SDK 35)

**Steps:**

1. **Clone the repository**
   ```bash
   git clone https://github.com/Shark2Dev/blockick.git
   cd BLOCKICK
   ```

2. **Open in Android Studio**
   - File → Open → Select the `BLOCKICK` folder
   - Wait for Gradle sync to complete

3. **Sync Gradle**
   ```bash
   ./gradlew sync
   ```

4. **Build Debug APK**
   ```bash
   ./gradlew assembleDebug
   ```

5. **Install on Device**
   - Find the APK at `app/build/outputs/apk/debug/app-debug.apk`
   - Transfer to device and install, or
   - Run directly from Android Studio

### Download Pre-built APK

*Releases will be available on the [GitHub Releases page](https://github.com/Shark2Dev/blockick/releases)*

## 🔧 Technical Details

### Architecture
- **Pattern**: Clean Architecture with MVVM
- **Language**: Kotlin 2.1.0
- **UI**: Jetpack Compose with Material Design 3
- **Database**: Room for persistent storage
- **DI**: Hilt for dependency injection
- **Background**: WorkManager for scheduled tasks
- **Networking**: OkHttp + Retrofit for HTTP requests
- **DNS**: dnsjava for DNS protocol handling

### Key Components
- **LocalVpnService**: Creates VPN tunnel, intercepts DNS packets
- **DnsPacketProcessor**: Parses DNS queries, applies filtering rules
- **BlocklistEngine**: O(1) domain lookup with HashSet
- **UpstreamResolver**: DNS-over-HTTPS to trusted providers

### Database Schema
- 8 tables including blocklists, domains, query logs, daily statistics
- Efficient indexing for fast lookups
- Automatic cleanup of old logs

### Security
- **No external servers** - all processing on-device
- **DNS-over-HTTPS** for encrypted upstream queries
- **Cleartext traffic disabled** in network security config
- **No analytics or tracking** of any kind

## 🛠️ Configuration

### DNS Filtering Provider
Choose from trusted providers:
- **Cloudflare** (Recommended) - Fastest, privacy-focused
- **Google DNS** - Reliable, extensive network
- **Quad9** - Security-focused, blocks malicious domains

### Protection Profiles
Each profile uses a curated combination of blocklists:

| Profile | Lists | Domains | Use Case |
|---------|-------|---------|----------|
| Minimal | OISD Small + Hagezi Light | ~100K | Low-end devices |
| Balanced | OISD Big + Hagezi Multi | ~500K | Most users |
| Ultra | 1Hosts Pro + Hagezi Pro | ~1M+ | Maximum privacy |

### Custom Blocklists
Add external filter lists in various formats:
- **Hosts format**: `0.0.0.0 domain.com`
- **Domain lists**: One domain per line
- **Adblock syntax**: `||domain.com^`

## 🤝 Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

### Development Setup

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Make your changes**
4. **Commit your changes**
   ```bash
   git commit -m 'Add amazing feature'
   ```
5. **Push to the branch**
   ```bash
   git push origin feature/amazing-feature
   ```
6. **Open a Pull Request**

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 🔒 Privacy

**BLOCKICK respects your privacy:**
- All DNS filtering happens locally on your device
- No data is sent to external servers (except blocklist updates and DNS queries to your chosen provider)
- No analytics, tracking, or telemetry
- Open source for transparency

For more details, see our [User Guide](docs/USER_GUIDE.md).

## 📚 Documentation

- [User Guide](docs/USER_GUIDE.md) - Complete feature documentation
- [Architecture](docs/ARCHITECTURE.md) - Technical architecture details
- [Contributing](CONTRIBUTING.md) - Development guidelines

## 🐛 Reporting Issues

Found a bug? Please report it on our [GitHub Issues](https://github.com/Shark2Dev/blockick/issues) page.

Include:
- Android version
- Device model
- Steps to reproduce
- Expected vs actual behavior
- Logcat output (if applicable)

## 💡 Feature Requests

Have an idea for a new feature? Open an issue with the `enhancement` label!

## ⭐ Show Your Support

Give us a ⭐ if you find this project useful!

## 📞 Contact

- **GitHub Issues**: [Bug reports and feature requests](https://github.com/Shark2Dev/blockick/issues)
- **Email**: Your email here

---

**BLOCKICK** - Privacy-focused ad blocking, locally on your device.
