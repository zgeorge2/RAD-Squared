package com.vmware.vap.saas;

import com.vmware.vim25.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.BindingProvider;
import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VCSession implements Closeable {

    private static final Logger logger = Logger.getLogger("VCSession");
    private static final int TIMEOUT = 300_000;
    public VimPortType vimPort;
    public ServiceContent serviceContent;

    static {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] { new TrustAllTrustManager() }, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        HttpsURLConnection.setDefaultHostnameVerifier((urlHostName, sslSession) -> true);

    }

    public VCSession(String host, String userName, String password) throws RuntimeFaultFaultMsg, InvalidLoginFaultMsg, InvalidLocaleFaultMsg {
        vimPort = new VimService().getVimPort();
        Map<String, Object> requestContext = ((BindingProvider) vimPort).getRequestContext();
        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "https://" + host + "/sdk");
        requestContext.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
        ManagedObjectReference serviceInstance = new ManagedObjectReference();
        serviceInstance.setType("ServiceInstance");
        serviceInstance.setValue("ServiceInstance");
        serviceContent = vimPort.retrieveServiceContent(serviceInstance);
        UserSession userSession = vimPort.login(serviceContent.getSessionManager(), userName, password, null);
        logger.fine("Logged in as: " + userSession.getUserName());
    }

    public ManagedObjectReference importOVF(String ovfTemplateURL, String vmName, Map<String, String> parameters, List<ManagedObjectReference> specs) throws Exception {
        ManagedObjectReference hostSystem = null;
        ManagedObjectReference dataStore = null;
        ManagedObjectReference network = null;
        ManagedObjectReference folder = null;
        ManagedObjectReference resourcePool = null;
        for (ManagedObjectReference mor : specs) {
            switch (mor.getType()) {
                case "HostSystem":
                    hostSystem = mor;
                    break;
                case "Datastore":
                    dataStore = mor;
                    break;
                case "Network":
                    network = mor;
                    break;
                case "Folder":
                    folder = mor;
                    break;
                case "ResourcePool":
                    resourcePool = mor;
                    break;
            }
        }

        if (hostSystem == null) {
            List<Map<String, Object>> list = search(null, "HostSystem", "name");
            hostSystem = (ManagedObjectReference) list.get(0).get("_id");
        }

        Map<String, Object> map = retrieveProperties(hostSystem, "parent", "datastore", "network", "vm");

        if (dataStore == null) {
            dataStore = ((ArrayOfManagedObjectReference) map.get("datastore")).getManagedObjectReference().get(0);
        }

        if (network == null) {
            network = ((ArrayOfManagedObjectReference) map.get("network")).getManagedObjectReference().get(0);
        }
        String networkName = retrieveProperty(network, "name");
        OvfNetworkMapping networkMapping = new OvfNetworkMapping();
        networkMapping.setName(networkName);
        networkMapping.setNetwork(network);

        if (folder == null) {
            folder = ((ArrayOfManagedObjectReference) map.get("vm")).getManagedObjectReference().get(0);
            folder = retrieveProperty(folder, "parent");
        }

        if (resourcePool == null) {
            ManagedObjectReference computeResource = (ManagedObjectReference) map.get("parent");
            resourcePool = retrieveProperty(computeResource, "resourcePool");
        }

        OvfCreateImportSpecParams params = new OvfCreateImportSpecParams();
        params.setLocale("");
        params.setDeploymentOption("");
        params.setHostSystem(hostSystem);
        params.setEntityName(vmName);
        params.getNetworkMapping().add(networkMapping);
        String ovfDescriptor = getOvfDescriptor(ovfTemplateURL, parameters);
        OvfCreateImportSpecResult importSpec = vimPort.createImportSpec(serviceContent.getOvfManager(), ovfDescriptor, resourcePool, dataStore, params);
        ManagedObjectReference httpNfcLease = vimPort.importVApp(resourcePool, importSpec.getImportSpec(), folder, hostSystem);
        String urlPrefix = ovfTemplateURL.substring(0, ovfTemplateURL.lastIndexOf("/") + 1);
        return uploadFiles(httpNfcLease, importSpec, urlPrefix);
    }

    private static String getOvfDescriptor(String url, Map<String, String> parameters) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document document = factory.newDocumentBuilder().parse(url);
        NodeList list = document.getElementsByTagName("Property");
        final String OVF = "http://schemas.dmtf.org/ovf/envelope/1";
        for (int i = 0; i < list.getLength(); i++) {
            Element element = (Element) list.item(i);
            String key = element.getAttributeNS(OVF, "key");
            if (parameters.containsKey(key)) {
                element.setAttributeNS(OVF, "value", parameters.get(key));
            }
        }
        StringWriter writer = new StringWriter();
        TransformerFactory.newInstance().newTransformer().transform(new DOMSource(document), new StreamResult(writer));
        return writer.toString();
    }

    private ManagedObjectReference uploadFiles(ManagedObjectReference httpNfcLease, OvfCreateImportSpecResult importSpec, String url)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException, TimedoutFaultMsg, InvalidStateFaultMsg, HttpFaultFaultMsg, SSLVerifyFaultFaultMsg {
        Map<String, Object> map;
        while (true) {
            map = retrieveProperties(httpNfcLease);
            Element stateElement = (Element) map.get("state");
            String state = stateElement.getFirstChild().getTextContent();
            logger.info("State: " + state + " Progress: " + map.get("initializeProgress") + "%");
            if ("ready".equals(state)) {
                logger.info("initialization complete " + map.get("initializeProgress") + "%");
                break;
            }
            if ("error".equals(state)) {
                throw new RuntimeException("An Error occurred to get httpNfcLease.");
            }
            Thread.sleep(1000);
        }
        HttpNfcLeaseInfo httpNfcLeaseInfo = (HttpNfcLeaseInfo) map.get("info");
        HttpNfcLeaseCapabilities capabilities = (HttpNfcLeaseCapabilities) map.get("capabilities");
        String mode = (String) map.get("mode");
        boolean pull = false;
        if (capabilities != null) {
            pull = capabilities.isPullModeSupported();
        } else if ("pushOrGet".equals(mode)) {
            pull = true;
        }
        if (pull) {
            List<HttpNfcLeaseSourceFile> files = new ArrayList<>();
            for (OvfFileItem fileItem : importSpec.getFileItem()) {
                HttpNfcLeaseSourceFile file = new HttpNfcLeaseSourceFile();
                file.setTargetDeviceId(fileItem.getDeviceId());
                file.setUrl(url + fileItem.getPath());
                file.setSize(fileItem.getSize());
                files.add(file);
            }
            ManagedObjectReference task = vimPort.httpNfcLeasePullFromUrlsTask(httpNfcLease, files);
            TaskInfoState state = waitForTask(task);
            if (state == TaskInfoState.SUCCESS) {
                return httpNfcLeaseInfo.getEntity();
            }
        }
        AtomicLong uploaded = new AtomicLong();
        int totalSizeMB = (int) (importSpec.getFileItem().stream().mapToLong(OvfFileItem::getSize).sum() / 1024 / 1024);
        Map<String, Character> status = new HashMap<>();
        long start = System.currentTimeMillis();
        for (HttpNfcLeaseDeviceUrl httpNfcLeaseDeviceUrl : httpNfcLeaseInfo.getDeviceUrl()) {
            String importKey = httpNfcLeaseDeviceUrl.getImportKey();
            String targetUrl = httpNfcLeaseDeviceUrl.getUrl();
            for (OvfFileItem ovfFileItem : importSpec.getFileItem()) {
                if (ovfFileItem.getDeviceId().equals(importKey)) {
                    String file = ovfFileItem.getPath();
                    status.put(file, '.');
                    new Thread(() -> {
                        try {
                            uploadFile(url + file, targetUrl, ovfFileItem, uploaded);
                            status.put(file, 'P');
                        } catch (Exception e) {
                            status.put(file, 'F');
                            logger.log(Level.SEVERE, "An error occurred uploading " + file + ": " + e.getMessage(), e);
                        }
                    }).start();
                    break;
                }
            }
        }
        while (status.values().contains('.')) {
            Thread.sleep(5_000);
            int uploadedMB = (int) (uploaded.get() / 1024 / 1024);
            int percent = 100 * uploadedMB / totalSizeMB;
            int time = (int) (System.currentTimeMillis() - start) / 1000;
            vimPort.httpNfcLeaseProgress(httpNfcLease, percent);
            System.out.print(String.format("\n%d / %d MB (%d%%) [%d MB/s]", uploadedMB, totalSizeMB, percent, uploadedMB / time));
            System.gc();
        }
        System.out.println();
        if (status.values().contains('F')) {
            LocalizedMethodFault fault = new LocalizedMethodFault();
            fault.setLocalizedMessage("Unable to upload some files using push method.");
            fault.setFault(new MethodFault());
            vimPort.httpNfcLeaseAbort(httpNfcLease, fault);
            throw new RuntimeException(fault.getLocalizedMessage());
        }
        vimPort.httpNfcLeaseProgress(httpNfcLease, 100);
        vimPort.httpNfcLeaseComplete(httpNfcLease);
        return httpNfcLeaseInfo.getEntity();
    }

    private static void uploadFile(String sourceUrl, String targetUrl, OvfFileItem ovfFileItem, AtomicLong uploaded) throws IOException {
        long size = ovfFileItem.getSize();
        int chunkSize = ovfFileItem.getChunkSize() == null ? 16384 : ovfFileItem.getChunkSize().intValue();
        logger.info("Uploading " + ovfFileItem.getPath() + " to " + targetUrl + " (size: " + size + " bytes)");
        long start = System.currentTimeMillis();
        HttpsURLConnection conn = (HttpsURLConnection) new URL(targetUrl).openConnection();
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setFixedLengthStreamingMode(size);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Content-Type", "application/x-vnd.vmware-streamVmdk");
        conn.setRequestProperty("Content-Length", Long.toString(size));
        try (BufferedInputStream inputStream = new BufferedInputStream(new URL(sourceUrl).openStream());
             BufferedOutputStream outputStream = new BufferedOutputStream(conn.getOutputStream())) {
            byte[] buf = new byte[chunkSize];
            int read;
            while ((read = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, read);
                uploaded.addAndGet(read);
            }
            outputStream.flush();
        }
        conn.disconnect();
        int duration = (int) ((System.currentTimeMillis() - start) / 1000);
        logger.info("Uploaded " + sourceUrl + " to " + targetUrl + " in " + duration + " seconds");
    }

    @SuppressWarnings("unchecked")
    private  <T> T retrieveProperty(ManagedObjectReference managedObjectReference, String property) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        return (T) retrieveProperties(managedObjectReference, property).get(property);
    }

    private Map<String, Object> retrieveProperties(ManagedObjectReference mor, String... properties) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        PropertySpec propertySpec = new PropertySpec();
        if (properties.length == 0) {
            propertySpec.setAll(true);
        } else {
            propertySpec.getPathSet().addAll(Arrays.asList(properties));
        }
        propertySpec.setType(mor.getType());
        ObjectSpec objectSpec = new ObjectSpec();
        objectSpec.setObj(mor);
        PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
        propertyFilterSpec.getPropSet().add(propertySpec);
        propertyFilterSpec.getObjectSet().add(objectSpec);
        List<PropertyFilterSpec> propertyFilterSpecs = Collections.singletonList(propertyFilterSpec);
        List<DynamicProperty> dynamicProperties = vimPort.retrieveProperties(serviceContent.getPropertyCollector(), propertyFilterSpecs).get(0).getPropSet();
        Map<String, Object> map = new HashMap<>();
        for (DynamicProperty dynamicProperty : dynamicProperties) {
            map.put(dynamicProperty.getName(), dynamicProperty.getVal());
        }
        return map;
    }

    public List<Map<String, Object>> search(ManagedObjectReference mor, String type, String... properties) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        if (mor == null) {
            mor = serviceContent.getRootFolder();
        }
        List<String> types = Collections.singletonList(type);
        ManagedObjectReference viewManager = serviceContent.getViewManager();
        ManagedObjectReference containerView = vimPort.createContainerView(viewManager, mor, types, true );
        ObjectSpec objectSpec = new ObjectSpec();
        objectSpec.setObj(containerView);
        objectSpec.setSkip(true);
        TraversalSpec traversalSpec = new TraversalSpec();
        traversalSpec.setName("traverseEntities");
        traversalSpec.setPath("view");
        traversalSpec.setSkip(false);
        traversalSpec.setType("ContainerView");
        objectSpec.getSelectSet().add(traversalSpec);
        PropertySpec propertySpec = new PropertySpec();
        propertySpec.setType(type);
        if (properties.length == 0) {
            propertySpec.setAll(true);
        } else {
            propertySpec.getPathSet().addAll(Arrays.asList(properties));
        }
        PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
        propertyFilterSpec.getObjectSet().add(objectSpec);
        propertyFilterSpec.getPropSet().add(propertySpec);
        List<PropertyFilterSpec> propertyFilterSpecs = Collections.singletonList(propertyFilterSpec);
        List<ObjectContent> objectContents = vimPort.retrieveProperties(serviceContent.getPropertyCollector(), propertyFilterSpecs);
        List<Map<String, Object>> list = new ArrayList<>();
        for (ObjectContent objectContent : objectContents) {
            Map<String, Object> props = new HashMap<>();
            props.put("_id", objectContent.getObj());
            for (DynamicProperty dynamicProperty : objectContent.getPropSet()) {
                props.put(dynamicProperty.getName(), dynamicProperty.getVal());
            }
            list.add(props);
        }
        return list;
    }

    public String getIp(ManagedObjectReference virtualMachine) throws Exception {
        long timeout = System.currentTimeMillis() + TIMEOUT;
        while (System.currentTimeMillis() < timeout) {
            ArrayOfGuestNicInfo nics = retrieveProperty(virtualMachine, "guest.net");
            if (nics != null && nics.getGuestNicInfo().size() != 0) {
                for (GuestNicInfo nic : nics.getGuestNicInfo()) {
                    if (!nic.isConnected()) {
                        continue;
                    }
                    for (String ip : nic.getIpAddress()) {
                        try {
                            InetAddress inetAddress = InetAddress.getByName(ip);
                            if (inetAddress.isLoopbackAddress()) {
                                continue;
                            }
                            if (inetAddress.isReachable(5000)) {
                                return ip;
                            }
                        } catch (IOException e) {
                            logger.warning(e.toString());
                        }
                    }
                }
                break;
            }
            Thread.sleep(5000);
        }
        throw new InterruptedException("Timed out");
    }

    public void destroyVM(ManagedObjectReference virtualMachine) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InvalidStateFaultMsg, TaskInProgressFaultMsg, InterruptedException, VimFaultFaultMsg {
        VirtualMachinePowerState powerState = retrieveProperty(virtualMachine, "runtime.powerState");
        if (powerState != VirtualMachinePowerState.POWERED_OFF) {
            try {
                vimPort.terminateVM(virtualMachine);
            } catch (Exception e) {
                ManagedObjectReference mor = vimPort.powerOffVMTask(virtualMachine);
                TaskInfoState taskInfoState = waitForTask(mor);
                if (taskInfoState == TaskInfoState.ERROR) {
                    throw new RuntimeException("Cannot destroy VM as the VM could not be powered off.");
                }
            }
        }
        ManagedObjectReference task = vimPort.destroyTask(virtualMachine);
        waitForTask(task);
    }

    public String revertSnapshot(ManagedObjectReference vm) throws Exception {
        ManagedObjectReference task = vimPort.revertToCurrentSnapshotTask(vm, null, false);
        TaskInfoState state = waitForTask(task);
        if (state == TaskInfoState.ERROR) {
            throw new RuntimeException("Revert Snapshot of " + vm.getValue() + " is failed.");
        }
        return getIp(vm);
    }

    public TaskInfoState waitForTask(ManagedObjectReference taskMOR) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        long timeout = System.currentTimeMillis() + TIMEOUT;
        while (System.currentTimeMillis() < timeout) {
            TaskInfo task = retrieveProperty(taskMOR, "info");
            String msg = String.format("%s: %s '%s' %s", task.getKey(), task.getName(), task.getEntityName(), task.getState());
            if (task.getProgress() != null) {
                msg += " (" + task.getProgress() + "%)";
            }
            logger.info(msg);
            TaskInfoState state = task.getState();
            switch (state) {
                case QUEUED:
                case RUNNING:
                    break;
                case ERROR:
                    logger.warning(task.getError().getLocalizedMessage());
                case SUCCESS:
                    return state;
            }
            Thread.sleep(5000);
        }
        throw new InterruptedException("Timed out");
    }

    public static String toString(InputStream inputStream) {
        return new Scanner(inputStream).useDelimiter("\\A").next();
    }

    @Override
    public void close() throws IOException {
        try {
            vimPort.logout(serviceContent.getSessionManager());
        } catch (RuntimeFaultFaultMsg runtimeFaultFaultMsg) {
            throw new IOException(runtimeFaultFaultMsg);
        }
    }

    private static final class TrustAllTrustManager implements X509TrustManager {

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }
    }
}
