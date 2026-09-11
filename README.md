# LLD Coach

> A LeetCode-style practice platform for Low-Level Design (LLD).

LLD Coach is a focused practice platform that helps learners improve their object-oriented and domain-design skills through repeated design, submission, evaluation, and feedback.

The platform is intentionally designed as a small MVP rather than a full learning-management or assessment system.

---

## 1. Problem Statement

Low-Level Design is difficult to practice because there can be multiple valid solutions to the same design problem.

Traditional coding platforms provide deterministic test cases, but LLD requires evaluating qualities such as:

* Responsibility assignment
* Abstraction
* Encapsulation
* Relationships between objects
* Extensibility
* Overall design quality

LLD Coach provides a structured practice loop:

```text
Choose Problem
      ↓
Read Requirements
      ↓
Start Attempt
      ↓
Design / Explain
      ↓
Submit
      ↓
Evaluate
      ↓
Receive Feedback
      ↓
Review History
      ↓
Practice Again
```

The goal is not to identify one "correct" class diagram, but to provide useful and repeatable feedback on the quality of a learner's design.

---

## 2. MVP Features

### Problems

The MVP contains three LLD problems:

1. Parking Lot — Easy
2. Vending Machine — Medium
3. Elevator System — Medium

Each problem provides a clear domain context and requirements before the learner begins an attempt.

### Practice Attempts

A learner can:

* Start an attempt
* Write their class/design structure
* Add code or pseudocode
* Explain design decisions
* Save work as a draft
* Submit an attempt

### Evaluation

Submitted attempts can be evaluated against a structured rubric.

| Dimension      |   Weight |
| -------------- | -------: |
| Responsibility |      25% |
| Abstraction    |      20% |
| Encapsulation  |      15% |
| Relationships  |      15% |
| Extensibility  |      15% |
| Design Quality |      10% |
| **Total**      | **100%** |

The current deterministic evaluator also uses problem-specific signals for domains such as Parking Lot, Vending Machine, and Elevator System.

### Feedback

Evaluation results include:

* Overall score
* Dimension-level scores
* Strengths
* Identified issues
* Why an issue matters
* Concrete suggestions for improvement

### Attempt History

Previous attempts are preserved so learners can:

* Review completed attempts
* Compare scores
* See previous feedback
* Retry a problem without overwriting the previous attempt

### UI

The React frontend provides:

* Problems page
* Practice page
* Evaluation and feedback
* Attempt history
* Practice Again workflow
* Light/dark theme
* Responsive, developer-focused UI

---

## 3. Architecture

LLD Coach is implemented as a simple monolithic application.

```text
                    ┌─────────────────────┐
                    │     React UI        │
                    │   TypeScript/Vite   │
                    └──────────┬──────────┘
                               │ REST
                               ▼
                    ┌─────────────────────┐
                    │   Spring Boot API   │
                    ├─────────────────────┤
                    │ Problem             │
                    │ Attempt             │
                    │ Submission          │
                    │ Evaluation          │
                    │ Feedback            │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │ In-Memory Storage   │
                    └─────────────────────┘
```

### Backend

* Java 21
* Spring Boot
* Maven
* REST APIs
* Bean Validation
* In-memory storage

The backend is organized around the core domain areas:

```text
com.lldcoach
├── problem
├── attempt
├── evaluation
└── common
```

### Frontend

* React
* TypeScript
* Vite
* CSS

The frontend communicates with the backend through REST APIs.

---

## 4. Evaluation Architecture

Evaluation is separated from the practice workflow through an evaluator abstraction.

Conceptually:

```text
                ┌──────────────┐
                │  Evaluator   │
                │  interface   │
                └──────┬───────┘
                       │
              ┌────────┴─────────┐
              ▼                  ▼
   ┌──────────────────┐  ┌──────────────────┐
   │ Deterministic    │  │ Future LLM       │
   │ Evaluator        │  │ Evaluator        │
   └──────────────────┘  └──────────────────┘
```

The MVP uses the deterministic evaluator because it provides:

* Reproducible results
* Fast evaluation
* Easy testing
* No external service dependency
* Predictable behavior

An LLM-based evaluator can be introduced later for more semantic design reasoning without changing the learner's core practice workflow.

This separation also allows additional evaluator implementations to be added in the future.

---

## 5. Attempt Lifecycle

Attempts follow a simple lifecycle:

```text
DRAFT
  ↓
SUBMITTED
  ↓
EVALUATING
  ↓
COMPLETED
```

If evaluation fails:

```text
EVALUATING
     ↓
   FAILED
```

A failed evaluation does not destroy the learner's submission. The attempt can be evaluated again.

---

## 6. API

### Problems

```http
GET /api/problems
GET /api/problems/{id}
```

### Attempts

```http
POST /api/problems/{problemId}/attempts
GET  /api/attempts/{attemptId}
PUT  /api/attempts/{attemptId}
POST /api/attempts/{attemptId}/submit
POST /api/attempts/{attemptId}/evaluate
```

