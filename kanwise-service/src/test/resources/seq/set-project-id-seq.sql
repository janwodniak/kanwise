SELECT setval('project_id_seq', (SELECT MAX(id) FROM "project"));
