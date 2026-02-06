# Implementation Plan: [FEATURE]

**Date**: [DATE]
**Spec**: [LINK]

## Summary

[Extract from feature spec: primary requirements + technical approach from research]

## Technical Context

<!-- 
    ACTION REQUIRED: Replace the content in this section with the technical details
    for the project. The structure here is presented in advisory capacity to guide 
    the iteration process.
-->

**Languages/Versions**: [e.g. Python 3.11, Swift 5.9, Rust 1.75, or NEEDS CLARIFICATION]
**Primary Dependencies**: [e.g. FastAPI, UIkit,  LLVM or NEEDS CLARIFICATION]
**Storage**: [if applicable, e.g. PostgreSQL, Redis, S3 or NEEDS CLARIFICATION]
**Testing**: [e.g. Pytest, XCTest, Jest or NEEDS CLARIFICATION]
**Target Platforms**: [e.g., Linux servers, iOS 17, Web Browsers or NEEDS CLARIFICATION]
**Project Type**: [single/web/mobile - determine source structure]
**Performance Goals**: [Domain-specific, e.g. 1000 req/s, 99.9% uptime or NEEDS CLARIFICATION]
**Constraints**: [domain-specific,  e.g. <200ms p95, <100MB memory, offline-capable or NEEDS CLARIFICATION]
**Scale/Scope**: [domain-specific, e.g. 10k users, 1M items or NEEDS CLARIFICATION]

## Project Structure

### Documentation (this future)

```text
/specs/[feature]/
    ├── plan.md               # Implementation plan (this document)
    └── spec.md       # Feature specification
```

### Source Code (repository root)

<!-- 
    ACTION REQUIRED: Replace the placeholder tree below with the concrete layout
    for this feature. Delete unused options and expand the chosen structure with
    real paths (e.g., apps/admin, packages/something). The delivered plan must
    not include Options labels.
 -->

```text
# [REMOVE IF UNUSED] Option 1: Single project (DEFAULT)
src/
├── models/           # Data models
├── services/         # Core business logic
├── cli/             # Command-line interface
└── lib/            # Shared libraries/utilities

tests/
├── contract/
├── integration/
└── unit/

# [REMOVE IF UNUSED] Option 2: Web application (when frontend + backend detected)
backend/
├── src/
│   ├── models/
│   ├── services/
│   └── api/
└── tests/

frontend/
├── src/
│   ├── components/
│   ├── pages/
│   └── services/
└── tests/
```

**Structure Decisions**: [Document the selected structure and reference the real
directories captured above]

<!-- 
    ==========================================================================
    IMPORTANT: The task below is SAMPLE TASKS for illustration purposes only.

    You MUST replace these with the actual tasks based on:
    - User stories from the spec.md
    - Feature requirements from this file
    - Entities required for the use case
    - Enpoints required

    DO NOT keep the sample tasks.
    ==========================================================================
 -->

## Phase 1: Setup (Shared Infrastructure)

**Purpose**:  Project initialization and basic structure.

- [ ] T001: Create project per implementation plan
- [ ] T002: Initialize [language] project with [frameworks] dependencies
- [ ] T003: Configure linting and formatting tools

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be completed before ANY user story can be implemented.

**CRITICAL**: No user story work can begin until this phase is complete.

Example of foundational tasks (adjust based on your project):

- [ ] T004: Setup database schema and migrations framework
- [ ] T005: Implement authentication and authorization framework
- [ ] T006: Setup API and middleware structure
- [ ] T007: Create base models/entities that all stories depend on
- [ ] T008: Configure error handling and logging infrastructure
- [ ] T009: Setup environment configuration management

**Checkpoint**: Foundation ready - User story implementation can now begin in parallel.
---

## Phase 3: User Story 1 - [Title] (Priority: P1)

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Tests for User Story 1

- [ ] T010: [P] [US1] Contract test for [endpoint] in test/contract/test_[name].py
- [ ] T011: [P] [US1] Integration test for [user journey] in test/integration/test_[name].py

### Implementation Tasks for User Story 1

- [ ] T012: [P] [US1] Create [Entity1] model in src/models/[entity1].py
- [ ] T013: [P] [US1] Create [Entity2] model in src/models/[entity2].py
- [ ] T014: [US1] Implement [Service] in src/services/[service].py (depends on T012, T013)
- [ ] T015: [US1] Implement [Endpoint/Feature] in src/[location]/[file].py
- [ ] T016: [US1] Add validation and error handling
- [ ] T017: [US1] Add logging for user story 1 operations

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently.

---

## Phase 4: User Story 2 - [Title] (Priority: P2)

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Tests for User Story 2

- [ ] T018: [P] [US2] Contract test for [endpoint] in test/contract/test_[name].py
- [ ] T019: [P] [US2] Integration test for [user journey] in test/integration/test_[name].py

### Implementation Tasks for User Story 2

- [ ] T020: [P] [US2] Create [Entity] model in src/models/[entity].py
- [ ] T021: [US2] Implement [Service] in src/services/[service].py
- [ ] T022: [US2] Implement [Endpoint/Feature] in src/[location]/[file].py
- [ ] T023: [US2] Integrate with User Story 1 components (if needed)

**Checkpoint**: At this point, User Story 1 AND 2 should be work independently.

---

## Phase 5: User Story 3 - [Title] (Priority: P3)

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Tests for User Story 3

- [ ] T024: [P] [US3] Contract test for [endpoint] in test/contract/test_[name].py
- [ ] T025: [P] [US3] Integration test for [user journey] in test/integration/test_[name].py

### Implementation Tasks for User Story 3

- [ ] T026: [P] [US3] Create [Entity] model in src/models/[entity].py
- [ ] T027: [US3] Implement [Service] in src/services/[service].py
- [ ] T028: [US3] Implement [Endpoint/Feature] in src/[location]/[file].py

**Checkpoint**: All user stories should now be independently functional

---

[Add more phases as needed, following the same pattern]

---

## Phase N: Polish & Cross-Cutting Concerns

**Purpose**: Inprovements that affect multiple user stories

- [ ] TXXX Documentation updates in docs/
- [ ] TXXX Code cleanup and refactoring
- [ ] TXXX Performance optimizations across all user stories
- [ ] TXXX Additional unit tests in test/unit/
- [ ] TXXX Security hardening

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately.
- **Foundational (Phase 2)**: Depends on Phase 1 completion - BLOCKS all user stories.
- **User Stories (Phases 3+)**: All depend on Foundational phase completion
   - User stories can then proceed in parallel (if staffed)
    - Or sequentially in priority order (P1 -> P2 -> P3)
- **Polish (Final Phase)**: Depends on all desired user stories being complete.

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories.
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - May integrate US1 but should be independently testable.
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) - May integrate US1/US2 but should be independently testable.

### Within Each User Story

- Models before Services
- Services before Endpoints
- Core implementation before integration
- Setup complete before moving to next priority
- Tests after implementation

## Notes

- [Story] label maps task to specific user story for traceability.
- Each user story should be independently complete and testable.
- Verify test pass
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence
 
