SELECT setval('task_id_seq', (SELECT MAX(id) FROM "task"));
