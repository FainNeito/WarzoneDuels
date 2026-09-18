# WarzoneDuels SPEAR tasks

- [x] **TDD-015** - Correct playtest spawn/countdown, party announcements, explosive self-damage, and leader departure regressions.
  Tag: TDD
  References: REQ-009, REQ-016, REQ-019, REQ-020, REQ-021; `docs/implementation.md#match-execution`
  Acceptance: All six roster slots resolve to their configured sides; countdown begins after entry; victory names the complete winning party; leader departure clears memberships and invitations while locks remain respected; explosive self-damage follows the confirmed player preference.
  Evidence:
  - Existing `DuelService.spawnFor`, `ArenaDefinition.teamSpawn`, `startCountdown`, `DuelPartyService.leaveParty`, `DuelListener.onGenericDamage`, and `ExplosiveCombatPolicy` expose the reported failures.
  - Existing org.junit.jupiter.api.Test and org.junit.jupiter.api.Assertions support executable regression tests; java.lang.reflect and sun.misc.Unsafe provide an isolated DuelService fixture without starting Bukkit or invoking plugin constructors.
  - Existing dev.minecraft.warzoneduels.domain classes and org.bukkit.Location supply actual roster and arena values for spawn tests.
  Validation: `playtest-red.log` reproduces the wrong spawn side, missing party announcement, leader-leave rejection, and canceled self-damage. `playtest-green.log` passes 13 focused tests. `playtest-26.3-verify.log` and `playtest-26.2-verify.log` each pass all 62 tests on Java 25; EARS and changed-domain architecture checks pass. The final stable-API artifact is `target/WarzoneDuels-1.0.2.jar` (3,642,458 bytes). The old preparation source assertions encoded the wrong indexes and were replaced by executable six-slot service regression coverage. Live client confirmation remains outstanding.

## Brownfield baseline

The repository began SPEAR adoption with 14 passing tests. Initial team, party, and challenge domain seams added another 12 tests before adoption. No historical red/green claim is made for these 26 tests.

- [x] **TDD-001** - Establish the framework-free team, Duel Party, and unanimous challenge domain seam.
  Tag: TDD
  References: REQ-002, REQ-003, REQ-005, REQ-006, REQ-007, REQ-012, REQ-013; `docs/implementation.md#competitive-core`
  Acceptance: Existing one-versus-one construction remains compatible; teams and parties contain one to three unique players; challenges reject unequal or overlapping teams, snapshot rules and rosters, expire, and require unanimous acceptance.
  Evidence:
  - Existing `ActiveDuel`, `MatchParticipant`, `DuelSettings`, and `DuelService` sources established the one-versus-one compatibility surface.
  - JDK `java.util` collections and UUID types are the only dependencies added to the competitive core.
  - `ActiveDuelTeamTest`, `MatchTeamTest`, `DuelPartyTest`, and `DuelChallengeTest` pass as brownfield baseline evidence; no pre-implementation red is claimed.
  Validation: `mvn clean verify` passed 26 tests and produced the shaded 1.0.1 JAR before SPEAR adoption.

- [x] **INFRA-001** - Adopt project-local SPEAR workflow and traceability.
  Tag: INFRA
  References: REQ-014; `docs/implementation.md#spear-adoption`
  Acceptance: Project-local skills, EARS validator, Windows state helper, requirements, implementation notes, tasks, verification mapping, and contributor instructions exist; requirements validate and the clean build stays green.
  Evidence:
  - Upstream BadgersMC/spear-plugin revision `2c91bae` project-local snapshot: `spear-using-spear`, phase skills, and EARS validator.
  - Existing WarzoneDuels `pom.xml` identifies Java 21, Maven, Paper 1.21.11, H2, and JUnit 5.
  - Existing source and test inventory supplies the brownfield package and verification baseline.
  Validation: EARS validation passed all 14 requirements; the architecture scan found no framework imports or forbidden annotations in the new competitive-core types; Maven clean verify passed all 26 tests and produced the shaded JAR.

