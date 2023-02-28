CREATE DATABASE report;
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'report';
ALTER DATABASE report OWNER TO kanwise;
GRANT ALL PRIVILEGES ON DATABASE report TO kanwise;
DO
$$
    BEGIN
        RAISE LOG 'Created database report';
    END
$$;

CREATE DATABASE "user";
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'user';
ALTER DATABASE "user" OWNER TO kanwise;
GRANT ALL PRIVILEGES ON DATABASE "user" TO kanwise;
DO
$$
    BEGIN
        RAISE LOG 'Created database user';
    END
$$;

CREATE DATABASE quartz;
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'quartz';
ALTER DATABASE quartz OWNER TO kanwise;
GRANT ALL PRIVILEGES ON DATABASE quartz TO kanwise;
DO
$$
    BEGIN
        RAISE LOG 'Created database quartz';
    END
$$;

CREATE DATABASE project;
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'project';
ALTER DATABASE project OWNER TO kanwise;
GRANT ALL PRIVILEGES ON DATABASE project TO kanwise;
DO
$$
    BEGIN
        RAISE LOG 'Created database project';
    END
$$;
