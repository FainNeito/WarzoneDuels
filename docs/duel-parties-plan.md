# Duel Parties implementation plan

## Current scope

WarzoneDuels owns competitive rosters, challenge contracts, match execution, and results. The first supported party sizes will be 1v1, 2v2, and 3v3. Both sides must have exactly the same roster size.

Guild champions, guild permissions, active-war checks, champion settlements, war wagers, and LumaGuilds callbacks are deliberately deferred. No guild concepts should enter the core match engine during the Duel Parties work.

## Invariants established in phase 1

- A `MatchTeam` contains one to three unique players.
- Existing 1v1 duels are represented as two singleton teams without changing their current runtime behavior.
- A `DuelParty` has one leader and at most three members.
- A pending challenge locks both party rosters against joins, leaves, kicks, and leadership changes.
- A `DuelChallenge` snapshots both teams and the selected rules.
- Unequal roster sizes and a player appearing on both teams are rejected.
- Every snapshotted participant must accept before a challenge becomes ready.
- Expired or declined challenges cannot become matches.

## Next phases

1. Add an application-level party registry, invitations, lifecycle cleanup, and `/duel party` commands.
2. Replace the single global request with challenge contracts and per-player acceptance tracking.
3. Persist parties/challenges needed for safe reconnect and restart behavior.
4. Add six configurable arena spawns and team-aware countdown, containment, friendly-fire, elimination, disconnect, restoration, and victory handling.
5. Make wagers, spoils, stats, analytics, spectating, announcements, and runtime recovery team-aware.
6. Exercise 1v1 regression plus 2v2/3v3 Paper-server scenarios before enabling party matches in production.

## Deferred decisions

- Whether a disconnected party member is immediately eliminated or receives a grace period.
- Whether normal party matches permit wagers and, if so, how contributions and payouts are divided.
- Whether teammates can damage one another.
- Whether a party survives logout/restart or is intentionally session-only.

These choices affect persistence and payout design and should be settled before wiring party challenges into the live match service.
