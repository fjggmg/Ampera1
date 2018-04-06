package com.lifeform.main.transactions;

import java.math.BigInteger;

public class TransactionFeeCalculator {

    public static final BigInteger OUTPUT_RATE = BigInteger.ONE;
    public static final BigInteger INPUT_RATE = BigInteger.valueOf(3);
    public static final BigInteger P2SH_OUTPUT_RATE = BigInteger.valueOf(5);
    public static final BigInteger P2SH_INPUT_RATE = BigInteger.TEN;
    public static final BigInteger MIN_FEE = BigInteger.TEN;

    public static BigInteger calculateMinFee(ITrans trans) {
        BigInteger fee = BigInteger.ZERO;

        for (Input i : trans.getInputs()) {
            fee = fee.add(INPUT_RATE);
            if (i.getAddress().isP2SH()) {
                fee = fee.add(P2SH_INPUT_RATE);
            }
        }

        for (Output o : trans.getOutputs()) {
            fee = fee.add(OUTPUT_RATE);
            if (o.getAddress().isP2SH()) {
                fee = fee.add(P2SH_OUTPUT_RATE);
            }
        }
        if (fee.compareTo(MIN_FEE) < 0) return MIN_FEE;
        return fee;
    }


    public static BigInteger calculateMinFee(int outputs, int inputs, int p2shOutputs, int p2shInputs) {
        return OUTPUT_RATE.multiply(BigInteger.valueOf(outputs)).add(INPUT_RATE.multiply(BigInteger.valueOf(inputs))).add(P2SH_OUTPUT_RATE.multiply(BigInteger.valueOf(p2shOutputs))).add(P2SH_INPUT_RATE.multiply(BigInteger.valueOf(p2shInputs)));
    }
}
