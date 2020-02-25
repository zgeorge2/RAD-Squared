/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.examples.test;

import com.rad2.common.utils.PrintUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class JustABunchOfCode {
    private static final Logger logger = LoggerFactory.getLogger(JustABunchOfCode.class);

    public static void main(String[] args) {
        JustABunchOfCode j = new JustABunchOfCode();

        j.testRandomNumGeneration();

        j.testReflectionClassMethods();
        // extract the string in path upto the last '/'
        findParentPathAndLastPathComponent("akka://abc/defg/aaa/bb/fghij");
        findParentPathAndLastPathComponent("/");
        findParentPathAndLastPathComponent("");
        findParentPathAndLastPathComponent("//");
        findParentPathAndLastPathComponent("akka://abc/defg/aaa/bb/fghij/");

        // create matcher for pattern p and given string
        Pattern cClassPattern = Pattern.compile("^([a-zA-Z0-9]+)(Controller)");
        String[] input = {"", "foo", "bar", "ControllerController", "Controller", "RController",
            "FooController"};
        for (String s : input) {
            Matcher m = cClassPattern.matcher(s);
            String msg = m.find() ?
                String.format("Whole:[%s]; First[%s]; Second[%s]", m.group(0), m.group(1), m.group(2)) :
                "No Match";
            PrintUtils.printToActor("%s -> {%s}", s, msg);
        }

        try {
            String className = j.getClass().getName();
            Class<?> clazz = Class.forName(className);
            PrintUtils.printToActor("j's Class = %s", className);
            JustABunchOfCode j1 = (JustABunchOfCode) clazz.getDeclaredConstructor().newInstance();
            j1.testReflectionClassMethods();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException
            | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }

        j.testJobEnum();
    }

    private static void findParentPathAndLastPathComponent(String path) {
        if (Objects.isNull(path) || path.length() == 0) return;
        String parentPath = path.substring(0, path.lastIndexOf('/'));
        String lastPathPart = path.substring(path.lastIndexOf('/') + 1, path.length());
        PrintUtils.printToActor("path=[%s]; parentPath=[%s]; lastPathPart=[%s]", path, parentPath,
            lastPathPart);
    }

    private void testRandomNumGeneration() {
        List<String> list = Arrays.asList("Alpha", "Beta", "Chi", "Zeta");
        IntStream.range(0, 100).forEach(i -> {
            int randPos = (int) (Math.random() * list.size());
            String msg = list.size() > 0 ? list.get(randPos) : "EMPTY_LIST";
            System.out.println(String.format("%d->%s", randPos, msg));
        });
    }

    private void testReflectionClassMethods() {
        PrintUtils.printToActor("this.getClass() = %s", this.getClass());
        PrintUtils.printToActor("this.getClass().getName() = %s", this.getClass().getName());
        PrintUtils.printToActor("this.getClass().getCanonicalName() = %s",
            this.getClass().getCanonicalName());
        PrintUtils.printToActor("this.getClass().getSimpleName().toUpperCase() = %s",
            this.getClass().getSimpleName().toUpperCase());
        PrintUtils.printToActor("this.getClass().getTypeName() = %s", this.getClass().getTypeName());
    }

    private void testJobEnum() {
        PrintUtils.printToActor("%s:%s", JobStatusEnum.get("JOB_STATUS_NOT_STARTED"),
            JobStatusEnum.JOB_STATUS_NOT_STARTED.name());
        PrintUtils.printToActor("%s:%s", JobStatusEnum.get("JOB_STATUS_IN_PROGRESS"),
            JobStatusEnum.JOB_STATUS_IN_PROGRESS.name());
        PrintUtils.printToActor("%s:%s", JobStatusEnum.get("JOB_STATUS_SUCCESS"),
            JobStatusEnum.JOB_STATUS_SUCCESS.name());
        PrintUtils.printToActor("%s:%s", JobStatusEnum.get("JOB_STATUS_FAILED"),
            JobStatusEnum.JOB_STATUS_FAILED.name());
        PrintUtils.printToActor("%s:%s", JobStatusEnum.get("JOB_STATUS_INVALID"),
            JobStatusEnum.JOB_STATUS_INVALID.name());
        PrintUtils.printToActor("%s:%s", JobStatusEnum.get("FAKE_JOB_STATUS"),
            JobStatusEnum.JOB_STATUS_INVALID.name());
    }

    public enum JobStatusEnum {
        JOB_STATUS_NOT_STARTED(),
        JOB_STATUS_IN_PROGRESS(),
        JOB_STATUS_SUCCESS(),
        JOB_STATUS_FAILED(),
        JOB_STATUS_INVALID();

        public static JobStatusEnum get(String status) {
            return Arrays.stream(JobStatusEnum.values())
                .filter((en) -> status.equals(en.name()))
                .findFirst().orElse(JOB_STATUS_INVALID);
        }
    }
}
