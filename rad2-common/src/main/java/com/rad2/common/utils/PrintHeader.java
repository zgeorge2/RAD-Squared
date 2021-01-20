package com.rad2.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helper class to print standard headers (e.g. for database tables)
 */
public final class PrintHeader {
    private final List<Pair<String, Integer>> fields;
    private String format;
    private String divider;
    private final List<String> columnHeaders;

    @SafeVarargs
    public PrintHeader(Pair<String, Integer>... fields) {
        this.fields = new ArrayList<>();
        this.columnHeaders = new ArrayList<>();
        Collections.addAll(this.fields, fields);
        setup();
    }

    private void setup() {
        StringBuffer headerRowFormatBuffer = new StringBuffer("\t");
        StringBuffer dividerRowBuffer = new StringBuffer("\t");
        fields.forEach(item -> {
            headerRowFormatBuffer.append("%").append(item.getRight()).append("s");
            dividerRowBuffer.append(String.join("",
                    Collections.nCopies(item.getRight(), "-")));
            this.columnHeaders.add(item.getLeft());
        });
        this.format = headerRowFormatBuffer.toString() + "\n";
        this.divider = dividerRowBuffer.toString() + "\n";
    }

    public String format() {
        return this.format;
    }

    @Override
    public String toString() {
        return String.format(this.format, this.columnHeaders.toArray())
                + this.divider;
    }

    public static void main(String args[]) {
        PrintHeader ph = new PrintHeader(
                new Pair<>("Fin Corp", 30),
                new Pair<>("Branch", 15),
                new Pair<>("Key", 30));
        System.out.print(ph);
        System.out.printf(ph.format(), "Village Bank", "Bank1", "VillageBank/Bank1");
        System.out.printf(ph.format(), "Village Bank", "Bank2", "VillageBank/Bank2");
    }
}
