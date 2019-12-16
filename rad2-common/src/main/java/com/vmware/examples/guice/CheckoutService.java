package com.vmware.examples.guice;

import com.vmware.common.utils.PrintUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

abstract public class CheckoutService implements ICheckoutService {
    public CheckoutService() {
        PrintUtils.printToActor("*** CREATING A CheckoutService!!! ***");
    }

    @Override
    public double checkout(ShoppingCart cart) {
        Discountable discountable = this.getDiscountableInstance(cart);
        double totalAfterDiscount = (1.0 - discountable.getDiscount()) * cart
            .getCartTotal();
        PrintUtils.printToActor("%nShopping Cart = [$%.2f] with discount of %.2f%% = [$%.2f]%n%n",
            cart.getCartTotal(), discountable.getDiscount() * 100, totalAfterDiscount);

        return totalAfterDiscount;
    }

    abstract protected Discountable getDiscountableInstance(ShoppingCart cart);

    public static class GoogleCheckoutService extends CheckoutService {
        // return a Discountable based on
        // ShoppingCart total
        @Inject
        DiscountableFactory factory;

        @Override
        protected Discountable getDiscountableInstance(ShoppingCart cart) {
            PrintUtils.printToActor("*** USING the GoogleCheckoutService!!! ***");
            return this.factory.getDiscount(cart);
        }
    }

    public static class FlipkartCheckoutService extends CheckoutService {
        // returns a random Discountable
        @Inject
        Provider<Discountable> provider;

        @Override
        protected Discountable getDiscountableInstance(ShoppingCart cart) {
            PrintUtils.printToActor("*** USING the FlipkartCheckoutService !!! ***");
            return provider.get();
        }
    }

    public static class MyntraCheckoutService extends CheckoutService {
        // returns a particular Discountable
        @Inject
        @Named(Discountable.DEFAULT_FIXED_DISCOUNT)
        Discountable discountable;

        @Override
        protected Discountable getDiscountableInstance(ShoppingCart cart) {
            PrintUtils.printToActor("*** USING the MyntraCheckoutService!!! ***");
            return this.discountable;
        }
    }
}
