package net.sf.fjep.autojar;

import java.util.HashSet;
import java.util.Set;

import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.bcel.classfile.JavaClass;

public class ClassVisitor extends EmptyVisitor {

    protected JavaClass javaClass;
    protected Set refClasses;

    ClassVisitor(JavaClass javaClass)
    {
        this.javaClass = javaClass;
        refClasses = new HashSet();
    }

    public void visitConstantClass(ConstantClass constClass) {
        String cstr = javaClass.getConstantPool().getConstant(constClass.getNameIndex()).toString();
        int ia = cstr.indexOf('"');
        int ie = cstr.lastIndexOf('"');
        String  name = cstr.substring(ia + 1, ie);
        // skip arrays
        if (name.startsWith("["))
            return;
        refClasses.add(name);
    }

    public String[] getRefClasses() {
        String[] result = (String[]) refClasses.toArray(new String[refClasses.size()]);
        return result;
    }

}
