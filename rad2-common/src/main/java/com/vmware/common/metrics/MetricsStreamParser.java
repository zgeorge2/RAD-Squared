package com.vmware.common.metrics;

import com.vmware.common.HasEndpointIdentifier;
import com.vmware.common.validation.ValidCloudAccount;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.vmware.vap.bridge.common.VAPBridgeConstants.VC_UUID;
import static com.vmware.vap.bridge.common.VAPBridgeConstants.VM_MOR;

/**
 * Parses a Wavefront Data Format metric line into its constituent parts https://docs.wavefront
 * .com/wavefront_data_format.html Syntax: <metricName> <metricValue> <timestamp> source=<source> [pointTags]
 */
public class MetricsStreamParser {
    private static final String filename = "C:\\Users\\zgeorge\\IdeaProjects\\test1.txt";
    // negative lookbehind with basic separator of \"<space>
    private static final String metricLineSplitter = ("(?<!\\\\)\" ");
    // replace =<space>" with  =
    private static final String tupleCleaner1 = "=\\s*\\\"";
    // replace " not preceded by \ at end of a value with <empty>
    private static final String tupleCleaner2 = "(?<!\\\\)\\\"$";
    // finally replace \" with " - uses a positive lookbehind
    private static final String valueCleaner = "\\\\\\\"";

    public static void main(String[] args) {
        MetricsStreamParser parser = new MetricsStreamParser();
        try {
            List<MetricLineParts> metrics =
                new BufferedReader(new FileReader(new File(filename))).lines()
                    .map(MetricsStreamParser::getParts).collect(Collectors.toList());
            metrics.forEach(System.out::println);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public List<MetricLineParts> xtractMetricLineParts(String metricLines) {
        MetricsStreamParser parser = new MetricsStreamParser();

        List<MetricLineParts> metrics =
            new BufferedReader(new StringReader(metricLines)).lines()
                .map(MetricsStreamParser::getParts).collect(Collectors.toList());
        if (metrics == null) {
            metrics = new ArrayList<>();
        }

        return metrics;
    }

    private static MetricLineParts getParts(String metricLine) {
        Map<String, String> keyValuePairs = new HashMap<>();
        Arrays.asList(metricLine.split(metricLineSplitter)).forEach(tuple -> {
            tuple = tuple.replaceFirst(tupleCleaner1, "=").replaceFirst(tupleCleaner2, "");
            String[] tp = tuple.split("=", 2);
            keyValuePairs.put(tp[0], tp[1].replaceAll(valueCleaner, "\""));
        });
        return new MetricLineParts(keyValuePairs);
    }

    public static List<MetricLineParts> parsePayload(String payload) {
        List<MetricLineParts> metricLineParts = new ArrayList<>();
        for (final String line : payload.split("\n")) {
            metricLineParts.add(getParts(line));
        }
        return metricLineParts;
    }

    private MetricLineParts getParts1(String metricLine) {
        return new MetricLineParts(Pattern.compile(metricLineSplitter).splitAsStream(metricLine)
            .map(t -> t.replaceFirst(tupleCleaner1, "=").replaceFirst(tupleCleaner2, "").split("=", 2))
            .collect(Collectors.toMap(val -> val[0], val -> val[1].replaceAll(valueCleaner, "\""))));
    }

    /**
     * convenience class that further parses the source parts (the first part of the format)
     */
    public static class MetricLineParts implements HasEndpointIdentifier {
        String source;
        String metricName;
        double metricValue;
        String timestamp;
        @ValidCloudAccount
        String vc_uuid;
        String vm_mor;
        Map<String, String> keyValuePairs;

        MetricLineParts(Map<String, String> keyValuePairs) {
            this.keyValuePairs = keyValuePairs;
            // find the key that has "<metricName> <metricValue> <timestamp> source" as its parts
            String sourcePartsKey = keyValuePairs.keySet().stream()
                .filter(k -> k.endsWith(" source")).findAny().get();
            // break up the sourcePartsKey
            String[] parts = sourcePartsKey.split(" ");
            // assign the parts
            this.metricName = parts[0];
            this.metricValue = Double.parseDouble(parts[1].trim());
            this.timestamp = parts[2];
            // grab the existing value of source
            this.source = keyValuePairs.get(sourcePartsKey);
            this.vc_uuid = keyValuePairs.remove(VC_UUID);
            this.vm_mor = keyValuePairs.remove(VM_MOR);
            // get rid of the original key
            keyValuePairs.remove(sourcePartsKey);
        }

        public String getSource() {
            return source;
        }

        public String getMetricName() {
            return metricName;
        }

        public double getMetricValue() {
            return metricValue;
        }

        public String getTimestamp() {
            return timestamp;
        }

        @Override
        public String getVc_uuid() {
            return this.vc_uuid;
        }

        @Override
        public String getVm_mor() {
            return this.vm_mor;
        }

        public Map<String, String> getKeyValuePairs() {
            return keyValuePairs;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("_METRIC_NAME=[").append(this.getMetricName()).append("]\n");
            sb.append("_METRIC_VALUE=[").append(this.getMetricValue()).append("]\n");
            sb.append("_METRIC_TIMESTAMP=[").append(this.getTimestamp()).append("]\n");
            sb.append("_METRIC_SOURCE=[").append(this.getSource()).append("]\n");
            getKeyValuePairs().entrySet()
                .forEach(e -> sb.append(String.format("\t[%s]=[%s]\n", e.getKey(), e.getValue())));
            return sb.toString();
        }
    }
}
