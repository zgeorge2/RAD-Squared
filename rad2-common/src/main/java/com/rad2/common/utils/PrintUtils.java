/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrintUtils {
    private static Logger log = LoggerFactory.getLogger(PrintUtils.class);

    public static String padFormat(String format, String s, char padChar) {
        return String.format(format, s).replace(' ', padChar);
    }

    public static void print(String format, Object... args) {
        log.info(String.format(format, args));
        //System.out.println(composeMessage(String.format(format, args)));
    }

    public static void print(String message) {
        log.info(message);
        //System.out.println(composeMessage(message));
    }

    private static String composeMessage(String message) {
        Exception ex = new Exception();
        //String threadName = Thread.currentThread().getName();
        long threadId = Thread.currentThread().getId();
        //int tPos = threadName.lastIndexOf("/") + 1;
        //String tName = threadName.substring(tPos) + "(" + threadId + ")";
        StackTraceElement ste = ex.getStackTrace()[2];
        String fileName = ste.getFileName();
        int lineNum = ste.getLineNumber();
        //String clsNameFull = ste.getClassName();
        //int pos = clsNameFull.lastIndexOf('.') + 1;
        //String clsName = clsNameFull.substring(pos);
        String method = ste.getMethodName();

        return String.format("(%s:%d):[%d]:%s: %s", fileName, lineNum, threadId, method, message);
    }
}
