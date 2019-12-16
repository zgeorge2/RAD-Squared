package com.vmware.examples.guice;

import com.vmware.common.utils.PrintUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Random;

@Singleton
public class DiscountableProvider implements Provider<Discountable> {
    private final Random random;
    private final Map<Discountable.DiscountableOption, Discountable> discountableMap;

    @Inject
    public DiscountableProvider(Map<Discountable.DiscountableOption, Discountable>
                                    discountableMap, Random random) {
        PrintUtils.printToActor( "*** CREATING A DiscountableProvider ***");
        this.random = random;
        this.discountableMap = discountableMap;
    }

    public Discountable get() {
        return this.discountableMap.get(Discountable.DiscountableOption.getOption(this.random));
    }
}
