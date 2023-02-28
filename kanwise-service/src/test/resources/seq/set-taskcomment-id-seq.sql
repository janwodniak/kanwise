SELECT setval('taskcomment_id_seq', (SELECT MAX(id) FROM "taskcomment"));
