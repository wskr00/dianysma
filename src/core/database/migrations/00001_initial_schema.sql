-- +goose Up
-- Idempotent baseline copied from the Hibernate-generated Flyway V1.
-- Existing production rows and objects are preserved; missing schema objects are added.

CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- PostgreSQL has no CREATE TYPE IF NOT EXISTS. Ignore only an already existing type.
-- +goose StatementBegin
DO $$
BEGIN
    CREATE TYPE educationtype AS ENUM ('CERTIFICATION', 'COURSE', 'DEGREE');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END;
$$;
-- +goose StatementEnd

-- +goose StatementBegin
DO $$
BEGIN
    CREATE TYPE languageproficiency AS ENUM ('ADVANCED', 'BASIC', 'FLUENT', 'INTERMEDIATE', 'NATIVE');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END;
$$;
-- +goose StatementEnd

-- These casts are part of Hibernate's generated enum schema and remain harmless for Go.
-- +goose StatementBegin
DO $$
BEGIN
    CREATE CAST (varchar AS educationtype) WITH INOUT AS IMPLICIT;
EXCEPTION
    WHEN duplicate_object THEN NULL;
END;
$$;
-- +goose StatementEnd

-- +goose StatementBegin
DO $$
BEGIN
    CREATE CAST (educationtype AS varchar) WITH INOUT AS IMPLICIT;
EXCEPTION
    WHEN duplicate_object THEN NULL;
END;
$$;
-- +goose StatementEnd

-- +goose StatementBegin
DO $$
BEGIN
    CREATE CAST (varchar AS languageproficiency) WITH INOUT AS IMPLICIT;
EXCEPTION
    WHEN duplicate_object THEN NULL;
END;
$$;
-- +goose StatementEnd

-- +goose StatementBegin
DO $$
BEGIN
    CREATE CAST (languageproficiency AS varchar) WITH INOUT AS IMPLICIT;
EXCEPTION
    WHEN duplicate_object THEN NULL;
END;
$$;
-- +goose StatementEnd

