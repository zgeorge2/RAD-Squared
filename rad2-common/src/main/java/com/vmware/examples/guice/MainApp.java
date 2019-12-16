package com.vmware.examples.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.vmware.common.utils.PrintUtils;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

public class MainApp {
    private final Set<ICheckoutService> services;

    @Inject
    public MainApp(Set<ICheckoutService> services) {
        this.services = services;
    }

    public static void main(String args[]) {
        Injector guice = Guice.createInjector(new GuiceModule());
        MainApp service = guice.getInstance(MainApp.class);
        service.start();
    }

    void start() {
        while (true) {
            this.services.forEach(e -> {
                PrintUtils.printToActor("CheckoutService InstanceId:" + e);
                e.checkout(this.getCart());
            });
        }
    }

    ShoppingCart getCart() {
        String total = null;
        System.out.print("Enter cart total: ");

        try {
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            total = bufferRead.readLine();
        } catch (IOException doh) {
        }

        return new ShoppingCart(Double.valueOf(total));
    }
}