- [x] **TDD-002** - Add session Duel Party registry and invitation lifecycle.
  Tag: TDD
  References: REQ-003, REQ-004, REQ-005, REQ-013; `docs/implementation.md#competitive-core`
  Acceptance: A player belongs to at most one party; only leaders invite or remove; invitations expire and require target acceptance; leave, transfer, kick, and disband maintain indexes; roster locks reject every membership mutation.
  Evidence:
  - Existing `DuelParty` defines the framework-free roster and challenge-lock invariants.
  - Existing `DuelCommand` is the Bukkit command adapter and `PermissionPolicy` owns command authorization.
  - JUnit 5 and JDK collections are already present in `pom.xml` and existing tests.
  Validation: `party-service-red.log` records the missing application service; `party-service-green.log` passes five focused lifecycle tests; the application import scan found only domain and JDK dependencies; `party-service-verify.log` passes all 31 tests and packages the shaded JAR.

- [x] **TDD-003** - Add the roster-locking challenge coordinator and participant acceptance lifecycle.
  Tag: TDD
  References: REQ-001, REQ-002, REQ-005, REQ-006, REQ-007, REQ-013; `docs/implementation.md#competitive-core`
  Acceptance: Party leaders create equal-roster challenges; both rosters lock; every participant is indexed and must accept; decline, cancellation, expiry, and completion unlock both rosters; busy or unequal rosters cannot create partial locks.
  Evidence:
  - Existing `DuelRequest` and `DuelService.pendingRequest` identify the current single-request boundary.
  - Existing `DuelChallenge` supplies roster snapshots, expiration, and unanimous acceptance.
  - Existing request expiry, combat, spawn, and online checks in `DuelService` are the safety baseline.
  Validation: `challenge-service-red.log` records the missing coordinator; `challenge-service-green.log` passes four focused roster-lock and lifecycle tests; the application coupling scan found no server-framework dependency; `challenge-service-verify.log` passes all 37 tests and packages the shaded JAR.

- [x] **TDD-007** - Integrate party challenge contracts into the live request and GUI flow.
  Tag: TDD
  References: REQ-001, REQ-005, REQ-006, REQ-007, REQ-013; `docs/implementation.md#competitive-core`
  Acceptance: Individual challenges retain the current review flow; challenging a party leader snapshots both parties; all participants receive the contract; each acceptance is recorded; decline, expiry, logout, or failed combat/location/online checks release locks; only a ready challenge reaches match preparation.
  Evidence:
  - Existing `DuelService.pendingRequest`, builder GUI callbacks, request expiry, accept, deny, and start validation define the live individual flow.
  - `DuelChallengeService` owns party challenge indexing, roster locks, unanimous acceptance, expiry, and completion.
  - `DuelGui` and `DuelGuiListener` define the existing request-review and confirmation adapter.
  Validation: `party-challenge-live-red.log` records the absent composition and GUI callback flow; `party-challenge-live-green.log` passes the live contract and coordinator suites; `party-challenge-live-verify.log` passes all 50 tests and packages the shaded JAR. Party wagers are rejected, roster checks are repeated before start, and decline, expiry, logout, or failed start releases both rosters.

- [x] **TDD-006** - Expose Duel Party lifecycle through Bukkit commands.
  Tag: TDD
  References: REQ-004, REQ-005, REQ-008, REQ-013; `docs/implementation.md#competitive-core`
  Acceptance: `/duel party` supports create, invite, accept, inspect, leave, kick, transfer, and disband; help and tab completion reflect permissions and party state; domain failures produce actionable player messages without bypassing the application service.
  Evidence:
  - Existing `DuelCommand` is the Bukkit command router and tab completer.
  - Existing `PermissionPolicy` and `plugin.yml` define the permission hierarchy and parent tests.
  - TDD-002 supplies the framework-free application service consumed by the adapter.
  Validation: `party-command-red.log` records missing routing and composition; `party-command-green.log` passes the focused command-contract and permission suites; the competitive-core coupling scan is clean; `party-command-verify.log` passes all 33 tests and packages the shaded JAR.

- [x] **TDD-004** - Define framework-free team combat and elimination policy.
  Tag: TDD
  References: REQ-001, REQ-002, REQ-009, REQ-010, REQ-013; `docs/implementation.md#match-execution`
  Acceptance: Team membership is authoritative for friendly-fire checks; eliminating one member does not end a multi-player match; victory is returned only when every opponent is eliminated; draw readiness requires every non-eliminated participant.
  Evidence:
  - Existing `ArenaDefinition`, `DuelService`, and `DuelListener` define current two-spawn preparation, containment, and death boundaries.
  - Existing `RuntimeStateStore` and `LoadoutArchiveStore` define recovery and inventory safety requirements.
  - Paper 1.21.11 API is the provided server contract in `pom.xml`.
  Validation: `team-policy-red.log` records the missing policy; `team-policy-green.log` passes friendly-fire, partial elimination, team victory, and draw-consent tests; the domain coupling scan is clean; `team-policy-verify.log` passes all 41 tests and packages the shaded JAR.

