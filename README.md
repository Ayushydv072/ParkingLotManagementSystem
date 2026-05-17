# Parking Management System — Java Swing GUI
### Complete source code • All features • Dark-themed UI

---

## Project Structure

```
ParkingSystem/
└── src/
    ├── Main.java                        ← Entry point (run this)
    ├── model/
    │   ├── Vehicle.java                 ← Vehicle entity (number, type, owner)
    │   ├── ParkingSlot.java             ← Slot entity (id, type, status)
    │   ├── ParkingTicket.java           ← Ticket (unique ID, entry/exit, fee)
    │   └── PricingPolicy.java           ← Pricing rules (base + hourly + penalty)
    ├── db/
    │   └── ParkingDatabase.java         ← In-memory singleton DB (22 slots)
    ├── service/
    │   └── ParkingService.java          ← Business logic (park, checkout, payment)
    ├── util/
    │   ├── UITheme.java                 ← Design tokens (colours, fonts, sizes)
    │   └── UIComponents.java            ← Reusable styled widgets
    └── gui/
        ├── MainWindow.java              ← Top-level JFrame with card-layout navigation
        ├── SidebarPanel.java            ← Left navigation sidebar
        ├── DashboardPanel.java          ← Real-time overview + slot map + live table
        ├── EntryPanel.java              ← Vehicle entry form + ticket issuance
        ├── ExitPanel.java               ← Checkout, fee computation, payment & receipt
        └── AdminPanel.java              ← Pricing editor, slot list, daily report
```

---

## How to Compile & Run

### Requirements
- Java 17+ (JDK)

### Compile (from the `src/` directory)
```bash
cd ParkingSystem/src

javac -cp . \
  model/Vehicle.java model/ParkingSlot.java model/ParkingTicket.java model/PricingPolicy.java \
  db/ParkingDatabase.java \
  service/ParkingService.java \
  util/UITheme.java util/UIComponents.java \
  gui/SidebarPanel.java gui/DashboardPanel.java \
  gui/EntryPanel.java gui/ExitPanel.java \
  gui/AdminPanel.java gui/MainWindow.java \
  Main.java
```

### Run
```bash
java -cp . Main
```

---

## Features

### Entry Gate
- Enter vehicle number, owner name, and vehicle type
- System auto-assigns the nearest available slot
- Issues a unique ticket with ID, entry time, and slot number
- Availability indicator updates live for all three vehicle types
- If full → vehicle is added to a **waiting queue** with queue position shown

### Exit Gate
- Look up by **Ticket ID** or **Vehicle Number**
- Calculates parking duration and fee using pricing rules
- Payment options: Cash / Card / UPI-QR
- Generates a complete receipt on payment confirmation
- Slot is freed and waiting-queue vehicle (if any) is auto-assigned

### Dashboard
- Live KPI cards: Total Slots, Available, Occupied, Revenue, Vehicles Today, Queue Size
- **Slot Map**: colour-coded tiles (green = free, red = occupied) with tooltips
- **Active Vehicles Table**: all currently parked vehicles with live duration
- Auto-refreshes every 3 seconds

### Admin Panel
- **Pricing Policies**: edit base charge, hourly rate, and penalty per vehicle type; save instantly
- **Slot Management**: tabular view of all 22 slots with status highlighting; refresh on demand
- **Reports**: today's revenue + count KPIs, full transaction history table

---

## Slot Layout (22 slots total)
| Range    | Type          | Count |
|----------|---------------|-------|
| TW-01…10 | Two-Wheeler   | 10    |
| FW-01…08 | Four-Wheeler  | 8     |
| HV-01…04 | Heavy Vehicle | 4     |

---

## Default Pricing (editable at runtime)
| Vehicle Type   | Base Charge | Hourly Rate | Penalty/Hr (>12h) |
|----------------|-------------|-------------|-------------------|
| Two-Wheeler    | ₹ 10        | ₹ 5         | ₹ 2               |
| Four-Wheeler   | ₹ 20        | ₹ 15        | ₹ 5               |
| Heavy Vehicle  | ₹ 50        | ₹ 30        | ₹ 10              |

First hour is free (base charge only). Each additional started hour is billed. Stays beyond 12 h incur an additional penalty per hour.

---

## UI Design
- **Theme**: Midnight-blue dark palette with amber / teal / green accents
- **Typography**: Segoe UI + Consolas mono
- **Components**: Custom rounded panels, animated nav buttons, styled tables
- **Layout**: Sidebar + card-switching main content area
