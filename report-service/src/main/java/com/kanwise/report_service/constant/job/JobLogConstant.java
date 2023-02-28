package com.kanwise.report_service.constant.job;

import lombok.experimental.UtilityClass;

@UtilityClass
public class JobLogConstant {
    public static final String JOB_CREATED_MESSAGE = "JOB_CREATED";
    public static final String JOB_RESTARTED_MESSAGE = "JOB_RESTARTED";
    public static final String JOB_STOPPED_MESSAGE = "JOB_STOPPED";
    public static final String JOB_DELETED_MESSAGE = "JOB_DELETED";
    public static final String JOB_EXECUTION_SUCCESS_MESSAGE = "JOB_EXECUTION_SUCCESS";
    public static final String JOB_EXECUTION_FAILED_MESSAGE = "JOB_EXECUTION_FAILED_BY_EXCEPTION_%s";
    public static final String REPORT_URL = "reportUrl";
}
