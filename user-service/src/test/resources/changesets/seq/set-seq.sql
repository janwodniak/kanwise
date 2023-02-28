SELECT setval('user_id_seq', (SELECT MAX(id) FROM "user"));

SELECT setval('password_reset_token_id_seq', (SELECT MAX(id) FROM "password_reset_token"));

SELECT setval('one_time_password_id_seq', (SELECT MAX(id) FROM "one_time_password"));

SELECT setval('profile_image_id_seq', (SELECT MAX(id) FROM "profile_image"));