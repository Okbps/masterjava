DROP TABLE IF EXISTS users, cities, groups, projects, user_groups, project_groups;
DROP SEQUENCE IF EXISTS user_seq, group_seq, project_seq;
DROP TYPE IF EXISTS user_flag, group_type;

CREATE TYPE user_flag AS ENUM ('active', 'deleted', 'superuser');
CREATE TYPE group_type AS ENUM ('REGISTERING', 'CURRENT', 'FINISHED');

CREATE SEQUENCE user_seq START 100000;
CREATE SEQUENCE group_seq START 100000;
CREATE SEQUENCE project_seq START 100000;

CREATE TABLE cities
(
    id   TEXT PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE users
(
    id        INTEGER PRIMARY KEY DEFAULT nextval('user_seq'),
    full_name TEXT      NOT NULL,
    email     TEXT      NOT NULL,
    flag      user_flag NOT NULL,
    city      TEXT REFERENCES cities (id)
);

CREATE UNIQUE INDEX email_idx ON users (email);

CREATE TABLE groups
(
    id   INTEGER PRIMARY KEY DEFAULT nextval('group_seq'),
    name TEXT       NOT NULL,
    type group_type NOT NULL
);

CREATE TABLE projects
(
    id          INTEGER PRIMARY KEY DEFAULT nextval('project_seq'),
    name        TEXT NOT NULL,
    description TEXT NOT NULL
);

CREATE TABLE project_groups
(
    project_id INTEGER REFERENCES projects (id) ON DELETE CASCADE,
    group_id   INTEGER REFERENCES groups (id) ON DELETE CASCADE
);

CREATE TABLE user_groups
(
    user_id    INTEGER REFERENCES users (id) ON DELETE CASCADE,
    group_id INTEGER REFERENCES groups (id) ON DELETE CASCADE
);
