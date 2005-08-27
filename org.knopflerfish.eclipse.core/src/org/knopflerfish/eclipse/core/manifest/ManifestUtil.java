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

package org.knopflerfish.eclipse.core.manifest;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class ManifestUtil {
  
  private static final int MAX_LINE_LENGTH_EXCL_NEWLINE = 70;

  static public int findAttributeLine(StringBuffer buf, String attr) {
    // Find position of attribute
    BufferedReader reader = new BufferedReader(new StringReader(buf.toString()));
    int attrLine = -1;
    int currentLine = 1;
    
    String line = null;
    try {
      while((line = reader.readLine()) != null && attrLine == -1) {
        if (attrLine == -1 && line.startsWith(attr)) {
          // Find start of manifest attribute
          attrLine = currentLine;
        }
        currentLine++;
      }
    } catch (IOException e) {}
    
    return attrLine;
  }
  
  /**
   * Adds a main attribute to a manifest file. The document passed shall 
   * contain a manifest file.
   * 
   * @param doc document containing a manifest file
   * @param name attribute name
   * @param value attribute value
   */
  static public StringBuffer setManifestAttribute(StringBuffer buf, String attr, String value) {
    
    // Find position of attribute
    BufferedReader reader = new BufferedReader(new StringReader(buf.toString()));
    int startPos = -1;
    int endPos = -1;
    String line = null;
    try {
      while((line = reader.readLine()) != null) {
        if (startPos == -1 && line.startsWith(attr)) {
          // Find start of manifest attribute
          startPos = buf.indexOf(line);
          endPos = startPos+line.length();
          if (endPos < buf.length() && buf.charAt(endPos) == '\r') {
            endPos += 1;
          }
          if (endPos < buf.length() && buf.charAt(endPos) == '\n') {
            endPos += 1;
          }
        } else if (startPos != -1) {
          // Find end of manifest attribute
          if (line.startsWith(" ")) {
            endPos += line.length();
            if (endPos < buf.length() && buf.charAt(endPos) == '\r') {
              endPos += 1;
            }
            if (endPos < buf.length() && buf.charAt(endPos) == '\n') {
              endPos += 1;
            }
          } else {
            break;
          }
        }
      }
    } catch (IOException e) {}
    
    if (startPos != -1) {
      
      // Check if attribute shall be added or removed
      if (value != null && value.trim().length() > 0) {
        // Replace attribute
        buf.replace(startPos, endPos, createAttributeLine(attr, value));
      } else  {
        // Remove attribute
        buf.replace(startPos, endPos, "");
      }
      
    } else if (value != null && value.trim().length() > 0) {
      // Append attribute
      int idx = buf.indexOf("\r\n\r\n");
      if (idx != -1) {
        // Insert attribute
        buf.insert(idx+2, createAttributeLine(attr, value));
      } else {
        // Add attribute to end
        if (!buf.toString().endsWith("\r\n")) {
          buf.append("\r\n");
        }
        buf.append(createAttributeLine(attr, value));
      }
    }
    
    return buf;
  }
  
  /**
   * Concatenates a name and value into a manifest attribute line.
   *  
   * @param name attribute name
   * @param value attribute value
   * @return concatenated attribute line
   */
  private static String createAttributeLine(String name, String value) {
    StringBuffer buf = new StringBuffer();
    buf.append(name);
    buf.append(": ");
    // Line length not allowed to exceed 72 bytes including \r\n
    int lineLength = buf.length();
    int offset = 0;
    int restLength = value.length();
    while (restLength > 0) {
      if (lineLength+restLength > MAX_LINE_LENGTH_EXCL_NEWLINE) {
        String s = value.substring(offset, offset+MAX_LINE_LENGTH_EXCL_NEWLINE-lineLength);
        offset += s.length();
        restLength = restLength - s.length();
        buf.append(s);
        buf.append("\r\n ");
        lineLength = 1;
      } else {
        String s = value.substring(offset);
        offset += s.length();
        restLength = restLength - s.length();
        buf.append(s);
        buf.append("\r\n");
      }
    }
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
  static public BundleManifest createManifest(byte[] bytes) {
    BundleManifest manifest = null;
    try {
      manifest = new BundleManifest(new ByteArrayInputStream(bytes));
    } catch (IOException e) {
      manifest = new BundleManifest();
    }
    
    return manifest;
  }
}
