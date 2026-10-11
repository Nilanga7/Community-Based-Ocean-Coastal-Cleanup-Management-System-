# Frontend — Community-Based Ocean & Coastal Cleanup Management System

React 19 + Vite, built on the **Tideline design system**. Talks to the Spring Boot API.

## Run it

Start the backend first (`cd ../backend && ./mvnw spring-boot:run`), then:

```
npm install
npm run dev
```

Opens at `http://localhost:5173`. Vite proxies `/api` to `http://localhost:8080`, so there is
no cross-origin request in development. To point at a deployed API instead, set `VITE_API_URL`.

Sign in with any seeded account — the login screen lists them, and all use `password123`.

## Structure

- `src/api/` — `client.js` (fetch wrapper, JWT header, error mapping) and `index.js` (endpoints).
- `src/auth/AuthContext.jsx` — session state; restores the user from a stored token on boot.
- `src/hooks/useApi.js` — runs a call on mount, exposes `{ data, loading, error, reload }`.
- `src/components/AsyncState.jsx` — shared loading / error / empty rendering.
- `src/components/RequireAuth.jsx` — route guard; the console additionally requires ADMIN or AUTHORITY.
- `src/design-system/` — the Tideline tokens and 21 components.
- `src/layouts/` — `VolunteerShell` (mobile PWA, bottom tabs) and `AuthorityShell` (desktop console).
- `src/pages/volunteer/` — feed, report detail with community voting and "start a cleanup",
  submission, cleanups (list, join, organiser progress), alerts, opportunities, profile.
- `src/pages/volunteer/OrganisationOpportunities.jsx` — what an organisation sees on the
  Opportunities tab: post assignments, review applicants' diving record, accept or decline.
- `src/pages/authority/` — review queue, report review, projects, project detail, alerts, account verifications,
  analytics, and users (administrators only).
- `src/components/` — shared pieces: `AlertList`, `ProjectCard`, `ProjectProgress`,
  `ProjectTimeline`, `ProgressUpdateForm`, `ParticipantList` (organiser ratings), `MapLink`,
  and `Modal`.

## What the UI does against the API

| Screen | Calls |
| --- | --- |
| Landing | `GET /api/analytics/summary` (public) |
| Login / Register | `POST /api/auth/login` · `/register` (multipart, with certificates for divers and a website for organisations; divers and organisations see a pending-verification screen) |
| Feed | `GET /api/reports?status=` |
| Report detail | `GET /api/reports/{id}` · `POST /votes` · `GET`/`POST /comments` (replies) · `POST /comments/{cid}/reactions` · `GET /api/projects?reportId=` (shows review progress; once approved, report links redirect to the project) |
| Cleanups | `GET /api/projects` · `GET /{id}` · `POST /{id}/participants` · `POST /{id}/updates` (project owner) · `POST /{id}/participants/{pid}/mark` (project owner, once complete) |
| Submit report | `POST /api/reports` (multipart, with photo and video evidence) and browser geolocation |
| Alerts (both apps) | `GET /api/alerts` · `POST /{id}/read` · `POST /read-all` · `GET /unread-count` (nav badge) |
| Opportunities — diver | `GET /api/opportunities` · `POST /{id}/applications` · `GET /applications/mine` (shows accepted / not selected) |
| Opportunities — organisation | `POST /api/opportunities` · `GET /{id}/applications` · `POST /applications/{id}/decision` |
| Member profile | `GET /api/users/{id}` — names on reports, comments, projects and participants link here |
| Profile | `GET`/`PUT /api/users/me` (account status, project owner badge and owned projects) (incl. browser geolocation for alerts) · `PUT /api/users/me/diver-profile` (incl. regions) |
| Review queue | `GET /api/reports?reviewQueue=true` (verified, with authority, rejected) |
| Report review | `POST /moderation` and `POST /authority-decision` — approve, request more info, reject · Additional information tab (`GET /info-requests`, photos via authenticated blob) |
| Additional information (reporter) | `GET /api/reports/{id}/info-requests` · `POST /info-requests/{rid}/response` (multipart) |
| Projects | `GET /api/projects` · `GET /{id}` (read-only for officials) |
| Analytics | `GET /api/analytics/summary` |
| Verifications (admin) | `GET /api/admin/verifications?status=` · `POST /api/admin/verifications/{id}` · `GET /api/admin/documents/{id}` |
| Users (admin) | `GET /api/admin/users?query=` · `POST /api/admin/users/{id}/suspension` |

The review screen shows different actions per role: an administrator gets approve (send to the
authority) / request more info / reject, while an authority officer gets approve
(create the project) / request more info / reject on a report sent to them — mirroring the backend's permissions rather than guessing at them.

## Deviations from the vendored design system

`Input`, `Select` and `Textarea` originally read `const id = props.id || React.useId()`, which
calls a hook conditionally and is flagged as an error by this project's own linter. They now
call `useId()` unconditionally and fall back to it. Behaviour and the component API are
unchanged. Worth folding back upstream.

`Dialog` itself is unchanged, but it positions itself absolutely against its container — on a
scrolled page that is the top of the document, off-screen. App code opens dialogs through
`src/components/Modal.jsx`, which supplies a fixed, viewport-sized container. Also worth raising
upstream.

`public/assets/icons/heart.svg` is added for comment reactions — the vendored icon set has no
plain heart. It uses the same 24px stroke format as the other icons.

## Not built yet

- An embedded map. Reports and cleanups link out to OpenStreetMap instead.
