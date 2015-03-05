package net.sf.fjep.autojar;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class FileClassFinder implements IClassFinder {
    
    private String basename;
    
    public FileClassFinder(String basename) {
        this.basename = basename;
    }
    
    public byte[] findClass(String name) {
        byte[] result = null;
        String className = basename + name.replace('.', '/') + ".class";
        File f = new File(className);
        if (f.exists()) {
            int len = (int) f.length();
            result = new byte[len];
            InputStream in;
            try {
                in = new FileInputStream(f);
                in.read(result);
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}
