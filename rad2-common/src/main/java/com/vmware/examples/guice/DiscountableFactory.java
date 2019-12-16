package com.vmware.examples.guice;

public interface DiscountableFactory {
    /**
     * Return a particular variant of {@link Discountable} based on the contents of the cart. Thus the
     * discounting is based on examining the cart's contents at run time. Contrast with {@link
     * DiscountableProvider}, which cannot take a runtime argument to determine which variant of {@link
     * Discountable} it needs to provide.
     *
     * @return
     */
    Discountable getDiscount(ShoppingCart cart);
}
