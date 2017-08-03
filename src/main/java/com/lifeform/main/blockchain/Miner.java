package com.lifeform.main.blockchain;

import com.lifeform.main.IKi;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.network.NewBlockPacket;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Bryan on 7/31/2017.
 */
public class Miner extends Thread {

    public static boolean mining = true;
    public static String blockPropped = "";
    public static boolean foundBlock = false;
    private IKi ki;
    private BigInteger guess;
    private BigInteger maxGuess;
    private BigInteger guessSet;
    private boolean canMine;
    public static String prevID = "0";
    public static BigInteger height = BigInteger.ZERO;
    public Miner(IKi ki, BigInteger guess,BigInteger maxGuess)
    {
        this.ki = ki;
        this.guess = guess;
        this.guessSet = guess;
        this.maxGuess = maxGuess;
    }
    @Override
    public void run() {
        L1:
        while (mining) {
            while (ki.getChainMan().canMine()) {
                if (ki.getEncryptMan().getPublicKey() != null) {
                    Block b = ki.getChainMan().formEmptyBlock();

                    canMine = ki.getChainMan().canMine();
                    b.ID = EncryptionManager.sha256(b.header());
                    while ((b.ID != null) && !(new BigInteger(b.ID,16).compareTo(ki.getChainMan().getCurrentDifficulty()) < 0) && canMine) {
                        if (!mining) break;
                        b.timestamp = System.currentTimeMillis();
                        b.height = height;
                        b.prevID = prevID;
                        guess = guess.add(BigInteger.ONE);

                        if (guess.compareTo(maxGuess) > 0) {
                            guess = guessSet;
                            System.out.println("1Mhash");
                        }
                        b.payload = guess.toString();

                        b.ID = EncryptionManager.sha256(b.header());

                        if(foundBlock)
                            break;
                    }

                    //ki.getMainLog().info("Found block!");
                    if (!mining) break;
                    if(!foundBlock) {
                        if(!ki.getChainMan().softVerifyBlock(b)) {
                            continue L1;
                        }
                        //ki.getMainLog().info("Block found");
                        if (!ki.getChainMan().canMine()) continue;
                        ki.getMainLog().info("Block verified");
                        NewBlockPacket nb = new NewBlockPacket(ki);
                        Map<String, String> data = new HashMap<>();
                        data.put("block", b.toJSON());
                        nb.setData(data);
                        ki.getNetMan().broadcastPacket(nb);
                    }
                    boolean wait = true;
                    foundBlock = true;
                    L2:while (height.compareTo(ki.getChainMan().currentHeight()) == 0 && !blockPropped.equals(b.ID)) {
                        if (wait)
                            ki.getMainLog().info("waiting for block propogation to mine");
                        wait = false;
                    }
                    foundBlock = false;

                }
            }
        }
    }
}
