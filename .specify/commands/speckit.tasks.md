---
description: Generate an actionable, dependency-ordered tasks.md for the feature based on available design artifacts.
handoffs:
  - label: Analyze For Consistency
    agent: speckit.analyze
    prompt: Run a project analysis for consistency; after approval, hand implementation to superpowers
    send: true
---

## Shared contract

Follow [the command contract](README.md) for path resolution, existing-artifact protection,
hooks, authorization, and validation.

## User Input

$ARGUMENTS

## Workflow

1. Run .specify/scripts/bash/setup-tasks.sh --json from the repository root. Parse
   FEATURE_DIR, TASKS_TEMPLATE_CONTENT, TASKS_TEMPLATE, and AVAILABLE_DOCS. Paths supplied by the
   script are absolute. Set SPECIFY_FEATURE_DIRECTORY explicitly when resuming without the local
   pointer.
2. Read plan.md and spec.md from FEATURE_DIR. Use available research.md, data-model.md,
   contracts/, quickstart.md, and .specify/memory/constitution.md when present.
3. Map requirements, user stories, entities, contracts, dependencies, and project structure to
   concrete tasks. Group user-story work by priority and identify dependencies and safe parallel
   work. Include meaningful behavior regression validation for behavior changes according to
   [development.md#验证](../../docs/agent-rules/development.md#验证); use structural, link, parse, or
   diff checks for pure documentation and low-risk configuration.
4. Use TASKS_TEMPLATE_CONTENT (or TASKS_TEMPLATE for older setup output). If FEATURE_DIR/tasks.md
   already exists, preserve it and apply only an explicitly requested revision or an append-only
   convergence update.
5. New tasks use the format
   - [ ] T001 [P] [US1] Description with an exact file path.
   The unchecked marker applies only to new tasks. During revisions, preserve existing completion
   markers, including [x]; do not reset completed work.
   IDs are unique and ordered; [P] and [US#] appear only when applicable. Include setup,
   foundational, user-story, and cross-cutting work only when the design requires it.
6. Report the tasks path, count, story mapping, dependencies, validation coverage, and suggested
   next command. Do not invoke the disabled implementation entrypoint.

Apply the shared hook protocol to before_tasks and after_tasks; do not interpret hook conditions
in this command.
