/*******************************************************************************
 * Copyright (c) 2004 Ferenc Hechler - ferenc_hechler@users.sourceforge.net
 * 
 * This file is part of the Fat Jar Eclipse Plug-In
 *
 * The Fat Jar Eclipse Plug-In is free software;
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.
 * 
 * The Fat Jar Eclipse Plug-In is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with the Fat Jar Eclipse Plug-In;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *  
 *******************************************************************************/
package net.sf.fjep.fatjar.wizards.export;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Tree functionality for use in GUI elements
 */
public class StringTree {
    
    private StringTree parent;
    private String label;
    private Object data;
    private ArrayList children;
    
    public interface IStringTreeCondition {
        boolean check(StringTree stringTree);
    }
    
    public StringTree(String label, Object data) {
        parent = null;
        this.label = label;
        this.data = data;
        children = null;
    }

    public String getLabel() {
        return label;
    }
    public Object getData() {
        return data;
    }
    public StringTree getParent() {
        return parent;
    }
    public boolean isRoot() {
        boolean result = parent == null;
        return result;
    }
    protected void setParent(StringTree parent) {
        this.parent = parent;
    }
    public StringTree addChild(StringTree child) {
        if (children == null) {
            children = new ArrayList();
        }
        child.setParent(this);
        children.add(child);
        return child;
    }
    private static StringTree[] emptyStringTreeArray = new StringTree[0];
    public StringTree[] getChildren() {
        StringTree[] result;
        if (children == null) {
            result = emptyStringTreeArray;
        }
        else {
            result = (StringTree[]) children.toArray(new StringTree[children.size()]);
        }
        return result;
    }
    public boolean hasChildren() {
        boolean result = (children != null) && (children.size() > 0);
        return result;
    }
    
    public String toString() {
        return getLabel();
    }
    /**
     * 
     * @param n zero based
     * @return null if out of bounds
     */
    public StringTree getChild(int n) {
        StringTree result = null;
        if ((children != null) && (children.size() > n)) {
            result = (StringTree) children.get(n);
        }
        return result;
    }
    public int findChildIndex(StringTree child) {
        int result = -1;
        if (children != null) {
            result = children.lastIndexOf(child);
        }
        return result;
    }
    /**
     * go to next element in tree in dfs preorder-traverse. 
     * Start with root, end with rightmost bottom element,
     * Visit parent nodes first.
     * @return next element or null if this is the last
     */
    public StringTree iterateNext() {
        StringTree result = null;
        if (hasChildren()) {
            result = getChild(1);
        }
        if (result == null) {
            StringTree base = this;
            while (base != null) {
                result = base.getNextSibling();
                if (result != null) {
                    break;
                }
                base = base.getParent();
            }
        }
        return result;
    }
    /**
     * if this is child with index n, 
     * then return sibling with index n+1 or 
     * null if this is root or there are no more siblings 
     * @return sibling or null
     */
    public StringTree getNextSibling() {
        StringTree result = null;
        StringTree parent = getParent();
        if (parent != null) {
            int n = parent.findChildIndex(this);
            result = parent.getChild(n+1);
        }
        return result;
    }
    /**
     * search in (sub-)tree from this node
     * @param condition
     * @return first match in dfs preorder
     */
    public StringTree findFirst(IStringTreeCondition condition) {
        StringTree result = null;
        StringTree testElement = this;
        if (condition.check(this)) {
            result = this;
        }
        else if (children != null) {
            for (Iterator iter = children.iterator(); iter.hasNext();) {
                StringTree child = (StringTree) iter.next();
                result = child.findFirst(condition);
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }
}
