SELECT setval('membership_id_seq', (SELECT MAX(id) FROM "membership"));
