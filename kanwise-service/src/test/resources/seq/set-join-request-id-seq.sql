SELECT setval('join_request_id_seq', (SELECT MAX(id) FROM "join_request"));
