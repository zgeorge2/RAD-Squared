package com.vmware.vap.service.topic;

public class BootstrapTopic extends Topic {
    private String requestId;

    public BootstrapTopic(String topic) {
        super(topic);
        super.type = TopicType.BOOTSTRAP;
        this.requestId = super.getTopicComps()[2];
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
