---
description: Generate a custom checklist for the current feature based on requirements quality needs.
---

## Shared contract

Follow [the command contract](README.md) for path resolution, existing-artifact protection,
hooks, authorization, and validation.

## Purpose and ownership

A custom checklist reviews the quality of requirements: completeness, clarity, consistency,
measurability, traceability, and scenario coverage. It does not verify implementation behavior.
The checklist is reviewer-owned. [x] means the reviewer accepted the requirement-quality item;
newly generated items remain [ ]. Preserve reviewer markers and content. Never mark, replace,
delete, or automatically clean up existing checklist items.

## User Input

$ARGUMENTS

## Workflow

1. Run .specify/scripts/bash/check-prerequisites.sh --json --template checklist-template from the
   repository root. Parse FEATURE_DIR, AVAILABLE_DOCS, and TEMPLATE_CONTENT; use absolute paths.
2. Read the constitution when present and the relevant sections of spec.md, plan.md, and tasks.md.
   Derive the checklist domain, audience, timing, risk focus, and explicit items from the request
   and source artifacts. Ask only material clarification questions when the request is ambiguous.
3. Resolve a short descriptive checklist filename under FEATURE_DIR/checklists/. Create the
   directory when needed. If the target file is absent, create it with CHK001; if present, append
   after its highest existing CHK identifier. Do not overwrite existing content.
4. Generate questions about the requirements themselves, grouped by useful quality dimensions.
   Include a traceability reference to a spec section or a marker such as [Gap], [Ambiguity],
   [Conflict], or [Assumption]. Do not turn an item into a code, test, QA, or implementation check.
5. Use TEMPLATE_CONTENT for the title, metadata, ownership note, category headings, checkbox format,
   and notes. Leave every new item unchecked and preserve all prior marker states.
6. Report the absolute checklist path, whether it was created or appended, item count, focus,
   explicit user requirements included, and any unresolved clarification.

Apply the shared hook protocol to before_checklist and after_checklist; do not interpret hook
conditions in this command.