CREATE TABLE IF NOT EXISTS claim (
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    participation_id uuid,
    content text NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS claim_skill (
    claim_id uuid NOT NULL,
    skill_id uuid NOT NULL,
    PRIMARY KEY (claim_id, skill_id)
);

CREATE TABLE IF NOT EXISTS education (
    completed_at date,
    expected boolean,
    started_at date,
    workload_hours integer,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    degree varchar(100),
    institution varchar(100) NOT NULL,
    title varchar(120) NOT NULL,
    description text,
    type educationtype NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS experience (
    ended_at date,
    started_at date,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    client_id uuid,
    employer_id uuid NOT NULL,
    id uuid NOT NULL,
    employment_type varchar(40) NOT NULL,
    role varchar(200) NOT NULL,
    description text,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS knowledge_chunk (
    indexed_at timestamp(6) with time zone NOT NULL,
    claim_id uuid,
    education_id uuid,
    id uuid NOT NULL,
    participation_id uuid,
    model varchar(100) NOT NULL,
    hash varchar(128) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT ck_knowledge_chunk_one_source
        CHECK (num_nonnulls(participation_id, claim_id, education_id) = 1)
);

CREATE TABLE IF NOT EXISTS language (
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    profile_id uuid NOT NULL,
    name varchar(50) NOT NULL,
    proficiency languageproficiency NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS organization (
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    name varchar(120) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS participation (
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    experience_id uuid,
    id uuid NOT NULL,
    project_id uuid,
    description text,
    PRIMARY KEY (id),
    CONSTRAINT ck_participation_has_context
        CHECK (num_nonnulls(experience_id, project_id) >= 1)
);

CREATE TABLE IF NOT EXISTS profile (
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    phone varchar(20),
    city varchar(40),
    country varchar(40),
    state varchar(40),
    email varchar(50),
    name varchar(120),
    github_url varchar(150),
    linkedin_url varchar(150),
    portfolio_url varchar(150),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS project (
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    organization_id uuid,
    name varchar(200) NOT NULL,
    description text,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS skill (
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    name varchar(80) NOT NULL,
    aliases text[],
    PRIMARY KEY (id)
);

-- The Flyway V1 vector_store schema is intentionally retained (json, nullable metadata,
-- and UUID v4 default) so this migration works against existing production data unchanged.
CREATE TABLE IF NOT EXISTS vector_store (
    id uuid DEFAULT uuid_generate_v4() NOT NULL,
    content text,
    metadata json,
    embedding vector(1536),
    CONSTRAINT vector_store_pkey PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_claim_participation_id ON claim (participation_id);
CREATE INDEX IF NOT EXISTS idx_claim_skill_skill_id ON claim_skill (skill_id);
CREATE INDEX IF NOT EXISTS idx_experience_employer_id ON experience (employer_id);
CREATE INDEX IF NOT EXISTS idx_experience_client_id ON experience (client_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_chunk_participation_id ON knowledge_chunk (participation_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_chunk_claim_id ON knowledge_chunk (claim_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_chunk_education_id ON knowledge_chunk (education_id);
CREATE INDEX IF NOT EXISTS idx_language_profile_id ON language (profile_id);
CREATE INDEX IF NOT EXISTS idx_participation_experience_id ON participation (experience_id);
CREATE INDEX IF NOT EXISTS idx_participation_project_id ON participation (project_id);
CREATE INDEX IF NOT EXISTS idx_project_organization_id ON project (organization_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_skill_name_case_insensitive ON skill (upper(name));
CREATE UNIQUE INDEX IF NOT EXISTS uq_profile_singleton ON profile ((true));
CREATE INDEX IF NOT EXISTS idx_vector_store_embedding_hnsw
    ON vector_store USING hnsw (embedding vector_cosine_ops);

-- The old Modulith event_publication table is deliberately not recreated. It is not
-- part of the Go application; if present in an existing database, this migration leaves it alone.

-- Add constraints omitted by older schemas. Named checks make reruns safe.
-- +goose StatementBegin
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_claim_participation' AND conrelid = 'claim'::regclass) THEN
        ALTER TABLE claim ADD CONSTRAINT fk_claim_participation FOREIGN KEY (participation_id) REFERENCES participation;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_claim_skill_skill' AND conrelid = 'claim_skill'::regclass) THEN
        ALTER TABLE claim_skill ADD CONSTRAINT fk_claim_skill_skill FOREIGN KEY (skill_id) REFERENCES skill;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_experience_client' AND conrelid = 'experience'::regclass) THEN
        ALTER TABLE experience ADD CONSTRAINT fk_experience_client FOREIGN KEY (client_id) REFERENCES organization;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_experience_employer' AND conrelid = 'experience'::regclass) THEN
        ALTER TABLE experience ADD CONSTRAINT fk_experience_employer FOREIGN KEY (employer_id) REFERENCES organization;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_knowledge_chunk_education' AND conrelid = 'knowledge_chunk'::regclass) THEN
        ALTER TABLE knowledge_chunk ADD CONSTRAINT fk_knowledge_chunk_education FOREIGN KEY (education_id) REFERENCES education;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_knowledge_chunk_participation' AND conrelid = 'knowledge_chunk'::regclass) THEN
        ALTER TABLE knowledge_chunk ADD CONSTRAINT fk_knowledge_chunk_participation FOREIGN KEY (participation_id) REFERENCES participation;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_language_profile' AND conrelid = 'language'::regclass) THEN
        ALTER TABLE language ADD CONSTRAINT fk_language_profile FOREIGN KEY (profile_id) REFERENCES profile;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_participation_experience' AND conrelid = 'participation'::regclass) THEN
        ALTER TABLE participation ADD CONSTRAINT fk_participation_experience FOREIGN KEY (experience_id) REFERENCES experience;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_participation_project' AND conrelid = 'participation'::regclass) THEN
        ALTER TABLE participation ADD CONSTRAINT fk_participation_project FOREIGN KEY (project_id) REFERENCES project;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_project_organization' AND conrelid = 'project'::regclass) THEN
        ALTER TABLE project ADD CONSTRAINT fk_project_organization FOREIGN KEY (organization_id) REFERENCES organization;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_participation_context' AND conrelid = 'participation'::regclass) THEN
        ALTER TABLE participation ADD CONSTRAINT uk_participation_context UNIQUE NULLS NOT DISTINCT (experience_id, project_id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_knowledge_chunk_one_source' AND conrelid = 'knowledge_chunk'::regclass) THEN
        ALTER TABLE knowledge_chunk ADD CONSTRAINT ck_knowledge_chunk_one_source
            CHECK (num_nonnulls(participation_id, claim_id, education_id) = 1);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_participation_has_context' AND conrelid = 'participation'::regclass) THEN
        ALTER TABLE participation ADD CONSTRAINT ck_participation_has_context
            CHECK (num_nonnulls(experience_id, project_id) >= 1);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_experience_dates' AND conrelid = 'experience'::regclass) THEN
        ALTER TABLE experience ADD CONSTRAINT ck_experience_dates
            CHECK (ended_at IS NULL OR started_at IS NULL OR ended_at >= started_at);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_education_dates' AND conrelid = 'education'::regclass) THEN
        ALTER TABLE education ADD CONSTRAINT ck_education_dates
            CHECK (completed_at IS NULL OR started_at IS NULL OR completed_at >= started_at);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_education_workload' AND conrelid = 'education'::regclass) THEN
        ALTER TABLE education ADD CONSTRAINT ck_education_workload
            CHECK (workload_hours IS NULL OR workload_hours >= 0);
    END IF;
END;
$$;
-- +goose StatementEnd

-- Match the Go domain's delete behavior. Replacing these named FKs changes no rows;
-- the constraints are validated before the migration commits.
ALTER TABLE claim_skill DROP CONSTRAINT IF EXISTS fk_claim_skill_claim;
ALTER TABLE claim_skill ADD CONSTRAINT fk_claim_skill_claim
    FOREIGN KEY (claim_id) REFERENCES claim ON DELETE CASCADE;

ALTER TABLE knowledge_chunk DROP CONSTRAINT IF EXISTS fk_knowledge_chunk_claim;
ALTER TABLE knowledge_chunk ADD CONSTRAINT fk_knowledge_chunk_claim
    FOREIGN KEY (claim_id) REFERENCES claim ON DELETE CASCADE;

ALTER TABLE knowledge_chunk DROP CONSTRAINT IF EXISTS fk_knowledge_chunk_vector_store;
ALTER TABLE knowledge_chunk ADD CONSTRAINT fk_knowledge_chunk_vector_store
    FOREIGN KEY (id) REFERENCES vector_store (id) ON DELETE CASCADE;
