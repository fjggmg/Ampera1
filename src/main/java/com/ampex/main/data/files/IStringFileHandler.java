package com.ampex.main.data.files;

import java.util.List;

/**
 * Created by Bryan on 7/17/2017.
 */
public interface IStringFileHandler {
    void addLine(String line);
    String getLine(int index);
    boolean hasLine(String line);
    void replaceLine(String oldLine,String newLine);
    void insertLine(String line, int index);
    void replaceLine(int index,String newLine);
    List<String> getLines();
}
