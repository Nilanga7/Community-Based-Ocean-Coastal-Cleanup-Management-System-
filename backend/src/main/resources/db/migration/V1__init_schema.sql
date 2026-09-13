-- ============================================================
-- Ocean & Coastal Cleanup Management System — baseline schema
-- Generated from docs/er-diagram.mmd (ER session 2026-09-03).
-- Naming: snake_case; USER -> `users` (MySQL reserved word).
-- ============================================================

-- ---------- Reference data ----------
CREATE TABLE district (
    district_id     INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    district_name   VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

-- ---------- Users & role subtypes (ISA) ----------
CREATE TABLE users (
    user_id         INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    password        VARCHAR(255) NOT NULL,
    phone           VARCHAR(30),
    `role`          ENUM('VOLUNTEER_NON_DIVER','VOLUNTEER_DIVER','ORGANIZATION','GOVERNMENT_OFFICER','ADMIN') NOT NULL,
    status          ENUM('ACTIVE','WARNED','RESTRICTED','SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
    date_of_birth   DATE,
    address_line    VARCHAR(255),
    latitude        DECIMAL(9,6),
    longitude       DECIMAL(9,6),
    created_date    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE INDEX idx_users_lat_long ON users (latitude, longitude);

CREATE TABLE volunteer_non_diver (
    user_id         INT UNSIGNED PRIMARY KEY,
    availability    VARCHAR(255),
    specialization  VARCHAR(255),
    CONSTRAINT fk_vnd_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE volunteer_interest (
    interest_id     INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id         INT UNSIGNED NOT NULL,
    interest        VARCHAR(255) NOT NULL,
    CONSTRAINT fk_vi_volunteer FOREIGN KEY (user_id) REFERENCES volunteer_non_diver (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_vi_user ON volunteer_interest (user_id);

CREATE TABLE volunteer_diver (
    user_id             INT UNSIGNED PRIMARY KEY,
    experience          VARCHAR(255),
    certification       VARCHAR(255),
    preferred_region    VARCHAR(255),
    CONSTRAINT fk_vd_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE diver_equipment (
    equipment_id    INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id         INT UNSIGNED NOT NULL,
    equipment_name  VARCHAR(255) NOT NULL,
    CONSTRAINT fk_de_diver FOREIGN KEY (user_id) REFERENCES volunteer_diver (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_de_user ON diver_equipment (user_id);

CREATE TABLE organization (
    user_id             INT UNSIGNED PRIMARY KEY,
    organization_name   VARCHAR(255) NOT NULL,
    organization_type   VARCHAR(100),
    business_reg_num    VARCHAR(100),
    CONSTRAINT fk_org_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE government_officer (
    user_id         INT UNSIGNED PRIMARY KEY,
    department      VARCHAR(255),
    designation     VARCHAR(255),
    CONSTRAINT fk_gov_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE admin (
    user_id         INT UNSIGNED PRIMARY KEY,
    CONSTRAINT fk_admin_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------- Registration verification (diver / org onboarding) ----------
CREATE TABLE verification_document (
    document_id     INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id         INT UNSIGNED NOT NULL,
    document_type   VARCHAR(100) NOT NULL,
    file_url        VARCHAR(500) NOT NULL,
    uploaded_date   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vdoc_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_vdoc_user ON verification_document (user_id);

CREATE TABLE registration_verification (
    verification_id     INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id              INT UNSIGNED NOT NULL,
    status               ENUM('PENDING','NEEDS_CLARIFICATION','RESUBMITTED','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    submitted_date       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_regver_user UNIQUE (user_id),
    CONSTRAINT fk_regver_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE registration_review_action (
    action_id           INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    verification_id     INT UNSIGNED NOT NULL,
    reviewer_id          INT UNSIGNED NOT NULL,
    decision             ENUM('APPROVE','REJECT','CLARIFY') NOT NULL,
    comment               TEXT,
    action_date           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rra_verification FOREIGN KEY (verification_id) REFERENCES registration_verification (verification_id) ON DELETE CASCADE,
    CONSTRAINT fk_rra_reviewer FOREIGN KEY (reviewer_id) REFERENCES users (user_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE INDEX idx_rra_verification ON registration_review_action (verification_id);
CREATE INDEX idx_rra_reviewer ON registration_review_action (reviewer_id);

-- ---------- Cleanup request post: report -> verify -> approve ----------
CREATE TABLE cleanup_request_post (
    request_post_id         INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    submitted_by_user_id    INT UNSIGNED NOT NULL,
    description              TEXT NOT NULL,
    incident_date_time       DATETIME NOT NULL,
    latitude                 DECIMAL(9,6) NOT NULL,
    longitude                DECIMAL(9,6) NOT NULL,
    district_id              INT UNSIGNED,
    current_stage            ENUM('COMMUNITY','ADMIN','GOVERNMENT','CLOSED') NOT NULL DEFAULT 'COMMUNITY',
    stage_status              ENUM('PENDING','NEEDS_CLARIFICATION','RESUBMITTED','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    community_result          ENUM('PENDING','VERIFIED','UNVERIFIED') NOT NULL DEFAULT 'PENDING',
    trust_percentage          DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    submitted_date            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    verification_deadline     DATETIME NOT NULL,
    CONSTRAINT fk_crp_submitter FOREIGN KEY (submitted_by_user_id) REFERENCES users (user_id) ON DELETE RESTRICT,
    CONSTRAINT fk_crp_district FOREIGN KEY (district_id) REFERENCES district (district_id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE INDEX idx_crp_submitter ON cleanup_request_post (submitted_by_user_id);
CREATE INDEX idx_crp_district ON cleanup_request_post (district_id);
CREATE INDEX idx_crp_stage_status ON cleanup_request_post (current_stage, stage_status);
CREATE INDEX idx_crp_lat_long ON cleanup_request_post (latitude, longitude);

CREATE TABLE request_post_media (
    media_id            INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    request_post_id      INT UNSIGNED NOT NULL,
    file_url              VARCHAR(500) NOT NULL,
    file_type             VARCHAR(50) NOT NULL,
    uploaded_date          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rpm_post FOREIGN KEY (request_post_id) REFERENCES cleanup_request_post (request_post_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_rpm_post ON request_post_media (request_post_id);

CREATE TABLE report_vote (
    vote_id             INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    request_post_id      INT UNSIGNED NOT NULL,
    user_id                INT UNSIGNED NOT NULL,
    vote_value             BOOLEAN NOT NULL,
    voted_date              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rv_post FOREIGN KEY (request_post_id) REFERENCES cleanup_request_post (request_post_id) ON DELETE CASCADE,
    CONSTRAINT fk_rv_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_rv_post ON report_vote (request_post_id);
CREATE INDEX idx_rv_user ON report_vote (user_id);

CREATE TABLE report_comment (
    comment_id          INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    request_post_id      INT UNSIGNED NOT NULL,
    user_id                INT UNSIGNED NOT NULL,
    comment_text            TEXT NOT NULL,
    created_date             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rc_post FOREIGN KEY (request_post_id) REFERENCES cleanup_request_post (request_post_id) ON DELETE CASCADE,
    CONSTRAINT fk_rc_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_rc_post ON report_comment (request_post_id);
CREATE INDEX idx_rc_user ON report_comment (user_id);

CREATE TABLE report_review_action (
    action_id           INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    request_post_id      INT UNSIGNED NOT NULL,
    reviewer_id            INT UNSIGNED NOT NULL,
    review_stage            ENUM('ADMIN','GOVERNMENT') NOT NULL,
    decision                 ENUM('APPROVE','REJECT','CLARIFY') NOT NULL,
    comment                   TEXT,
    action_date               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rra2_post FOREIGN KEY (request_post_id) REFERENCES cleanup_request_post (request_post_id) ON DELETE CASCADE,
    CONSTRAINT fk_rra2_reviewer FOREIGN KEY (reviewer_id) REFERENCES users (user_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE INDEX idx_rra2_post ON report_review_action (request_post_id);
CREATE INDEX idx_rra2_reviewer ON report_review_action (reviewer_id);

-- ---------- Sanctions (false report -> warning -> restriction) ----------
CREATE TABLE user_sanction (
    sanction_id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id                    INT UNSIGNED NOT NULL,
    issued_by_admin_id          INT UNSIGNED NOT NULL,
    type                          ENUM('WARNING','RESTRICTION') NOT NULL,
    reason                        TEXT NOT NULL,
    related_request_post_id      INT UNSIGNED,
    issued_date                   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_us_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_us_admin FOREIGN KEY (issued_by_admin_id) REFERENCES users (user_id) ON DELETE RESTRICT,
    CONSTRAINT fk_us_post FOREIGN KEY (related_request_post_id) REFERENCES cleanup_request_post (request_post_id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE INDEX idx_us_user ON user_sanction (user_id);
CREATE INDEX idx_us_admin ON user_sanction (issued_by_admin_id);
CREATE INDEX idx_us_post ON user_sanction (related_request_post_id);

-- ---------- Cleanup project: auto-created on government approval ----------
CREATE TABLE cleanup_project (
    project_id               INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    request_post_id            INT UNSIGNED NOT NULL,
    owner_id                     INT UNSIGNED NOT NULL,
    status                        ENUM('PLANNING','PARTICIPANT_RECRUITMENT','DETAILS_FINALIZED','READY_FOR_CLEANUP','CLEANUP_IN_PROGRESS','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PLANNING',
    divers_needed                 INT UNSIGNED NOT NULL DEFAULT 0,
    participants_needed            INT UNSIGNED NOT NULL DEFAULT 0,
    minimum_participants            INT UNSIGNED NOT NULL DEFAULT 0,
    specialists_needed               VARCHAR(255),
    equipment_required                VARCHAR(255),
    current_alert_radius_km            INT UNSIGNED NOT NULL DEFAULT 0,
    initiation_date                     DATE,
    created_date                         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finalized_date                        DATETIME,
    CONSTRAINT uk_cp_post UNIQUE (request_post_id),
    CONSTRAINT fk_cp_post FOREIGN KEY (request_post_id) REFERENCES cleanup_request_post (request_post_id) ON DELETE CASCADE,
    CONSTRAINT fk_cp_owner FOREIGN KEY (owner_id) REFERENCES users (user_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE INDEX idx_cp_owner ON cleanup_project (owner_id);
CREATE INDEX idx_cp_status ON cleanup_project (status);

CREATE TABLE project_alert (
    alert_id             INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    project_id             INT UNSIGNED NOT NULL,
    radius_km                INT UNSIGNED NOT NULL,
    sent_date                 DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pa_project FOREIGN KEY (project_id) REFERENCES cleanup_project (project_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_pa_project ON project_alert (project_id);

CREATE TABLE project_alert_recipient (
    recipient_id          INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    alert_id                 INT UNSIGNED NOT NULL,
    user_id                     INT UNSIGNED NOT NULL,
    notified_date                 DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    response                       ENUM('PENDING','JOINED','DECLINED','IGNORED') NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_par_alert FOREIGN KEY (alert_id) REFERENCES project_alert (alert_id) ON DELETE CASCADE,
    CONSTRAINT fk_par_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_par_alert ON project_alert_recipient (alert_id);
CREATE INDEX idx_par_user ON project_alert_recipient (user_id);

CREATE TABLE project_participant (
    participant_id         INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    project_id                INT UNSIGNED NOT NULL,
    user_id                      INT UNSIGNED NOT NULL,
    `role`                        ENUM('PARTICIPANT','DIVER') NOT NULL,
    status                         ENUM('CONFIRMED','WITHDRAWN') NOT NULL DEFAULT 'CONFIRMED',
    joined_date                     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pp_project FOREIGN KEY (project_id) REFERENCES cleanup_project (project_id) ON DELETE CASCADE,
    CONSTRAINT fk_pp_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_pp_project ON project_participant (project_id);
CREATE INDEX idx_pp_user ON project_participant (user_id);

CREATE TABLE project_progress_update (
    update_id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    project_id                INT UNSIGNED NOT NULL,
    updated_by_user_id           INT UNSIGNED NOT NULL,
    notes                           TEXT,
    update_date                      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ppu_project FOREIGN KEY (project_id) REFERENCES cleanup_project (project_id) ON DELETE CASCADE,
    CONSTRAINT fk_ppu_user FOREIGN KEY (updated_by_user_id) REFERENCES users (user_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE INDEX idx_ppu_project ON project_progress_update (project_id);
CREATE INDEX idx_ppu_user ON project_progress_update (updated_by_user_id);

CREATE TABLE project_progress_image (
    image_id               INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    update_id                 INT UNSIGNED NOT NULL,
    image_url                    VARCHAR(500) NOT NULL,
    CONSTRAINT fk_ppi_update FOREIGN KEY (update_id) REFERENCES project_progress_update (update_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_ppi_update ON project_progress_image (update_id);

-- ---------- Notifications (generic log) ----------
CREATE TABLE notification (
    notification_id         INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id                     INT UNSIGNED NOT NULL,
    message                       TEXT NOT NULL,
    type                            VARCHAR(50) NOT NULL,
    priority_level                    VARCHAR(20),
    sent_date                          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_status                          BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_notif_user ON notification (user_id);

-- ---------- Diver Opportunity module (separate, org-initiated) ----------
CREATE TABLE diver_opportunity (
    opportunity_id           INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    organization_id             INT UNSIGNED NOT NULL,
    title                          VARCHAR(255) NOT NULL,
    description                      TEXT,
    location                           VARCHAR(255),
    required_skills                      VARCHAR(255),
    status                                  VARCHAR(30),
    posted_date                              DATE NOT NULL DEFAULT (CURRENT_DATE),
    CONSTRAINT fk_do_org FOREIGN KEY (organization_id) REFERENCES organization (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_do_org ON diver_opportunity (organization_id);

CREATE TABLE opportunity_signup (
    signup_id                INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    opportunity_id              INT UNSIGNED NOT NULL,
    diver_id                       INT UNSIGNED NOT NULL,
    status                           ENUM('INTERESTED','SELECTED','REJECTED') NOT NULL DEFAULT 'INTERESTED',
    signup_date                        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_os_opportunity FOREIGN KEY (opportunity_id) REFERENCES diver_opportunity (opportunity_id) ON DELETE CASCADE,
    CONSTRAINT fk_os_diver FOREIGN KEY (diver_id) REFERENCES volunteer_diver (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_os_opportunity ON opportunity_signup (opportunity_id);
CREATE INDEX idx_os_diver ON opportunity_signup (diver_id);
