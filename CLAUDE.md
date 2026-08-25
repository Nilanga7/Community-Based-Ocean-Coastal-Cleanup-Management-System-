# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

This repository currently contains **no source code** — `backend/` and `frontend/` are empty
placeholder directories and no commit has been made yet (git repo has no history). The only
content is planning documentation under `docs/` (SRS, project proposal, and final report PDFs).

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
- **Volunteer non-divers** — submit pollution reports (photos/videos/GPS), vote/comment to verify others' reports, receive location-based cleanup alerts.
- **Non-registered (public) users** — read-only access to verified reports and awareness content.
- **Volunteer divers** — maintain diving certification/equipment/region profile, receive location-based cleanup alerts, join underwater cleanup activities.
- **Government authority officers** — review escalated reports, approve/reject with official comments.
- **Organizations** (NGOs, tourism operators, research teams) — create/coordinate cleanup projects, post diver opportunities, view analytics.
- **System administrator** — manage users, moderate content, handle abuse/complaints, Review verified and unverified posts, If a post is unclear can request more information from the posted user.

Planned feature modules (SRS §5): user registration/profiles, pollution reporting & incident
submission, community verification (voting/comments), admin review & moderation, government
coordination & approval, alerts/notifications, project progress monitoring, diver opportunity
management, and a reporting/analytics dashboard.

## Intended architecture (per SRS §2.1/§3.3, not yet implemented)

Standard 3-tier web architecture:
- **Presentation layer** — React.js, built and shipped as a **Progressive Web App**: installable on desktop/mobile without an app store, works offline for cached content via a service worker, supports push notifications.
- **Application layer** — Spring Boot REST API handling business logic, auth, report processing/verification, notifications, and analytics.
- **Data layer** — MySQL for persistent storage of users, reports, votes, projects, diver profiles, and analytics data.

Frontend and backend communicate over REST APIs using JSON. External integrations called out in
the SRS: the Google Maps API for map-based reporting and location display, and a notification
service for alerts. Explicitly out of scope for the current design: native mobile apps, real-time
GPS tracking of volunteers, and legal enforcement features.

When scaffolding the actual `backend/` and `frontend/` projects, this maps to:
- `backend/` → a Spring Boot (Java) application exposing REST endpoints, backed by MySQL, built with Maven.
- `frontend/` → a React.js PWA (Vite or CRA + service worker) consuming those REST endpoints.

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
| VPC Rasanga | Pollution Reporting | System entry point: report submission, evidence upload, location capture. |
| SMR Kaveeshwara | Community Verification + Diver Opportunity Management | Both are trust/participation features built on voting-style logic and user profiles. |
| MDB Amarakoon | Admin Review | 
| MRS Nishadini | Alert & Notification + Project Monitoring | Both are status-change driven — a project progress update is the alert payload. |
| HGMN Kodiweera | Analytics | Downstream of every other module's data — dashboards, trends, participation stats. |


## Working conventions

- **One module = one owner = one feature branch = one PR.** Branch naming: `feature/<module-slug>-<owner-initials>`, e.g. `feature/pollution-reporting-vpc`.
- Don't edit another module's controller/service/component files without checking with its owner first.
- Shared code lives in `backend/.../common/` (User/Role entities, security config, exceptions) and `frontend/src/shared/` (API client, auth context, shared components) — changes here affect everyone, so flag them to the team before merging.
- `main` should stay demoable; merges require at least one teammate's review.
- Secrets (DB credentials, Google Maps API key) go in `.env` / `application-local.properties`, gitignored — never committed.
- Community verification trust threshold: 75% (per SRS) — encode this as a named constant, not a magic number, wherever it's checked.

## Build order

Modules have real dependencies — build in this order to avoid blocking teammates:
1. Database schema / JPA entities from the SRS ER diagram (shared session, before anyone forks off).
2. User Registration & Profile Management (auth/roles everything else needs).
3. Pollution Reporting + Community Verification (the core report-and-vote loop).
4. Admin Review + Government Coordination (consumes reports from step 3).
5. Alert & Notification + Project Monitoring (fires once approved projects exist).
6. Diver Opportunity Management + Analytics (Analytics is only meaningful once other modules produce real data).
