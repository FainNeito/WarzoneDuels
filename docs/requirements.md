# WarzoneDuels requirements

Date: 2026-09-17

This is a brownfield SPEAR adoption. Requirements describing behavior that predates adoption are baseline requirements and do not claim historical red/green evidence.

### REQ-001 - Preserve existing one-versus-one behavior

WHEN two individual players complete the existing challenge flow THE SYSTEM SHALL preserve the current one-versus-one rules, safety, persistence, statistics, spoils, spectator, and recovery behavior.

### REQ-002 - Equal competitive teams

IF a proposed match has empty, duplicate, oversized, overlapping, or unequal rosters THEN THE SYSTEM SHALL reject the match before arena preparation or economic mutation.

### REQ-003 - Duel Party ownership

THE SYSTEM SHALL provide lightweight Duel Parties with one leader, one to three unique members, and no dependency on guild membership.

### REQ-004 - Controlled party membership

WHEN a Duel Party leader invites, removes, or transfers leadership THE SYSTEM SHALL enforce leader authority, membership uniqueness, the three-player limit, and explicit invite acceptance.

### REQ-005 - Roster locking

WHILE a Duel Party participates in a pending or accepted challenge THE SYSTEM SHALL prevent joins, leaves, removals, disbanding, and leadership changes until that challenge terminates.

### REQ-006 - Challenge contracts

WHEN a leader challenges another equally sized Duel Party THE SYSTEM SHALL snapshot both rosters and selected rules into a separately expiring challenge contract.

### REQ-007 - Unanimous acceptance

WHEN a Duel Party challenge is pending THE SYSTEM SHALL start no match until every snapshotted participant has accepted and all start-time safety checks still pass.

### REQ-008 - Party commands

THE SYSTEM SHALL expose create, invite, accept, inspect, leave, kick, and disband operations through the `/duel party` command hierarchy with permission-aware help and tab completion.

### REQ-009 - Team-aware match execution

WHEN a valid one-versus-one, two-versus-two, or three-versus-three challenge becomes ready THE SYSTEM SHALL prepare every participant, assign a team spawn, prevent teammate damage, and declare victory only when an opposing team is fully eliminated.

### REQ-010 - Disconnect and interruption safety

IF a participant disconnects or the server interrupts a party match THEN THE SYSTEM SHALL apply the configured forfeit or recovery policy without duplicating rewards, losing archived loadouts, or recording an invented result.

### REQ-011 - Team-aware outcomes

WHEN a party match ends THE SYSTEM SHALL restore all participants and apply wagers, spoils, statistics, analytics, announcements, and cleanup according to an explicit team policy.

### REQ-012 - Guild separation

THE SYSTEM SHALL keep guild membership, champion appointments, wars, settlements, and war resolution outside the Duel Party core until a separately approved integration requirement exists.

### REQ-013 - Architectural isolation

THE SYSTEM SHALL keep new party, challenge, and match rules independent of Bukkit and Paper while adapters translate commands, events, persistence, and server state.

### REQ-014 - SPEAR workflow

WHEN WarzoneDuels behavior changes THE SYSTEM SHALL maintain EARS requirements, linked tasks, red and green evidence for new behavior, architectural review, and clean-build verification before producing a test artifact.

### REQ-015 - Configurable duel duration

WHEN a duel is released THE SYSTEM SHALL leave its duration unlimited when configured to zero and otherwise end it as a draw after the configured number of seconds.

### REQ-016 - Explosive combat attribution

WHEN a duel participant causes crystal, respawn-anchor, or explosive-minecart damage THE SYSTEM SHALL attribute that damage to the participant, prevent damage to their teammates, and use the attributed opposing participant for spoils selection.

### REQ-017 - Simultaneous team elimination

IF both duel teams become fully eliminated during the same server tick THEN THE SYSTEM SHALL conclude the match as a draw without awarding spoils and restore every simultaneously defeated participant's archived pre-duel loadout after respawn.

### REQ-018 - Paper 26 platform baseline

THE SYSTEM SHALL build and run on Java 25 against stable Paper 26.2 while providing a pinned Paper 26.3 compatibility verification profile.

### REQ-019 - Party playtest regressions

WHEN a duel starts THE SYSTEM SHALL send every participant to the correct team spawn before the opening countdown and identify the entire winning party in the victory announcement.

### REQ-020 - Leader departure

WHEN an unlocked Duel Party leader leaves the party or disconnects THE SYSTEM SHALL disband that party and remove its memberships and invitations.

### REQ-021 - Vanilla explosive self-damage

WHEN a participant damages themselves with an explosive THE SYSTEM SHALL allow self-damage while continuing to prevent explosive damage to other members of their team.

### REQ-022 - Atomic analytics records

WHEN a duel result is persisted THE SYSTEM SHALL store its parent and participant records atomically, roll back failed inserts, and preserve any caller-owned transaction and connection mode.

IF analytics rollback fails THEN THE SYSTEM SHALL discard the uncertain connection without restoring auto-commit or reusing it for later writes.

### REQ-023 - Expired roster reuse

WHEN a leader creates a challenge after an earlier challenge expires THE SYSTEM SHALL release expired roster locks before checking availability without requiring an intermediate lookup.

### REQ-024 - Match type consistency

IF a match contains multiple participants per team and its type is not PARTY THEN THE SYSTEM SHALL reject it while preserving valid singleton matches.

### REQ-025 - Reliable SPEAR validation and state

WHEN SPEAR tooling validates requirements or changes phase THE SYSTEM SHALL reject missing requirement clauses and malformed state, replace state through a same-directory temporary file, and preserve the prior state if replacement fails.

### REQ-026 - Advancement evidence counters

WHEN a valid duel challenge is sent, captured spoils are withdrawn, all surviving participants agree to a draw, or a duel winner satisfies an approved ruleset or low-health condition THE SYSTEM SHALL persist the corresponding per-player advancement evidence counter in stats.yml without changing ordinary match statistics.

### REQ-027 - Advancement evidence semantics

WHEN a one-versus-one challenger wins with a non-default ruleset THE SYSTEM SHALL record challenger-custom-rules evidence, WHEN a player wins with Ender Pearls and Wind Charges both disabled THE SYSTEM SHALL record restricted-mobility evidence, and WHEN a one-versus-one kill winner has less than four health points before post-duel healing THE SYSTEM SHALL record low-health evidence.

### REQ-028 - Advancement integration boundary

THE SYSTEM SHALL expose only durable WarzoneDuels evidence needed by the advancement consumer and SHALL NOT implement guild-war achievements, spectator-betting achievements, or reward payouts as part of this slice.
