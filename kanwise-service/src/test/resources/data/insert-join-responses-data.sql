INSERT INTO public.join_response (id, message, responded_at, status, join_request_id, responded_by_id)
VALUES (1,
        'Hi Jarosław! Thank you for your interest in joining the Kanwise-Backend project. We are pleased to offer you a position on the team. Please let us know if you have any questions or if there is anything we can do to support you as you get started.',
        '2022-12-16 10:11:49.201691', 'ACCEPTED', 1, 1),
       (2,
        'Hi Jarosław! Thank you for your interest in joining the Kanwise-Frontend project. Unfortunately, we are unable to offer you a position on the team at this time. We appreciate your interest and encourage you to keep an eye out for future opportunities.',
        '2022-12-16 10:13:05.368388', 'REJECTED', 2, 1);


UPDATE public.join_request
SET join_response_id =
        CASE
            WHEN id = 1 THEN 1
            WHEN id = 2 THEN 2
            ELSE join_response_id
            END;