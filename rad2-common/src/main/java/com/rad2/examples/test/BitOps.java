package com.rad2.examples.test;

import com.rad2.common.utils.PrintUtils;

public class BitOps {

    private void printX(int x) {
        String bin = Integer.toBinaryString(x);
        String binF = PrintUtils.padFormat("%33s", bin, '_');
        int binLen = bin.length();
        System.out.printf("%12d in bin = %33s[%4d]\n", x, binF, binLen);
    }

    private void halveToZero(int x) {
        if (x == 0 || x == 1) {
            return;
        } else if (x == -1) {
            return;
        }
        x >>= 1;// right shift = dividing by 2 each time
        halveToZero(x);
    }

    public int sizeOf(int x) {
        //printX(x);
        if (x == 0 || x == 1) {
            return 1;
        } else if (x < 0) {
            return 32; // two's complement representation is always 32 bits for an int
        }
        x >>= 1;// right shift = dividing by 2 each time
        return 1 + sizeOf(x);
    }

    public int numOfOneBits(int x) {
        //printX(x);
        if (x == 1) {
            return 1;
        } else if (x == 0) {
            return 0;
        } else if (x == -1) {
            return 32; // for -ve's deduct each 0 in LSB from 32
        }
        int inc = 0;
        if (x > 1) {
            inc = ((x & 1) == 1) ? 1 : 0;// check if LSB is 0 or 1
        } else { // x is < -1
            inc = ((x & 1) == 1) ? 0 : -1;// check if LSB is 0 or 1. for -ve's use -1
        }
        x >>= 1;
        return Math.abs(inc + numOfOneBits(x));
    }

}
