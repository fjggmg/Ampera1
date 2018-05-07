package com.lifeform.main.transactions;

import java.math.BigInteger;

public class TransactionFeeCalculator {

    public static final BigInteger OUTPUT_RATE = BigInteger.ONE;
    public static final BigInteger INPUT_RATE = BigInteger.valueOf(3);
    public static final BigInteger P2SH_OUTPUT_RATE = BigInteger.valueOf(5);
    public static final BigInteger P2SH_INPUT_RATE = BigInteger.valueOf(20);
    public static final BigInteger MIN_FEE = BigInteger.TEN;
    public static final BigInteger ED_KEY_RATE = BigInteger.valueOf(1);
    public static final BigInteger BRAINPOOL_KEY_RATE = BigInteger.valueOf(15);
    public static final BigInteger BASE_FEE = BigInteger.valueOf(100);

    /**
     * Key rates only apply to inputs, since those are where the verification will happen
     *
     * @param trans transaction to calculate fee for
     * @return minimum fee required for this transaction
     */
    public static BigInteger calculateMinFee(ITrans trans) {
        BigInteger fee = BASE_FEE;

        for (Input i : trans.getInputs()) {
            fee = fee.add(INPUT_RATE);
            if (i.getAddress().isP2SH()) {
                fee = fee.add(P2SH_INPUT_RATE);
            }
            if (i.getAddress().getKeyType().equals(KeyType.ED25519)) {
                fee = fee.add(ED_KEY_RATE);
            } else if (i.getAddress().getKeyType().equals(KeyType.BRAINPOOLP512T1)) {
                fee = fee.add(BRAINPOOL_KEY_RATE);
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


    public static BigInteger calculateMinFee(int outputs, int inputs, int p2shOutputs, int p2shInputs, int edInputs, int bpInputs) {
        return BASE_FEE.add(OUTPUT_RATE.multiply(BigInteger.valueOf(outputs)).add(INPUT_RATE.multiply(BigInteger.valueOf(inputs))).add(P2SH_OUTPUT_RATE.multiply(BigInteger.valueOf(p2shOutputs))).add(P2SH_INPUT_RATE.multiply(BigInteger.valueOf(p2shInputs))).add(ED_KEY_RATE.multiply(BigInteger.valueOf(edInputs))).add(BRAINPOOL_KEY_RATE.multiply(BigInteger.valueOf(bpInputs))));
    }
}
