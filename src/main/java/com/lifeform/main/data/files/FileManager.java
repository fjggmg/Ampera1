package com.lifeform.main.data.files;

import com.lifeform.main.IKi;

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
            if(file.getParentFile() != null)
                if(!file.getParentFile().mkdirs())
                    return;
            if(!file.createNewFile())
                return;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
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
