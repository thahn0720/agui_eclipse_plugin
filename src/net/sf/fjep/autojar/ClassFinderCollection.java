package net.sf.fjep.autojar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ClassFinderCollection implements IClassFinder {
    
    private List classFinders;
    
    public ClassFinderCollection() {
        classFinders = new ArrayList();
    }

    public void addClassFinder(IClassFinder classFinder) {
        classFinders.add(classFinder);
    }
    
    public byte[] findClass(String name) {
        byte[] result = null;
        for (Iterator iter = classFinders.iterator(); iter.hasNext();) {
            IClassFinder classFinder = (IClassFinder) iter.next();
            result = classFinder.findClass(name);
            if (result != null) {
                break;
            }
        }
        return result;
    }

}
