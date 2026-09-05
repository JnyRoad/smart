---
description: Identify underspecified areas in the current feature spec and record accepted clarifications.
handoffs:
  - label: Build Technical Plan
    agent: speckit.plan
    prompt: Create a plan for the spec. I am building with...
---

## Shared contract

Follow [the command contract](README.md) for path resolution, existing-artifact protection,
hooks, authorization, and validation.

## User Input

$ARGUMENTS

## Workflow

1. Run .specify/scripts/bash/check-prerequisites.sh --json --paths-only --no-persist from the
   repository root. Parse FEATURE_DIR and FEATURE_SPEC; stop if the spec is missing and direct the
   user to /speckit.specify.
2. Read the current spec and the constitution when present. Inspect functional scope, actors,
   data, flows, non-functional needs, integrations, edge cases, terminology, and acceptance
   criteria. Select only ambiguities whose answer would change scope, behavior, validation, or risk.
3. Ask one clear question at a time. Use the format **Question:** <interrogative>? followed by
   why it matters and concise options or a short answer format. Do not ask about information
   already stated, and stop when the material ambiguities are resolved or the user ends the loop.
4. After each accepted answer, append one traceable entry under ## Clarifications and apply the
   answer to the most appropriate requirements, story, data, edge-case, terminology, or success
   criteria section. Preserve unrelated text and save the spec after each update.
5. Validate the updated spec after each write: no stale contradiction, no unresolved placeholder
   addressed by the answer, consistent terminology, valid headings, and one clarification entry per
   accepted answer.
6. If FEATURE_DIR/checklists/requirements.md exists, reassess only affected criteria and change
   only checkbox markers whose state changed. Preserve all other checklist text, ordering, whitespace,
   and marker case. Do not alter custom reviewer-owned checklists.
7. Report questions asked and answered, the updated path, sections touched, checklist changes, any
   deferred ambiguity, and the suggested next command.

Apply the shared hook protocol to before_clarify and after_clarify; do not interpret hook conditions
in this command.
