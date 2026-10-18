# Design decisions

This log records the decisions made while building Uzima that the original specification did not dictate, with the reason for each and the alternative that was turned down. New phases are appended at the end. Entries are numbered once and never renumbered; when a later decision replaces an earlier one, the old entry stays and is marked "Superseded by D-0xx".

## Phase 0 — Skeleton

### D-001 Clean repository, legacy history joined with `-s ours`
**Decision.** The rebuild started in a fresh repository whose history begins at an empty root commit. The legacy `uzima` and `uzima2` histories were later joined with a `git merge -s ours`, so the old commits are reachable but none of their files are in the working tree.
**Why.** The old code would not compile against the new build, and keeping its history in one place was requested.
**Rejected.** Copying the old files alongside the new code; rewriting the old history.

### D-002 No Java module system; separate `Launcher` class
**Decision.** The app runs from the plain classpath. `com.uzima.app.Launcher` is the `main` class and starts `UzimaApp`, which extends `Application`.
**Why.** A classpath app is simpler to test and package, and JavaFX refuses to start a classpath main class that extends `Application`.
**Rejected.** A `module-info.java` with JPMS packaging.

### D-003 H2 pinned to 2.3.232
**Decision.** H2 is pinned to 2.3.232, the newest version Flyway is verified against.
**Why.** H2 2.4.x made Flyway print a compatibility warning on every start.
**Rejected.** The newest H2 release.

### D-004 App data directory override
**Decision.** The app data directory follows the platform convention (`%APPDATA%\Uzima`, `~/Library/Application Support/Uzima`, `$XDG_DATA_HOME/uzima`) and can be overridden with `UZIMA_HOME` or `-Duzima.home`.
**Why.** Tests and demos need a throwaway directory without touching a real installation.
**Rejected.** A fixed `~/.uzima` folder.

### D-005 Embedded H2 user without a password
**Decision.** The default H2 file database uses user `uzima` and an empty password.
**Why.** The database is a local file protected by file-system permissions, and the specification forbids credentials in the repository.
**Rejected.** A committed default password.

### D-006 Layered configuration
**Decision.** Settings load from `application.properties` on the classpath, then `<app data>/uzima.properties`, then the `UZIMA_DB_URL`, `UZIMA_DB_USER` and `UZIMA_DB_PASSWORD` environment variables; later layers win.
**Why.** Defaults stay in the repo, an installation can override them, and secrets can live only in the environment.
**Rejected.** A single properties file.

### D-007 Database URLs redacted in logs
**Decision.** `JdbcUrls.redact` strips `user:password@` and `password=` parameters before any URL is logged, and `DatabaseConfig.toString()` never prints the password.
**Why.** The specification forbids logging credentials, and a MySQL URL can carry them.
**Rejected.** Not logging the URL at all, which makes startup problems hard to diagnose.

### D-008 Stable dependency versions
**Decision.** Dependencies use stable, widely used releases (for example JUnit 5.13 and Spotless 3.0) rather than the newest major versions.
**Why.** A portfolio build should work for reviewers without surprises.
**Rejected.** Always the latest release, including new majors.

### D-009 Coverage gate starts in Phase 1
**Decision.** The 80% JaCoCo line-coverage check runs in `verify` from Phase 1, over `domain`, `pricing` and `service`. The threshold is the `coverage.core.minimum` property.
**Why.** Phase 0 had no code in those packages to measure.
**Rejected.** A gate over the whole project, which would count UI and launcher code.

### D-010 Full `verify` on Windows CI
**Decision.** The Windows CI job runs the same `verify` as Ubuntu, including tests.
**Why.** Path and line-ending bugs only show up when tests run on Windows.
**Rejected.** Compile-only on Windows.

### D-011 LF line endings enforced
**Decision.** `.gitattributes` forces LF for source files and CRLF only for `.cmd` and `.bat`.
**Why.** Without it, Windows checkouts convert to CRLF and the formatting check fails.
**Rejected.** Relying on each developer's Git settings.

## Phase 1 — Domain and pricing

### D-012 Money rounding and scale
**Decision.** `Money` always holds a `BigDecimal` at scale 2. Any operation that produces more decimals rounds HALF_UP.
**Why.** Invoices must reconcile to the cent, and one rounding rule everywhere keeps them consistent.
**Rejected.** `double`; banker's rounding.

