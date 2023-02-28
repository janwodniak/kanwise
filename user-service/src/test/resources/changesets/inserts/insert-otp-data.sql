INSERT INTO one_time_password (id, code, confirmed_at, created_at, expires_at, status, user_id)
VALUES (1, '123456', null, now(), now() + interval '5 minutes', 'DELIVERED', 50),
       (2, '123456', null, now(), now() + interval '5 minutes', 'CREATED', 50)