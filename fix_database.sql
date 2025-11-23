-- Fix for invoice submission error
-- This adds the depth_height_charge column if it doesn't exist

USE javalearning;

-- Add the depth_height_charge column if missing
--ALTER TABLE invoices 
--ADD COLUMN IF NOT EXISTS depth_height_charge DECIMAL(10, 2) DEFAULT 0.00 
--AFTER tank_cost;

ALTER TABLE service_requests
ADD COLUMN IF NOT EXISTS scheduled_date TIMESTAMP NULL AFTER status;

-- Verify the column was added
DESCRIBE invoices;

SELECT 'Database fix completed successfully!' as Status;
