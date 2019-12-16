package com.vmware.examples.guice;

import com.vmware.common.utils.PrintUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class DiscountableFactoryImpl implements DiscountableFactory {
    final Map<Discountable.DiscountableOption, Discountable> discountableMap;

    @Inject
    public DiscountableFactoryImpl(Map<Discountable.DiscountableOption, Discountable> map) {
        PrintUtils.printToActor("*** CREATING A DiscountableFactory ***");
        this.discountableMap = map;
    }

    @Override
    public Discountable getDiscount(ShoppingCart cart) {
        return this.discountableMap.get(Discountable.DiscountableOption.getOption(cart));
    }
}
