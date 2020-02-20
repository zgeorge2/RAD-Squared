package com.rad2.apps.adm.ignite;

import java.util.Arrays;
import java.util.Objects;

public enum JobStatusEnum {
    JOB_STATUS_NOT_STARTED(),
    JOB_STATUS_IN_PROGRESS(),
    JOB_STATUS_SUCCESS(),
    JOB_STATUS_FAILED(),
    JOB_STATUS_INVALID(),
    JOB_STATUS_RETRIEVAL_ONLY(); // special job status to indicate job is for result retrieval only

    public static JobStatusEnum get(String status) {
        if (Objects.isNull(status) || (status.length() == 0)) {
            return JOB_STATUS_INVALID;
        }
        return Arrays.stream(JobStatusEnum.values())
                .filter((en) -> status.equals(en.name()))
                .findFirst().orElse(JOB_STATUS_INVALID);
    }

    public static final String JOB_GET_RESULT_FORMAT = "Use:[/adm/getJobResult/%s] to get result\n";
    public static final String JOB_RESULT_INTRO_FORMAT = "Job [%s] result may be partial\n" + JOB_GET_RESULT_FORMAT;
    public static final String JOB_TIMEOUT_FORMAT = "Job has timed out.\n" + JOB_GET_RESULT_FORMAT;
    public static final String JOB_FAILED_FORMAT = "Job has failed, expired or is invalid!";
}
