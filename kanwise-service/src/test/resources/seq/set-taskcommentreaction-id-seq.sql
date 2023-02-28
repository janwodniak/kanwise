SELECT setval('taskcommentreaction_id_seq', (SELECT MAX(id) FROM "taskcommentreaction"));
