INSERT INTO password_reset_token (id, confirmed_at, created_at, expires_at, token, user_id)
VALUES (1, null, now(), now() + interval '1 hour', '75ecd5ee-4a9e-45d3-9a3d-a77144fe6673', 1);