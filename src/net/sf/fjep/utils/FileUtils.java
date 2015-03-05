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
package net.sf.fjep.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * utiltiy class for file operations
 */
public class FileUtils {

	/**
	 * remove file or recursive remove folder
	 * @param f file or folder to remove
	 */
	public static void recursiveRm(File f) {
		if (f.isDirectory()) {
			String[] filenames = f.list();
			for (int i=0; i<filenames.length; i++)
				recursiveRm(new File(f, filenames[i]));
				f.delete();
		}
		else {
			f.delete();
		}
	}

	/**
	 * create all missing folders and return true,
	 * if folder on success
	 * @param f - folder to create
	 */
	public static boolean mkDirs(File f) {
		boolean ok = false;
		if (f.isDirectory()) 
			ok = true;
		else if (f.exists())
			ok = false;
		else {
			ok = f.mkdirs();
		}
		return ok;
	}

	/**
     * copy source to destination, missing dirs are created, 
     * existing destination-file will be overwritten
	 * @param source
	 * @param destination
	 * @return true on success
	 */
    public static boolean copyFile(File source, File destination) {
        boolean ok = false;
        InputStream in = null;
        try {
            mkDirs(destination.getParentFile());
            in = new FileInputStream(source);
            writeToFile(destination, in);
            in = null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try{ in.close(); }
                catch(IOException e){}
            }
        }
        return ok;
    }
    
    /**
     * writes a stream into a file. Existing files will be overwritten .
     * @param outputFile - target file to be created
     * @param in - stream to be written to file, Stream will be closed.
     * @return <code>true</code> on success
     */
    public static boolean writeToFile(File outputFile, InputStream in) {
        return writeToFile(outputFile, in, true);
    }

    /**
     * writes a stream into a file. Existing files will be overwritten .
     * @param outputFile - target file to be created
     * @param in - stream to be written to file
     * @param closeIn if true, in-Stream will be closed 
     * @return <code>true</code> on success
     */
    public static boolean writeToFile(File outputFile, InputStream in, boolean closeIn) {
        
        boolean ok = false;
        OutputStream out = null;
        try {
            out = new FileOutputStream(outputFile);
            byte[] buffer = new byte[1024];
            int cnt=0;
            if (in != null)
                cnt = in.read(buffer); 
            while (cnt > 0) {
                out.write(buffer, 0, cnt);
                cnt=in.read(buffer); 
            }
            ok = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (closeIn && (in != null))
                try{ in.close(); }catch(IOException e){}
            if (out != null)
                try{ out.close(); }catch(IOException e){}
        }
        return ok;
    }

	/**
	 * create all missing parent dirs
	 * @param fileName
	 * @return true on success
	 */
	public static boolean mkParentDirs(String fileName) {
		
		boolean ok = false;
		File f = new File(fileName);
		File parent = f.getParentFile();
		if (parent == null)
			ok = true;
		else
			ok = parent.mkdirs();
		return ok;
	}

    /**
     * @param file
     * @return content of file (in default encodeing) or null on error
     */
    public static String readContent(File file) {
        String result = null;
        try {
            byte[] buffer = new byte[(int)file.length()];
            FileInputStream in = new FileInputStream(file);
            in.read(buffer);
            in.close();
            result = new String(buffer);
        } catch (FileNotFoundException e) {} catch (IOException e) {}
        return result;
    }

    /**
     * read stream into a string,
     * the stream is closed.
     * @param in
     * @return content of stream as string (in default encoding) 
     * or null on error
     */
    public static String readContent(InputStream in) {
        String result = null;
        byte[] buffer = new byte[4096];
        try {
            StringBuffer sbuf = new StringBuffer();
            int cnt = in.read(buffer);
            while (cnt > 0) {
                String str = new String(buffer, 0, cnt);
                sbuf.append(str);
                cnt = in.read(buffer);
            }
            result = sbuf.toString();
        } catch (IOException e) {}
        try {
            in.close();
        } catch (IOException e1) {}
        return result;
    }

    /**
     * @param conflictOutputFile
     * @param newManifest
     */
    public static void writeToFile(File outputFile, String content) {
        InputStream stream = new ByteArrayInputStream(content.getBytes());
        writeToFile(outputFile, stream);
    }

	
}
