---
description: Execute the implementation planning workflow using the plan template to generate design artifacts.
handoffs:
  - label: Create Tasks
    agent: speckit.tasks
    prompt: Break the plan into tasks
    send: true
  - label: Create Checklist
    agent: speckit.checklist
    prompt: Create a checklist for the following domain...
---

## Shared contract

Follow [the command contract](README.md) for path resolution, existing-artifact protection,
hooks, authorization, and validation.

## User Input

$ARGUMENTS

## Workflow

1. Run .specify/scripts/bash/setup-plan.sh --json from the repository root. Parse
   FEATURE_SPEC, IMPL_PLAN, SPECS_DIR, and BRANCH. When resuming without the local pointer,
   set SPECIFY_FEATURE_DIRECTORY explicitly.
2. Read FEATURE_SPEC, .specify/memory/constitution.md when present, and the resolved
   plan-template. If IMPL_PLAN already exists, preserve it and update only the sections
   explicitly requested.
3. Fill the plan with the technical context, constitution check, architecture or project
   structure, research decisions, data model, interface contracts, and quickstart validation
   artifacts that the feature needs. Mark unresolved decisions clearly and stop on an
   unjustified constitution gate.
4. Preserve existing research.md, data-model.md, contracts/, quickstart.md, and unrelated plan
   decisions. Do not regenerate a template over them.
5. Report the branch, IMPL_PLAN, generated or updated artifacts, gate results, and remaining
   decisions.

Apply the shared hook protocol to before_plan and after_plan; do not interpret hook conditions
in this command. The plan command ends after design artifacts; implementation is handed off by
the workflow after task analysis.
