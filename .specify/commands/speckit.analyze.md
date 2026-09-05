---
description: Perform a non-destructive cross-artifact consistency and quality analysis across spec.md, plan.md, and tasks.md after task generation.
---

## Shared contract

Follow [the command contract](README.md) for path resolution, existing-artifact protection,
hooks, authorization, and validation.

## User Input

$ARGUMENTS

## Scope

Run this command after tasks.md exists. Compare spec.md, plan.md, tasks.md, and the applicable
constitution as the current sources of intent. This command is strictly read-only: it writes no
artifact, changes no code, and does not create or delete external issues. Recommendations are
report content and require a separate, explicitly authorized action.

The constitution is authoritative within this analysis. A conflict with a MUST principle is a
highest-severity finding that requires adjustment to spec.md, plan.md, or tasks.md; this command
does not reinterpret, dilute, or silently ignore the principle.

## Workflow

1. Run .specify/scripts/bash/check-prerequisites.sh --json --require-tasks --include-tasks --no-persist
   from the repository root. Parse FEATURE_DIR and AVAILABLE_DOCS, then derive absolute paths for
   spec.md, plan.md, and tasks.md. Stop with the missing prerequisite command if any required file
   is absent.
2. Read the relevant requirements, user stories and acceptance scenarios from spec.md; decisions,
   structure and constraints from plan.md; task IDs, phases, paths and dependencies from tasks.md;
   and governing MUST/SHOULD statements from the constitution when it is filled.
3. Check requirement coverage, story and task alignment, terminology, ordering, placeholders,
   contradictions, and constitution compliance. Include only buildable work when mapping success
   criteria; report business outcomes without inventing implementation tasks.
4. Produce a compact Markdown report with findings (stable category IDs, severity, locations,
   evidence, and recommendation), a requirement-to-task coverage table, constitution issues,
   unmapped tasks, and counts. Report zero findings with the same structure.
5. End with next actions. If a remediation edit is useful, describe the requested scope and wait
   for explicit authorization before any other command performs it.

Apply the shared hook protocol to before_analyze and after_analyze; do not interpret hook
conditions in this command.
