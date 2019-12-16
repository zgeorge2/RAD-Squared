package com.vmware.examples.guice;

public class ShoppingCart {
    private double cartTotal;

    public ShoppingCart(double cartTotal) {
        this.cartTotal = cartTotal;
    }

    public double getCartTotal() {
        return this.cartTotal;
    }
}
