package com.lifeform.main.blockchain;

import com.lifeform.main.IKi;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.network.BlockEnd;
import com.lifeform.main.network.BlockHeader;
import com.lifeform.main.network.TransactionPacket;

import java.math.BigInteger;

public class CPUMiner extends Thread implements IMiner{

    public static boolean mining = true;
    public static String blockPropped = "";
    public static volatile boolean foundBlock = false;
    private IKi ki;
    private BigInteger guess;
    private BigInteger maxGuess;
    private BigInteger guessSet;
    private boolean canMine;
    public static String prevID = "0";
    public static BigInteger height = BigInteger.ZERO;
    public CPUMiner(IKi ki, BigInteger guess,BigInteger maxGuess)
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
                    b.ID = EncryptionManager.sha512(b.header());
                    while ((b.ID != null) && !(new BigInteger(b.ID,16).compareTo(ki.getChainMan().getCurrentDifficulty()) < 0) && canMine) {
                        if (!mining) break;
                        b.timestamp = System.currentTimeMillis();
                        b.height = height;
                        b.prevID = prevID;
                        guess = guess.add(BigInteger.ONE);

                        if (guess.compareTo(maxGuess) > 0) {
                            guess = guessSet;
                            System.out.println("1Mhash");
                            System.out.println("Current diff: " + ki.getChainMan().getCurrentDifficulty());

                        }
                        b.payload = guess.toString();

                        b.ID = EncryptionManager.sha512(b.header());
                        //System.out.println(b.ID);

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
                        sendBlock(b);
                        ki.getMainLog().info("Sent NBP to network");
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

    private void sendBlock(Block b)
    {
        BlockHeader bh2 = formHeader(b);
        ki.getNetMan().broadcast(bh2);


        for(String key:b.getTransactionKeys())
        {
            TransactionPacket tp = new TransactionPacket();
            tp.block = b.ID;
            tp.trans = b.getTransaction(key).toJSON();
            ki.getNetMan().broadcast(tp);
        }
        BlockEnd be = new BlockEnd();
        be.ID = b.ID;
        ki.getNetMan().broadcast(be);
    }
    private BlockHeader formHeader(Block b)
    {
        BlockHeader bh = new BlockHeader();
        bh.timestamp = b.timestamp;
        bh.solver = b.solver;
        bh.prevID = b.prevID;
        bh.payload = b.payload;
        bh.merkleRoot = b.merkleRoot;
        bh.ID = b.ID;
        bh.height = b.height;
        bh.coinbase = b.getCoinbase().toJSON();
        return bh;
    }
}
