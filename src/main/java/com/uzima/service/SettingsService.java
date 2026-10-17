package com.uzima.service;

import com.uzima.domain.money.TaxRate;
import com.uzima.domain.user.Permission;
import java.math.BigDecimal;
import java.util.Map;

/** Reads and updates company settings. Every change is audited with its old and new value. */
public final class SettingsService {

  static final int DEFAULT_IDLE_MINUTES = 15;

  private final SettingsRepository repository;
  private final Session session;
  private final AuditLog audit;
  private final Transactions tx;

  public SettingsService(
      SettingsRepository repository, Session session, AuditLog audit, Transactions tx) {
    this.repository = repository;
    this.session = session;
    this.audit = audit;
    this.tx = tx;
  }

  /** Current settings, for any signed-in user. */
  public Settings current() {
    session.requireUser();
    return read();
  }

  /** The VAT rate new quotes are priced with. */
  public TaxRate currentTaxRate() {
    return read().taxRate();
  }

  /** Minutes of inactivity before the session locks. Read before sign-in, so no session check. */
  public int idleLockMinutes() {
    return read().idleLockMinutes();
  }

  /** Saves changed values and audits them as "old → new". ADMIN only. */
  public Settings update(Settings updated) {
    session.require(Permission.EDIT_SETTINGS);
    updated.validate();
    return tx.inTransaction(
        () -> {
          Settings before = read();
          Changes changes = new Changes();
          save(SettingKey.COMPANY_NAME, before.companyName(), updated.companyName(), changes);
          save(SettingKey.COMPANY_ADDRESS, before.address(), updated.address(), changes);
          save(SettingKey.COMPANY_PHONE, before.phone(), updated.phone(), changes);
          save(SettingKey.COMPANY_EMAIL, before.email(), updated.email(), changes);
          save(SettingKey.COMPANY_KRA_PIN, before.kraPin(), updated.kraPin(), changes);
          save(SettingKey.COMPANY_LOGO_PATH, before.logoPath(), updated.logoPath(), changes);
          if (!before.taxRate().equals(updated.taxRate())) {
            repository.save(SettingKey.TAX_RATE, updated.taxRate().value().toPlainString());
            changes.field("tax rate", before.taxRate(), updated.taxRate());
          }
          if (before.idleLockMinutes() != updated.idleLockMinutes()) {
            repository.save(
                SettingKey.IDLE_LOCK_MINUTES, Integer.toString(updated.idleLockMinutes()));
            changes.field("idle lock minutes", before.idleLockMinutes(), updated.idleLockMinutes());
          }
          save(SettingKey.EXPORT_FOLDER, before.exportFolder(), updated.exportFolder(), changes);
          if (!changes.isEmpty()) {
            audit.record(AuditAction.SETTINGS_CHANGED, "settings", "", changes.toString());
          }
          return read();
        });
  }

  private void save(SettingKey key, String before, String after, Changes changes) {
    if (!before.equals(after)) {
      repository.save(key, after);
      changes.field(label(key), quoted(before), quoted(after));
    }
  }

  private Settings read() {
    Map<SettingKey, String> v = repository.load();
    return new Settings(
        v.getOrDefault(SettingKey.COMPANY_NAME, ""),
        v.getOrDefault(SettingKey.COMPANY_ADDRESS, ""),
        v.getOrDefault(SettingKey.COMPANY_PHONE, ""),
        v.getOrDefault(SettingKey.COMPANY_EMAIL, ""),
        v.getOrDefault(SettingKey.COMPANY_KRA_PIN, ""),
        v.getOrDefault(SettingKey.COMPANY_LOGO_PATH, ""),
        v.containsKey(SettingKey.TAX_RATE)
            ? new TaxRate(new BigDecimal(v.get(SettingKey.TAX_RATE)))
            : TaxRate.KENYA_STANDARD,
        v.containsKey(SettingKey.IDLE_LOCK_MINUTES)
            ? Integer.parseInt(v.get(SettingKey.IDLE_LOCK_MINUTES))
            : DEFAULT_IDLE_MINUTES,
        v.getOrDefault(SettingKey.EXPORT_FOLDER, ""));
  }

  private static String label(SettingKey key) {
    return switch (key) {
      case COMPANY_NAME -> "company name";
      case COMPANY_ADDRESS -> "address";
      case COMPANY_PHONE -> "phone";
      case COMPANY_EMAIL -> "email";
      case COMPANY_KRA_PIN -> "KRA PIN";
      case COMPANY_LOGO_PATH -> "logo";
      case TAX_RATE -> "tax rate";
      case IDLE_LOCK_MINUTES -> "idle lock minutes";
      case EXPORT_FOLDER -> "export folder";
    };
  }

  private static String quoted(String value) {
    return value.isEmpty() ? "(none)" : "\"" + value + "\"";
  }
}
