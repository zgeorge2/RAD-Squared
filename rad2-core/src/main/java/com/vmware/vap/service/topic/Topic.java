package com.vmware.vap.service.topic;


public class Topic {

    private static String BOOTSTRAP_TOPIC_SUFFIX = "infra/bootstrap";

    public static boolean isBootstrapTopic(String topic) {
        boolean ret = false;
        if (topic.endsWith(BOOTSTRAP_TOPIC_SUFFIX)) {
            ret = true;
        }

        return ret;
    }

    public static boolean isBootstrapTopic(Topic topic) {
        boolean ret = false;
        if (topic != null && topic.getType() == TopicType.BOOTSTRAP) {
            ret = true;
        }

        return ret;
    }

    public static Topic createTopic(String topic) {
        if (isBootstrapTopic(topic)) {
            return new BootstrapTopic(topic);
        } else {
            return null;
        }
    }

    enum TopicType {
        TOPIC, BOOTSTRAP
    }

    private String topic;
    private String vcId;
    private String vmMor;
    protected TopicType type = TopicType.TOPIC;
    private String[] topicComps;


    public Topic(String topic) {
        this.topic = topic;
        topicComps = topic.split("/");
        this.vcId = topicComps[0];
        this.vmMor = topicComps[1];
    }

    public String getEndpointId() {
        return vcId + "_" + vmMor;
    }

    public String getTopic() {
        return topic;
    }

    public String getVcId() {
        return vcId;
    }

    public String getVmMor() {
        return vmMor;
    }

    public TopicType getType() { return type; }

    public String[] getTopicComps() {
        return topicComps;
    }
}
