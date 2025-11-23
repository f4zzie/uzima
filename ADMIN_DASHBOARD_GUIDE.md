# Admin Dashboard - User Guide

## Quick Start

### 1. Access Admin Dashboard
```
Login Email:    admin@uzima.com
Password:       password123

You will see the Admin Dashboard with:
- Client search & filter panel
- Professional data table
- Control buttons at the bottom
```

---

## 🔍 Search Functionality

### How to Search
1. Go to **Search Field** at top of dashboard
2. Type any text to search for
3. Results update **instantly** as you type

### What Can You Search?
- **Client Names**: "John", "Sarah", "Mike"
- **Email Addresses**: "john@example", "gmail"
- **Phone Numbers**: "555-", "0700"
- **Locations/Counties**: "Nairobi", "Mombasa", "Kisumu"

### Search Examples
```
Search "john"   → Finds all clients with "john" in any field
Search "@gmail" → Finds all Gmail users
Search "555"    → Finds all phones starting with 555
Search "Nairobi"→ Finds all Nairobi-based clients
```

---

## 🔤 Sort Functionality

### How to Sort
1. Click the **Sort By** dropdown
2. Select your preferred sort order
3. Table automatically reorganizes

### Sort Options Explained

**Name (A-Z)**
- Sorts clients alphabetically by full name
- Example: Adam, Barbara, Charles, Diana

**Name (Z-A)**
- Reverse alphabetical order
- Example: Diana, Charles, Barbara, Adam
- Useful: Find clients starting with letters near Z

**Email (A-Z)**
- Sorts by email address alphabetically
- Useful: Finding email patterns or domains

**ID (Ascending)**
- Sorts by Client ID from lowest to highest
- 1, 2, 3, 4, 5...
- Useful: Viewing oldest clients first

**ID (Descending)**
- Sorts by Client ID from highest to lowest
- 5, 4, 3, 2, 1...
- Useful: Viewing newest clients first

**County**
- Sorts alphabetically by location/county
- Kisumu, Mombasa, Nairobi...
- Useful: Geographic organization

---

## 📂 Filter Functionality

### How to Filter
1. Click the **Category** dropdown
2. Select a category to filter by
3. Table shows only matching clients

### Filter Categories

**All** (Default)
- Shows all registered clients
- No filtering applied

**Borehole**
- Clients requesting borehole drilling services
- Drilling equipment selection focus

**Pump Installation**
- Clients requesting pump installation services
- Pump type and capacity focus

**Plumbing**
- Clients requesting plumbing services
- Pipe fitting and water distribution focus

**Tank Installation**
- Clients requesting tank installation services
- Water storage solution focus

### Filter Examples
```
Filter: Borehole        → See only borehole service clients
Filter: Pump            → See only pump installation clients
Filter: Tank            → See only tank installation clients
Filter: All             → See everyone again
```

---

## 🎯 Combining Search + Sort + Filter

### Example 1: Find all Nairobi Borehole Clients
1. **Search**: Type "Nairobi"
2. **Filter**: Select "Borehole"
3. **Sort**: Choose "Name (A-Z)"
4. Result: Nairobi borehole clients, sorted A-Z

### Example 2: Find newest Pump Clients
1. **Search**: (Leave empty for all)
2. **Filter**: Select "Pump Installation"
3. **Sort**: Choose "ID (Descending)"
4. Result: All pump clients, newest first

### Example 3: Find Sarah's Records
1. **Search**: Type "Sarah"
2. **Filter**: Select "All" (any category)
3. **Sort**: Any option
4. Result: All records mentioning Sarah

### Example 4: Find Gmail Users by County
1. **Search**: Type "gmail"
2. **Filter**: Select category (or "All")
3. **Sort**: Choose "County"
4. Result: Gmail users organized by location

---

## 🔘 Dashboard Buttons

### 🔄 Refresh Button (Blue)
**What it does:**
- Reloads all clients from database
- Clears search field
- Resets sort to default
- Resets filter to "All"
- Shows original complete list

**When to use:**
- After another admin adds new clients
- To clear all filters and start over
- To see latest data from database
- If table appears outdated

### ✖ Clear All Button (Purple)
**What it does:**
- Clears search text
- Resets sort dropdown to default
- Resets filter to "All"
- Reloads table with all clients

**When to use:**
- Quick way to remove all filters
- Start fresh filtering
- Alternative to Refresh

### 💾 Export Button (Green)
**What it does:**
- Exports current table data
- Available in future updates
- Will support CSV/PDF formats

**When to use:**
- Generate reports
- Backup client data
- Share client information

