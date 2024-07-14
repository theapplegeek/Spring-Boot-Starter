INSERT INTO role (name)
VALUES ('user'),
       ('admin');

INSERT INTO user_profile (email, enabled, password, role_id, username, name, surname)
VALUES ('admin@mail.com', true, '$2a$10$KWYO4oLjsHyydrEcXL.CseiYe144WLp5C6rczHwDjDU2eNWlaoU7S', 2,
        'admin', 'Admin', 'Admin');
INSERT INTO user_profile (email, enabled, password, role_id, username, name, surname)
VALUES ('user@mail.com', true, '$2a$10$KWYO4oLjsHyydrEcXL.CseiYe144WLp5C6rczHwDjDU2eNWlaoU7S', 1, 'user',
        'User', 'User');
