---
description: Disabled Spec Kit implementation entrypoint; implementation is handed to superpowers.
---

# Implementation entrypoint disabled

Follow [the command contract](README.md). This command writes no business code, tests, configuration,
or task files. The workflow runs speckit.analyze after speckit.tasks, then hands an approved task
list to superpowers for execution. Use the project validation standard in
[development.md#验证](../../docs/agent-rules/development.md#验证) while executing those tasks.
