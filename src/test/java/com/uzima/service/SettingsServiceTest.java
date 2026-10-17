package com.uzima.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.common.ValidationException;
import com.uzima.domain.money.TaxRate;
import com.uzima.domain.user.AccessDeniedException;
import com.uzima.domain.user.Role;
import com.uzima.persistence.JdbcSettingsRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class SettingsServiceTest {

  private final AuthTestFixture f = new AuthTestFixture();

  @AfterEach
  void close() {
    f.close();
  }

  private static Settings with(Settings s, TaxRate rate, int idle, String email, String pin) {
    return new Settings(
        s.companyName(),
        s.address(),
        s.phone(),
        email,
        pin,
        s.logoPath(),
        rate,
        idle,
        s.exportFolder());
  }

  @Test
  void defaultsComeFromReferenceData() {
    f.admin();
    Settings s = f.settings.current();
    assertThat(s.companyName()).isEqualTo("Uzima Borehole Drilling Company");
    assertThat(s.taxRate()).isEqualTo(TaxRate.of("0.16"));
    assertThat(s.idleLockMinutes()).isEqualTo(15);
    assertThat(s.logoPath()).isEmpty();
    assertThat(f.settings.currentTaxRate()).isEqualTo(TaxRate.of("0.16"));
    assertThat(f.settings.idleLockMinutes()).isEqualTo(15);
  }

  @Test
  void currentRequiresASignedInUser() {
    assertThatThrownBy(f.settings::current).isInstanceOf(NotSignedInException.class);
  }

  @Test
  void adminUpdateSavesAndAuditsOnlyChangedFieldsAsOldToNew() {
    f.admin();
    Settings before = f.settings.current();
    Settings changed =
        new Settings(
            before.companyName(),
            "Moi Avenue, Nairobi",
            before.phone(),
            "info@uzima.co.ke",
            "p051234567x",
            "/home/uzima/logo.png",
            TaxRate.of("0.14"),
            30,
            before.exportFolder());

    Settings saved = f.settings.update(changed);

    assertThat(saved).isEqualTo(changed);
    assertThat(saved.kraPin()).isEqualTo("P051234567X");
    assertThat(f.settings.currentTaxRate()).isEqualTo(TaxRate.of("0.14"));
    AuditEntry audit = f.ctx.auditEntries().getFirst();
    assertThat(audit.action()).isEqualTo(AuditAction.SETTINGS_CHANGED);
    assertThat(audit.details())
        .contains("tax rate: 16% → 14%")
        .contains("idle lock minutes: 15 → 30")
        .contains("address: (none) → \"Moi Avenue, Nairobi\"")
        .contains("logo: (none) → \"/home/uzima/logo.png\"")
        .doesNotContain("company name");
  }

  @Test
  void unchangedUpdateWritesNoAuditEntry() {
    f.admin();
    long before = f.ctx.auditEntries().size();
    f.settings.update(f.settings.current());
    assertThat(f.ctx.auditEntries()).hasSize((int) before);
  }

  @Test
  void onlyAdminsEditSettings() {
    f.admin();
    f.user("manager", Role.MANAGER, "manager-password-1");
    f.auth.signOut();
    f.auth.login("manager", "manager-password-1".toCharArray());

    Settings current = f.settings.current();
    assertThatThrownBy(() -> f.settings.update(current)).isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void reportsEveryInvalidField() {
    f.admin();
    Settings bad = with(f.settings.current(), TaxRate.of("0.16"), 0, "not-an-email", "12345");
    assertThatThrownBy(() -> f.settings.update(bad))
        .isInstanceOfSatisfying(
            ValidationException.class,
            e ->
                assertThat(e.errors())
                    .extracting(err -> err.field())
                    .containsExactlyInAnyOrder("idleLockMinutes", "email", "kraPin"));
    Settings blankName = new Settings(" ", "", "", "", "", "", TaxRate.of("0.16"), 15, "");
    assertThatThrownBy(() -> f.settings.update(blankName))
        .isInstanceOfSatisfying(
            ValidationException.class, e -> assertThat(e.messageFor("companyName")).isPresent());
  }

  @Test
  void failedAuditRollsBackTheWholeUpdate() {
    f.admin();
    AuditRepository failing =
        new AuditRepository() {
          @Override
          public long append(AuditEntry entry) {
            throw new IllegalStateException("audit store unavailable");
          }

          @Override
          public List<AuditEntry> search(AuditQuery query) {
            return List.of();
          }
        };
    SettingsService broken =
        new SettingsService(
            new JdbcSettingsRepository(f.ctx.jdbc),
            f.ctx.session,
            new AuditLog(failing, f.ctx.session, f.ctx.clock),
            f.ctx.tx);
    Settings changed = with(f.settings.current(), TaxRate.of("0.14"), 20, "", "");

    assertThatThrownBy(() -> broken.update(changed)).hasMessage("audit store unavailable");

    assertThat(f.settings.currentTaxRate()).isEqualTo(TaxRate.of("0.16"));
    assertThat(f.settings.idleLockMinutes()).isEqualTo(15);
  }
}
