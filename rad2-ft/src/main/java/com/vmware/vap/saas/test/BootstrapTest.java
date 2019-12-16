package com.vmware.vap.saas.test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.vmware.vap.saas.Http;
import org.testng.annotations.Test;
import com.vmware.vap.saas.test.TestBed.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.testng.Assert.assertEquals;

public class BootstrapTest {
    private static final Logger logger = Logger.getLogger("test");
    private static final Gson gson = new Gson();

    @Test(dataProvider = "getInstance", dataProviderClass = TestBed.class)
    public void testBootstrap(TestBed testBed) throws IOException {
        JsonArray manageEndpointServiceStates = new JsonArray();
        for (VCenter vCenter : testBed.vap.vCenters) {
            JsonArray endpoints = new JsonArray();
            for (VM vm : vCenter.vms) {
                JsonObject endpoint = new JsonObject();
                endpoint.addProperty("vc_id", vCenter.uuid);
                endpoint.addProperty("vm_mor", vm.mor.getValue());
                endpoint.addProperty("user", vm.userName);
                endpoint.addProperty("password", vm.password);
                endpoints.add(endpoint);
            }
            JsonObject manageEndpointServiceState = new JsonObject();
            manageEndpointServiceState.addProperty("vc_ip", vCenter.host);
            manageEndpointServiceState.addProperty("vc_user", vCenter.userName);
            manageEndpointServiceState.addProperty("vc_password", vCenter.password);
            manageEndpointServiceState.add("endpoints", endpoints);
            manageEndpointServiceStates.add(manageEndpointServiceState);
        }
        JsonObject request = new JsonObject();
        request.addProperty("cloudProxyId", "");
        request.addProperty("job", "install");
        request.add("manageEndpointServiceStates", manageEndpointServiceStates);
        String payload = gson.toJson(request);
        logger.info("Bootstrap request: " + payload);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=utf8");
        headers.put("Accept", "application/json");
        Http.Response response = Http.exchange("http://localhost:9080/vap-saas/api/jobs/agentdeployment", headers, Http.Method.POST, payload.getBytes());
        assertEquals(response.code, 200, "Bootstrap failed: " + new String(response.data));
        logger.info(response.toString());
        // TODO: Write a get API to check the job status.
    }
}