- [x] **TDD-008** - Add backward-compatible arena spawn groups.
  Tag: TDD
  References: REQ-001, REQ-002, REQ-009, REQ-013; `docs/implementation.md#match-execution`
  Acceptance: Arena configuration supplies three stable positions per team while preserving `spawn1` and `spawn2`; missing configured positions use deterministic safe offsets from the legacy spawn; callers resolve a spawn by team and roster slot without exposing mutable location state.
  Evidence:
  - Existing `ArenaDefinition`, `DuelService.startDuel`, `spawnFor`, and `startCountdown` define the current two-player flow.
  - Existing `config.yml` `arena.spawn1` and `arena.spawn2` values must remain valid after upgrade.
  - Paper `Location.clone` and vector offsets are already used by the arena code.
  Validation: `team-spawns-red.log` records the absent grouped-spawn API; `team-spawns-green.log` passes slot resolution and defensive-copy tests; the existing Bukkit-coupled arena value remains documented brownfield debt; `team-spawns-verify.log` passes all 43 tests and packages the shaded JAR.

- [x] **TDD-010** - Generalize match preparation and countdown to complete rosters.
  Tag: TDD
  References: REQ-001, REQ-002, REQ-009, REQ-013; `docs/implementation.md#match-execution`
  Acceptance: Match preparation archives, clears combat state, prepares, teleports, warns, and freezes every participant; each participant receives the stable team-slot spawn; failed terrain preparation restores wager state and notifies every participant; existing one-versus-one behavior is unchanged.
  Evidence:
  - Existing `DuelService.startDuel`, `prepareCombatant`, `spawnFor`, `startCountdown`, and loadout archive calls define the current two-player flow.
  - TDD-008 supplies backward-compatible spawn groups.
  - `ActiveDuel.participants` and `MatchTeam.participants` supply stable roster order.
  Validation: `team-preparation-red.log` records the absent live roster preparation contract; `team-preparation-green.log` verifies party entry, roster iteration, grouped spawns, and complete-roster countdown; `team-preparation-verify.log` passes all 48 tests and packages the shaded JAR.

- [x] **TDD-009** - Apply team policy to live combat, disconnects, containment, and cleanup.
  Tag: TDD
  References: REQ-001, REQ-009, REQ-010, REQ-013; `docs/implementation.md#match-execution`
  Acceptance: Friendly fire is cancelled; death and disconnect timeout eliminate one member; a match continues while that member has a surviving teammate; a winner is declared only after a whole team is eliminated; every participant is contained, messaged, restored, and cleaned exactly once.
  Evidence:
  - Existing `DuelListener.onDamage` and `DuelService.shouldCancelDamage`, `handleDeath`, disconnect monitor, containment monitor, and conclusion paths define the live combat boundary.
  - TDD-004 supplies the framework-free policy used by the Bukkit orchestration.
  - Existing loadout archive and recovery markers protect participant inventories and restart exits.
  Validation: `team-combat-red.log` records the absent live team-policy contract; `team-combat-green.log` verifies friendly-fire, whole-team victory, unanimous surviving-player draws, and killer-aware death handling; `team-combat-verify.log` passes all 49 tests and packages the shaded JAR.

- [x] **TDD-005** - Version runtime persistence for complete teams and safe interruption recovery.
  Tag: TDD
  References: REQ-001, REQ-010, REQ-011, REQ-013; `docs/implementation.md#persistence-and-recovery`
  Acceptance: Team runtime state writes match type, team identity, and every participant; the reader accepts both legacy two-participant files and the new team schema; corrupt or incomplete teams produce no active duel; shutdown/restart recovery enumerates every participant and creates no invented winner.
  Evidence:
  - Existing `RuntimeStateStore`, `StatsService`, `SpoilsService`, `DuelAnalyticsService`, and `DuelService` define the current one-versus-one outcome behavior.
  - Existing shutdown, resume-marker, loadout archive, and spectator tests establish the recovery baseline.
  Validation: `team-runtime-schema-red.log` records absent schema and whole-roster recovery; `team-runtime-schema-green.log` passes the schema and recovery contract; the version-2 writer retains legacy participant keys while storing full teams; `team-runtime-schema-verify.log` passes all 45 tests and packages the shaded JAR.

