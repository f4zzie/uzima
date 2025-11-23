# UZIMA UI Color Guide & Features

## 🎨 Admin Dashboard - Search, Sort & Filter

```
┌──────────────────────────────────────────────────────────────────────────┐
│  Admin Dashboard - Client Management                          [🚪 Logout] │
├──────────────────────────────────────────────────────────────────────────┤
│  Filter Panel (Light Gray Background #F0F2F5)                             │
│  ┌────────────────────────────────────────────────────────────────────┐  │
│  │ 🔍 Search: [________________]  🔤 Sort: [Name (A-Z)      ▼]       │  │
│  │ 📂 Category: [All Categories  ▼]                                   │  │
│  └────────────────────────────────────────────────────────────────────┘  │
│                                                                            │
│  Data Table (Professional Styling)                                        │
│  ┌───┬──────────────┬─────────────────────┬──────────┬────────┬────────┐│
│  │ID │     Name     │    Email            │  Phone   │ County │ Category││
│  │   │ (Segoe UI)   │   (Professional)    │          │        │        ││
│  ├───┼──────────────┼─────────────────────┼──────────┼────────┼────────┤│
│  │1  │John Doe      │john@example.com     │555-1234  │Nairobi │Borehole││
│  │2  │Sarah Smith   │sarah@example.com    │555-5678  │Mombasa │Pump    ││
│  │3  │Mike Johnson  │mike@example.com     │555-9999  │Kisumu  │Plumbing││
│  └───┴──────────────┴─────────────────────┴──────────┴────────┴────────┘│
│                                                                            │
│  Bottom Control Panel (Light Gray Background)                             │
│  ┌────────────────────────────────────────────────────────────────────┐  │
│  │  [🔄 Refresh]  [✖ Clear All]  [💾 Export]                       │  │
│  └────────────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────────┘
```

### Search Features
- **Real-time**: Updates as you type
- **Case-insensitive**: "John" = "john" = "JOHN"
- **Multiple fields**: Name, Email, Phone, County
- **Instant results**: No refresh needed

### Sort Options
```
🔤 Sort By: ▼
├─ Name (A-Z)          → Ascending alphabetical
├─ Name (Z-A)          → Descending alphabetical  
├─ Email (A-Z)         → By email address
├─ ID (Ascending)      → Lowest ID first
├─ ID (Descending)     → Highest ID first
└─ County              → By location/county
```

### Filter Categories
```
📂 Category: ▼
├─ All                 → All clients
├─ Borehole            → Borehole drilling services
├─ Pump Installation   → Pump installation services
├─ Plumbing            → Plumbing services
└─ Tank Installation   → Tank installation services
```

---

## 🎯 Service Request Wizard - Button Colors

```
Step 1 of 7                            [✖ Cancel] [← Prev] [Next →]
                                          Red      Blue     Green
```

### Button Styling

**CANCEL Button** 🔴
```
Background: #F44336 (Red)
Hover:      #D32F2F (Dark Red)
Text:       White
Font:       Bold Segoe UI 12px
Icon:       ✖
Cursor:     Hand pointer
```

**PREVIOUS Button** 🔵
```
Background: #6496C8 (Light Blue)
Hover:      #4A78B4 (Darker Blue)
Text:       White
Font:       Bold Segoe UI 12px
Icon:       ←
Cursor:     Hand pointer
```

**NEXT Button** 🟢
```
Background: #4CAF50 (Green)
Hover:      #45A049 (Darker Green)
Text:       White
Font:       Bold Segoe UI 12px
Icon:       →
Cursor:     Hand pointer
```

### Interactive Features
- Smooth hover color transition
- Darkens by ~15-20 RGB values on hover
- Hand cursor on mouse-over
- Clear visual feedback for user actions

---

## 🌈 Main Menu - Card Colors