### D-013 Typed amounts with more than two decimals are rejected
**Decision.** `Money.parse` rejects input such as `1.234` with a field error instead of rounding it.
**Why.** Silently changing what the user typed hides mistakes.
**Rejected.** Rounding user input.

### D-014 Money display format
**Decision.** Amounts display as `KSh 1,234,567.00` and negatives as `-KSh 1,000.00`, using fixed separators that do not depend on the machine's locale.
**Why.** The output must be identical on every machine and in every document.
**Rejected.** Locale-driven formatting; brackets for negatives.

### D-015 Metres value type
**Decision.** `Metres` is non-negative, at most two decimal places and at most 999,999.99, matching `DECIMAL(8,2)`. More decimals are rejected, not rounded.
**Why.** Depth and height feed prices, so they get the same care as money.
**Rejected.** `double` metres.

### D-016 Tax rate precision
**Decision.** `TaxRate` is a fraction in [0, 1) with up to four decimal places (two decimals as a percentage, e.g. 14.5%).
**Why.** It covers any realistic VAT rate while keeping the stored value exact.
**Rejected.** Whole percentages only.

### D-017 One quote line per tier band
**Decision.** Tiered depth and lift height produce one line per band used, e.g. "Drilling, 100–200 m band": 50 m × 1,500.
**Why.** Every saved line is then exactly quantity × unit price, so invoices show their working.
**Rejected.** A single "tiered drilling" line with no unit price.

### D-018 Fee lines always present
**Decision.** Survey and authority fee lines appear on every quote, even when a fee is set to 0.
**Why.** The specification says the fees always apply; showing them makes a zero fee visible rather than missing.
**Rejected.** Omitting zero-value fee lines.

### D-019 PricingEngine is the only place prices are calculated
**Decision.** All price arithmetic lives in `PricingEngine`, built on `Money` operations. Controllers, services and SQL never calculate prices.
**Why.** The old app duplicated pricing in about eight places and they drifted apart.
**Rejected.** Pricing helpers on the entities or in the UI.

### D-020 VAT on collected amounts
**Decision.** The VAT in each payment is `amount × r / (1 + r)`, computed in one rounding step HALF_UP to cents, using the job's saved rate. Reversals are negative and reduce it. When a payment brings net paid exactly to the grand total, that payment's VAT is the invoiced VAT minus the VAT already counted.
**Why.** Rounding each payment separately can drift: 684,400.00 paid as 228,133.33 + 228,133.33 + 228,133.34 gives 31,466.67 each, 94,400.01 in total against 94,400.00 invoiced. The final payment therefore carries 31,466.66.
**Rejected.** Pro-rating VAT from the cumulative total, which the agreed rule does not describe.

### D-021 Plumbing pricing behind an extension point
**Decision.** Itemised plumbing is priced by a `PlumbingLinePricer`; the default `LengthBasedPlumbingPricer` charges pipe cost per metre × length. Diameter and outlets don't affect price yet.
**Why.** The specification flags diameter and outlets as a likely future pricing change.
**Rejected.** Hard-coding the length rule inside `PricingEngine`.

### D-022 Diameter per pipe line, outlets per plumbing job
**Decision.** Each `PipeRun` has its own pipe type, diameter in millimetres and length. `Plumbing.Itemised` has one outlet count for the whole plumbing job.
**Why.** Jobs mix pipe sizes (confirmed in review). The specification describes "plus number of outlets" once for the plumbing section.
**Rejected.** One diameter for the whole job.

### D-023 Pump height named `liftHeight`
**Decision.** The tiered pump input is `PumpSpec.liftHeight`.
**Why.** The specification calls it "tank height", but the charge is for how high the pump lifts water; the new name avoids confusion with tank capacity.
**Rejected.** `tankHeight`.

### D-024 Client category fixed after registration
**Decision.** A client's category could not change because it is part of the client ID.
**Why.** It kept the ID prefix and category consistent.
**Rejected.** Allowing changes.
Superseded by D-025.

### D-025 Client category can change; the ID never does
**Decision.** Users with `CHANGE_CLIENT_CATEGORY` (ADMIN, MANAGER) can move a client to another category through `Client.withCategory`, and the service audits old → new. The client ID keeps its original prefix. Saved jobs keep their snapshotted fees; only new quotes use the new category. Ordinary edits cannot change the category.
**Why.** Categories get corrected in real use, and IDs printed on past invoices must stay valid.
**Rejected.** Re-issuing the ID; deactivating and re-registering the client.

