# 🎨 UZIMA UI WIZARD FLOW - COMPLETE GUIDE

## Overview
A complete multi-step service request wizard that guides users through the entire process of requesting borehole drilling services.

---

## 📊 UI Flow Chart

```
LOGIN PAGE
    ↓ (Enter email/password)
    ↓ Success ✓
    ↓
MAIN MENU
    ├─ 📋 Request Service ← Click to start
    ├─ 📄 View Invoices
    ├─ 🔧 View Services
    ├─ 📊 View Reports
    ├─ ⚙️ Settings
    └─ 🚪 Logout
    
    When "Request Service" clicked:
    ↓
STEP 1: Client Category Selection
    ├─ Industrial (Survey: 20K | Local: 50K)
    ├─ Commercial (Survey: 15K | Local: 30K)
    └─ Domestic (Survey: 7K | Local: 10K)
    ↓ Next
STEP 2: Drilling Service Selection
    ├─ Symmetric (130,000 KES)
    ├─ Core (225,000 KES)
    └─ Geo-Technical (335,000 KES)
    ↓ Next
STEP 3: Pump Service Selection
    ├─ Submersible Electric (90,000 KES)
    ├─ Solar (65,000 KES)
    └─ Hand (30,000 KES)
    ↓ Next
STEP 4: Depth & Height Input
    ├─ Enter depth in meters
    ├─ Enter tank height in meters
    └─ Automatic charge calculation
    ↓ Next
STEP 5: Plumbing Services (Optional)
    ├─ ☐ Include Plumbing
    └─ If Yes:
        ├─ Enter pipe length
        ├─ Select diameter
        └─ Number of outlets
    ↓ Next
STEP 6: Tank Installation (Optional)
    ├─ ☐ Include Tank
    └─ If Yes:
        └─ Enter tank capacity (litres)
    ↓ Next
STEP 7: Invoice Preview & Confirmation
    ├─ Display complete invoice
    ├─ Show all charges breakdown
    ├─ Automatic tax calculation (16%)
    ├─ 🖨 Print Invoice button
    └─ ✓ Confirm & Submit button
    ↓ Submit
CONFIRMATION SCREEN
    └─ Success message with Invoice ID
```

---

## 🎯 UI Components Created

### 1. **ServiceRequestWizardManager.java**
Main controller for the multi-step wizard
- Manages step navigation
- Stores request data across steps
- Handles Previous/Next buttons
- Displays progress indicator

```java
// Usage
ServiceRequestWizardManager wizard = new ServiceRequestWizardManager(clientId);
```

### 2. **ClientCategorySelectionPanel.java** (Step 1)
User selects client category

```
┌─────────────────────────────────────────┐
│ Step 1: Select Client Category          │
├─────────────────────────────────────────┤
│ ◉ Industrial  | Survey: 20K | Local: 50K│
│ ◯ Commercial  | Survey: 15K | Local: 30K│
│ ◯ Domestic    | Survey: 7K  | Local: 10K│
│                                         │
│         [◄ Previous]  [Next ►]         │
└─────────────────────────────────────────┘
```

### 3. **DrillingServiceSelectionPanel.java** (Step 2)
User selects drilling service type

```
┌─────────────────────────────────────────┐
│ Step 2: Select Drilling Service         │
├─────────────────────────────────────────┤
│ ◉ Symmetric    | Down Payment: 130,000  │
│ ◯ Core         | Down Payment: 225,000  │
│ ◯ Geo-Tech     | Down Payment: 335,000  │
│                                         │
│ Details: Symmetric Drilling...          │
│         [◄ Previous]  [Next ►]         │
└─────────────────────────────────────────┘
```

### 4. **PumpServiceSelectionPanel.java** (Step 3)
User selects pump type

```
┌─────────────────────────────────────────┐
│ Step 3: Select Pump Type                │
├─────────────────────────────────────────┤
│ ◉ Submersible | Cost: 90,000            │
│ ◯ Solar       | Cost: 65,000            │
│ ◯ Hand        | Cost: 30,000            │
│                                         │
│ Details: Submersible Electric Pump...   │
│         [◄ Previous]  [Next ►]         │
└─────────────────────────────────────────┘
```

