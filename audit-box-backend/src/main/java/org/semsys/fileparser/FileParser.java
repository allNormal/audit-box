package org.semsys.fileparser;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public abstract class FileParser {

    private String type;

    public FileParser(String type) {
        this.type = type;
    }

    public abstract void parse(InputStream inputStream, InputStream rmlStream, OutputStream outputStream,
                               Map<String, String> ns);

    public abstract boolean checkIfProv();
}