### D-026 Editing a pending job re-priced it at current prices
**Decision.** Saving an edit to a PENDING_SURVEY job re-priced the whole quote against the live catalog.
**Why.** It was the simplest behaviour.
**Rejected.** Keeping old prices.
Superseded by D-027.

### D-027 Edits keep snapshotted prices; re-pricing is explicit
**Decision.** A job stores its price basis (catalog item prices, category fees, rate tiers and tax rate). Edits to a PENDING_SURVEY job are priced against that basis; items newly added during an edit use the current catalog price. "Re-price at current catalog" is a separate operation that returns a before/after comparison, and the service applies it only when asked. In code: `Job.priceBasis()` (`PriceBasis`: fees, tiers, tax rate) plus the catalog items held in `QuoteSpec`; edits go through `PricingEngine.requote(job, editedSpec)`, re-pricing through `PricingEngine.reprice(job, currentCatalog, currentBasis)`, which returns a `Job.Repricing`. A `Job` rejects a quote whose tax rate differs from its price basis.
**Why.** A quote sent to a client must not change just because someone fixed a typo; price changes should be deliberate and visible.
**Rejected.** Silently re-pricing on every edit.

### D-028 Partial reversals
**Decision.** A reversal can be for part of a payment, up to the amount not yet reversed. A reversal can't itself be reversed and can't be dated before the payment it reverses. Future dates are rejected in the service.
**Why.** Partial refunds are common, for example keeping the survey fee on a cancelled job.
**Rejected.** Full reversals only.

### D-029 Reversals get their own receipt number
**Decision.** Each reversal is issued its own `RCP-yyyy-NNNNN` number.
**Why.** A refund is a document of its own and needs a reference to print and file.
**Rejected.** Reusing the original payment's number.

### D-030 Refund references follow the payment rules
**Decision.** A reversal needs a reference when the refund method is M-Pesa, bank transfer or cheque, with the same format rules as payments; it is optional for cash.
**Why.** Non-cash refunds leave a trail that must be traceable (review change).
**Rejected.** Optional references for every reversal.

### D-031 M-Pesa references normalised before checking
**Decision.** M-Pesa references are trimmed and upper-cased, then must be exactly 10 letters or digits.
**Why.** Staff often type receipt codes in lower case; the code itself is case-insensitive.
**Rejected.** Rejecting lower-case input.

### D-032 Payments can't be future-dated
**Decision.** A payment dated after today is rejected.
**Why.** A future payment would distort collections reports and the dashboard.
**Rejected.** Allowing post-dated entries.

### D-033 Cancelled jobs
**Decision.** A cancelled job owes nothing: its balance is 0 and it is excluded from outstanding balances, aged receivables and invoiced revenue. If net paid is above 0, the difference is shown as "Refund due". Reversals are allowed on cancelled jobs; new payments are blocked.
**Why.** Agreed in review: cancelling must not leave phantom debt, and refunds must be possible.
**Rejected.** Blocking cancellation once a job has payments.

### D-034 Status API on `JobStatus` and `StatusChange`
**Decision.** `JobStatus` answers questions (`next`, `canAdvanceTo`, `canCancel`, `canReopen`, `isQuoteEditable`, `acceptsPayments`). `StatusChange.advance`, `cancel` and `reopen` validate a move and return the history entry. `Job.advanceTo`, `cancel` and `reopen` return the updated job plus that entry.
**Why.** Every transition rule lives in one place, and the history row comes from the same validated call.
**Rejected.** A separate state-machine class with string event names.

### D-035 Extra admin-only permissions
**Decision.** `REOPEN_JOB`, `BACKUP_DATABASE` and `LOAD_DEMO_DATA` are ADMIN-only permissions.
**Why.** The specification names these actions but not their roles; each changes data broadly.
**Rejected.** Letting managers reopen jobs or run backups.

### D-036 One permission matrix
**Decision.** The role → permission map is a single `EnumMap` inside `Role`. Services call `Role.require(permission)`, which throws `AccessDeniedException` with a readable message.
**Why.** One place to read, test and change; the UI and the services can't disagree.
**Rejected.** Permission checks spread across services as role comparisons.

