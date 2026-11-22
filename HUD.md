# WarzoneDuels animated titles

## Installation on Minecraft Java 26.2

1. Stage the accompanying WarzoneDuels JAR in the test server's plugins directory as the single WarzoneDuels version.
2. With Nexo, copy the resource pack's `assets/warzoneduels` directory into `plugins/Nexo/pack/assets/warzoneduels`. Build and distribute the combined server pack through the existing Nexo workflow. For a standalone server-delivered pack, distribute the supplied pack ZIP through the server's normal resource-pack settings.
3. Set `settings.animated-hud-pack: true` in WarzoneDuels config after the combined pack is available. Activate the candidate through your normal server release procedure. Clients should accept and finish loading the server pack.
4. Test 1v1 and a team match. Confirm the countdown number, FIGHT at release, victory for the winning roster, and defeat after respawn for a dead opponent. A draw should have no victory or defeat cue. Test a client declining the server pack too.

The five final countdown numbers, start, defeat and victory each have eight transparent frames. The server advances the title every two game ticks using the font `warzoneduels:duel_hud`. Countdown lengths over five begin with native numerals. The match subtitle uses the real team size and map name. The installed pack adds no global font overrides. For custom pack distributors, the plugin checks Paper's server-pack-loaded status; the administrator must include the HUD assets in the distributed pack before enabling the feature.

The build is based on fork main 07af124, including the TNT cart fix. The separate team starting plates PR is independent. The preview GIF displays the artwork on a neutral background. Titles in game render over the actual arena. Source artwork can be regenerated with Python, Pillow and tools/hud/generate.py.
