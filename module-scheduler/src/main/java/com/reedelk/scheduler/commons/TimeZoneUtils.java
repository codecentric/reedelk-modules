package com.reedelk.scheduler.commons;

import com.reedelk.runtime.api.commons.StringUtils;

import java.util.TimeZone;

public class TimeZoneUtils {


    public static TimeZone getOrDefault(String timeZone) {
        if (StringUtils.isBlank(timeZone)) {
            return TimeZone.getDefault();
        } else {
            return TimeZone.getTimeZone(timeZone);
        }
    }
}