### D-037 Login lockout details
**Decision.** The fifth consecutive failure locks the account for exactly 15 minutes and resets the counter. Failures while locked are ignored and don't extend the lock. An admin password reset clears the lock.
**Why.** Extending the lock on each attempt would let anyone keep an account locked forever.
**Rejected.** Sliding lock windows.

### D-038 Password strength is a hint only
**Decision.** The only rule is at least 10 characters, counted in Unicode code points, and not equal to the username. The strength meter (STRONG at 16+ characters, or 12+ with three character types) is advice, not a rule.
**Why.** The specification asks for no composition rules; length matters most.
**Rejected.** Required digits or symbols.

### D-039 Identifier numbers widen instead of wrapping
**Decision.** Client numbers past 9999 and job or receipt numbers past 99999 simply get more digits.
**Why.** A wrapped number would duplicate an existing ID.
**Rejected.** Failing when the counter passes the padded width.

### D-040 Immutable entities
**Decision.** Entities are records; changes return a new record through `with...` methods, and unsaved entities use id 0.
**Why.** Immutable values are easier to reason about and to test, and a rejected change leaves the original untouched.
**Rejected.** Mutable JavaBeans with setters.

### D-041 Validator reports every field error at once
**Decision.** `Validator` collects all field errors before throwing one `ValidationException`.
**Why.** A form can mark every bad field at once instead of making the user fix them one by one.
**Rejected.** Failing on the first error.

### D-042 Architecture test
**Decision.** A test scans `domain` and `pricing` sources and fails on imports of JavaFX, JDBC, persistence, UI, service or app packages, and on `double` or `float` in money and pricing code.
**Why.** The layering rules are only real if the build enforces them.
**Rejected.** Adding ArchUnit, which would be a new dependency.

## Phase 2 — Persistence and services

### D-043 BCrypt with SHA-512 pre-hash for long passwords
**Decision.** Passwords are hashed with BCrypt `$2a$` at cost 12 (`BcryptPasswordHasher`). Passwords longer than BCrypt's 72-byte limit are pre-hashed with SHA-512 so every character counts. Tests use cost 4 for speed.
**Why.** Cost 12 is a sensible desktop default; the pre-hash avoids silently ignoring characters past byte 72.
**Rejected.** Truncation (ignores part of the password) and rejecting long passwords (punishes passphrases).

### D-044 Passwords travel as char[]
**Decision.** `PasswordHasher`, `AuthService` and the login API take passwords as `char[]`, never `String`.
**Why.** Callers can clear the array after use, and it's harder to log a password by accident.
**Rejected.** `String` parameters, which linger in memory and print easily.

### D-045 One failure message for every failed sign-in
**Decision.** Unknown username, wrong password, deactivated account and locked account all return the same `LoginResult.FAILURE_MESSAGE`. The real reason goes to the audit log. For unknown usernames a BCrypt check still runs against a dummy hash.
**Why.** The screen must not confirm which usernames exist or which accounts are locked, and response time shouldn't either.
**Rejected.** Specific messages such as "account locked", which are friendlier but leak account state.

### D-046 Attempts on locked or deactivated accounts don't count
**Decision.** A sign-in attempt on a deactivated or currently locked account is audited but doesn't change the failure counter or extend the lock.
**Why.** An attacker hammering a locked account shouldn't be able to keep its owner locked out indefinitely.
**Rejected.** Extending the lock on every attempt.

### D-047 Lock-screen failures count toward lockout
**Decision.** Wrong passwords on the idle lock screen use the same 5-failure / 15-minute lockout as sign-in. The fifth failure locks the account; `AuthService.unlock` then ends the session itself and returns `SESSION_ENDED`, and the UI goes back to the sign-in screen with the generic failure message (Phase 2 follow-up).
**Why.** An unattended, idle-locked machine must not offer unlimited guesses.
**Rejected.** A separate, unlimited lock-screen check.

### D-048 Change-password checks the current password but doesn't count failures
**Decision.** Changing your own password requires the current password, the new one must meet the policy, differ from the username and differ from the current password. A wrong current password here is a field error, not a lockout failure.
**Why.** The user is already signed in; locking them out over a typo in a settings form would be heavy-handed.
**Rejected.** Counting these toward lockout.