### Evaluation

```http
GET /api/attempts/{attemptId}/evaluation
```

### History

```http
GET /api/attempts
GET /api/problems/{problemId}/attempts
```

---

## 7. Running the Project

### Prerequisites

* Java 21
* Maven 3.9+
* Node.js and npm

### Start the Backend

From the project root:

```bash
mvn spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

### Start the Frontend

Open another terminal:

```bash
cd frontend
npm install
npm run dev
```

The frontend runs on:

```text
http://localhost:5173
```

Both the backend and frontend should be running together.

---

## 8. Testing

### Backend

Run:

```bash
mvn clean test
```

The backend includes tests covering:

* Problem behavior
* Attempts and submissions
* Evaluation
* Feedback
* Attempt history
* Deterministic evaluator behavior

The evaluator tests specifically verify that weak, reasonable, strong, and irrelevant submissions produce meaningfully different results.

### Frontend

Run:

```bash
cd frontend
npm run build
```

This runs TypeScript compilation and the Vite production build.

---

## 9. Design Decisions

### Why deterministic evaluation?

LLD does not have a single universally correct implementation.

However, introducing an LLM into the MVP would add:

* Nondeterministic results
* Latency
* External service dependency
* Additional failure modes
* Cost

The MVP therefore uses deterministic evaluation for predictable and testable feedback.

The evaluator abstraction keeps the architecture open for future LLM-based semantic evaluation.

### Why a monolith?

The current system has a small domain and a straightforward workflow.

A monolithic Spring Boot application provides:

* Lower operational complexity
* Faster development
* Easier local execution
* Clear domain boundaries

There is no current requirement that justifies microservices or distributed infrastructure.

### Why in-memory storage?

The assignment focuses primarily on LLD and product behavior.

In-memory storage keeps the MVP simple while allowing persistent storage such as PostgreSQL to be introduced later.

### Why multiple attempts?

LLD improves through iteration.

Preserving previous attempts allows the learner to:

```text
Attempt → Feedback → Improve → Retry → Compare
```

rather than replacing their previous work.

---

## 10. Extensibility

The current design leaves clear extension points.

### Additional Evaluators

A new evaluator can implement the existing evaluator abstraction.

Possible future implementations include:

```text
DeterministicEvaluator
LlmEvaluator
HybridEvaluator
```

### Additional Submission Formats

The practice workflow can be extended to support formats such as:

* Structured class definitions
* Code
* Pseudocode
* Diagram-based submissions

without changing the core attempt lifecycle.

### Persistent Storage

The current in-memory repositories can later be replaced with a database-backed implementation.

Potential future entities include:

```text
Problem
Attempt
Submission
Evaluation
```

### More Problems

Additional LLD problems can be added without changing the overall practice workflow.

---

## 11. Scope

### Included

* LLD problem practice
* Attempts
* Draft saving
* Submission
* Evaluation
* Feedback
* Attempt history
* Retry workflow
* Light/dark theme

### Intentionally Excluded

The MVP does not attempt to provide:

* Authentication
* User profiles
* LMS functionality
* Admin dashboards
* Real-time collaboration
* Microservices
* Kubernetes
* Complex distributed infrastructure
* Production-grade persistence
* Advanced diagram editors

These can be considered only if future product requirements justify them.

---

## 12. Project Structure

```text
LLDcoach/
│
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── lldcoach/
│   │               ├── problem/
│   │               ├── attempt/
│   │               ├── evaluation/
│   │               └── common/
│   │
│   └── test/
│
├── frontend/
│   ├── src/
│   │   ├── pages/
│   │   │   ├── ProblemsPage.tsx
│   │   │   ├── PracticePage.tsx
│   │   │   └── HistoryPage.tsx
│   │   ├── services/
│   │   ├── types/
│   │   ├── App.tsx
│   │   └── ...
│   ├── package.json
│   └── vite.config.ts
│
├── pom.xml
└── README.md
```

---

## 13. Future Improvements

Potential next steps include:

1. PostgreSQL persistence
2. Authentication and learner profiles
3. LLM-assisted semantic evaluation
4. Hybrid deterministic + LLM evaluation
5. Diagram-based submission
6. More LLD problems
7. Better comparison between attempts
8. Personalized feedback and learning recommendations

These are deliberately outside the current MVP scope.

---

## 14. AI-Assisted Development

AI tools were used during development for implementation support, debugging, design exploration, evaluator design, and frontend development.

Important architectural decisions were reviewed against the assignment requirements and the resulting implementation was tested locally.

See [`AI_USAGE.md`](AI_USAGE.md) for details about significant AI-assisted decisions, alternatives considered, and decisions accepted or rejected.

---

## 15. Summary

LLD Coach is a focused LLD practice system built around one core idea:

> **Practice LLD through repeated design, evaluation, feedback, and iteration.**

The MVP prioritizes a clear learner workflow, domain-oriented design, explainable evaluation, maintainable architecture, and practical engineering scope.
