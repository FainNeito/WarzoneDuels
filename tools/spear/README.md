# SPEAR project tooling

Upstream: `BadgersMC/spear-plugin` revision `2c91bae`.

`ears.mjs` is the upstream EARS validator. Project-local skills under `.agents/skills/spear-*` are the upstream Codex skill snapshot. `state.mjs` is the Windows-compatible project adaptation used instead of Bash and `jq`.

Typical commands:

```text
node tools/spear/ears.mjs docs/requirements.md
node tools/spear/state.mjs state_task TDD-002 REQ-004
node tools/spear/state.mjs state_assert_phase idle
node tools/spear/state.mjs state_set_phase spec
```

Transient state is stored under `.claude` and ignored. Durable requirements, task status, and evidence live under `docs`.