### D-049 Temporary passwords
**Decision.** Admin-created and admin-reset accounts get a 12-character password from an alphabet without look-alikes (no 0/O/1/l/I), generated with `SecureRandom`. It's returned once to show the admin, never stored in plain text, never audited, and hidden from `CreatedUser.toString()`. The user must change it at first sign-in. A reset also clears any lockout.
**Why.** Admins read these aloud or write them down; ambiguous characters cause support calls.
**Rejected.** Emailing passwords (no mail integration) and admin-chosen passwords (weak and known to the admin).

### D-050 There is always an active administrator
**Decision.** The last active ADMIN cannot be demoted or deactivated, and nobody can deactivate their own account. An admin may demote themselves while another active admin exists.
**Why.** Losing the last admin would lock everyone out of user management and settings with no recovery path in the app.
**Rejected.** A hidden "superuser" recovery account, which would be a default credential.

### D-051 Theme is a personal preference
**Decision.** Each user's light/dark theme is stored on their `users` row. Changing it needs no permission and isn't audited.
**Why.** It affects only the user's own screen, not business data.
**Rejected.** Auditing it, which would bury meaningful entries.

### D-052 Settings keys are a whitelist; missing keys use defaults
**Decision.** Settings are key/value rows whose keys come only from the `SettingKey` enum. A key with no row reads as its default (empty text, 16% VAT, 15 idle minutes), and saving inserts it. The logo is stored as a file path (`company.logo_path`), not as bytes.
**Why.** New settings don't need a migration, and no key name can come from user input.
**Rejected.** A V3 migration just to add the logo key; storing the image in the database.

### D-053 Settings validation and auditing
**Decision.** Company name is required. Email is optional but validated. KRA PIN is optional; if given it must be a letter, nine digits and a letter (upper-cased). Idle lock is 1–240 minutes. Company phone is free text (it may be a landline). Only fields that changed are saved and audited as `field: old → new`; an update that changes nothing writes no audit entry.
**Why.** Reviewers of the audit log want to see exactly what changed; Kenyan company phones are often landlines that the mobile-number rule would reject.
**Rejected.** Auditing a full before/after dump of every setting.

### D-054 Some settings are readable before sign-in
**Decision.** `SettingsService.currentTaxRate()` and `idleLockMinutes()` don't require a session; `current()` does.
**Why.** The idle timer is configured before anyone signs in, and quoting services enforce their own permissions.
**Rejected.** Requiring a session everywhere and threading special cases through the UI.

### D-055 Usernames are unique case-insensitively
**Decision.** Usernames are normalised to lower case, so "Otieno" and "otieno" are the same account. The service checks for a taken username before inserting; the unique index is the backstop.
**Why.** Staff type usernames inconsistently; two accounts that differ only by case would be confusing.
**Rejected.** Case-sensitive usernames.

### D-056 No hard delete for clients
**Decision.** `ClientService` has deactivate and reactivate but no delete. The foreign keys from `jobs` also stop a client with jobs from being removed by hand.
**Why.** A client's history (jobs, invoices, payments) must stay readable, and a deactivated client drops out of pickers anyway.
**Rejected.** A "delete if unused" action; it adds a second removal path for little benefit.

### D-057 Category changes are a separate operation
**Decision.** `ClientService.update` refuses to change the category; `changeCategory` does it, needs `CHANGE_CLIENT_CATEGORY` and writes its own audit entry ("category: Domestic → Commercial"). The client code is untouched.
**Why.** Category drives fees, so it deserves its own permission and a clearly labelled audit trail.
**Rejected.** Allowing it through the general edit form, which would bury it among cosmetic edits.

### D-058 Audit entries list only fields that changed
**Decision.** Updates audit "field: old → new" for changed fields only, and a save that changes nothing writes no entry. Coordinates are compared by value, so 36.80 and the stored 36.800000 are equal.
**Why.** Keeps the audit log readable and free of noise from re-saving a form.
**Rejected.** Logging the full before and after records.

### D-059 Catalog SQL identifiers come from a whitelist enum
**Decision.** The four catalog tables share one repository. Table, name, price and referencing-column names come from a private enum keyed by `CatalogKind`; values are always bound parameters.
**Why.** One implementation for four near-identical tables without ever building SQL from input.
**Rejected.** Four copy-pasted repositories, or string-built identifiers.