- [x] **TDD-011** - Apply explicit team outcome policies to wagers, statistics, and analytics.
  Tag: TDD
  References: REQ-001, REQ-010, REQ-011, REQ-013; `docs/implementation.md#persistence-and-recovery`
  Acceptance: Party wagers are rejected until a split policy is approved; all winners receive one win and all losers one loss; analytics retain team size and participant membership without corrupting existing records.
  Evidence:
  - Existing `StatsService`, `SpoilsService`, `DuelAnalyticsService`, `DuelAnalyticsStore`, and `DuelRecord` define one-versus-one outcomes.
  - Existing economy hold/refund/payout paths assume two contributors and one recipient.
  - `TeamMatchPolicy` defines authoritative winning and losing rosters.
  Validation: `team-outcome-red.log` records the missing team outcome policy; `team-outcome-green.log` passes roster outcome and wager rejection tests; the domain import scan is clean; `team-outcome-verify.log` passes all 47 tests and packages the shaded JAR. The analytics schema preserves legacy leader columns and adds match type, team size, and indexed participant membership.

- [x] **TDD-012** - Capture defeated inventories once under an explicit team spoils policy.
  Tag: TDD
  References: REQ-001, REQ-010, REQ-011, REQ-013; `docs/implementation.md#persistence-and-recovery`
  Acceptance: Each defeated inventory is captured once for a valid opposing killer; when no valid killer exists, the winning roster receives a deterministic recipient; teammate or unrelated damage never receives spoils; legacy one-versus-one ownership is unchanged.
  Evidence:
  - Existing `SpoilsService` creates one vault from a winner and defeated participant snapshot.
  - Existing `DuelService.handleDeath` and disconnect timeout paths select the current one-versus-one winner.
  - `TeamMatchPolicy` defines authoritative team membership and surviving participants.
  Validation: `team-spoils-red.log` records the missing policy; `team-spoils-green.log` verifies valid opposing-killer preference and deterministic surviving-opponent fallback; the domain import scan is clean; `team-spoils-verify.log` passes all 52 tests and packages the shaded JAR.

- [x] **DOC-001** - Complete live Paper acceptance checklist and produce a testing JAR.
  Tag: DOC
  References: REQ-001, REQ-007, REQ-009, REQ-010, REQ-011, REQ-014; `docs/implementation.md#match-execution`
  Acceptance: Automated verification is green, the JAR is checksum-recorded, and manual steps cover 1v1 regression, 2v2, 3v3, friendly fire, deaths, disconnects, restart interruption, inventory restoration, wagers, spectators, arena reset, and dependent-plugin coexistence.
  Evidence:
  - Existing `MANUAL_TESTING.md` and `PLAYER_GUIDE.md` define the current operator and player behavior.
  - Maven Shade output is the existing deployable artifact format.
  Validation: `final-testing-jar-verify.log` records the original acceptance build; the latest Java 25 stable build is recorded in `paper-26.2-java25-verify.log` with all 58 tests passing. The current shaded testing JAR is `target/WarzoneDuels-1.0.1.jar` (3,641,773 bytes), SHA-256 `9C396553B20F2F5B6A3D3E3227AB05892E99F5CF2D3E96AB688C34D76D230A3F`. Its class-file major version is 69 and packaged `api-version` is `26.2`. `MANUAL_TESTING.md` includes 1v1, complete 2v2/3v3, explosive/double-KO, duel-duration, and Paper 26.x live-server checks; live Paper/client testing remains for the operator.