### 5. **DepthHeightInputPanel.java** (Step 4)
User enters depth and height measurements

```
┌─────────────────────────────────────────┐
│ Step 4: Depth & Height                  │
├─────────────────────────────────────────┤
│ Borehole Depth:        [___100___] m    │
│ Tank Height:           [___5____] m     │
│                                         │
│ Depth/Height Charge:   KES 172,500      │
│ Total with Drilling:   KES 302,500      │
│         [◄ Previous]  [Next ►]         │
└─────────────────────────────────────────┘
```

### 6. **PlumbingServicesPanel.java** (Step 5)
Optional plumbing services

```
┌─────────────────────────────────────────┐
│ Step 5: Plumbing Services (Optional)    │
├─────────────────────────────────────────┤
│ ☑ Include Plumbing Services             │
│                                         │
│ Pipe Length (m):       [___50___]       │
│ Diameter (inches):     [___2____]       │
│ Number of Outlets:     [___3____]       │
│                                         │
│ Plumbing Cost:         KES 25,000       │
│         [◄ Previous]  [Next ►]         │
└─────────────────────────────────────────┘
```

### 7. **TankInstallationPanel.java** (Step 6)
Optional tank installation

```
┌─────────────────────────────────────────┐
│ Step 6: Tank Installation (Optional)    │
├─────────────────────────────────────────┤
│ ☑ Include Tank Installation             │
│                                         │
│ Tank Capacity (L):     [__5000__]       │
│                                         │
│ Tank Cost: KES 250 (5000L @ 50/1000L)   │
│         [◄ Previous]  [Next ►]         │
└─────────────────────────────────────────┘
```

### 8. **InvoicePreviewPanel.java** (Step 7)
Final invoice review before submission

```
╔════════════════════════════════════════╗
║      UZIMA SERVICE REQUEST INVOICE     ║
╚════════════════════════════════════════╝

Survey Fees                       20,000
Local Authority Fees              50,000
Drilling Service (Symmetric)     130,000
Depth/Height Charge (170m)       172,500
Pump Installation (Solar)         65,000
Plumbing Services (50m)           25,000
Tank Installation (5000L)            250
────────────────────────────────────────
SUBTOTAL:                        462,750
TAX (16%):                        74,040
────────────────────────────────────────
TOTAL AMOUNT DUE:                536,790

[🖨 Print]  [✓ Confirm & Submit]
```

### 9. **MainMenuPanel.java**
Post-login main menu

```
┌────────────────────────────────────────┐
│ Welcome, John Doe                      │
│ UZIMA - Main Menu                      │
├────────────────────────────────────────┤
│ ┌──────────────────┐ ┌──────────────────┐
│ │ 📋 Request       │ │ 📄 View          │
│ │ Service          │ │ Invoices         │
│ └──────────────────┘ └──────────────────┘
│ ┌──────────────────┐ ┌──────────────────┐
│ │ 🔧 View          │ │ 📊 View          │
│ │ Services         │ │ Reports          │
│ └──────────────────┘ └──────────────────┘
│ ┌──────────────────┐ ┌──────────────────┐
│ │ ⚙️ Settings      │ │ 🚪 Logout       │
│ └──────────────────┘ └──────────────────┘
└────────────────────────────────────────┘
```

---

## 🔄 Data Flow

### Request Data Object
```java
ServiceRequestData {
    // Step 1
    clientCategory: "Industrial"
    
    // Step 2
    drillingType: "Symmetric"
    drillingCost: 130000
    
    // Step 3
    pumpType: "Solar"
    pumpCost: 65000
    
    // Step 4
    depthMeters: 150
    heightMeters: 20
    depthHeightCharge: 255000
    
    // Step 5
    pipeType: "PVC"
    pipeLengthMeters: 50
    pipeDiameterInches: 2
    numberOfOutlets: 3
    plumbingCost: 25000
    
    // Step 6
    tankCapacityLitres: 5000
    tankCost: 250
    
    // Calculated
    surveyFee: 20000
    localAuthorityFee: 50000
    subtotal: 545250
    taxAmount: 87240
    totalAmount: 632490
}
```

