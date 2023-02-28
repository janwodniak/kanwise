INSERT INTO public.project (id, active, created_at, description, status, title)
VALUES (1, true, '2022-12-16 09:53:07.364900',
        'This is the backend component of the Kanwise application. It is responsible for providing the APIs and data storage for the frontend, as well as handling any business logic and integration with other systems.',
        'CREATED', 'Kanwise-Backend'),
       (3, true, '2022-12-16 09:56:35.902237',
        'This project manages the deployment, monitoring, and maintenance of the Kanwise application. It includes tasks such as setting up infrastructure, automating builds and deployments, and monitoring the app''s performance and stability.',
        'ON_TRACK', 'Kanwise-DevOps'),
       (2, true, '2022-12-16 09:53:41.243753',
        'This is the frontend component of the Kanwise application. It is responsible for rendering the user interface and handling user interactions. It communicates with the backend to retrieve data and perform actions.',
        'ON_HOLD', 'Kanwise-Frontend');
