package net.sf.fjep.autojar;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.DescendingVisitor;
import org.apache.bcel.classfile.JavaClass;

public class AutoJarFilter implements IClassFinder {

    Map contents;
    boolean searchClassForName;
    Set usedClasses;
    
    public AutoJarFilter(Map contents, boolean searchClassForName) {
        this.contents = contents;
        this.searchClassForName = searchClassForName;
        this.usedClasses = new HashSet();
    }

    public byte[] findClass(String name) {
        byte[] contentBytes = null;
        String className = name + ".class";
        if (contents.containsKey(className)) {
            contentBytes = (byte[])contents.get(className);
        }
        return contentBytes;
    }

    public void addRefClasses(String className) {
        if (!isChecked(className)) {
            usedClasses.add(className);
            if (contents.containsKey(className + ".class")) {
                byte[] bytecode = (byte[])contents.get(className + ".class");
                if (bytecode != null) {
                    String[] refClasses = getRefClasses(className, bytecode);
                    for (int i = 0; i < refClasses.length; i++) {
                        if (contents.containsKey(refClasses[i] + ".class")) {
                            addRefClasses(refClasses[i]);
                        }
                    }
                }
            }
        }
    }

    private String[] getRefClasses(String className, byte[] bytecode) {
        String[] result = null;
        try {
            InputStream in = new ByteArrayInputStream(bytecode);
            ClassParser parser = new ClassParser(in, className);
            JavaClass javaClass = parser.parse();
            ClassVisitor visitor;
            if (searchClassForName) {
                visitor = new ClassVisitorSearchCFN(javaClass);
            }
            else {
                visitor = new ClassVisitor(javaClass);
            }
    //        ConstantPoolGen pool = new ConstantPoolGen(javaClass.getConstantPool());
    
            DescendingVisitor dvis = new DescendingVisitor(javaClass, visitor);
            dvis.visit();
            in.close();

            result = visitor.getRefClasses();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    public boolean isChecked(String className) {
        boolean result = usedClasses.contains(className);
        return result;
    }

}
