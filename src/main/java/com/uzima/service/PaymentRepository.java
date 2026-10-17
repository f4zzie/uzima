package com.uzima.service;

import com.uzima.domain.payment.Payment;
import java.util.List;
import java.util.Optional;

/**
 * The append-only payment ledger. There are deliberately no update or delete methods: mistakes are
 * corrected with a {@link Payment.Reversal}.
 */
public interface PaymentRepository {

  /** Saves a payment or reversal (id 0) and returns it with its generated id. */
  Payment insert(Payment payment);

  /** A job's ledger ordered by payment date, then entry order. */
  List<Payment> findByJob(long jobId);

  Optional<Payment> findById(long id);
}
