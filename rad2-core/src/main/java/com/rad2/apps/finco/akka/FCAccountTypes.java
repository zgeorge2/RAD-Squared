package com.rad2.apps.finco.akka;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public enum FCAccountTypes {
    NRO("NRO", 8),
    NRE("NRE", 8),
    SAVINGS("Savings", 8),
    CHECKING("Checking", 8);
    private final String name;
    private final int percent;

    FCAccountTypes(String name, int percent) {
        this.name = name;
        this.percent = percent;
    }

    public static List<String> getTypes() {
        return Arrays.stream(values()).map(FCAccountTypes::getName).collect(Collectors.toList());
    }

    public static String checkType(String name) {
        return getTypes()
                .stream()
                .filter(name::equalsIgnoreCase)
                .findAny()
                .orElse(null);
    }

    public static void applyToAllTypes(Consumer<String> func) {
        getTypes().forEach(func::accept);
    }

    public String getName() {
        return name;
    }

    public int getPercent() {
        return percent;
    }

    public String toString() {
        return this.name;
    }
}
