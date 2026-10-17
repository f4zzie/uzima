package com.uzima.service;

import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.client.ClientCode;
import com.uzima.domain.job.JobNumber;
import com.uzima.domain.payment.ReceiptNumber;
import java.time.Year;

/**
 * Issues document numbers. Must be called inside {@link Transactions#inTransaction}: the counter
 * row stays locked until the transaction ends, and a rollback returns the number, so numbers are
 * unique and gap-free.
 */
public interface SequenceGenerator {

  ClientCode nextClientCode(ClientCategory category);

  JobNumber nextJobNumber(Year year);

  ReceiptNumber nextReceiptNumber(Year year);
}
