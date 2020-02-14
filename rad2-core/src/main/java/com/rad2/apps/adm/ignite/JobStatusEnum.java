package com.rad2.apps.adm.ignite;

import java.util.Arrays;
import java.util.Objects;

public enum JobStatusEnum {
    JOB_STATUS_NOT_STARTED(),
    JOB_STATUS_IN_PROGRESS(),
    JOB_STATUS_SUCCESS(),
    JOB_STATUS_FAILED(),
    JOB_STATUS_INVALID();

    public static JobStatusEnum get(String status) {
        if (Objects.isNull(status) || (status.length() == 0)) {
            return JOB_STATUS_INVALID;
        }
        return Arrays.stream(JobStatusEnum.values())
            .filter((en) -> status.equals(en.name()))
            .findFirst().orElse(JOB_STATUS_INVALID);
    }
}
