package com.vmware.vap.service.dto;

//TODO once we know how to patch the discovery service, we may need to modify this. May be only
// cloudAccountDocumentLink is sufficient. We may not need other CloudAccountDetails
public class CloudAccountToVAPMappingRequest {
    public String mappedVAPDcId;
    public String hostName;
    public String dcId;
    public String privateKeyId;
    public String privateKey;
    public String cloudAccountId;
}
