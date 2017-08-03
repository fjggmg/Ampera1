import com.lifeform.main.MainGUI;
import org.junit.Test;

/**
 * Created by Bryan on 7/18/2017.
 */
public class GUITest {


    /**
     * can visually load GUI to show setup works, this test will be removed for production
     */
    @Test
    public void simpleGUILoadTest()
    {
        MainGUI gui = MainGUI.guiFactory(new DummyKi());
        try{
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