### 🚪 Logout Button (Red)
**What it does:**
- Signs out of admin account
- Returns to login screen
- Ends admin session

**When to use:**
- When leaving dashboard
- Switching to another account
- End of work shift

---

## 📊 Table Columns Explained

| Column | Content | Width | Notes |
|--------|---------|-------|-------|
| **ID** | Client Number | 50px | Unique identifier |
| **Name** | Full Name | 150px | Client's complete name |
| **Email** | Email Address | 180px | Contact email |
| **Phone** | Phone Number | 120px | Contact phone |
| **County** | Location | 100px | Geographic location |
| **Category** | Service Type | 120px | What service they need |

---

## 🔧 Advanced Tips

### Tip 1: Multi-Field Search
Search works across multiple fields. Example:
- Search "john@" finds John's email
- Search "555" finds John's phone number
- Search "Nairobi" finds John's location

### Tip 2: Case-Insensitive
Search doesn't care about uppercase/lowercase:
- "john" = "JOHN" = "John"
- Searches are always case-insensitive

### Tip 3: Partial Matches
You can search partial text:
- Search "nar" finds "Leonardo", "Zanary", "Primary"
- Search "com" finds all .com emails

### Tip 4: Sort + Filter Combination
Best combinations for common tasks:

```
TASK: Find newest pump clients in Nairobi
ACTION:
  1. Search: "Nairobi"
  2. Filter: "Pump Installation"
  3. Sort: "ID (Descending)"

TASK: See all drilling clients by name
ACTION:
  1. Search: (empty)
  2. Filter: "Borehole"
  3. Sort: "Name (A-Z)"

TASK: Check tank installation clients by location
ACTION:
  1. Search: (empty)
  2. Filter: "Tank Installation"
  3. Sort: "County"
```

### Tip 5: Page Refresh
The table is fully scrollable. Scroll right to see all columns if screen is narrow.

---

## 🎨 Color Meaning in Dashboard

| Color | Location | Meaning |
|-------|----------|---------|
| 🔵 Dark Blue | Headers, Titles | Primary information |
| ⚪ White | Table background | Data area |
| 🟣 Purple | Panel background | Filter section |
| 🔴 Red | Logout button | Destructive action |
| 🟢 Green | Export button | Positive action |
| 🔵 Blue | Refresh button | Information action |

---

## ⚙️ Settings & Preferences

### Current Admin Account
```
Email:    admin@uzima.com
Password: password123
Role:     System Administrator
```

### Database Connection
- Host: localhost
- Port: 3306
- Database: javalearning
- Tables: clients, services, invoices, etc.

---

## 🐛 Troubleshooting

### Issue: Search returns no results
**Solution:**
- Check spelling of search term
- Try searching different fields (name vs email)
- Clear search and use filter instead

### Issue: Table appears frozen
**Solution:**
- Click "Refresh" button to reload
- Check database connection
- Try logging out and back in

### Issue: Filter not working
**Solution:**
- Make sure database has clients in that category
- Check that there's data in that field
- Click "Clear All" and try again

### Issue: Sort order looks wrong
**Solution:**
- Names sort alphabetically (A comes before B)
- Numbers sort by value (1 before 2)
- Check current sort selection

---

## 📋 Common Workflows

### Daily Admin Check
1. Login with admin@uzima.com
2. Click "Refresh" to see latest clients
3. Check for new registrations
4. Sort by ID (Descending) to see newest
5. Review any errors or missing data

### Generate County Report
1. Click "Sort By" → Select "County"
2. Review clients by location
3. Use "Export" when ready
4. Can save organized data

### Find Specific Client
1. Search for client's name or email
2. Results appear instantly
3. Click table row for details (if feature added)
4. Or note down the information

### Check Service Category Distribution
1. Filter by "Borehole" → Note count
2. Filter by "Pump Installation" → Note count
3. Filter by "Plumbing" → Note count
4. Filter by "Tank Installation" → Note count
5. Calculate totals for reporting

---

## 🎯 What's Coming Next?

Future enhancements to the Admin Dashboard:
- [ ] Click row to view client details
- [ ] Edit client information
- [ ] Delete or deactivate accounts
- [ ] Advanced date range filtering
- [ ] Generate PDF reports
- [ ] Export to Excel/CSV
- [ ] Bulk actions
- [ ] User activity logs
- [ ] Dark mode option
- [ ] Custom filters/saved searches

---

## 📞 Support

For issues or questions:
1. Check this guide first
2. Review the troubleshooting section
3. Check database connection
4. Verify admin login credentials
5. Contact system administrator

---

**Last Updated**: November 23, 2025
**Status**: ✅ Live and Tested
