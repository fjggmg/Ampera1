package com.lifeform.main.blockchain;

import com.lifeform.main.IKi;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.data.Utils;
import com.lifeform.main.network.BlockEnd;
import com.lifeform.main.network.BlockHeader;
import com.lifeform.main.network.TransactionPacket;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

@Deprecated
public class CPUMiner extends Thread implements IMiner{

    public static boolean mining = true;
    public static String blockPropped = "";
    public static volatile boolean foundBlock = false;
    private IKi ki;
    private BigInteger guess;
    private BigInteger maxGuess;
    private BigInteger guessSet;
    private static boolean canMine;
    public static String prevID = "0";
    public static BigInteger height = BigInteger.ZERO;
    private Block b;

    private boolean cDebug;

    public CPUMiner(IKi ki, BigInteger guess, BigInteger maxGuess, boolean cDebug)
    {
        this.ki = ki;
        this.guess = guess;
        this.guessSet = guess;
        this.maxGuess = maxGuess;
        this.cDebug = cDebug;

    }
    @Override
    public void run() {
        canMine = true;
                if (ki.getEncryptMan().getPublicKey() != null) {
                    b = ki.getChainMan().formEmptyBlock();
                    canMine = ki.getChainMan().canMine();
                    b.ID = EncryptionManager.sha512(b.header());

                    byte[] hash = new byte[0];
                    try {
                        hash = EncryptionManager.sha512(b.header().getBytes("UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    BigInteger cd = ki.getChainMan().getCurrentDifficulty();

                    while ((b.ID != null) && !(new BigInteger(hash).abs().compareTo(cd) < 0) && canMine) {
                        //ki.debug("Mining super awesomely");
                        if (!mining) break;
                        b.timestamp = System.currentTimeMillis();
                        b.height = height;
                        b.prevID = prevID;
                        guess = guess.add(BigInteger.ONE);

                        if (guess.compareTo(maxGuess) > 0) {
                            guess = guessSet;
                            System.out.println("1Mhash");
                            System.out.println("Current diff: " + cd);
                        }
                        b.payload = guess.toByteArray();

                        try {
                            hash = EncryptionManager.sha512(b.header().getBytes("UTF-8"));

                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        if(foundBlock)
                            break;
                    }
                    b.ID = Utils.toBase64(hash);
                    if(!canMine) return;
                    if (!mining) return;

                        if(!ki.getChainMan().softVerifyBlock(b)) return;
                        if (!ki.getChainMan().canMine()) return;
                    if (cDebug)
                        ki.getMainLog().info("Block verified");
                        sendBlock(b);
                    if (cDebug)
                        ki.getMainLog().info("Sent NBP to network");
                        canMine = false;

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

    @Override
    public void setup(int index) {

    }
}
