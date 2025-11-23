# Database Fix Instructions

## Issue
When submitting a service request invoice, you may encounter an error related to the `depth_height_charge` column.

## Quick Fix

### Option 1: Run the fix script (Recommended)
```bash
mysql -u root -pteddy < fix_database.sql
```

### Option 2: Recreate the entire database
```bash
mysql -u root -pteddy < database_schema.sql
```

### Option 3: Manual SQL command
```sql
USE javalearning;

ALTER TABLE invoices 
ADD COLUMN IF NOT EXISTS depth_height_charge DECIMAL(10, 2) DEFAULT 0.00 
AFTER tank_cost;

ALTER TABLE service_requests
ADD COLUMN IF NOT EXISTS scheduled_date TIMESTAMP NULL AFTER status;
```

## Verification

After running the fix, verify the column was added:

```bash
mysql -u root -pteddy javalearning -e "DESCRIBE invoices;"
```

You should see `depth_height_charge` in the column list.

## What This Fixes

- ✅ Invoice submission errors
- ✅ Service request completion
- ✅ Invoice preview and calculation

## After the Fix

1. Restart the application if it's running
2. Try submitting a service request again
3. The invoice should now save successfully with the generated Invoice ID

## Test Credentials

After fixing the database, you can test with these accounts:

### Admin Account
- Email: admin@uzima.com
- Password: password123

### Test Client Accounts
- Email: john.kamau@test.com
- Password: password123
- Category: Individual

- Email: mary.wanjiku@test.com
- Password: password123
- Category: Commercial

- Email: peter.ochieng@test.com
- Password: password123
- Category: NGO

## Need Help?

If the error persists:
1. Check the console output for specific error messages
2. Verify MySQL is running: `sudo systemctl status mysql`
3. Verify database credentials in DatabaseConnection.java
4. Check that the database exists: `mysql -u root -pteddy -e "SHOW DATABASES;"`
