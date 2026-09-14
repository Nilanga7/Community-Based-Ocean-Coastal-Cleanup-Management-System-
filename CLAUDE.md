# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

This repository currently contains **no source code** — `backend/` and `frontend/` are empty
placeholder directories and no commit has been made yet (git repo has no history). Planning
documentation lives under `docs/` (SRS, project proposal, final report PDFs). As of the shared
ER-modeling session, `docs/` should also gain the confirmed workflow diagrams
(`Project Workflow.drawio.new.pdf`, `Activity Diagram.drawio.pdf`) and the resulting schema design
(`docs/er-diagram.mmd` — see "Data model" below) once committed; add them there rather than leaving
them as loose uploads.

Because there is no existing code, architecture, or build tooling to discover, treat this as a
greenfield project: there are no build/lint/test commands to run yet, and no established code
conventions to follow. When the user starts adding code, prefer setting up `backend/` and
`frontend/` according to the technology choices documented in the SRS (below) unless the user
directs otherwise. **Once real code lands, update this file** with actual build/lint/test
commands — don't leave this section stale.

## What this system is

The Community-Based Ocean & Coastal Cleanup Management System is a web platform connecting
citizens, volunteer divers, NGOs/organizations, and government authorities (e.g. MEPA) to report,
verify, and coordinate cleanup of polluted ocean/coastal sites in Sri Lanka.

Core user roles (each with distinct permissions/UI needs — see `docs/Software Requirements
Specification - Group 8.pdf` §2.2):
- **Volunteer non-divers** — submit pollution/cleanup reports (photos/videos/GPS), vote/comment to verify others' reports, receive location-based cleanup alerts, join cleanup projects.
- **Non-registered (public) users** — read-only access to verified reports and awareness content; also surfaced a low-visibility report (see "Community verification" below) to encourage them to register and help verify it.
- **Volunteer divers** — maintain diving certification/equipment/region profile, receive location-based cleanup alerts, join underwater cleanup activities, browse/apply to organization-posted diver opportunities.
- **Government authority officers** — review admin-approved reports, approve (with cleanup instructions)/reject/request clarification.
- **Organizations** (NGOs, tourism operators, research teams) — post diver opportunities, view analytics. (Cleanup *projects* are system-generated from approved reports, not organization-created — see "Confirmed core workflow.")
- **System administrator** — verifies diver/organization registration documents, reviews reports (approve/reject/request clarification), moderates content, issues warnings/restrictions for false reports.

Planned feature modules (SRS §5): user registration/profiles, pollution reporting & incident
submission, community verification (voting/comments), admin review & moderation, government
coordination & approval, alerts/notifications, project progress monitoring, diver opportunity
management, and a reporting/analytics dashboard.

## Confirmed core workflow

The SRS describes the modules; this section records the actual end-to-end pipeline the team
confirmed during the ER-modeling session (2026-09), since it's more specific than — and in one
place corrects — the SRS module descriptions above (cleanup projects are **system-generated**,
not organization-created). Treat this as the authoritative process description; update it if the
team changes the flow, and keep `docs/er-diagram.mmd` in sync with it.

