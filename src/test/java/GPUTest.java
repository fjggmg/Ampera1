import com.lifeform.main.blockchain.GPUKernel;
import com.lifeform.main.data.EncryptionManager;
import org.junit.Test;

import java.util.Random;


public class GPUTest {

    @Test
    public void simpleGPU() {
        byte[] data = new byte[0x7ffffff];
        Random rand = new Random();
        rand.nextBytes(data);
        GPUKernel gpu = new GPUKernel(data);
        gpu.execute(1);
        long gstart = System.currentTimeMillis();
        gpu.execute(0x7ffffff);
        long gend = System.currentTimeMillis();
        long gdelta = gend - gstart;
        long start = System.currentTimeMillis();


        //gpu.run();
        doCPU(data);
        /*
        doCPU(data2);
        doCPU(data3);
        doCPU(data4);
        doCPU(data5);
        doCPU(data6);
        */
        /*
        for(double i = 0; i < 8192*16;i++)
        {
          data[i] = ~data[i];
            data[i] = data[i]>>>16;
            data[i] = data[i]*data[i];
            data[i] = data[i]<<16;
            data[i] = data[i]>>>data[i];
        }
        */
        long end = System.currentTimeMillis();
        long delta = end - start;
        double conversionTime = gpu.getConversionTime();
        //gpu.results();
        System.out.println("took: " + gdelta + " milliseconds (GPU)");
        System.out.println("took: " + delta + " milliseconds (CPU)");
    }


    private void doCPU(byte[] data) {
        for (int i = 0; i < 0x7ffffff; i++) {
            EncryptionManager.sha512(data);
        }
    }
}
