# WarzoneDuels technology stack

- Language: Java 21 source and target
- Build: Maven
- Server API: Paper 1.21.11-R0.1-SNAPSHOT, provided scope
- Analytics integration: Plan API 5.7-R0.2, provided scope
- Persistence: H2 2.2.224 plus YAML stores
- Tests: JUnit Jupiter 5.11.4
- Packaging: Maven Shade plugin with H2 embedded
- Optional runtime integrations: Vault, EnthusiaTeleport, EnthusiaTags, NotBounties, CombatLogX, and Plan

Local verification currently uses JDK 23 targeting Java 21 because a JDK 21 installation is not available on this workstation. The existing Maven compiler configuration emits a warning recommending `--release 21`; this is tracked as build debt rather than silently treated as Java 21 runtime validation.
