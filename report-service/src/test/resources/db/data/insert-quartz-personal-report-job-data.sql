INSERT INTO public.qrtz_job_details (sched_name, job_name, job_group, description, job_class_name, is_durable,
                                     is_nonconcurrent, is_update_data, requests_recovery, job_data)
VALUES ('MyClusteredScheduler', '8d5d705e-6270-481b-b7bd-457fb3c49164', 'PERSONAL_REPORT', null,
        'com.kanwise.report_service.job.personal.PersonalReportJob', false, false, false, true,
        E'\\xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C77080000001000000001740002696474002438643564373035652D363237302D343831622D623762642D3435376662336334393136347800');
INSERT INTO public.qrtz_job_details (sched_name, job_name, job_group, description, job_class_name, is_durable,
                                     is_nonconcurrent, is_update_data, requests_recovery, job_data)
VALUES ('MyClusteredScheduler', 'ecae2660-94ef-4636-8c7b-f1cab90f29d8', 'PERSONAL_REPORT', null,
        'com.kanwise.report_service.job.personal.PersonalReportJob', false, false, false, true,
        E'\\xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C77080000001000000001740002696474002465636165323636302D393465662D343633362D386337622D6631636162393066323964387800');

INSERT INTO public.qrtz_locks (sched_name, lock_name)
VALUES ('MyClusteredScheduler', 'STATE_ACCESS');
INSERT INTO public.qrtz_locks (sched_name, lock_name)
VALUES ('MyClusteredScheduler', 'TRIGGER_ACCESS');

INSERT INTO public.qrtz_scheduler_state (sched_name, instance_name, last_checkin_time, checkin_interval)
VALUES ('MyClusteredScheduler', 'MacBook-Pro-Jan.local1672309177772', 1672309755923, 1000);


INSERT INTO public.qrtz_triggers (sched_name, trigger_name, trigger_group, job_name, job_group, description,
                                  next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time,
                                  end_time, calendar_name, misfire_instr, job_data)
VALUES ('MyClusteredScheduler', 'ecae2660-94ef-4636-8c7b-f1cab90f29d8', 'DEFAULT',
        'ecae2660-94ef-4636-8c7b-f1cab90f29d8', 'PERSONAL_REPORT', null, 1672309560000, 1672309500000, 5, 'PAUSED',
        'CRON', 1672309419000, 0, null, 0, '');
INSERT INTO public.qrtz_triggers (sched_name, trigger_name, trigger_group, job_name, job_group, description,
                                  next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time,
                                  end_time, calendar_name, misfire_instr, job_data)
VALUES ('MyClusteredScheduler', '8d5d705e-6270-481b-b7bd-457fb3c49164', 'DEFAULT',
        '8d5d705e-6270-481b-b7bd-457fb3c49164', 'PERSONAL_REPORT', null, 1672309740000, 1672309680000, 5, 'PAUSED',
        'CRON', 1672309393000, 0, null, 0, '');


INSERT INTO public.qrtz_cron_triggers (sched_name, trigger_name, trigger_group, cron_expression, time_zone_id)
VALUES ('MyClusteredScheduler', 'ecae2660-94ef-4636-8c7b-f1cab90f29d8', 'DEFAULT', '0 0/1 * * * ?', 'Europe/Warsaw');
INSERT INTO public.qrtz_cron_triggers (sched_name, trigger_name, trigger_group, cron_expression, time_zone_id)
VALUES ('MyClusteredScheduler', '8d5d705e-6270-481b-b7bd-457fb3c49164', 'DEFAULT', '0 0/1 * * * ?', 'Europe/Warsaw');