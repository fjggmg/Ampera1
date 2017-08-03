import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.util.concurrent.ConcurrentMap;

/**
 * Created by Bryan on 7/23/2017.
 */
public class TestDB {


    ConcurrentMap<String,String> map;
    @Before
    public void testSaveSetup()
    {
        DB db = DBMaker.fileDB("test").fileMmapEnableIfSupported().transactionEnable().make();

        map = db.hashMap("test", Serializer.STRING,Serializer.STRING).createOrOpen();

        map.put("test","test");

        Assert.assertNotNull(map.get("test"));
        Assert.assertTrue(map.get("test").equalsIgnoreCase("test"));
        db.commit();
        db.close();
    }




    @Test
    public void testLoad()
    {
        DB db = DBMaker.fileDB("test").fileMmapEnableIfSupported().transactionEnable().make();
        map = db.hashMap("test", Serializer.STRING,Serializer.STRING).createOrOpen();

        Assert.assertNotNull(map.get("test"));
        Assert.assertTrue(map.get("test").equalsIgnoreCase("test"));
    }


}
