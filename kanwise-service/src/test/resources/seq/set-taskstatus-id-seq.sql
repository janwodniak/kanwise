SELECT setval('taskstatus_id_seq', (SELECT MAX(id) FROM "taskstatus"));