1. **Submission.** A Volunteer (diver or non-diver) submits a Cleanup Request Post — description, date/time, photos/videos, GPS location.
2. **Community verification (≈2 weeks).** Other users vote true/false and may comment; trust % recalculates on each vote. If fewer than **8 users** have voted, the system surfaces a pop-up on the home page — visible even to unregistered/public users — prompting nearby people to help verify it. After the ~2-week window, the post is marked Community Verified or Community Unverified (≥75% trust threshold) and **either way** goes to Admin — there's no skip-admin path.
3. **Admin review.** Admin approves, rejects, or requests clarification (clarification → user updates → resubmitted → automatically returns to admin for another decision). Admin can also issue a warning to a user for a false report, and restrict the user on a repeat offense. The submitter is notified of the outcome either way. On approval, forwarded to a Government Officer.
4. **Government approval.** Same approve/reject/clarify pattern. Approval includes cleanup instructions (equipment/specialist guidance etc.) that the project owner will read later. Both admin and government rejections notify the submitter directly; approval notifies the admin and the submitter (who becomes the project owner).
5. **Automatic project creation.** On government approval, the system auto-creates a Cleanup Project from the request post. The project owner is whoever originally submitted the post — **not** necessarily an organization.
6. **Owner sets recruitment needs.** Before any alert goes out, the owner sets divers needed, participants needed, a **minimum participant threshold**, specialists needed, and equipment required (informed by the government officer's instructions).
7. **Alert & escalation.** The system alerts eligible users within a 10km radius of the report location. Every user has a home address captured at registration, geocoded to lat/long, used for this distance calculation. After 2 weeks, if the owner's minimum participant threshold hasn't been reached, the system automatically expands the radius by another 10km and alerts **only the newly eligible users** (no duplicate alerts to the first ring). This can repeat.
8. **Owner decision.** The project owner decides whether the minimum participant threshold has been met. If yes: finalize project details (date, equipment, specialists) using the government officer's instructions, and the system notifies other participants, admin, and the government officer. If no: the project is stopped/cancelled, and the same set of people is notified of that outcome.
9. **Progress tracking.** The owner posts progress updates (notes/images) over time. Project status is a single manually-updated field — `PLANNING → PARTICIPANT_RECRUITMENT → DETAILS_FINALIZED → READY_FOR_CLEANUP → CLEANUP_IN_PROGRESS → COMPLETED` (or `CANCELLED`) — there is no separate numeric completion percentage.
10. **Diver Opportunities are a separate track.** Organizations can independently post diver opportunities (recruiting divers for org-run activities); this is unrelated to the auto-generated project pipeline above — a project's "specialist needed" does **not** create a diver opportunity.

## Data model

The confirmed workflow above was translated into a full ER diagram (26 entities) —
`docs/er-diagram.mmd` (mermaid `erDiagram` source, reviewed and committed) — which is now real,
not pending: the JPA entities under `backend/.../common/entity/` and the baseline Flyway
migration (`backend/src/main/resources/db/migration/V1__init_schema.sql`) are both generated from
it and live in the repo. This supersedes the "Module scope" table below wherever they'd disagree —
the table exists to assign ownership, the `.mmd` file is the schema source of truth.

Key structural decisions baked into that model (context for anyone extending it):
- `USER` is the ISA root with five subtype tables (`VolunteerNonDiver`, `VolunteerDiver`,
  `Organization`, `GovernmentOfficer`, `Admin`), each keyed on `userId`.
- `CleanupRequestPost` and `CleanupProject` are **separate, linked** tables (1:1 once approved) —
  a report only becomes a project after government approval; don't conflate the two.
- `CleanupRequestPost` carries `currentStage` (COMMUNITY/ADMIN/GOVERNMENT/CLOSED) and
  `stageStatus` (PENDING/NEEDS_CLARIFICATION/RESUBMITTED/APPROVED/REJECTED) rather than one flat
  status enum — admin and government both use the identical approve/reject/clarify pattern, and
  this avoids doubling every status value per stage.
- `ReportReviewAction` is a shared audit-log table for **both** admin's and government's
  decisions (distinguished by `reviewStage`) — multiple clarify/resubmit rounds are separate rows,
  not overwritten fields, because the workflow explicitly allows repeat clarification cycles.
- `ProjectAlert` / `ProjectAlertRecipient` track each escalation round and exactly who was
  notified in it — required to support "only alert newly eligible users" on radius expansion.
  `ProjectParticipant` is the separate confirmed-joiner roster checked against
  `minimumParticipants`.
- `UserSanction` records warnings/restrictions issued for false reports, optionally linked to the
  triggering `CleanupRequestPost`.
- No analytics table exists — Analytics has no owned tables (per SRS/module scope) and aggregates
  the other tables at query time.

## Intended architecture (per SRS §2.1/§3.3, not yet implemented)

Standard 3-tier web architecture:
- **Presentation layer** — React.js, built and shipped as a **Progressive Web App**: installable on desktop/mobile without an app store, works offline for cached content via a service worker, supports push notifications.
- **Application layer** — Spring Boot REST API handling business logic, auth, report processing/verification, notifications, and analytics.
- **Data layer** — MySQL for persistent storage of users, reports, votes, projects, diver profiles, and analytics data.

Frontend and backend communicate over REST APIs using JSON. External integrations called out in
the SRS: the Google Maps API for map-based reporting, location display, and geocoding user
addresses to lat/long (needed for the alert-radius calculation — see "Confirmed core workflow"),
and a notification service for alerts. Explicitly out of scope for the current design: native
mobile apps, real-time GPS tracking of volunteers, and legal enforcement features.

When scaffolding the actual `backend/` and `frontend/` projects, this maps to:
- `backend/` → a Spring Boot (Java) application exposing REST endpoints, backed by MySQL, built with Maven.
- `frontend/` → a React.js PWA, scaffolded with Vite (`vite-plugin-pwa` for the manifest/service worker) consuming those REST endpoints.

Supporting tooling from the report: **Git/GitHub** for version control, **Docker** (via
`docker-compose.yml` at the repo root) so all contributors run an identical local MySQL instance,
and cloud deployment targeting AWS/GCP/Azure.

## Team & module ownership

Six-person team; nine functional modules from SRS §5. Module ownership is organized **vertically**
— each owner builds their module's database tables, API endpoints, and UI screens together, not
split by frontend-only/backend-only — because the individual demonstration requires each person to
explain and show both layers of the features they built.

| Owner | Module(s) | Notes |
|---|---|---|
| KMNI Ranasinghe | User Registration & Profile Management + Government Coordination | Foundation — build and merge first; everything else depends on auth/roles. |
| VPC Rasanga | Pollution Reporting | System entry point: cleanup request post submission, evidence upload, location capture. |
| SMR Kaveeshwara | Community Verification + Diver Opportunity Management | Both are trust/participation features built on voting-style logic and user profiles. |
| MDB Amarakoon | Admin Review | Owns both the report-moderation queue and the registration-verification queue (see Module scope). |
| MRS Nishadini | Alert & Notification + Project Monitoring | Both are status-change driven — a project progress update is the alert payload; this owner also builds the 2-week escalation job. |
| HGMN Kodiweera | Analytics | Downstream of every other module's data — dashboards, trends, participation stats. |

### Module scope

Confirmed against the ER session's data model (`docs/er-diagram.mmd`) — this replaces the earlier
SRS-only draft. Still worth re-checking as modules land, since implementation may surface further
boundary questions.

| Module | Owned tables | Key endpoints (draft) | Key screens (draft) |
|---|---|---|---|
| User Registration & Profile Mgmt | `User`, `VolunteerNonDiver`, `VolunteerDiver`, `Organization`, `GovernmentOfficer`, `Admin`, `VolunteerInterest`, `DiverEquipment`, `VerificationDocument` | `/auth/*`, `/users/*`, `/users/{id}/profile` | Sign up/login, profile edit, diver/org profile setup with document upload |
| Government Coordination | writes `ReportReviewAction` (reviewStage=GOVERNMENT) and `CleanupRequestPost.currentStage/stageStatus`; triggers `CleanupProject` creation on approval | `/gov/requests`, `/gov/requests/{id}/decision` | Officer review queue, approve (+instructions)/reject/clarify |
| Pollution Reporting | `CleanupRequestPost`, `RequestPostMedia`, `District` | `/requests`, `/requests/{id}`, `/requests/{id}/media` | Submit cleanup request post (photo/video/GPS), post detail |
| Community Verification | `ReportVote`, `ReportComment`; writes `CleanupRequestPost.communityResult/trustPercentage` | `/requests/{id}/votes`, `/requests/{id}/comments` | Verify/vote UI, comment thread, low-vote public prompt |
| Diver Opportunity Mgmt | `DiverOpportunity`, `OpportunitySignup` | `/opportunities`, `/opportunities/{id}/signup` | Opportunity listing, signup flow |
| Admin Review | writes `ReportReviewAction` (reviewStage=ADMIN) and `CleanupRequestPost.currentStage/stageStatus`; owns `RegistrationVerification`, `RegistrationReviewAction`, `UserSanction` | `/admin/requests`, `/admin/registrations`, `/admin/users/{id}/sanction` | Moderation dashboard (one queue, two entities underneath — reports vs. registrations), sanction flow |
| Alert & Notification | `Notification`, `ProjectAlert`, `ProjectAlertRecipient` | `/alerts`, `/notifications` | Alert feed/settings |
| Project Monitoring | `CleanupProject`, `ProjectParticipant`, `ProjectProgressUpdate`, `ProjectProgressImage` | `/projects`, `/projects/{id}/progress`, `/projects/{id}/finalize` | Project setup (recruitment needs), progress tracker, finalize-details screen |
| Analytics | none (read-only aggregation over other tables) | `/analytics/*` | Dashboards, trend charts, participation stats |

`CleanupRequestPost` is written by **three different module owners in sequence**
(Pollution Reporting creates it; Community Verification writes `communityResult`/`trustPercentage`;
Admin Review and Government Coordination both write `currentStage`/`stageStatus` at their
respective stages) — route every write through a service-layer method scoped to that stage rather
than mutating the fields directly, so two owners don't race on the same row.

## Working conventions

- **One module = one owner = one feature branch = one PR.** Branch naming: `feature/<module-slug>-<owner-initials>`, e.g. `feature/pollution-reporting-vpc`.
- Don't edit another module's controller/service/component files without checking with its owner first.
- Shared code lives in `backend/.../common/` (User/Role entities, security config, exceptions) and `frontend/src/shared/` (API client, auth context, shared components) — changes here affect everyone, so flag them to the team before merging.
- `main` should stay demoable; merges require at least one teammate's review.
- Secrets (DB credentials, Google Maps API key) go in `.env` / `application-local.properties`, gitignored — never committed.
- **Named constants** — encode all of these as named constants, not magic numbers, wherever checked:
  - Community verification trust threshold: **75%**
  - Community verification window: **14 days** (report is forwarded to admin either way once this elapses)
  - Low-visibility public-prompt vote count: **8 votes**
  - Initial alert radius: **10km**; escalation increment: **10km**; escalation check interval: **14 days**

### API conventions

Not yet ratified — proposed defaults so the six modules integrate cleanly without a late
reconciliation pass. Adjust and lock in during step 2 (auth/roles), then treat as binding:
- REST paths: plural nouns, kebab-case for multi-word resources (`/diver-opportunities`, not `/diverOpportunities`).
- Request/response bodies: camelCase JSON fields.
- Errors: a consistent shape across all modules, e.g. `{ "error": { "code": "...", "message": "..." } }`, with standard HTTP status codes (400/401/403/404/409/500) — avoid ad-hoc per-module error formats.
- Pagination (reports, comments, projects, opportunities): consistent query params, e.g. `?page=&size=`.
- Every list/detail endpoint that returns another module's entity should return IDs, not embedded objects, unless the SRS explicitly calls for a combined view — keeps modules decoupled.

### Commit & PR conventions

- Commit messages: short imperative summary line (`Add report submission endpoint`), body only if the "why" isn't obvious.
- PRs should state which module they touch, link the relevant SRS section, and call out any change to shared code (see above) explicitly in the description so the reviewer knows to look there.
- Reviewer checks: does it stay inside the module's owned tables/endpoints (per Module scope above), does it touch `common/`/`shared/` without a heads-up, does it hardcode the 75% threshold or other named constants above, does it write to `CleanupRequestPost.currentStage/stageStatus` from outside that stage's owning module.

### Testing expectations

User Registration & Profile Management (backend + frontend) has landed with real test coverage —
the pattern below is what every other module should follow as it lands its own first endpoint.

- **Backend** — from `backend/`: `./mvnw test` (or `mvnw.cmd test` on Windows). Requires the
  Docker MySQL from "Local development setup" to be running first (`docker-compose up -d` from
  the repo root) — tests hit the real dev database via `@SpringBootTest`/`@DataJpaTest`, not an
  in-memory substitute, with `@Transactional` rolling back DB writes and `@AfterEach` cleanup for
  anything written to disk (e.g. uploaded verification documents). Add at least a
  controller/service test for every new endpoint, plus a unit test for any shared `common/`
  utility with real branching logic (e.g. `RequestAuthorization`).
- **Frontend** — from `frontend/`: `npm test` (Vitest + React Testing Library; `npm run
  test:watch` for watch mode during development). No backend/Docker dependency — component tests
  mock the `shared/api`/module `api/` layers rather than hitting a real server. Add at least a
  component test per screen covering its validation/error states and, for anything behind
  `ProtectedRoute`, an auth-failure case.

## Open technical decisions

Things the SRS doesn't pin down and that block more than one module — resolve these during Build
order step 1–2 and record the decision here once made, rather than letting each owner guess
differently:
- **Auth mechanism**: JWT vs. server-side session. Affects every module immediately (Spring Security config, frontend auth context, PWA offline behavior).
- **DB migration strategy**: Flyway/Liquibase-managed migrations vs. Hibernate `ddl-auto`. Matters once six people are extending a schema that starts from one shared session.
- **File/media storage**: local disk, S3-compatible bucket, or DB blob — needed for `RequestPostMedia` (multiple photos/videos per report) and `VerificationDocument` (diver/org registration docs). Affects Pollution Reporting, Admin Review, and User Registration.
- **Notification service**: which provider/library actually sends the alerts referenced in the SRS (push, email, or both) — affects Alert & Notification module's build.
- **Google Maps API key ownership**: who provisions it and how it's shared into each teammate's local `.env` without committing it. Now used for two things: map display/report location, and geocoding user home addresses to lat/long at registration.
- **Escalation scheduler**: how the 2-week "check participant count, expand radius if needed" job actually runs — Spring `@Scheduled` polling, a dedicated cron job, or a queue/worker. Owned by Alert & Notification; needs to be decided before that module's first PR.
- **Distance calculation**: Haversine formula computed in application code vs. MySQL spatial functions/a geospatial index, for both the alert-radius query and any "nearby reports" feature. Matters more if the user base grows past what a naive per-request scan can handle, but worth deciding up front so Pollution Reporting and Alert & Notification agree on how location queries are written.

## Local development setup

1. **Start MySQL.** From the repo root: `docker-compose up -d`. This brings up a single
   `mysql:8.0` container, publishing it on host port `3308` (container's `3306`), with a
   `cleanup_system` database and root password `MySqL11` created automatically — and persists
   data in the `db_data` named volume across restarts. All three values (`DB_PORT`, `DB_NAME`,
   `DB_PASSWORD`) come from environment variables with those defaults baked in, so this works with
   no further setup on a fresh clone.
2. **Configure secrets (optional unless you're changing a default).** Copy `.env.example` to
   `.env` at the repo root and fill in real values. `docker-compose.yml` reads `.env`
   automatically. `application.properties` reads the same `DB_*` variable names via
   `${VAR:default}` placeholders, but Spring Boot doesn't load `.env` files itself — export them
   into your shell (or your IDE's run configuration) before starting the backend if you want them
   to take effect there too; otherwise it silently uses the same defaults as docker-compose, and
   the two stay in sync automatically. `GOOGLE_MAPS_API_KEY` and `NOTIFICATION_SERVICE_API_KEY`
   are placeholders for now — see "Open technical decisions" for who provisions/picks these.
3. **Run the backend.** From `backend/`: `./mvnw spring-boot:run` (or `mvnw.cmd` on Windows).
   `spring.flyway.enabled=true` means Flyway runs `V1__init_schema.sql` against the empty database
   automatically on first startup (creating all 26 tables + `district`); `ddl-auto=validate` means
   Hibernate never generates or alters schema itself — all schema changes from here on go through
   a new `V<n>__...sql` migration file, not entity annotation changes alone.
4. **Seed data.** No seed script exists yet — the schema starts completely empty after Flyway
   runs, including no admin user. Creating one (and any other baseline data) is part of Build
   order step 2 (User Registration & Profile Management), not step 1.
5. **Frontend.** `frontend/` is scaffolded — React 19 + Vite, with `vite-plugin-pwa`
   (`registerType: 'autoUpdate'`) wired up in `vite.config.js` and the PWA manifest already
   branded (name, theme/background color, icons) for this project. UI content is still the
   default Vite+React starter page (`src/App.jsx`) — not yet wired to the backend: no API client,
   no `frontend/.env`, and no `frontend/src/shared/` yet (see Working conventions). Linting is
   Oxlint (`.oxlintrc.json`); there's no frontend test runner configured yet. From `frontend/`:
   - `npm install` — once, after cloning
   - `npm run dev` — Vite dev server with HMR, default `http://localhost:5173`
   - `npm run build` — production build to `frontend/dist/`
   - `npm run preview` — serve the production build locally
   - `npm run lint` — run Oxlint

## Build order

Modules have real dependencies — build in this order to avoid blocking teammates:
1. Database schema / JPA entities — a full draft ER diagram exists (`docs/er-diagram.mmd`, 26
   entities, confirmed against the team's workflow diagrams); next step is team review of that
   draft, then generating migrations + JPA entities from it (shared session, before anyone forks off).
2. User Registration & Profile Management (auth/roles everything else needs).
3. Pollution Reporting + Community Verification (the core report-and-vote loop).
4. Admin Review + Government Coordination (consumes reports from step 3).
5. Alert & Notification + Project Monitoring (fires once approved projects exist).
6. Diver Opportunity Management + Analytics (Analytics is only meaningful once other modules produce real data).
