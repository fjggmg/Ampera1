package com.lifeform.main.data.files;

import com.lifeform.main.IKi;
import com.lifeform.main.Ki;

import java.io.File;
import java.io.IOException;

/**
 * Created by Bryan on 7/17/2017.
 */
public class FileManager implements IFileManager {
    protected File file;

    public FileManager(IKi ki, String fileName)
    {
        file = new File(fileName);
        if(!file.exists()) try {
            if (file.getParentFile() != null && !file.getParentFile().exists())
                if(!file.getParentFile().mkdirs()) {
                    Ki.getInstance().debug("Failed to make parent folder for file manager: " + fileName);
                    //return;
                }
            if (!file.exists())
                if (!file.createNewFile()) {
                Ki.getInstance().debug("Failed to make file: " + fileName);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Deprecated
    @Override
    public boolean save() {


        return false;
    }

    @Override
    public boolean delete()
    {
        return file.delete();
    }
}