- [x] **TDD-013** - Add an optional duel-duration limit that defaults to unlimited.
  Tag: TDD
  References: REQ-001, REQ-009, REQ-010, REQ-011, REQ-014, REQ-015; `docs/implementation.md#match-execution`
  Acceptance: `settings.duel-time-limit-seconds` defaults to zero; zero schedules no timeout; a positive value starts when combat is released, concludes an unfinished 1v1 or party match as a draw, and is cancelled on every ordinary conclusion or shutdown path.
  Evidence:
  - Existing `DuelService.reloadConfig`, `startCountdown`, `concludeDuel`, `disable`, and Bukkit task cancellation methods define the configuration, combat-release, draw, and lifecycle boundaries.
  - Existing `DuelEndReason.DRAW`, `StatsService`, and wager refund behavior define the safe timeout result without introducing a second conclusion path.
  - Existing JUnit 5 source-contract tests verify Bukkit orchestration where a live scheduler is unavailable in the local unit-test harness.
  Validation: `duel-duration-red.log` records the missing duration policy; `duel-duration-green.log` passes the focused deadline, rounding, configuration, scheduling, cancellation, and persistence contracts; `duel-duration-verify.log` passes EARS validation and all 54 tests, then packages the shaded JAR. Zero remains unlimited by default, while positive limits persist their active deadline across plugin reloads and conclude through the existing draw/refund/cleanup path.

- [x] **TDD-014** - Make explosive party combat attributable and resolve same-tick double knockouts safely.
  Tag: TDD
  References: REQ-001, REQ-009, REQ-010, REQ-011, REQ-013, REQ-016, REQ-017; `docs/implementation.md#match-execution`
  Acceptance: Crystal, respawn-anchor, and explosive-minecart damage retains a participant source; attributed teammate damage and unattributed party explosion damage are cancelled; attributed enemy damage supplies the spoils killer; deaths resolve together on the next primary-thread tick; eliminating both complete teams in that batch produces a draw, no spoils, and an archived-loadout restore after respawn.
  Evidence:
  - Paper 1.21.11 `EntityDamageEvent.getDamageSource`, `DamageSource.getCausingEntity`, `EntityDamageByBlockEvent`, `TNTPrimed.getSource`, `EntityPlaceEvent`, and Bukkit scheduler APIs define the adapter evidence available in the locally resolved `paper-api` JAR.
  - Existing `DuelListener.onDamage`, `handleCrystalDamage`, entity/block explosion handlers, and `PlayerDeathEvent` handling define the current event boundary.
  - Existing `DuelService.handleDeath`, `TeamMatchPolicy`, `TeamSpoilsPolicy`, `LoadoutArchiveStore`, and draw conclusion path define the elimination, spoils, restore, and outcome boundaries.
  - JUnit 5 and JDK collection/UUID APIs are already present in `pom.xml` and the framework-free domain test suite.
  Validation: `explosive-combat-red.log` records the absent attribution and batch-outcome policies; `explosive-combat-green.log` passes 13 focused combat, spoils, and recovery tests; the new domain policies import only JDK types; `explosive-combat-verify.log` passes EARS validation and all 58 tests. Explosion owners now flow through friendly-fire and spoils decisions, while a same-tick complete-team knockout draws without spoils and persists archived-loadout restoration across restart.

- [x] **INFRA-002** - Move the supported platform baseline to Java 25 and verify Paper 26.2/26.3.
  Tag: INFRA
  References: REQ-014, REQ-018; `docs/implementation.md#platform-compatibility`
  Acceptance: Maven emits Java 25 bytecode; the default provided API is pinned to stable Paper 26.2; `plugin.yml` declares API 26.2; a pinned `paper-26.3` profile compiles and passes the same suite; the stable build is rerun last and recorded as the testing JAR.
  Evidence:
  - Paper's official getting-started documentation specifies Java 25 for Paper 26.1 and newer.
  - Paper's official project-setup documentation defines the `26.2.build.<build>-stable` Maven coordinate format and Java 25 toolchain.
  - Paper's official downloads page identifies Paper 26.2 build 123 as the current stable build consulted for this task.
  - Paper's official API documentation identifies the currently consulted Paper 26.3 API as `26.3.build.8-alpha`.
  - Existing `pom.xml`, `plugin.yml`, Maven compiler, Surefire, Shade, and the 58-test suite define the local build and compatibility surface.
  Validation: `paper-26.2-java25-verify.log` passes EARS validation and all 58 tests against `26.2.build.123-stable`; `paper-26.3-java25-verify.log` passes all 58 tests against `26.3.build.8-alpha`. The stable 26.2 build was run last, emits Java class-file major version 69, packages `api-version: 26.2`, and produced the checksum-recorded testing JAR.
