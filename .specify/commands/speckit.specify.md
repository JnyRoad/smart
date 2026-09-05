---
description: Create or update the feature specification from a natural language feature description.
handoffs:
  - label: Build Technical Plan
    agent: speckit.plan
    prompt: Create a plan for the spec. I am building with...
  - label: Clarify Spec Requirements
    agent: speckit.clarify
    prompt: Clarify specification requirements
    send: true
---

## Shared contract

Follow [the command contract](README.md) for path resolution, existing-artifact protection,
hooks, authorization, and validation.

## User Input

$ARGUMENTS

## Workflow

1. If the input is empty, report "No feature description provided" and stop.
2. Resolve the feature directory. Reuse an explicit SPECIFY_FEATURE_DIRECTORY or the existing
   .specify/feature.json value. If an existing spec.md has no explicitly requested revision, report
   its path and stop before running hooks or writing anything. A revision updates only the requested
   sections. Do not create a new feature to replace a missing local pointer.
3. For a new feature, use specs/ by default. When no explicit feature directory is supplied, derive
   the directory name from .specify/init-options.json and existing specs according to its configured
   numbering mode. Create the directory, resolve spec-template through the template stack, and copy
   it only when spec.md is absent. Persist the actual resolved directory in .specify/feature.json;
   hooks do not create the spec directory or file. Branch and spec directory names are independent.
4. Read the active template, the user description, and .specify/memory/constitution.md when it
   exists. Write user stories, acceptance scenarios, functional requirements, success criteria,
   edge cases, entities, and assumptions relevant to the request. Mark only materially unresolved
   choices for later clarification.
5. Preserve existing formatting and unrelated decisions. Validate that required sections are
   complete and requirements are testable. Create or reassess the built-in
   checklists/requirements.md only as required by the existing lifecycle; preserve custom
   reviewer-owned checklists.
6. Write only the authorized specification artifacts, then report SPECIFY_FEATURE_DIRECTORY,
   SPEC_FILE, checklist status, and the suggested next command.

Apply the shared hook protocol to before_specify and after_specify; do not interpret hook
conditions in this command.

## Done when

- The specification path is resolved and the requested content is recorded.
- Existing artifacts outside the authorized revision remain unchanged.
- Validation results and any unresolved clarification are reported.
