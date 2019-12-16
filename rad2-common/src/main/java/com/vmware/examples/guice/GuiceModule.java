package com.vmware.examples.guice;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

public class GuiceModule extends AbstractModule {
    protected void configure() {

        Multibinder<ICheckoutService> setBinder
            = Multibinder.newSetBinder(binder(), ICheckoutService.class).permitDuplicates();
        setBinder.addBinding().to(CheckoutService.GoogleCheckoutService.class);
        setBinder.addBinding().to(CheckoutService.FlipkartCheckoutService.class);
        setBinder.addBinding().to(CheckoutService.MyntraCheckoutService.class);

        // this map binder is used by both the DiscountableFactory and the DiscountableProvider
        MapBinder<Discountable.DiscountableOption, Discountable> mapBinder =
            MapBinder.newMapBinder(binder(), Discountable.DiscountableOption.class, Discountable.class);
        mapBinder.addBinding(Discountable.DiscountableOption.Small).to(Discountable.DiscountSmall.class);
        mapBinder.addBinding(Discountable.DiscountableOption.None).to(Discountable.DiscountNone.class);
        mapBinder.addBinding(Discountable.DiscountableOption.Large).to(Discountable.DiscountLarge.class);

        // three ways to get a Discountable - using a Custom Factory,
        // a Guice Provider, and an Annotated binding to a specific instance
        bind(DiscountableFactory.class).to(DiscountableFactoryImpl.class);
        bind(Discountable.class).toProvider(DiscountableProvider.class);
        bind(Discountable.class).annotatedWith(Names.named(Discountable.DEFAULT_FIXED_DISCOUNT))
            .to(Discountable.DiscountSmall.class);

        // shown for completeness. This will happen automatically with Guice
        // Guice knows to call Random's default constructor as needed.
        // There is no need to do this binding here.
        // bind(Random.class).toInstance(new Random());
    }
}

