package com.vmware.vap.saas;

import org.testng.TestNG;

import java.net.URI;
import java.net.URISyntaxException;

public class Main {

    public static URI uri;

    public static void main(String[] args) throws URISyntaxException {
        uri = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        if (args.length == 0) {
            String path = uri.getPath();
            if (path.endsWith(".jar")) {
                TestNG.main(new String[] { "-testjar", path});
            } else {
                TestNG.main(new String[] { Main.class.getResource("/testng.xml").getFile() });
            }
        } else {
            TestNG.main(args);
        }
    }
}
