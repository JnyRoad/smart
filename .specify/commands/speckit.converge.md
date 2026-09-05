---
description: Assess the current codebase against the feature artifacts and append remaining work to tasks.md.
---

## Shared contract

Follow [the command contract](README.md) for path resolution, existing-artifact protection,
hooks, authorization, and validation.

## User Input

$ARGUMENTS

## Scope

Run after superpowers has worked through the current tasks.md. Treat spec.md, plan.md, tasks.md,
the constitution, and the current code as the inputs. This command does not modify spec.md, plan.md,
application code, or external systems.

## Workflow

1. Run .specify/scripts/bash/check-prerequisites.sh --json --require-tasks --include-tasks --no-persist
   from the repository root. Parse FEATURE_DIR and derive absolute paths to spec.md, plan.md, and
   tasks.md. Stop with the appropriate prerequisite command when a required artifact is missing.
2. Read the buildable requirements, acceptance scenarios, plan decisions, constitution obligations,
   current task IDs, and the code paths named by the artifacts.
3. Compare the intended behavior with the current code. Record only unmet or partially met work,
   including the requirement, evidence, affected path, and a concrete remediation task.
4. If gaps exist, append one Phase N: Convergence section to tasks.md. Use the next available task
   IDs and preserve every existing line, task order, marker, and prior convergence phase. Do not
   rewrite, renumber, reorder, delete, or complete tasks, and do not create application files.
5. If no gaps exist, leave tasks.md byte-for-byte unchanged and report a clean convergence result.
6. Report the evidence, appended task IDs when any, and the next execution or review step.

The constitution remains authoritative; a violated MUST obligation produces a remediation task.
Apply the shared hook protocol to before_converge and after_converge; do not interpret hook
conditions in this command.
