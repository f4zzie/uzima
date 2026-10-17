package com.uzima.persistence;

import com.uzima.domain.payment.Payment;
import com.uzima.domain.payment.PaymentMethod;
import com.uzima.domain.payment.ReceiptNumber;
import com.uzima.service.PaymentRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

/**
 * The payment ledger. Insert and select only: rows are never updated or deleted. Received payments
 * have {@code reversal_of} NULL and a positive amount; reversals point at the payment they reverse
 * and carry a negative amount (also enforced by a CHECK constraint).
 */
public final class JdbcPaymentRepository implements PaymentRepository {

  private static final String COLUMNS =
      "id, receipt_number, job_id, amount, payment_date, method, reference, received_by, notes,"
          + " reversal_of, reason";

  private final JdbcTransactions tx;
  private final Jdbc jdbc;
  private final Clock clock;

  public JdbcPaymentRepository(JdbcTransactions tx, Jdbc jdbc, Clock clock) {
    this.tx = tx;
    this.jdbc = jdbc;
    this.clock = clock;
  }

  @Override
  public Payment insert(Payment payment) {
    if (!tx.inTransaction()) {
      // Receipt numbers come from a row-locked sequence, so the insert belongs in that transaction.
      throw new IllegalStateException("Payments must be saved inside a transaction");
    }
    if (payment.id() != 0) {
      throw new IllegalArgumentException("Payment is already saved: " + payment.id());
    }
    Long reversalOf = null;
    String reason = null;
    if (payment instanceof Payment.Reversal r) {
      reversalOf = r.reversedPaymentId();
      reason = r.reason();
    }
    long id =
        jdbc.insert(
            "INSERT INTO payments (receipt_number, job_id, amount, payment_date, method,"
                + " reference, received_by, notes, reversal_of, reason, created_at)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            payment.receiptNumber().value(),
            payment.jobId(),
            payment.amount(),
            payment.date(),
            payment.method(),
            payment.reference(),
            payment.receivedBy(),
            payment.notes(),
            reversalOf,
            reason,
            clock.instant());
    return switch (payment) {
      case Payment.Received p ->
          new Payment.Received(
              id,
              p.receiptNumber(),
              p.jobId(),
              p.amount(),
              p.date(),
              p.method(),
              p.reference(),
              p.receivedBy(),
              p.notes());
      case Payment.Reversal r ->
          new Payment.Reversal(
              id,
              r.receiptNumber(),
              r.jobId(),
              r.amount(),
              r.date(),
              r.method(),
              r.reference(),
              r.receivedBy(),
              r.notes(),
              r.reversedPaymentId(),
              r.reason());
    };
  }

  @Override
  public List<Payment> findByJob(long jobId) {
    return jdbc.query(
        "SELECT " + COLUMNS + " FROM payments WHERE job_id = ? ORDER BY payment_date, id",
        JdbcPaymentRepository::map,
        jobId);
  }

  @Override
  public Optional<Payment> findById(long id) {
    return jdbc.queryOne(
        "SELECT " + COLUMNS + " FROM payments WHERE id = ?", JdbcPaymentRepository::map, id);
  }

  private static Payment map(ResultSet rs) throws SQLException {
    long id = rs.getLong("id");
    ReceiptNumber receipt = ReceiptNumber.parse(rs.getString("receipt_number"));
    long jobId = rs.getLong("job_id");
    var amount = Jdbc.money(rs, "amount");
    var date = Jdbc.date(rs, "payment_date");
    PaymentMethod method = Jdbc.enumValue(rs, "method", PaymentMethod.class);
    String reference = rs.getString("reference");
    long receivedBy = rs.getLong("received_by");
    String notes = rs.getString("notes");
    Long reversalOf = Jdbc.nullableLong(rs, "reversal_of");
    if (reversalOf == null) {
      return new Payment.Received(
          id, receipt, jobId, amount, date, method, reference, receivedBy, notes);
    }
    return new Payment.Reversal(
        id,
        receipt,
        jobId,
        amount,
        date,
        method,
        reference,
        receivedBy,
        notes,
        reversalOf,
        rs.getString("reason"));
  }
}
