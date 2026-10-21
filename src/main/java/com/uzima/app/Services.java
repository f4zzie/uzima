package com.uzima.app;

import com.uzima.pricing.PricingEngine;
import com.uzima.service.AuditLog;
import com.uzima.service.AuthService;
import com.uzima.service.CatalogService;
import com.uzima.service.ClientService;
import com.uzima.service.JobService;
import com.uzima.service.PaymentService;
import com.uzima.service.Session;
import com.uzima.service.SettingsService;
import com.uzima.service.UserService;
import java.time.Clock;

/**
 * The use cases the UI calls. Built once by {@link CompositionRoot}. {@code clock} is the same
 * clock the services use (Africa/Nairobi), so screens default dates such as "today" consistently.
 */
public record Services(
    Session session,
    AuthService auth,
    UserService users,
    SettingsService settings,
    ClientService clients,
    CatalogService catalog,
    JobService jobs,
    PaymentService payments,
    AuditLog audit,
    PricingEngine pricing,
    Clock clock) {}
