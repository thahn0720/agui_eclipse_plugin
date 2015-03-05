package net.sf.fjep.autojar;

import org.apache.bcel.classfile.ConstantMethodref;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantString;
import org.apache.bcel.classfile.JavaClass;

public class ClassVisitorSearchCFN extends ClassVisitor {

    private String lastConst = null;

    ClassVisitorSearchCFN(JavaClass javaClass)
    {
        super(javaClass);
    }

    public void visitConstantMethodref(ConstantMethodref ref)
    {
        ConstantPool    pool = javaClass.getConstantPool();
        String          cstr = ref.getClass(pool);

        if (cstr.equals("java.lang.Class"))
        {
            int     iname = ref.getNameAndTypeIndex();
            String  name = ((ConstantNameAndType)pool.getConstant(iname)).getName(pool);

            if (name.equals("forName")) {
                System.out.println("found Class.forName('" + javaClass.getClassName() + "')");
                ConstantNameAndType cnat = (ConstantNameAndType)pool.getConstant(iname);
                String cfnStr = cnat.getName(pool);
                if (lastConst != null) {
                    refClasses.add(lastConst.replace('.', '/'));
                    lastConst = null;
                }
            }
        }
    }

    public void visitConstantString(ConstantString constStr) {
        ConstantPool    pool = javaClass.getConstantPool();
        String stringText = constStr.getBytes(pool);
        lastConst = stringText;
    }
}
