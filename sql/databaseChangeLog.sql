--liquibase formatted sql

--changeset gkislin:1
CREATE SEQUENCE IF NOT EXISTS common_seq START 100000;

CREATE SEQUENCE IF NOT EXISTS mail_seq START 100000;

DO $$
	BEGIN
		IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'group_type') THEN
			CREATE TYPE group_type AS ENUM ('REGISTERING', 'CURRENT', 'FINISHED');
		END IF;
		IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'mail_result') THEN
			CREATE TYPE mail_result AS ENUM ('SUCCESS', 'FAILED', 'EXCEPTION');
		END IF;
	END;
$$;

CREATE TABLE IF NOT EXISTS city (
  ref  TEXT PRIMARY KEY,
  name TEXT NOT NULL
);

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS city_ref TEXT REFERENCES city (ref) ON UPDATE CASCADE;

--changeset gkislin:2
CREATE TABLE IF NOT EXISTS project (
  id          INTEGER PRIMARY KEY DEFAULT nextval('common_seq'),
  name        TEXT UNIQUE NOT NULL,
  description TEXT
);

CREATE TABLE IF NOT EXISTS groups (
  id         INTEGER PRIMARY KEY DEFAULT nextval('common_seq'),
  name       TEXT UNIQUE NOT NULL,
  type       GROUP_TYPE  NOT NULL,
  project_id INTEGER     NOT NULL REFERENCES project (id)
);

CREATE TABLE IF NOT EXISTS user_group (
  user_id  INTEGER NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  group_id INTEGER NOT NULL REFERENCES groups (id),
  CONSTRAINT users_group_idx UNIQUE (user_id, group_id)
);

CREATE TABLE IF NOT EXISTS mail_descriptor (
  id         INTEGER PRIMARY KEY DEFAULT nextval('mail_seq'),
  subject TEXT NOT NULL,
  to_addresses TEXT NOT NULL,
  cc_addresses TEXT NOT NULL,
  sent_date TIMESTAMP NOT NULL,
  sent_result  MAIL_RESULT  NOT NULL
);
