---
description: Convert existing tasks into actionable, dependency-ordered GitHub issues for the feature.
tools: ['github/github-mcp-server/list_issues', 'github/github-mcp-server/issue_write']
---

## Shared contract

Follow [the command contract](README.md) for path resolution, existing-artifact protection,
hooks, authorization, and validation.

## User Input

$ARGUMENTS

## Scope and authorization

This command performs external writes. Run it only when the user explicitly asks to create GitHub
issues for the current task list. Analysis, specification, planning, and convergence results do
not grant that permission. It never deletes issues.

## Workflow

1. Run .specify/scripts/bash/check-prerequisites.sh --json --require-tasks --include-tasks from the
   repository root and parse the absolute FEATURE_DIR and available documents. Load the constitution
   when present and read tasks.md.
2. Read remote.origin.url with git config --get remote.origin.url. Stop unless it identifies the
   repository represented by a GitHub remote; use that repository for every subsequent tool call.
3. Collect each task ID matching T followed by at least three digits. Use the declared
   github/.../list_issues tool without a state filter, paginate with its cursor and perPage 100,
   and match whole IDs in issue titles. Stop once all task IDs are accounted for or no page remains.
4. For each unmatched task, strip its checkbox and labels and create one issue with the declared
   github/.../issue_write tool. Use exactly one matched task ID in the canonical title
   `<task-id>: <description>`, preserving the full ID (for example, `T1234`). Skip IDs already found
   and report them. Do not create issues in another repository.
5. Report the remote repository, created issue IDs, skipped task IDs, and failures.

Apply the shared hook protocol to before_taskstoissues and after_taskstoissues; do not interpret
hook conditions in this command.
