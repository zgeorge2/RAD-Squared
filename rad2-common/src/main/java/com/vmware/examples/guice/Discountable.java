package com.vmware.examples.guice;

import com.vmware.common.utils.PrintUtils;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public interface Discountable {
    String DEFAULT_FIXED_DISCOUNT = "DefaultFixedDiscount";

    double getDiscount();

    enum DiscountableOption {
        None(0, 0.0D, 1000.0D), Small(1, 1001.0D, 10000.0D), Large(2,
            10001.0D, Double.MAX_VALUE);
        private int index;
        private double lower;
        private double upper;

        DiscountableOption(int index, double lower, double upper) {
            this.index = index;
            this.lower = lower;
            this.upper = upper;
        }

        public static Discountable.DiscountableOption getOption(ShoppingCart
                                                                    cart) {
            double value = cart.getCartTotal();
            return getAll().stream()
                .filter(e -> (value >= e.getLower() && value <= e
                    .getUpper()))
                .findAny()
                .orElse(Discountable.DiscountableOption.None);
        }

        public static Discountable.DiscountableOption getOption(Random random) {
            List<DiscountableOption> all = getAll();
            int value = random.nextInt(all.size());
            PrintUtils.printToActor("%n*** Random value = %d%n", value);
            return all.stream()
                .filter(e -> (value == e.getIndex()))
                .findAny()
                .orElse(Discountable.DiscountableOption.None);
        }

        public static List<DiscountableOption> getAll() {
            return Arrays.stream(values()).collect(Collectors.toList());
        }

        public double getLower() {
            return lower;
        }

        public double getUpper() {
            return upper;
        }

        public int getIndex() {
            return index;
        }
    }

    abstract class DiscountBase implements Discountable {
        DiscountBase() {
            PrintUtils.printToActor("*** CREATING A DiscountBase class ***");
        }
    }

    @Singleton
    class DiscountSmall extends DiscountBase {
        @Override
        public double getDiscount() {
            return 0.25D;
        }
    }

    @Singleton
    class DiscountNone extends DiscountBase {
        @Override
        public double getDiscount() {
            return 0.0D;
        }
    }

    @Singleton
    class DiscountLarge extends DiscountBase {
        @Override
        public double getDiscount() {
            return 0.5D;
        }
    }
}
