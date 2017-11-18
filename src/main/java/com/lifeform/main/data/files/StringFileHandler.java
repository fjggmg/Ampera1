package com.lifeform.main.data.files;

import com.lifeform.main.IKi;
import com.lifeform.main.Ki;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Bryan on 7/17/2017.
 */
public class StringFileHandler extends FileManager implements IStringFileHandler{

    public StringFileHandler(IKi ki, String fileName)
    {
        super(ki,fileName);
    }
    @Override
    public void addLine(String line) {
        try {

            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file,true)),true);
            writer.println(line);
            writer.close();

            //Files.write(file.toPath(), (line + "/n").getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            Ki.getInstance().debug("File manager for: " + file.getName() + " failed on addLine");
        }
    }

    @Override
    public String getLine(int index) {
        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            if(index >= lines.size()) return null;
            return lines.get(index);
        } catch (IOException e) {
            Ki.getInstance().debug("File manager for: " + file.getName() + " failed on getLine");
        }
        return null;
    }

    @Override
    public boolean hasLine(String line) {
        try {
            if(Files.readAllLines(file.toPath(),StandardCharsets.UTF_8).contains(line))
            {
                return true;
            }
        } catch (IOException e) {
            Ki.getInstance().debug("File manager for: " + file.getName() + " failed on hasLine");
        }
        return false;
    }

    @Override
    public void replaceLine(String oldLine, String newLine) {
        try {
            int i = 0;
            for(String line:Files.readAllLines(file.toPath(),StandardCharsets.UTF_8))
            {
                if(line.matches(Pattern.quote(oldLine)))
                {
                    replaceLine(i,newLine);
                    return;
                }
                i++;
            }
        } catch (IOException e) {
            Ki.getInstance().debug("File manager for: " + file.getName() + " failed on replaceLine");
        }
    }

    @Override
    public void insertLine(String line, int index) {
        try {
            List<String> lines = Files.readAllLines(file.toPath(),StandardCharsets.UTF_8);

            lines.add(index,line);
            if(!file.delete()) throw new IOException("Could not delete file");
            if(!file.createNewFile()) throw new IOException("Could not create new file");
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file),StandardCharsets.UTF_8));
            for(String l:lines)
            {
                writer.println(l);
            }
            writer.close();

        } catch (IOException e) {
            Ki.getInstance().debug("File manager for: " + file.getName() + " failed on insertLine");
        }
    }

    private void insertNoLoad(String line, int index, List<String> lines)
    {
        try {

            lines.add(index,line);
            if(!file.delete()) throw new IOException("Could not delete file");
            if(!file.createNewFile()) throw new IOException("Could not create new file");
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file),StandardCharsets.UTF_8));
            for(String l:lines)
            {
                writer.println(l);
            }
            writer.close();

        } catch (IOException e) {
            Ki.getInstance().debug("File manager for: " + file.getName() + " failed on insertNoLoad with message: " + e.getMessage());
        }
    }

    @Override
    public void replaceLine(int index, String newLine) {
        try {
            List<String> lines = Files.readAllLines(file.toPath(),StandardCharsets.UTF_8);
            if(lines.size() > index)
            lines.remove(index);
            insertNoLoad(newLine,index,lines);

        } catch (IOException e) {
            Ki.getInstance().debug("File manager for: " + file.getName() + " failed on replaceLine");
        }
    }

    @Override
    public List<String> getLines()
    {
        try {
            return Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Ki.getInstance().debug("File manager for: " + file.getName() + " failed on getLines");
        }
        return null;
    }

}
