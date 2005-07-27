/*
 * Copyright (c) 2003-2005, KNOPFLERFISH project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution.
 *
 * - Neither the name of the KNOPFLERFISH project nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.knopflerfish.eclipse.core.ui.editors.manifest;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.jar.Manifest;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * @author Anders Rimén
 */
public class ManifestUtil {

  /**
   * Adds a main attribute to a manifest file. The document passed shall 
   * contain a manifest file.
   * 
   * @param doc document containing a manifest file
   * @param name attribute name
   * @param value attribute value
   * @throws IOException
   * @throws BadLocationException
   */
  static public void setManifestAttribute(IDocument doc, String name, String value) throws IOException, BadLocationException {
    // Check if entry exists
    String contents = doc.get();
    BufferedReader reader = new BufferedReader(new StringReader(contents));
    
    int startLine = -1;
    int endLine = -1;
    int lineNo = 0;
    String line = null;
    
    while((line = reader.readLine()) != null) {
      if (startLine == -1 && line.startsWith(name)) {
        // Find start of manifest attribute
        startLine = lineNo;
        endLine = lineNo;
      } else if (startLine != -1) {
        // Find end of manifest attribute
        if (line.startsWith(" ")) {
          endLine = lineNo;
        } else {
          break;
        }
      }
      lineNo++;
    }
    
    // Check if attribute exists
    if (startLine != -1) {
      int offset = doc.getLineOffset(startLine);
      int length = 0;
      for (int i=startLine; i<=endLine; i++) {
        length += doc.getLineLength(i);
      }
      
      // Check if attribute shall be added or removed
      if (value != null && value.trim().length() > 0) {
        // Replace attribute
        doc.replace(offset, length, createAttributeLine(name, value));
      } else  {
        // Remove attribute
        doc.replace(offset, length, "");
      }
    } else if (value != null && value.trim().length() > 0) {
      // Add attribute last in main section 
      // TODO: Does not support JAR specific sections, should be improved
      // maybe 
      StringBuffer buf = new StringBuffer(contents);
      
      int idx = buf.indexOf("\r\n\r\n");
      if (idx != -1) {
        // Insert attribute
        buf.insert(idx+2, createAttributeLine(name, value));
      } else {
        // Add attribute to end
        if (!buf.toString().endsWith("\r\n")) {
          buf.append("\r\n");
        }
        buf.append(createAttributeLine(name, value));
      }
      doc.set(buf.toString());
    }
  }
  
  /**
   * Concatenates a name and value into a manifest attribute line.
   *  
   * @param name attribute name
   * @param value attribute value
   * @return concatenated attribute line
   */
  static public String createAttributeLine(String name, String value) {
    StringBuffer buf = new StringBuffer();
    buf.append(name);
    buf.append(": ");
    buf.append(value);
    buf.append("\r\n");
    return buf.toString();
  }

  /**
   * Creates an manifes object from a document containing a manifest
   * file. If there is an error reading/parsing the document contents
   * an empty manifest is returned.
   * 
   * @param doc document containing a manifest file
   * @return manifest object
   */
  static public Manifest createManifest(IDocument doc) {
    Manifest manifest = null;
    try {
      manifest = new Manifest(new ByteArrayInputStream(doc.get().getBytes()));
    } catch (IOException e) {
      manifest = new Manifest();
    }
    
    return manifest;
  }
  
}
