# Verification and traceability

Brownfield baseline before SPEAR adoption: Maven clean verify passed 26 tests with zero failures, errors, or skips and produced `WarzoneDuels-1.0.1.jar`.

| Requirements | Automated evidence | Live evidence still required |
| --- | --- | --- |
| REQ-001 | Existing permission, persistence, spectator, Plan, and teleport tests | Full one-versus-one Paper regression |
| REQ-002 | `MatchTeamTest`, `ActiveDuelTeamTest`, `DuelChallengeTest` | Invalid live challenge messages |
| REQ-003 | `DuelPartyTest` | Party command usability |
| REQ-004 | `DuelPartyServiceTest` covers leader authority, expiry, acceptance, membership indexing, transfer, kick, leave, and disband | Multi-client invitation flow |
| REQ-005 | `DuelPartyTest.challengeLockPreventsEveryRosterMutation`, `DuelPartyServiceTest.rosterLockRejectsEveryMembershipMutation` | Locked command feedback |
| REQ-006, REQ-007 | `DuelChallengeTest` and `DuelChallengeServiceTest` cover snapshots, equal rosters, locking, indexing, unanimous acceptance, decline, expiry, and completion | Live GUI integration and multi-client unanimous acceptance |
| REQ-008 | `DuelPartyCommandContractTest`, `PermissionPolicyTest`, and `PermissionParentsTest` verify routing, operations, composition, configuration, and permission metadata | Multi-client messages and tab-completion check |
| REQ-009 | `TeamMatchPolicyTest` covers friendly fire, partial elimination, whole-team victory, and unanimous surviving draw consent | 1v1, 2v2, and 3v3 arena combat |
| REQ-010 | `RuntimeStateTeamSchemaContractTest` verifies versioned full-team storage, legacy keys, and whole-roster restart/recovery enumeration | Disconnect and full restart staging |
| REQ-011 | Planned team outcome tests | Vault, economy, stats, Plan, and announcements |
| REQ-012 | Source scan for guild or LumaGuilds coupling | None until integration is approved |
| REQ-013 | Architecture review for every changed party-core file | Plugin coexistence staging |
| REQ-014 | EARS validation, SPEAR state history, linked task evidence, and clean Maven verification | None |
| REQ-015 | `DuelDurationPolicyTest` covers unlimited defaults, release-based deadlines, tick conversion, configuration wiring, cancellation, and runtime persistence | Enable a short limit on staging and verify an unfinished 1v1 and party match end as draws |
| REQ-016 | `ExplosiveTeamCombatPolicyTest` plus the focused combat/spoils suites cover attributed enemies, teammate cancellation, unattributed party explosions, and live adapter wiring | Crystal, anchor, and TNT-minecart combat with multiple real clients |
| REQ-017 | `ExplosiveTeamCombatPolicyTest` covers complete-team double elimination; source contracts verify next-tick batching and persistent archived-loadout restoration | Same-explosion final deaths, respawn, and restart-before-respawn staging |
| REQ-018 | Java 25 clean verification passes 58 tests against pinned Paper 26.2 stable and Paper 26.3 alpha APIs; artifact metadata confirms class-file 69 and API 26.2 | Full startup and gameplay matrix on actual Paper 26.2 and 26.3 servers with dependencies |

Automated tests and a clean package build do not approve production deployment. Paper/client behavior, plugin interoperability, restart recovery, and latency-sensitive combat remain staging checks.

1.0.2 regression evidence: `PlaytestRegressionTest` executes all six DuelService spawn lookups against ArenaDefinition, checks complete-party victory labels, leader disband/index/invitation cleanup, and explosive self-versus-teammate damage (REQ-019 through REQ-021). Both pinned Paper API builds pass 62 tests. Live countdown, arrival, leader logout notification, and self-explosion checks remain in MANUAL_TESTING.md.
