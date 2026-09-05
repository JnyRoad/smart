---
description: Create or update the project constitution from interactive or provided principle inputs.
handoffs:
  - label: Build Specification
    agent: speckit.specify
    prompt: Implement the feature specification based on the updated constitution. I want to build...
---

## Shared contract

Follow [the command contract](README.md) for path resolution, existing-artifact protection,
hooks, authorization, and validation.

## User Input

$ARGUMENTS

## Scope

This command writes only .specify/memory/constitution.md. Classify the input first. Defer feature
implementation, code, tests, deployment, and unrelated artifacts to a reported next action; do not
execute them. Ask before writing when it is unclear whether text is governance content.

## Workflow

1. Resolve constitution-template with
   .specify/scripts/bash/resolve-template.sh constitution-template --json from the repository root.
   Stop on a resolver error. Read the existing constitution when present and preserve applicable
   principles and amendments; never write a template layer.
2. Identify every [ALL_CAPS_IDENTIFIER] placeholder and collect its value from the input or
   repository context. Respect an explicitly requested number of principles. Use the original
   ratification date, set the last-amended date to today only when changed, and keep dates ISO-formatted.
3. Choose a semantic version bump: MAJOR for incompatible principle removal or redefinition, MINOR
   for a new or materially expanded principle or section, PATCH for clarification or wording-only
   changes. Explain the choice when it is not clear. Each principle needs a concise name, a
   non-negotiable rule, and its rationale. Governance must state amendment, versioning, and compliance
   review rules.
4. Draft from the resolved template, preserving heading hierarchy and applicable existing content.
   Replace defined placeholders; any retained placeholder or TODO needs an explicit reason.
5. Prepend a Sync Impact Report stating old and new versions, changed principles, added or removed
   sections, and deferred items. Validate the report/version match, dates, unexplained placeholders,
   principle completeness, and governance before writing.
6. Write only .specify/memory/constitution.md. Report the version, rationale, deferred intents, and
   suggested follow-up commands.

Apply the shared hook protocol to before_constitution and after_constitution; do not interpret hook
conditions in this command.