### D-060 Duplicate catalog names are validation errors
**Decision.** The repository turns unique-key violations into a `ValidationException` on `name` (or `capacityLitres` for tanks).
**Why.** The UI shows it next to the field like any other validation message; the database stays the single source of uniqueness.
**Rejected.** Checking for duplicates with a SELECT first, which races.

### D-061 Catalog edits keep the active flag; retire and reactivate are separate
**Decision.** `changeItem` changes name or capacity and price only; `retire` and `reactivate` change availability. Each is its own audit action.
**Why.** "Price changed" and "no longer offered" are different events for anyone reading the log.
**Rejected.** One save that can change everything at once.

### D-062 Current catalog prices include retired items
**Decision.** `CatalogRepository.catalogPrices()` looks items up by id regardless of the active flag.
**Why.** A pending job may hold an item that has since been retired; re-pricing it needs that item's current row.
**Rejected.** Active items only, which would make re-pricing fail for such jobs.

### D-063 Tax rate is passed into `currentBasis`
**Decision.** `CatalogService.currentBasis(category, taxRate)` takes the tax rate as a parameter instead of reading settings itself.
**Why.** Settings belong to `SettingsService`; keeping the catalog service independent of it avoids a dependency cycle and keeps tests simple.
**Rejected.** Reading the tax rate from the settings table inside the catalog service.

### D-064 Sequence counters are created outside the caller's transaction
**Decision.** When a counter row (e.g. `JOB-2027`) doesn't exist yet, it is inserted on a separate auto-commit connection; the caller then locks the existing row with `SELECT … FOR UPDATE`.
**Why.** On MySQL a locking read of a missing row takes a gap lock, and two first-time callers that both insert then deadlock (seen in the concurrent registration test as SQLState 40001). Creating a counter never uses up a number, so numbers stay gap-free.
**Rejected.** Creating the row inside the caller's transaction (deadlocks on MySQL), and seeding all counters in a migration (job and receipt counters are per year).

### D-065 Client search also matches local phone formats
**Decision.** A search like "0712 345" is matched against the stored `+254712345…` by dropping the leading zero.
**Why.** Staff type numbers the way clients say them.
**Rejected.** Requiring the international format in the search box.
### D-066 Job rows carry their whole price snapshot
**Decision.** `jobs` stores the catalog items a job was quoted with (id, name and price of the drilling type, pump and tank), the measurements, the plumbing mode, the priced category with its fees, the tax rate and the totals. `job_rate_tiers` holds the tier table it was priced with, `job_lines` the priced lines and `plumbing_lines` each pipe run with its pipe price. A loaded job is rebuilt from these alone, never from the live catalog.
**Why.** Invoices and later edits must reproduce the original quote exactly even after prices, fees, tiers or VAT change.
**Rejected.** Joining to the live catalog tables, which would rewrite old quotes whenever a price changes.

### D-067 Snapshot catalog items load as active
**Decision.** Catalog items rebuilt from a job's snapshot are always marked active.
**Why.** Whether the live item was retired later says nothing about what the job was quoted at, and it keeps a saved job equal to the job that was saved.
**Rejected.** Reading the live `active` flag, which would make a job's value depend on later catalog edits.

### D-068 Quote line quantities have a fixed scale of two
**Decision.** `QuoteLine` stores its quantity at scale 2 (`1.00`, `150.00`).
**Why.** `job_lines.quantity` is `DECIMAL(10,2)`; one canonical scale makes a reloaded job equal to the saved one, and metres never have more than two decimals.
**Rejected.** Comparing quantities with `compareTo` only in persistence tests, which would hide real mismatches in record equality.