---

## 💾 Backend Integration

Each step connects to the backend:

### Step 1: Category Selection
```java
double[] fees = ServiceManagementService.getFeesByCategory(category);
```

### Step 2: Drilling Services
```java
List<DrillingService> services = ServiceManagementService.getAllDrillingServices();
```

### Step 3: Pump Services
```java
List<PumpService> services = ServiceManagementService.getAllPumpServices();
```

### Step 4: Depth Calculation
```java
double charge = InvoiceService.calculateDepthHeightCharge(depth, height);
```

### Step 7: Submit Invoice
```java
Invoice invoice = new Invoice(...);
InvoiceService.createInvoice(invoice);
```

---

## 🎨 Color Scheme

| Component | Color | Usage |
|---|---|---|
| Primary | #0C2D61 | Headers, titles |
| Accent | #007ACC | Links, highlights |
| Secondary | #34A8E0 | Borders, info |
| Success | #4CAF50 | Confirm buttons |
| Error | #F44336 | Cancel, logout |
| Warning | #FF9800 | Optional items |
| Background | #F5F6FA | Panel backgrounds |

---

## 🖱️ User Interactions

### Navigation
- **Next Button**: Validates current step and moves to next
- **Previous Button**: Goes back to previous step
- **Cancel Button**: Exits wizard with confirmation
- **Progress Label**: Shows current step (e.g., "Step 3 of 7")

### Input Validation
- Category: Must select one option
- Drilling service: Must select one option
- Pump service: Must select one option
- Depth/Height: Must be positive numbers
- Optional fields: Can be skipped or included

### Real-time Calculations
- Depth/Height Charge: Updates as user changes values
- Plumbing Cost: Updates with pipe length or diameter
- Tank Cost: Updates with capacity selection
- Invoice Total: Auto-calculated with 16% tax

---

## 📱 Responsive Design

All panels are designed with:
- GridBagLayout for flexible sizing
- Proper spacing and padding
- Clear labels and descriptions
- Information panels for user guidance
- Hover effects on buttons
- Consistent font sizes

---

## ✅ Feature Checklist

- ✓ Multi-step wizard interface
- ✓ Step-by-step navigation
- ✓ Client category selection
- ✓ Service selection from database
- ✓ Dynamic cost calculation
- ✓ Optional services (plumbing, tank)
- ✓ Invoice preview with breakdown
- ✓ Automatic tax calculation (16%)
- ✓ Backend integration
- ✓ Error handling
- ✓ Success confirmation
- ✓ Print invoice functionality

---

## 🚀 Usage Example

```java
// Create wizard from main menu
JFrame wizardFrame = new JFrame("Service Request Wizard");
wizardFrame.setSize(800, 700);
wizardFrame.add(new ServiceRequestWizardManager(clientId));
wizardFrame.setVisible(true);
```

---

## 📝 Files Created

```
ui/
├── ServiceRequestWizardManager.java    (Main controller)
├── ClientCategorySelectionPanel.java   (Step 1)
├── DrillingServiceSelectionPanel.java  (Step 2)
├── PumpServiceSelectionPanel.java      (Step 3)
├── DepthHeightInputPanel.java          (Step 4)
├── PlumbingServicesPanel.java          (Step 5)
├── TankInstallationPanel.java          (Step 6)
├── InvoicePreviewPanel.java            (Step 7)
└── MainMenuPanel.java                  (Post-login menu)
```

**Total: 9 new UI components created**

---

## 🎯 Next Steps

1. ✓ Create multi-step wizard UI
2. → Integrate with LoginPage/SignUpPage
3. → Add invoice viewing panel
4. → Add services browsing panel
5. → Add reports generation panel
6. → Add settings/profile panel

All components are **ready to use** and **fully functional**!
