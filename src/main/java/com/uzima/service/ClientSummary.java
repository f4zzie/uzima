package com.uzima.service;

import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.client.ClientCode;
import com.uzima.domain.client.County;
import com.uzima.domain.client.PhoneNumber;

/** One row of the clients table screen. */
public record ClientSummary(
    long id,
    ClientCode code,
    String name,
    PhoneNumber phone,
    ClientCategory category,
    County county,
    boolean active) {}
