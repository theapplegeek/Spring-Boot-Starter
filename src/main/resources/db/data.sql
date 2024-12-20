INSERT INTO role (name)
VALUES ('user'),
       ('admin');

INSERT INTO permission (name)
VALUES ('USER_READ'),
       ('USER_CREATE'),
       ('USER_UPDATE'),
       ('USER_CHANGE_PASSWORD'),
       ('USER_DELETE'),
       ('ROLE_READ'),
       ('PERMISSION_READ');

INSERT INTO role_permission (role_id, permission_id)
VALUES (1, 1),
       (1, 4),
       (2, 1),
       (2, 2),
       (2, 3),
       (2, 4),
       (2, 5),
       (2, 6),
       (2, 7);

INSERT INTO user_profile (email, enabled, password, username, name, surname)
VALUES ('admin@mail.com', true, '$2a$10$KWYO4oLjsHyydrEcXL.CseiYe144WLp5C6rczHwDjDU2eNWlaoU7S',
        'admin', 'Admin', 'Admin'),
       ('user@mail.com', true, '$2a$10$KWYO4oLjsHyydrEcXL.CseiYe144WLp5C6rczHwDjDU2eNWlaoU7S',
        'user', 'User', 'User');

INSERT INTO user_role (user_id, role_id)
VALUES (1, 2),
       (2, 1);