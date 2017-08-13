import com.lifeform.main.data.EncryptionManager;
import org.junit.Test;

/**
 * Created by Bryan on 7/31/2017.
 */
public class CPUTest {

    @Test
    public void test()
    {
        new Thread() {

            public void run() {
                int i = 0;
                String s = "FFAA00";
                while (true) {
                    EncryptionManager.sha512(s);
                }
            }
        }.start();
        new Thread() {

            public void run() {
                int i = 0;
                String s = "FFAA00";
                while (true) {
                    EncryptionManager.sha512(s);
                }
            }
        }.start();
        new Thread() {

            public void run() {
                int i = 0;
                String s = "FFAA00";
                while (true) {
                    EncryptionManager.sha512(s);
                }
            }
        }.start();
        new Thread() {

            public void run() {
                int i = 0;
                String s = "FFAA00";
                while (true) {
                    EncryptionManager.sha512(s);
                }
            }
        }.start();
        new Thread() {

            public void run() {
                int i = 0;
                String s = "FFAA00";
                while (true) {
                    EncryptionManager.sha512(s);
                }
            }
        }.start();
        new Thread() {

            public void run() {
                int i = 0;
                String s = "FFAA00";
                while (true) {
                    EncryptionManager.sha512(s);
                }
            }
        }.start();
        new Thread() {

            public void run() {
                int i = 0;
                String s = "FFAA00";
                while (true) {
                    EncryptionManager.sha512(s);
                }
            }
        }.start();
        new Thread() {

            public void run() {
                int i = 0;
                String s = "FFAA00";
                while (true) {
                    EncryptionManager.sha512(s);
                }
            }
        }.start();

        while(true)
        {

        }
    }
}
