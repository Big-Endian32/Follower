# Feasibility Assessment: Android Tracking Detection App

## Overview

This document assesses the feasibility of building an Android application that combines:
1. **Network Survey functionality** (similar to [android-network-survey](https://github.com/christianrowlands/android-network-survey))
2. **Tracking Detection** (similar to [Chasing Your Tail NG](https://github.com/ArgeliusLabs/Chasing-Your-Tail-NG))

---

## Source Project Analysis

### Android Network Survey
| Aspect | Details |
|--------|---------|
| **Platform** | Android (Java 59%, Kotlin 41%) |
| **License** | Apache 2.0 (permissive, allows modification) |
| **Protocols** | Cellular (GSM/CDMA/UMTS/LTE/5G), WiFi, Bluetooth, GNSS |
| **Data Export** | GeoPackage, CSV, MQTT/gRPC streaming |
| **Key Strength** | Already solves Android permission/API challenges for network scanning |

### Chasing Your Tail NG (CYT)
| Aspect | Details |
|--------|---------|
| **Platform** | Linux only (Python 3.6+) |
| **License** | MIT (permissive, allows modification) |
| **Data Source** | WiFi probe requests via Kismet (requires monitor mode) |
| **Detection Method** | Temporal analysis (5/10/15/20 min windows), spatial correlation, persistence scoring |
| **Key Strength** | Sophisticated algorithms for detecting following devices |

---

## Critical Technical Challenge: WiFi Monitor Mode

### The Problem

CYT's core functionality relies on capturing **WiFi probe requests** - the frames that client devices broadcast when searching for networks. This requires:

1. **Monitor Mode** - A special WiFi adapter mode that captures all 802.11 frames
2. **Kismet** - A Linux-based wireless network detector that processes these frames

### Android Limitations

| Requirement | Android Support | Notes |
|-------------|-----------------|-------|
| WiFi Monitor Mode | ❌ Not available | Android's WiFi drivers don't expose this capability |
| Raw 802.11 Frame Access | ❌ Not available | WifiManager API only provides scan results, not raw frames |
| Probe Request Capture | ❌ Not available | Cannot see other devices' probe requests |
| Root Access Workaround | ⚠️ Limited | Even with root, requires specific chipset support (rare) |
| External USB Adapter | ⚠️ Possible | Requires OTG, root, custom drivers, limited device compatibility |

**Bottom Line**: Direct port of CYT's WiFi probe request analysis is **not feasible** on standard Android devices.

---

## External USB WiFi Adapter Option

Using an external USB WiFi adapter with monitor mode capability via USB-C/OTG is a potential workaround. Here's a detailed analysis:

### Proof of Concept: liber80211

The [liber80211](https://github.com/brycethomas/liber80211) project demonstrated that **monitor mode IS possible on Android without root** using an external USB WiFi adapter.

| Aspect | Details |
|--------|---------|
| **How it works** | User-space port of Linux kernel driver for ALFA AWUS036H |
| **Root required** | ❌ No (uses Android USB Host Mode APIs) |
| **Supported adapter** | ALFA AWUS036H only (Realtek RTL8187 chipset) |
| **Functionality** | Can capture probe requests and create PCAP files |
| **Status** | Proof of concept / early stage |

### Compatible USB WiFi Adapters

Based on current research, adapters known to support monitor mode:

| Adapter | Chipset | Monitor Mode | Android Support |
|---------|---------|--------------|-----------------|
| ALFA AWUS036H | RTL8187 | ✅ Yes | ✅ Proven (liber80211) |
| ALFA AWUS036NH | Atheros AR9271 | ✅ Yes | ⚠️ Needs driver port |
| ALFA AWUS036ACH | RTL8812AU | ✅ Yes | ⚠️ Needs driver port |
| TP-Link TL-WN722N v1 | Atheros AR9271 | ✅ Yes | ⚠️ Needs driver port |
| Panda PAU09 | Ralink RT5572 | ✅ Yes | ⚠️ Needs driver port |

**Note**: TP-Link TL-WN722N **v2 and v3** use different chipsets and do NOT support monitor mode.

### Technical Requirements

```
┌─────────────────────────────────────────────────────────────────┐
│                  USB WIFI ADAPTER SETUP                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐    USB OTG    ┌──────────────────────────┐   │
│  │   Android    │◄─────────────►│  USB WiFi Adapter        │   │
│  │    Phone     │    Cable      │  (Monitor Mode Capable)  │   │
│  └──────────────┘               └──────────────────────────┘   │
│         │                                   │                   │
│         │                                   │                   │
│         ▼                                   ▼                   │
│  ┌──────────────┐               ┌──────────────────────────┐   │
│  │ User-space   │               │  Captures:               │   │
│  │ Driver       │◄──────────────│  - Probe Requests        │   │
│  │ (like        │   Raw 802.11  │  - Beacon Frames         │   │
│  │  liber80211) │   Frames      │  - All WiFi Traffic      │   │
│  └──────────────┘               └──────────────────────────┘   │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Challenges and Limitations

| Challenge | Severity | Details |
|-----------|----------|---------|
| **Power consumption** | 🔴 High | USB adapters draw significant power; may drain phone battery quickly or not power on at all |
| **USB OTG support** | 🟡 Medium | Not all phones implement USB OTG correctly; power provisioning varies |
| **Driver availability** | 🔴 High | Only ALFA AWUS036H has a working Android user-space driver |
| **Simultaneous charging** | 🔴 High | Cannot charge phone while using USB adapter (unless using Y-cable + battery pack) |
| **Physical form factor** | 🟡 Medium | Awkward to carry phone + adapter + cables |
| **Heat generation** | 🟡 Medium | Continuous scanning generates heat |
| **App limitations** | 🟡 Medium | Cannot run Linux tools (airodump-ng); must implement packet parsing in app |

### Alternative: Rooted Device + Kali NetHunter

For users willing to root their device:

| Capability | Details |
|------------|---------|
| **Full adapter support** | Many more chipsets supported via kernel modules |
| **Native tools** | Can run aircrack-ng, Kismet, etc. |
| **Better integration** | Direct kernel access for monitor mode |
| **Downsides** | Voids warranty, security risks, limited device support |

### Alternative: Pixel 6/6 Pro Internal WiFi Sniffing

Google Pixel 6 devices can enable monitor mode on the **internal** WiFi chip:

| Requirement | Details |
|-------------|---------|
| **Build type** | Must flash `aosp_raven-userdebug` build |
| **Tool** | Uses `wifi_sniffer` utility |
| **Root** | Required |
| **Advantage** | No external hardware needed |
| **Limitation** | Only works on Pixel 6 series |

### Feasibility Assessment for USB Adapter Approach

| Factor | Rating | Notes |
|--------|--------|-------|
| **Technical feasibility** | ⚠️ MEDIUM | Proven possible but limited adapter support |
| **User experience** | 🔴 LOW | Cables, power issues, awkward form factor |
| **Development effort** | 🔴 HIGH | Must port drivers for each chipset |
| **Target audience** | ⚠️ NARROW | Only for technical users willing to carry hardware |
| **Detection capability** | ✅ HIGH | Full probe request capture when working |

### Recommendation for USB Adapter Support

**Include as Optional Advanced Feature**:

1. **Phase 1**: Build core app with Bluetooth-only detection (works for all users)
2. **Phase 2**: Add optional USB adapter support for power users
   - Start with ALFA AWUS036H (existing driver)
   - Port additional chipset drivers over time
3. **UI/UX**: Clearly communicate that USB adapter is optional enhancement, not required

### Automatic Tier Switching Design

The app should seamlessly upgrade from Tier 1 to Tier 2 when a compatible USB WiFi adapter is plugged in, with no user intervention required.

#### USB Detection Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    AUTOMATIC TIER SWITCHING                              │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  TIER 1 (Default)                    TIER 2 (When Adapter Connected)    │
│  ┌─────────────────────┐             ┌─────────────────────┐            │
│  │ • Bluetooth scanning│             │ • Bluetooth scanning│            │
│  │ • WiFi AP detection │   USB      │ • WiFi AP detection │            │
│  │ • Cellular survey   │  Plug-in    │ • Cellular survey   │            │
│  │ • GPS correlation   │ ─────────► │ • GPS correlation   │            │
│  │                     │             │ • PROBE REQUESTS    │            │
│  │                     │  USB       │ • RAW 802.11 FRAMES │            │
│  │                     │ ◄───────── │ • DEVICE FINGERPRINT│            │
│  └─────────────────────┘  Unplug     └─────────────────────┘            │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

#### Android USB Host API Implementation

```kotlin
// Detect USB adapter connection using Android's USB Host API
class UsbAdapterManager(context: Context) {

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

    // ALFA AWUS036H identifiers
    companion object {
        const val ALFA_VENDOR_ID = 0x0bda   // Realtek
        const val ALFA_PRODUCT_ID = 0x8187  // RTL8187
    }

    // BroadcastReceiver for USB events
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    if (isCompatibleAdapter(device)) {
                        onAdapterConnected(device)
                    }
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    onAdapterDisconnected()
                }
            }
        }
    }

    fun isCompatibleAdapter(device: UsbDevice?): Boolean {
        return device?.vendorId == ALFA_VENDOR_ID &&
               device?.productId == ALFA_PRODUCT_ID
    }

    private fun onAdapterConnected(device: UsbDevice) {
        // 1. Request USB permission if needed
        // 2. Initialize user-space driver (liber80211-style)
        // 3. Start monitor mode
        // 4. Notify detection engine to enable Tier 2 features
        DetectionEngine.enableTier2(device)
    }

    private fun onAdapterDisconnected() {
        // Gracefully fall back to Tier 1
        DetectionEngine.disableTier2()
    }
}
```

#### AndroidManifest.xml USB Configuration

```xml
<!-- Declare USB host feature -->
<uses-feature android:name="android.hardware.usb.host" android:required="false" />

<!-- USB device filter for auto-launch when adapter plugged in -->
<activity android:name=".MainActivity">
    <intent-filter>
        <action android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" />
    </intent-filter>
    <meta-data
        android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED"
        android:resource="@xml/usb_device_filter" />
</activity>
```

#### res/xml/usb_device_filter.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- ALFA AWUS036H (RTL8187) -->
    <usb-device vendor-id="3034" product-id="33159" />

    <!-- Future: Add more adapters as drivers are ported -->
    <!-- ALFA AWUS036NH (AR9271) -->
    <!-- <usb-device vendor-id="..." product-id="..." /> -->
</resources>
```

#### User Experience Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         USER EXPERIENCE FLOW                             │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  1. APP LAUNCH (No adapter)                                             │
│     ┌─────────────────────────────────────────┐                         │
│     │  🔵 Tracking Detection Active           │                         │
│     │  Mode: Standard (Bluetooth + WiFi AP)   │                         │
│     │                                         │                         │
│     │  [Scanning...]                          │                         │
│     └─────────────────────────────────────────┘                         │
│                                                                          │
│  2. USER PLUGS IN ALFA ADAPTER                                          │
│     ┌─────────────────────────────────────────┐                         │
│     │  🟢 USB Adapter Detected!               │                         │
│     │  ALFA AWUS036H connected                │                         │
│     │                                         │                         │
│     │  [Allow USB Access?]  [Yes] [No]        │                         │
│     └─────────────────────────────────────────┘                         │
│                                                                          │
│  3. ENHANCED MODE ACTIVE                                                │
│     ┌─────────────────────────────────────────┐                         │
│     │  🟢 Tracking Detection Active           │                         │
│     │  Mode: Enhanced (Full WiFi Monitor)     │                         │
│     │  ✓ Probe request capture enabled        │                         │
│     │  ✓ Device fingerprinting active         │                         │
│     │                                         │                         │
│     │  [Scanning... 47 devices detected]      │                         │
│     └─────────────────────────────────────────┘                         │
│                                                                          │
│  4. ADAPTER UNPLUGGED                                                   │
│     ┌─────────────────────────────────────────┐                         │
│     │  🔵 USB Adapter Disconnected            │                         │
│     │  Reverting to Standard mode...          │                         │
│     │  WiFi probe data preserved in database  │                         │
│     └─────────────────────────────────────────┘                         │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

#### Detection Engine Mode Handling

```kotlin
object DetectionEngine {

    enum class Tier { STANDARD, ENHANCED }

    private var currentTier = Tier.STANDARD
    private var usbDriver: Usb80211Driver? = null

    // Data sources always active (Tier 1)
    private val bluetoothScanner = BluetoothScanner()
    private val wifiApScanner = WifiApScanner()
    private val cellularScanner = CellularScanner()
    private val gpsTracker = GpsTracker()

    // Data source only active in Tier 2
    private var probeRequestScanner: ProbeRequestScanner? = null

    fun enableTier2(usbDevice: UsbDevice) {
        usbDriver = Usb80211Driver(usbDevice)
        usbDriver?.initialize()
        usbDriver?.enableMonitorMode()

        probeRequestScanner = ProbeRequestScanner(usbDriver!!)
        probeRequestScanner?.start()

        currentTier = Tier.ENHANCED
        notifyUiModeChanged(Tier.ENHANCED)
    }

    fun disableTier2() {
        probeRequestScanner?.stop()
        probeRequestScanner = null

        usbDriver?.close()
        usbDriver = null

        currentTier = Tier.STANDARD
        notifyUiModeChanged(Tier.STANDARD)

        // Note: Historical probe data remains in database for analysis
    }

    fun analyzeDevice(mac: String): ThreatScore {
        return when (currentTier) {
            Tier.STANDARD -> analyzeWithBluetoothAndAP(mac)
            Tier.ENHANCED -> analyzeWithFullWifiData(mac)
        }
    }
}
```

#### Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| **No manual mode switch** | User shouldn't have to configure anything - plug and play |
| **Graceful degradation** | Unplugging adapter doesn't lose data or crash app |
| **Permission prompt once** | Android remembers USB permission for the device |
| **Preserve historical data** | Probe data collected in Tier 2 remains useful after adapter removed |
| **UI feedback** | Clear indication of current mode so user knows what's active |
| **Auto-launch option** | App can auto-start when adapter plugged in (optional) |

### Hardware Cost Estimate

| Item | Price (USD) |
|------|-------------|
| ALFA AWUS036H | $30-40 |
| USB OTG cable | $5-10 |
| USB Y-cable (for power) | $10-15 |
| Portable battery pack | $20-30 |
| **Total** | **$65-95** |

---

## Feasibility by Detection Method

### ✅ HIGHLY FEASIBLE: Bluetooth Device Tracking

Android provides robust Bluetooth scanning APIs that can detect nearby devices.

| Capability | Android Support | API |
|------------|-----------------|-----|
| Scan for BLE devices | ✅ Full | BluetoothLeScanner |
| Scan for Classic Bluetooth | ✅ Full | BluetoothAdapter |
| Get device MAC addresses | ✅ Full | BluetoothDevice |
| Get signal strength (RSSI) | ✅ Full | ScanResult |
| Background scanning | ⚠️ Limited | Requires foreground service |

**Detection Algorithm Feasibility**:
- ✅ Track same Bluetooth MAC addresses over time
- ✅ Correlate device appearances with GPS locations
- ✅ Implement persistence scoring (CYT's 5/10/15/20 min windows)
- ✅ Detect devices following across multiple locations

**Limitations**:
- Modern phones use MAC randomization (changes every ~15 minutes)
- Dedicated tracking devices (AirTags, Tiles) often have static MACs
- Many cars/IoT devices have static Bluetooth MACs

### ⚠️ PARTIALLY FEASIBLE: WiFi Access Point Correlation

Android can see nearby WiFi networks but NOT client devices.

| Capability | Android Support | Notes |
|------------|-----------------|-------|
| Scan nearby access points | ✅ Full | WifiManager.getScanResults() |
| Get AP MAC (BSSID) | ✅ Full | Identifies specific routers |
| Get signal strength | ✅ Full | RSSI values |
| See client devices | ❌ No | Cannot detect phones/laptops |
| See probe requests | ❌ No | Core CYT feature unavailable |

**Alternative Approach - Portable Hotspot Detection**:
- Detect if the same mobile hotspot SSID/BSSID appears repeatedly
- Some followers might use portable WiFi devices with static identifiers
- Limited but provides additional signal

### ✅ HIGHLY FEASIBLE: Cellular Network Survey

Directly portable from android-network-survey.

| Capability | Android Support |
|------------|-----------------|
| Cell tower identification | ✅ Full |
| Signal strength logging | ✅ Full |
| Network type detection | ✅ Full |
| Location correlation | ✅ Full |

### ✅ HIGHLY FEASIBLE: GPS/Location Tracking

Required for spatial correlation algorithms.

| Capability | Android Support |
|------------|-----------------|
| High-accuracy GPS | ✅ Full |
| Background location | ⚠️ Requires permissions |
| Location history | ✅ Can implement |
| Geofencing | ✅ Full |

---

## Proposed Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    TRACKING DETECTION APP                        │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │  Bluetooth  │  │   WiFi AP   │  │  Cellular   │   BUILT-IN   │
│  │   Scanner   │  │   Scanner   │  │   Scanner   │   SCANNERS   │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘              │
│         │                │                │                      │
│         │    ┌───────────────────────┐    │                      │
│         │    │  USB WiFi Adapter     │    │    OPTIONAL          │
│         │    │  (Monitor Mode)       │    │    HARDWARE          │
│         │    │  - Probe Requests     │    │                      │
│         │    │  - Raw 802.11 Frames  │    │                      │
│         │    └───────────┬───────────┘    │                      │
│         │                │                │                      │
│         └────────────────┼────────────────┘                      │
│                          ▼                                       │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                   DEVICE DATABASE                            ││
│  │  - MAC addresses, timestamps, GPS coordinates                ││
│  │  - Signal strength history                                   ││
│  │  - Device appearance frequency                               ││
│  │  - Probe request patterns (when USB adapter connected)       ││
│  └─────────────────────────────────────────────────────────────┘│
│                          │                                       │
│                          ▼                                       │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │              TRACKING DETECTION ENGINE                       ││
│  │  - Temporal analysis (CYT-style time windows)               ││
│  │  - Spatial correlation (location clustering)                ││
│  │  - Persistence scoring (0-1.0 threat level)                 ││
│  │  - Pattern anomaly detection                                 ││
│  │  - WiFi probe fingerprinting (with USB adapter)             ││
│  └─────────────────────────────────────────────────────────────┘│
│                          │                                       │
│                          ▼                                       │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                    ALERT SYSTEM                              ││
│  │  - Real-time notifications                                  ││
│  │  - Threat level visualization                               ││
│  │  - Device history review                                    ││
│  └─────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
```

---

## Implementation Approach

### Option A: Fork android-network-survey (Recommended)

**Advantages**:
- Already handles Android permissions correctly
- Bluetooth and WiFi scanning infrastructure exists
- GeoPackage/location correlation already implemented
- Apache 2.0 license allows modification
- Mature, tested codebase

**Work Required**:
- Add tracking detection engine (port CYT algorithms)
- Add device persistence database
- Implement alert system
- UI for threat visualization

**Estimated Complexity**: Medium-High

### Option B: Build from Scratch

**Advantages**:
- Cleaner architecture focused solely on tracking detection
- Smaller app footprint

**Disadvantages**:
- Must solve all Android permission challenges
- More development time
- Reinventing existing solutions

**Estimated Complexity**: High

### Option C: Companion Device Approach

Use Android phone + external WiFi monitor device.

**Implementation**:
- Raspberry Pi Zero W with monitor-mode capable adapter
- Runs actual CYT or similar
- Communicates with Android app via Bluetooth/WiFi
- Android app provides UI and GPS

**Advantages**:
- Full CYT functionality possible
- True probe request capture

**Disadvantages**:
- Requires additional hardware (~$50-100)
- More complex setup
- Less convenient for users

---

## Feasibility Summary

| Feature | Feasibility | Notes |
|---------|-------------|-------|
| Bluetooth tracking detection | ✅ **HIGH** | Best detection vector on Android |
| WiFi probe request analysis (native) | ❌ **NOT POSSIBLE** | Android API limitation |
| WiFi probe requests (USB adapter) | ⚠️ **MEDIUM** | Requires external hardware + driver work |
| WiFi AP correlation | ⚠️ **MEDIUM** | Hotspot detection only |
| Cellular survey | ✅ **HIGH** | Direct port possible |
| GPS location correlation | ✅ **HIGH** | Standard Android API |
| CYT-style algorithms | ✅ **HIGH** | Portable to Kotlin/Java |
| Real-time alerts | ✅ **HIGH** | Standard Android notifications |

### Overall Assessment: **FEASIBLE WITH LIMITATIONS**

A useful tracking detection app IS feasible on Android, but with these caveats:

1. **Primary detection via Bluetooth** instead of WiFi probe requests
2. **Supplementary detection** via WiFi AP patterns (hotspots)
3. **Cannot detect** phones/laptops that aren't broadcasting Bluetooth
4. **Most effective against**: AirTags, Tiles, Bluetooth beacons, IoT devices, vehicles with Bluetooth, dedicated tracking hardware

---

## Recommended Development Path

### Phase 1: Core Infrastructure
- Fork android-network-survey
- Implement device persistence database
- Add background scanning service

### Phase 2: Detection Engine
- Port CYT temporal analysis algorithms
- Implement spatial correlation with GPS
- Build persistence scoring system

### Phase 3: User Interface
- Threat level dashboard
- Device history viewer
- Real-time alerts and notifications
- Map visualization of suspicious devices

### Phase 4: Enhancements
- Machine learning for pattern detection
- Integration with Apple AirTag detection (Android's built-in)
- Export/reporting functionality

---

## Legal and Privacy Considerations

1. **User Consent**: App only monitors devices near the user, not others
2. **Data Storage**: All data stored locally on device
3. **No Tracking Others**: App detects if user IS BEING tracked, doesn't enable tracking
4. **Permissions**: Must clearly explain why Bluetooth/Location permissions needed
5. **Google Play Policy**: Review requirements for background location access

---

## Conclusion

Building an Android tracking detection app is **feasible and valuable**. There are two tiers of functionality:

### Tier 1: Native Android (All Users)
- **Bluetooth-centric detection** provides meaningful protection against common tracking threats (AirTags, Tiles, vehicle Bluetooth, dedicated trackers)
- CYT's temporal and spatial analysis algorithms can be directly ported
- Works on any modern Android phone without additional hardware

### Tier 2: Enhanced Detection (Power Users)
- **External USB WiFi adapter** enables full probe request capture
- Replicates core CYT functionality on Android
- Requires ~$65-95 in hardware and technical comfort with cables/adapters
- Limited adapter support (primarily ALFA AWUS036H currently)

### Recommended Approach

**Fork android-network-survey** and implement in phases:
1. Core Bluetooth detection (reaches all users)
2. Optional USB adapter support (for users who want maximum detection capability)
3. Port CYT algorithms for both detection vectors

This hybrid approach provides value to casual users while offering full capability to those willing to invest in additional hardware.

---

## References

- [android-network-survey](https://github.com/christianrowlands/android-network-survey) - Android network scanning reference implementation
- [Chasing Your Tail NG](https://github.com/ArgeliusLabs/Chasing-Your-Tail-NG) - Tracking detection algorithms
- [liber80211](https://github.com/brycethomas/liber80211) - Proof of concept for Android monitor mode without root
- [USB WiFi Adapters for Monitor Mode](https://www.cellstream.com/2024/03/25/a-list-of-usb-wi-fi-adapters-that-support-monitor-mode/) - Compatible adapter list
- [Kali NetHunter](https://www.kali.org/docs/nethunter/) - Rooted Android security platform
