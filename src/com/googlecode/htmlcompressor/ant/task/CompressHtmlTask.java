package com.googlecode.htmlcompressor.ant.task;

import org.apache.tools.ant.Task;
import com.googlecode.htmlcompressor.compressor.*;
import java.io.*;
import java.util.Vector;
import java.util.Enumeration;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.DirectoryScanner;

public class CompressHtmlTask extends Task {
    private Vector filesets = new Vector();
    private String destdir = null;
    private boolean removeComments = true;
    private boolean removeJspComments = true;
    private boolean compressJS = false;
    private boolean compressCSS = true;
    private boolean leaveStrutsFormTagComments = false;
    
    public void setDestDir(String s) {
        destdir = s;
    }
    
    public void setSkipStrutsFormComments(boolean skipFormComments) {
       leaveStrutsFormTagComments = skipFormComments;
    }

    public void execute() {
       
        if (filesets.size() == 0) {
            return;
        }
        
        Enumeration e = filesets.elements();
        
        while (e.hasMoreElements()) {
            FileSet fs = (FileSet) e.nextElement();
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            
            String[] includedFiles = ds.getIncludedFiles();

            log("Compressing " + includedFiles.length + " files to " + this.destdir + " ...");
                
            
            for (int i = 0; i < includedFiles.length; i++) {
                File f = new File(ds.getBasedir(), includedFiles[i]);
                File d = new File(destdir, includedFiles[i]);
                
                File p = new File(d.getParent());
                
                if (!p.exists()) {
                    p.mkdirs();
                }
                
                writeFile(d.toString(), compressHTML(readFile(f.toString(), null)), null);
            }
        
        }
    }
       
    private String compressHTML(String buffer) {
        HtmlCompressor compressor = new HtmlCompressor();
        String newHTML = null;
    
        compressor.setEnabled(true); //if false all compression is off (default is true)
        compressor.setRemoveJspComments(this.removeJspComments);
        compressor.setRemoveComments(this.removeComments); //if false keeps HTML comments (default is true)
        compressor.setRemoveMultiSpaces(true); //if false keeps multiple whitespace characters (default is true)
        compressor.setRemoveIntertagSpaces(true);//removes iter-tag whitespace characters
        compressor.setRemoveQuotes(false); //removes unnecessary tag attribute quotes
        compressor.setCompressCss(this.compressCSS); //compress css using Yahoo YUI Compressor
        compressor.setCompressJavaScript(this.compressJS); //compress js using Yahoo YUI Compressor
        compressor.setYuiCssLineBreak(80); //--line-break param for Yahoo YUI Compressor
        compressor.setYuiJsDisableOptimizations(true); //--disable-optimizations param for Yahoo YUI Compressor
        compressor.setYuiJsLineBreak(-1); //--line-break param for Yahoo YUI Compressor
        compressor.setYuiJsNoMunge(false); //--nomunge param for Yahoo YUI Compressor
        compressor.setYuiJsPreserveAllSemiColons(true);//--preserve-semi param for Yahoo YUI Compressor
        
        // custom attribute to skip comments with Struts html:form TagElement
        compressor.setSkipStrutsFormComments(leaveStrutsFormTagComments);
        
        try {
            newHTML = compressor.compress(buffer);
        } catch (Exception q) {
            q.printStackTrace();
        }
    
        return(newHTML);
    }
     
    private String readFile(String path, String encoding) {
        String finalString = null;
        try {
            // summary: reads a file and returns a string
            if (encoding == null) {
                encoding = "utf-8";
            }
            
            File file = new File(path);
            
            BufferedReader input = new java.io.BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
            
            try {
                StringBuffer stringBuffer = new StringBuffer();
                String line = input.readLine();
                String lineSeparator = System.getProperty("line.separator");
                
                // Byte Order Mark (BOM) - The Unicode Standard, version 3.0, page
                // 324
                // http://www.unicode.org/faq/utf_bom.html
                
                // Note that when we use utf-8, the BOM should appear as "EF BB BF",
                // but it doesn't due to this bug in the JDK:
                // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4508058
                if (line != null && line.length() > 0 && line.charAt(0) == 0xfeff) {
                    // Eat the BOM, since we've already found the encoding on this
                    // file,
                    // and we plan to concatenating this buffer with others; the BOM
                    // should
                    // only appear at the top of a file.
                    line = line.substring(1);
                }
                
                while (line != null) {
                    stringBuffer.append(line);
                    stringBuffer.append(lineSeparator);
                    line = input.readLine();
                }
                
                // Make sure we return a JavaScript string and not a Java string.
                return stringBuffer.toString(); // String
            } finally {
                input.close();
            }
        } catch (Exception e) {
        }
        
        return(null);
    }
    
    
    private String writeFile(String path, String contents, String encoding) {
        String finalString = null;
        try {
            // summary: reads a file and returns a string
            if (encoding == null) {
                encoding = "utf-8";
            }
            
            File file = new File(path);
            String lineSeparator = System.getProperty("line.separator");
            BufferedWriter output = new java.io.BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
            try {
                output.write(contents, 0, contents.length());
            } finally {
                output.flush();
                output.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return(null);
    }
    
    /**
     *  A set of files to upload or download
     */
    public void addFileset(FileSet set) {
        filesets.addElement(set);
    }
    
    public void setRemoveJspComments(boolean removeComments) {
        this.removeJspComments = removeComments;
    }
    
    public void setRemoveComments(boolean removeComments) {
        this.removeComments = removeComments;
    }
    
    public void setCompressJs(boolean compress) {
        this.compressJS = compress;
    }
    
    public void setCompressCSS(boolean compress) {
        this.compressCSS = compress;
    }

}