### D-069 The first history row is written on insert
**Decision.** `JobRepository.insert` also writes the first `job_status_history` row (no previous status → the job's status, at its creation time). `history()` returns `StatusHistoryEntry` with an optional `from`, because `StatusChange` requires a previous status.
**Why.** The job detail timeline then starts at creation without the service having to remember a second call.
**Rejected.** A separate `recordCreated` method, and a fake "from" status for the first entry.

### D-070 Writes check the state they expect
**Decision.** `replaceQuote` only updates a job that is still `PENDING_SURVEY`, and `recordStatusChange` only updates a job whose current status is the change's `from`. Otherwise they throw `IllegalStateException` and nothing is written.
**Why.** The domain checks the job object in memory; these checks stop a stale screen from overwriting a job another user has already moved on, and stop duplicate history rows.
**Rejected.** Version columns for optimistic locking, which add a column and plumbing for the same protection here.

### D-071 Job and payment writes require a transaction
**Decision.** `JobRepository` writes that touch several tables, and `PaymentRepository.insert`, throw if called outside `Transactions.inTransaction`. `replaceQuote` takes the update time as a parameter.
**Why.** A job and its lines, or a payment and its receipt number and audit entry, must commit together; failing fast catches a service that forgot its boundary.
**Rejected.** Opening a transaction inside the repository, which would hide the boundary from the service that owns it.

### D-072 Payment state comes from the job_accounts view
**Decision.** The jobs list reads amount paid, balance, payment status and refund due from the `job_accounts` view. Tests check the view agrees with the domain `JobAccount` for unpaid, partly paid, paid, reversed, cancelled-with-money and cancelled-and-refunded jobs, on H2 and MySQL.
**Why.** The list needs these values for hundreds of jobs in one query, and they must never drift from the domain rules.
**Rejected.** Loading every payment and computing in Java for the list, and storing the values on `jobs`.

### D-073 Payments keep their own created_at
**Decision.** `JdbcPaymentRepository` takes a `Clock` and stamps `created_at` on insert; the domain `Payment` only has the business date.
**Why.** The payment date is what the client paid on; the entry time is an audit fact the domain doesn't need.
**Rejected.** Adding a timestamp to the `Payment` records.

### D-074 Date filters use Nairobi calendar days
**Decision.** Job search dates are calendar days in Africa/Nairobi, inclusive at both ends, converted to UTC bounds (`>= start of from-day`, `< start of the day after to-day`).
**Why.** Staff think in local days; a job created at 00:30 Nairobi time belongs to that day even though it is the previous day in UTC.
**Rejected.** Filtering on UTC dates.

### D-075 Job numbers use the Nairobi calendar year
**Decision.** `JobService` takes the year for `JOB-yyyy-NNNNN` from the creation instant in Africa/Nairobi, so a job saved at 00:30 on 1 January local time is numbered in the new year.
**Why.** Staff read the number against the local date printed on the quote.
**Rejected.** The UTC year, which would number the first three hours of each year in the previous year.

### D-076 Applying a re-price checks the total the user saw
**Decision.** `applyReprice(jobId, expectedGrandTotal)` recomputes inside the transaction and refuses if the new grand total differs from the one shown in `previewReprice`. Re-pricing with nothing changed saves and audits nothing.
**Why.** The user confirms a specific number; if someone edited the catalog in between, silently saving a different total would defeat the confirmation.
**Rejected.** Saving the preview object sent back by the UI (it could be stale or tampered with), or re-pricing without a check.

### D-077 Cancelling only through `cancel`
**Decision.** `advance(jobId, CANCELLED)` is refused with a message pointing to Cancel job, even for roles that may cancel.
**Why.** Cancelling needs its own permission and a reason; one entry point keeps both rules in one place.
**Rejected.** Letting `advance` cancel when a reason happens to be supplied.

### D-078 Unchanged edits are not saved or audited
**Decision.** `editQuote` and `updateNotes` compare the job before and after and write nothing (no row update, no audit entry) when nothing changed. A notes change on a locked job goes through `updateNotes`, audited as `JOB_QUOTE_EDITED` with "notes: old → new".
**Why.** Saving a form without changes shouldn't fill the audit log with noise.
**Rejected.** A separate audit action for notes; it would add an enum constant for a minor field.

### D-079 Service timestamps are truncated to milliseconds
**Decision.** `JobService` truncates `clock.instant()` to milliseconds before building jobs and status changes.
**Why.** Columns are `DATETIME(3)`; without truncation a reloaded job would not equal the job returned by `create`.
**Rejected.** Comparing jobs field by field with a tolerance in callers.
### D-080 Payments lock the job row
**Decision.** Recording or reversing a payment first runs `SELECT ... FOR UPDATE` on the job (`JobRepository.lock`), then reads the ledger and checks the rules inside the same transaction.
**Why.** Two people recording payments on one job at the same time would otherwise both see the old balance and could overpay it together. A test with eight concurrent payers of 30% each shows exactly three succeed on H2 and MySQL.
**Rejected.** A database constraint on the sum of payments (not expressible portably), or optimistic retries (more code for a desktop app with few users).

### D-081 Receipt numbers use the recording date's year
**Decision.** `RCP-yyyy-NNNNN` takes its year from today's date in Nairobi when the payment is recorded, not from the payment date the user enters. Reversals get their own receipt number from the same sequence.
**Why.** Numbers are issued in the order receipts are written, so a back-dated payment entered in January still continues that year's sequence without gaps.
**Rejected.** The payment date's year, which would reopen a closed year's sequence when someone back-dates.

### D-082 The refund method is chosen at reversal time
**Decision.** A reversal records its own method and reference (e.g. a cash payment refunded by M-Pesa) instead of copying the original's.
**Why.** Refunds often go back by a different channel than the money came in, and the refund reference must match how it was actually paid.
**Rejected.** Always refunding by the original method.

### D-083 Reads need a signed-in user, writes need a permission
**Decision.** Listing payments and reading a job's derived account need only a signed-in user; recording needs RECORD_PAYMENTS and reversing REVERSE_PAYMENTS.
**Why.** Every role works with jobs and needs to see what's been paid; only changes are restricted.
**Rejected.** A separate view permission, which nobody would ever be denied.

### D-084 Transactions are bound to the thread
**Decision.** `JdbcTransactions` keeps the transaction's connection in a ThreadLocal. Repositories ask it for "the current connection", so services wrap work in `Transactions.inTransaction(...)` without ever seeing a JDBC type, and nested calls join the outer transaction.
**Why.** Services own the boundary (job + lines + audit commit together) while SQL stays in `persistence`.
**Rejected.** Passing a `Connection` through every service and repository method; adding Spring just for `@Transactional`.

### D-085 A thin JDBC helper instead of an ORM
**Decision.** `Jdbc` offers query, queryOne, update and insert with positional parameters and a row mapper, plus shared conversions for Money, Metres, instants and enums.
**Why.** The SQL stays visible and portable between H2 and MySQL, with no mapping layer to learn or debug.
**Rejected.** JPA/Hibernate (the spec rules out an ORM) and a third-party JDBC template library (not in the approved dependency list).

### D-086 Instants are stored as UTC DATETIME(3)
**Decision.** Timestamps are written and read as `LocalDateTime` in UTC in `DATETIME(3)` columns; calendar logic converts to Africa/Nairobi in the service layer.
**Why.** The stored value never depends on the JVM or database session time zone, and millisecond precision is enough.
**Rejected.** MySQL `TIMESTAMP`, which converts through the session time zone and ends in 2038.

### D-087 Payments are append-only in code, signed by a CHECK
**Decision.** `PaymentRepository` has no update or delete. A database CHECK requires received payments to be positive and reversals (rows with `reversal_of`) to be negative.
**Why.** The rule is enforced in two places that cover each other, using SQL both databases accept.
**Rejected.** Triggers that block UPDATE/DELETE, because H2 triggers are Java classes and MySQL triggers are SQL; one migration can't hold both.

### D-088 One counter row per sequence and year
**Decision.** The `sequences` table holds rows named `CLIENT-DOM`, `JOB-2026`, `RCP-2026` and so on. Job and receipt numbers restart each year.
**Why.** It matches the `JOB-{yyyy}-{NNNNN}` and `RCP-{yyyy}-{NNNNN}` formats, and a missing row is simply created on first use.
**Rejected.** A single global counter per document type, which would make the year part of the number misleading.

### D-089 Integration tests run on H2 and on real MySQL
**Decision.** Repository, concurrency and selected service tests are parameterised over H2 and MySQL 8.4. MySQL runs in one shared Testcontainers container with a fresh schema per test, and those cases skip when Docker isn't available.
**Why.** The app supports both databases; this is how the MySQL gap-lock deadlock in the first sequence generator was found.
**Rejected.** H2-only tests, and a separate Maven profile that would usually be skipped.

### D-090 The composition root exposes a `Services` record
**Decision.** `CompositionRoot.wire(database, clock)` builds every repository and service once and returns them as a `Services` record for the UI. The clock is `Clock.system(Africa/Nairobi)`.
**Why.** One readable place shows the whole object graph, and tests can wire the same graph against any database and clock.
**Rejected.** A DI container, and service locators or static singletons.