```
┌─ MAIN MENU ────────────────────────────────────────────┐
│                                                         │
│  Welcome, [Client Name]                                │
│  UZIMA Borehole Drilling System - Main Menu            │
│  ─────────────────────────────────────────────         │
│                                                         │
│  ┌──────────────────┐  ┌──────────────────┐           │
│  │ 📋 REQUEST       │  │ 📄 VIEW          │           │
│  │ SERVICE          │  │ INVOICES         │           │
│  │ Create new       │  │ View your        │           │
│  │ request          │  │ invoices         │           │
│  │ #4CAF50 (Green)  │  │ #2196F3 (Blue)   │           │
│  └──────────────────┘  └──────────────────┘           │
│                                                         │
│  ┌──────────────────┐  ┌──────────────────┐           │
│  │ 🔧 VIEW          │  │ 📊 VIEW          │           │
│  │ SERVICES         │  │ REPORTS          │           │
│  │ Browse services  │  │ View reports     │           │
│  │ #FF9800 (Orange) │  │ #9C27B0 (Purple) │           │
│  └──────────────────┘  └──────────────────┘           │
│                                                         │
│  ┌──────────────────┐  ┌──────────────────┐           │
│  │ ⚙️ SETTINGS      │  │ 🚪 LOGOUT        │           │
│  │ Profile & prefs  │  │ Sign out         │           │
│  │ #009688 (Teal)   │  │ #F44336 (Red)    │           │
│  └──────────────────┘  └──────────────────┘           │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### Color Meanings

| Color | RGB | Meaning | Used For |
|-------|-----|---------|----------|
| 🟢 Green | #4CAF50 | Action, Go | Request Service |
| 🔵 Blue | #2196F3 | Trust, Info | View Invoices |
| 🟠 Orange | #FF9800 | Attention | View Services |
| 🟣 Purple | #9C27B0 | Premium, Data | View Reports |
| 🟦 Teal | #009688 | Customize | Settings |
| 🔴 Red | #F44336 | Stop, Logout | Logout |
| ⚫ Dark | #0C2D61 | Headers | Titles |

---

## 📊 Admin Table Styling

### Header (Dark Blue #0C2D61)
```
┌────┬────────────┬──────────────────────┬──────────┬────────┬────────┐
│ ID │    Name    │       Email          │  Phone   │ County │Category│
│    │(White Text)│   (White Text 12px)  │          │        │        │
│    │ Bold Font  │   Bold Segoe UI      │          │        │        │
└────┴────────────┴──────────────────────┴──────────┴────────┴────────┘
```

### Data Rows
```
Each Row: 28px height for readability
Font: Segoe UI 11px, Dark Gray text
Grid: Light gray separators (#C8C8C8)
Alternating: Optional row backgrounds
```

### Column Widths
```
ID:       50px  │ Compact
Name:    150px  │ Full names
Email:   180px  │ Long email addresses
Phone:   120px  │ Phone numbers
County:  100px  │ Location
Category:120px  │ Service type
```

---

## 🎯 Filter Panel Layout

```
Light Gray Background (#F0F2F5)
Padding: 15px all sides
Border: Empty border for spacing

Row 1:
┌─ 🔍 Search Field ────────────────────────────────────┐
│ [________________ (Text Input, 20 chars width)      │
└──────────────────────────────────────────────────────┘

Row 2:
┌─ 🔤 Sort By ─────────────────────────────────────────┐
│ [Name (A-Z)                                         ▼]│
└──────────────────────────────────────────────────────┘

Row 3:
┌─ 📂 Category ─────────────────────────────────────────┐
│ [All Categories                                     ▼]│
└──────────────────────────────────────────────────────┘
```

---

## 🚀 Interactive Features

### Mouse Hover Effects

**Buttons:**
```
Normal State:    Solid Color
On Hover:        Darken by ~20 RGB values
Visual Effect:   Smooth transition
Cursor:          Hand pointer (👆)
```

**Table Rows:**
```
Normal:    White background
On Hover:  Optional row highlight (future)
```

**Admin Cards (Main Menu):**
```
Normal:    Solid color, border
On Hover:  Raised effect, slightly larger
Cursor:    Hand pointer
```

---

## 📱 Responsive Design Notes

- **Min Width**: 800px (recommended)
- **Table Scrollable**: On smaller screens
- **Button Layout**: Wraps on narrow screens
- **Filter Panel**: Responsive grid layout

---

## ✨ Visual Hierarchy

1. **Title**: Bold 22px, Dark Blue
2. **Labels**: Bold 11px, Dark Blue
3. **Data**: Regular 11px, Dark Gray
4. **Helpers**: Regular 12px, Gray

---

## 🎨 Font Stack
```
Primary Font: Segoe UI
Fallback: Sans-serif
Sizes: 11px (body), 12px (labels), 20px+ (titles)
Weights: Normal, Bold
```

---

**Last Updated**: November 23, 2025
**Status**: ✅ Implemented & Tested
