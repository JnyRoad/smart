---
description: Task list template for feature implementation
---

# Tasks: [FEATURE NAME]

**Input**: Design documents from /specs/[###-feature-name]/
**Prerequisites**: plan.md, spec.md, and any applicable research.md, data-model.md, contracts/,
quickstart.md

## Task format

Every task uses:

- [ ] T001 [P] [US1] Description with an exact file path

T IDs are unique and ordered. Use [P] only for independent work and [US#] only in a user-story
phase. Omit labels that do not apply.

## Phase 1: Setup

[Shared project setup tasks, each with a checkbox, ID, and path.]

## Phase 2: Foundational

[Blocking prerequisites required by the planned stories.]

## Phase 3+: User Stories

For each prioritized story, include its goal and independent validation. Add only the tasks
required by the story.

### Validation

[Meaningful behavior regression tasks when behavior changes, following
development.md#验证. Use link, structure, parse, or diff checks for pure documentation and low-risk
configuration.]

### Implementation

[Models, services, interfaces, integration, and other tasks with exact paths.]

## Final Phase: Polish and Cross-Cutting Work

[Only work required across stories, with exact paths and validation.]

## Dependencies and Execution Order

[Record phase and story dependencies, ordering constraints, and safe parallel work.]

## Implementation Strategy

[Record the smallest independently verifiable delivery slices and their validation.]

## Notes

- Preserve the task checklist format and traceability.
- Do not delete or rewrite completed tasks when extending the list.
