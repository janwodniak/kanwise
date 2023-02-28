SELECT setval('join_response_id_seq', (SELECT MAX(id) FROM "join_response"));
