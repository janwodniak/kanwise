INSERT INTO public.taskstatus (id, label, set_at, set_till, set_by_id, task_id)
VALUES (1, 0, '2022-12-21 14:00:00'::timestamp - interval '191243 seconds', null, 1, 1),
       (3, 1, '2022-12-21 14:00:00'::timestamp - interval '432030 seconds', null, 1, 2),
       (2, 0, '2022-12-21 14:00:00'::timestamp - interval '518400 seconds',
        '2022-12-21 14:00:00'::timestamp - interval '432030 seconds', 1, 2),
       (4, 0, '2022-12-21 14:00:00'::timestamp - interval '193811 seconds',
        '2022-12-21 14:00:00'::timestamp - interval '81212 seconds', 1, 3),
       (6, 0, '2022-12-21 14:00:00'::timestamp - interval '345688 seconds', null, 1, 4),
       (7, 0, '2022-12-21 14:00:00'::timestamp - interval '83333 seconds', null, 1, 5),
       (8, 0, '2022-12-21 14:00:00'::timestamp - interval '11111 seconds', null, 1, 6),
       (10, 1, '2022-12-21 14:00:00'::timestamp - interval '89999 seconds', null, 1, 7),
       (9, 0, '2022-12-21 14:00:00'::timestamp - interval '192830 seconds',
        '2022-12-21 14:00:00'::timestamp - interval '89999 seconds', 1, 7),
       (11, 0, '2022-12-21 14:00:00'::timestamp - interval '213321 seconds',
        '2022-12-21 14:00:00'::timestamp - interval '121400 seconds', 1, 8),
       (14, 0, '2022-12-21 14:00:00'::timestamp - interval '81234 seconds', null, 1, 10),
       (15, 1, '2022-12-21 14:00:00'::timestamp - interval '345623 seconds', null, 1, 9),
       (13, 0, '2022-12-21 14:00:00'::timestamp - interval '666666 seconds',
        '2022-12-21 14:00:00'::timestamp - interval '345623 seconds', 1, 9),
       (16, 0, '2022-12-21 14:00:00'::timestamp - interval '259833 seconds', null, 1, 11),
       (19, 0, '2022-12-21 14:00:00'::timestamp - interval '172222 seconds', null, 1, 14),
       (20, 1, '2022-12-21 14:00:00'::timestamp - interval '1728234 seconds', null, 1, 12),
       (17, 0, '2022-12-21 14:00:00'::timestamp - interval '345600 seconds',
        '2022-12-21 14:00:00'::timestamp - interval '172800 seconds', 1, 12),
       (22, 1, '2022-12-21 14:00:00'::timestamp - interval '87890 seconds', null, 1, 15),
       (21, 0, '2022-12-21 14:00:00'::timestamp - interval '172310 seconds',
        '2022-12-21 14:00:00'::timestamp - interval '87890 seconds', 1, 15),
       (23, 2, '2022-12-21 14:00:00'::timestamp - interval '51441 seconds', null, 1, 3),
       (5, 1, '2022-12-21 14:00:00'::timestamp - interval '81212 seconds',
        '2022-12-21 14:00:00'::timestamp - interval '51441 seconds', 1, 3),
       (24, 2, '2022-12-21 14:00:00'::timestamp - interval '60001 seconds', null, 1, 8),
       (12, 1, '2022-12-21 14:00:00'::timestamp - interval '121400 seconds',
        '2022-12-21 14:00:00'::timestamp - interval '60001 seconds', 1, 8),
       (25, 2, '2022-12-21 14:00:00'::timestamp - interval '577830 seconds', null, 1, 13),
       (18, 1, '2022-12-21 14:00:00'::timestamp - interval '691211 seconds',
        '2022-12-21 14:00:00'::timestamp - interval '577830 seconds', 1, 13);