TRUNCATE TABLE cities, project_groups, user_groups, users, projects, groups;

ALTER SEQUENCE user_seq RESTART WITH 100000;
ALTER SEQUENCE groups_seq RESTART WITH 100000;
ALTER SEQUENCE projects_seq RESTART WITH 100000;

INSERT into cities(id, name)
VALUES ('spb', 'Санкт-Петербург'),
       ('mow', 'Москва'),
       ('kiv', 'Киев'),
       ('mnsk', 'Минск');

INSERT into users(full_name, email, flag, city)
VALUES ('Eric', 'eric@yandex.ru', 'active', 'spb'),
       ('Stan', 'stan@yandex.ru', 'active', 'mow');

INSERT into groups(name, type)
VALUES ('topjava01', 'FINISHED'),
       ('topjava02', 'FINISHED');

INSERT into projects(name, description)
VALUES ('topjava', 'Topjava'),
       ('masterjava', 'Masterjava');

INSERT into project_groups(project_id, group_id)
VALUES (100000, 100000),
       (100000, 100001);

INSERT into user_groups(user_id, group_id)
VALUES (100000, 100000),
       (100000, 100001);
