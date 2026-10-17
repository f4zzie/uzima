package com.uzima.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.uzima.domain.client.Client;
import com.uzima.domain.client.ClientCategory;
import com.uzima.domain.client.ClientDetails;
import com.uzima.domain.client.County;
import com.uzima.domain.common.DomainException;
import com.uzima.domain.common.ValidationException;
import com.uzima.domain.user.AccessDeniedException;
import com.uzima.domain.user.Permission;
import com.uzima.domain.user.Role;
import com.uzima.persistence.JdbcClientRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClientServiceTest {

  private ServiceTestContext ctx;
  private ClientService service;

  @BeforeEach
  void setUp() {
    ctx = ServiceTestContext.h2();
    service =
        new ClientService(
            new JdbcClientRepository(ctx.jdbc),
            ctx.sequences,
            ctx.tx,
            ctx.session,
            ctx.audit,
            ctx.clock);
  }

  @AfterEach
  void tearDown() {
    ctx.close();
  }

  static ClientDetails details(String name, ClientCategory category) {
    return ClientDetails.parse(name, category, "0712345678", "", County.MACHAKOS, "", "", "");
  }

  @Test
  void registerIssuesSequentialCodesPerCategoryAndAudits() {
    ctx.signInAs(Role.CLERK);

    Client first = service.register(details("Mwangi", ClientCategory.DOMESTIC));
    Client second = service.register(details("Njeri", ClientCategory.DOMESTIC));
    Client factory = service.register(details("Bidco", ClientCategory.INDUSTRIAL));

    assertThat(first.code().value()).isEqualTo("UZM-DOM-0001");
    assertThat(second.code().value()).isEqualTo("UZM-DOM-0002");
    assertThat(factory.code().value()).isEqualTo("UZM-IND-0001");
    assertThat(first.createdAt()).isEqualTo(ServiceTestContext.START);
    assertThat(service.find(first.id())).contains(first);

    AuditEntry entry = ctx.auditEntries().getLast();
    assertThat(entry.action()).isEqualTo(AuditAction.CLIENT_CREATED);
    assertThat(entry.entityId()).isEqualTo("UZM-DOM-0001");
    assertThat(entry.details()).isEqualTo("Mwangi (Domestic)");
  }

  @Test
  void updateRecordsOnlyChangedFields() {
    ctx.signInAs(Role.CLERK);
    Client client = service.register(details("Mwangi", ClientCategory.DOMESTIC));

    ClientDetails edited =
        ClientDetails.parse(
            "Mwangi & Sons",
            ClientCategory.DOMESTIC,
            "0712345678",
            "mwangi@example.co.ke",
            County.MACHAKOS,
            "",
            "",
            "");
    Client updated = service.update(client.id(), edited);

    assertThat(updated.name()).isEqualTo("Mwangi & Sons");
    assertThat(ctx.auditEntries().getFirst().details())
        .isEqualTo("name: Mwangi → Mwangi & Sons; email: (none) → mwangi@example.co.ke");

    int entries = ctx.auditEntries().size();
    service.update(client.id(), edited);
    assertThat(ctx.auditEntries()).hasSize(entries);
  }

  @Test
  void unchangedCoordinatesAreNotReportedAsAChange() {
    ctx.signInAs(Role.CLERK);
    ClientDetails withGps =
        ClientDetails.parse(
            "Kibet", ClientCategory.DOMESTIC, "0712345678", "", County.NANDI, "", "0.20", "35.10");
    Client client = service.register(withGps);
    int entries = ctx.auditEntries().size();

    service.update(client.id(), withGps);

    assertThat(ctx.auditEntries()).hasSize(entries);
  }

  @Test
  void updateCannotChangeTheCategory() {
    ctx.signInAs(Role.CLERK);
    Client client = service.register(details("Mwangi", ClientCategory.DOMESTIC));
    assertThatThrownBy(
            () -> service.update(client.id(), details("Mwangi", ClientCategory.COMMERCIAL)))
        .isInstanceOf(ValidationException.class);
  }

  @Test
  void categoryChangeKeepsTheClientIdAndIsAudited() {
    ctx.signInAs(Role.MANAGER);
    Client client = service.register(details("Mwangi", ClientCategory.DOMESTIC));

    Client changed = service.changeCategory(client.id(), ClientCategory.COMMERCIAL);

    assertThat(changed.category()).isEqualTo(ClientCategory.COMMERCIAL);
    assertThat(service.find(client.id()).orElseThrow().code().value()).isEqualTo("UZM-DOM-0001");
    AuditEntry entry = ctx.auditEntries().getFirst();
    assertThat(entry.action()).isEqualTo(AuditAction.CLIENT_CATEGORY_CHANGED);
    assertThat(entry.entityId()).isEqualTo("UZM-DOM-0001");
    assertThat(entry.details()).isEqualTo("category: Domestic → Commercial");
  }

  @Test
  void clerksCannotChangeCategoryOrDeactivate() {
    ctx.signInAs(Role.CLERK);
    Client client = service.register(details("Mwangi", ClientCategory.DOMESTIC));

    assertThatThrownBy(() -> service.changeCategory(client.id(), ClientCategory.COMMERCIAL))
        .isInstanceOfSatisfying(
            AccessDeniedException.class,
            e -> assertThat(e.permission()).isEqualTo(Permission.CHANGE_CLIENT_CATEGORY));
    assertThatThrownBy(() -> service.deactivate(client.id()))
        .isInstanceOfSatisfying(
            AccessDeniedException.class,
            e -> assertThat(e.permission()).isEqualTo(Permission.DEACTIVATE_CLIENTS));
    assertThat(service.find(client.id()).orElseThrow().active()).isTrue();
  }

  @Test
  void managersDeactivateAndReactivateWithAudit() {
    ctx.signInAs(Role.MANAGER);
    Client client = service.register(details("Mwangi", ClientCategory.DOMESTIC));

    assertThat(service.deactivate(client.id()).active()).isFalse();
    assertThat(ctx.auditEntries().getFirst().action()).isEqualTo(AuditAction.CLIENT_DEACTIVATED);
    int entries = ctx.auditEntries().size();
    service.deactivate(client.id());
    assertThat(ctx.auditEntries()).hasSize(entries);

    assertThat(service.reactivate(client.id()).active()).isTrue();
    assertThat(ctx.auditEntries().getFirst().action()).isEqualTo(AuditAction.CLIENT_REACTIVATED);
  }

  @Test
  void searchAndFindNeedASignedInUser() {
    assertThatThrownBy(() -> service.search(ClientSearch.all()))
        .isInstanceOf(NotSignedInException.class);
    assertThatThrownBy(() -> service.register(details("x", ClientCategory.DOMESTIC)))
        .isInstanceOf(NotSignedInException.class);

    ctx.signInAs(Role.CLERK);
    service.register(details("Mwangi", ClientCategory.DOMESTIC));
    assertThat(service.search(ClientSearch.all().withText("mwa"))).hasSize(1);
  }

  @Test
  void unknownClientIsReported() {
    ctx.signInAs(Role.MANAGER);
    assertThatThrownBy(() -> service.deactivate(404)).isInstanceOf(DomainException.class);
  }
}
