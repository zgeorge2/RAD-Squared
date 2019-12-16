package com.vmware.vap.saas.test;

import com.google.gson.Gson;
import com.vmware.vap.saas.Http;
import com.vmware.vap.saas.Main;
import com.vmware.vap.saas.VCSession;
import com.vmware.vim25.ManagedObjectReference;
import org.testng.annotations.Optional;
import org.testng.annotations.*;

import java.io.*;
import java.lang.ProcessBuilder.Redirect;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class TestBed {

    private static final Logger logger = Logger.getLogger("test");
    private static final String RECOMMENDED_BUILD_URL = "http://ucp-jenkins.eng.vmware.com:8080/view/UCP_VA-main/job/Recommendation_UCP_Changeset/ws/get_latest_stable_build/*view*/";
    private static final String DELIVERABLE = "http://buildapi.eng.vmware.com/ob/deliverable/?_format=json&path__endswith=_OVF10.ovf&build=";
    private static final String DELIVERABLE_PREFIX = "http://build-squid.eng.vmware.com/build/mts/release/bora-";
    private static final String VAP_VM_PREFIX = "VAP - ";

    private static TestBed instance;
    private static Gson gson = new Gson();
    VAP vap;
    private String buildNumber;
    private String ovfTemplateURL;
    private Process server;

    @DataProvider(name = "getInstance")
    public Object[][] getInstance(Method method) {
        logger.fine("Invoking Method: " + method.toString());
        return new Object[][] { new Object[] { instance }};
    }

    @Parameters({ "configuration" })
    @BeforeSuite
    public void beforeSuite(@Optional String configurationFile) throws Exception {
        instance = this;
        startServer();
        if (configurationFile == null) {
            try (Reader reader = new InputStreamReader(getClass().getResourceAsStream("/configuration.json"))) {
                vap = gson.fromJson((reader), VAP.class);
            }
        } else {
            try (Reader reader = new FileReader(configurationFile)) {
                vap = gson.fromJson((reader), VAP.class);
            }
        }
        try (InputStream inputStream = new URL(RECOMMENDED_BUILD_URL).openStream()) {
            buildNumber = VCSession.toString(inputStream).trim();
        }
        try (Reader reader = new InputStreamReader(new URL(DELIVERABLE + buildNumber).openStream())) {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> list = (List<Map<String, String>>) gson.fromJson(reader, Map.class).get("_list");
            ovfTemplateURL = list.stream()
                    .map(m -> m.get("path"))
                    .findAny()
                    .orElseThrow(NullPointerException::new);
            ovfTemplateURL = DELIVERABLE_PREFIX + buildNumber + "/" + ovfTemplateURL;
        }

        for (VCenter vCenter : vap.vCenters) {
            try (VCSession vcSession = new VCSession(vCenter.host, vCenter.userName, vCenter.password)) {
                vCenter.uuid = vcSession.serviceContent.getAbout().getInstanceUuid();
                if (vCenter.host.equals(vap.hostedVc)) {
                    configureVAP(vcSession);
                }
                for (VM vm : vCenter.vms) {
                    vcSession.revertSnapshot(vm.mor);
                }
            }
        }
    }

    private void configureVAP(VCSession vcSession) throws Exception {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("ucp_api_admin_password", vap.password);
        parameters.put("guestinfo.cis.appliance.ssh.enabled", "True");
        ManagedObjectReference vapVM = null;
        List<Map<String, Object>> vms = vcSession.search(null, "VirtualMachine", "name");
        for (Map<String, Object> virtualMachine: vms) {
            ManagedObjectReference vm = (ManagedObjectReference) virtualMachine.get("_id");
            String vmName = (String) virtualMachine.get("name");
            if (!vmName.startsWith(VAP_VM_PREFIX)) {
                continue;
            }
            if (vmName.endsWith(instance.buildNumber)) {
                vap.host = vcSession.revertSnapshot(vm);
                vapVM = vm;
            } else {
                vcSession.destroyVM(vm);
            }
            break;
        }
        if (vapVM == null) {
            vapVM = vcSession.importOVF(instance.ovfTemplateURL, VAP_VM_PREFIX + instance.buildNumber, parameters, vap.specs);
            ManagedObjectReference task = vcSession.vimPort.powerOnVMTask(vapVM, null);
            vcSession.waitForTask(task);
            vap.host = vcSession.getIp(vapVM);
            task = vcSession.vimPort.createSnapshotTask(vapVM, "Test", "Test", true, false);
            vcSession.waitForTask(task);
        }
        String auth = vap.userName + ":" + vap.password;
        auth = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", auth);
        headers.put("Content-Type", "application/json; charset=utf-8");
        headers.put("Accept", "application/json");
        String url = "https://" + vap.host + ":9000/core/authn/basic";
        byte[] payload = "{ \"requestType\": \"LOGIN\"}".getBytes();
        Http.Response response = Http.retry(url, headers, Http.Method.POST, payload);
        url = "https://" + vap.host + ":9000/ucp/config/api";
        auth = response.headers.get("x-xenon-auth-token").get(0);
        headers.remove("Authorization");
        headers.put("x-xenon-auth-token", auth);
        payload = gson.toJson(vap.configServiceRequest).getBytes();
        response = Http.exchange(url, headers, Http.Method.POST, payload);
        if (response.code != 200) {
            throw new RuntimeException("Configuration failed - " + response);
        }
    }

    private static void startServer() throws IOException {
        Path java = Paths.get(System.getProperty("java.home")).resolve("bin").resolve("java");
        if (!Files.isExecutable(java)) {
            java = java.resolveSibling("java.exe");
        }
        if (!Files.isExecutable(java)) {
            throw new RuntimeException("Java executable not found.");
        }
        Path pwd = Paths.get(Main.uri).resolve("../../..").toRealPath();
        Path jar = pwd.resolve("vapaas-sb/target/vapaas-sb-1.0-SNAPSHOT.jar");
        if (!Files.isReadable(jar)) {
            throw new RuntimeException("Jar file not found.");
        }
        String[] cmd = { java.toString(), "-Dspring.profiles.active=dev", "-jar", jar.toString(),
                "--akka.conf=application_akka_vap_service.conf", "--server.port=9080" };
        Redirect redirect = Redirect.to(File.createTempFile("server", ".log"));
        instance.server = new ProcessBuilder(cmd)
                .directory(pwd.toFile())
                .redirectOutput(redirect)
                .redirectError(redirect)
                .start();
        Http.Response response = Http.retry("http://localhost:9080/vap-saas/infra", null, Http.Method.GET, null);
        logger.info("Test URL returned: " + response);
    }

    @AfterSuite
    public void afterSuite() throws Exception {
        logger.info("Killing server process");
        instance.server.destroyForcibly().waitFor(5, TimeUnit.MINUTES);
        logger.info("Killed server process");
    }

    static class VAP {
        List<VCenter> vCenters;
        List<ManagedObjectReference> specs = Collections.emptyList();
        String host;
        String userName = "admin@ucp.local";
        String password;
        String hostedVc;
        Map<String, String> configServiceRequest;

        @Override
        public String toString() {
            return host;
        }
    }

    static class VCenter {
        String host;
        String userName;
        String password;
        String uuid;
        List<VM> vms;

        @Override
        public String toString() {
            return host + " [" + uuid + "]";
        }
    }

    static class VM {
        ManagedObjectReference mor;
        String userName;
        String password;
    }
}
