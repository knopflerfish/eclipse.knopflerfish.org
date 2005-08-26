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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class NativeCodeClause {
  private static final String SEPARATOR = ";";
  private static final String ENV_PARAM_PROCESSOR = "processor"; 
  private static final String ENV_PARAM_OSNAME    = "osname"; 
  private static final String ENV_PARAM_OSVERSION = "osversion"; 
  private static final String ENV_PARAM_LANGUAGE  = "language"; 
  
  private ArrayList nativePaths = new ArrayList();
  private ArrayList processorDef = new ArrayList();
  private ArrayList osNameDef = new ArrayList();
  private ArrayList osVersionDef = new ArrayList();
  private ArrayList languageDef = new ArrayList();
  
  public NativeCodeClause() {
    // Create empty native code clause
  }
  
  public NativeCodeClause(String s) {
    // Parse native code clause from string
    StringTokenizer st = new StringTokenizer(s, SEPARATOR);
    
    boolean pathAllowed = true;
    while (st.hasMoreTokens()) {
      String token = st.nextToken().trim();
      int idx = token.indexOf('=');
      if (idx != -1) {
        // Found environment parameter, no native paths allowed after this
        pathAllowed = false;
        
        // Extract attribute and value
        String attr = "";
        if (idx > 0) {
          attr = token.substring(0, idx).trim();
        }
        String value = "";
        if (idx+1 < token.length()) {
          value = token.substring(idx+1).trim();
        }
        // Removes qoutes if qouted
        if (value.startsWith("\"") && value.endsWith("\"") ) {
          value = value.substring(1, value.length()-1);
        }

        // Check what attribute this is
        if (ENV_PARAM_PROCESSOR.equals(attr)) {
          addProcessorDef(value);
        } else if (ENV_PARAM_OSNAME.equals(attr)) {
          addOSNameDef(value);
        } else if (ENV_PARAM_OSVERSION.equals(attr)) {
          addOSVersionDef(value);
        } else if (ENV_PARAM_LANGUAGE.equals(attr)) {
          addLanguageDef(value);
        } else {
          // Unknown environment parameter, just ignore it
        }
      } else if (pathAllowed) {
        addNativePath(token);
      } else {
        // Found native path at wrong place in clause, just ignore it
      }
    }
  }
  
  /****************************************************************************
   * java.lang.Object methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString() {
    StringBuffer buf = new StringBuffer();
    
    // Add all native paths
    for(Iterator i=nativePaths.iterator();i.hasNext();) {
      if (buf.length() > 0) {
        buf.append(SEPARATOR);
      }
      buf.append((String) i.next());
    }

    // Add all processor defs
    for(Iterator i=processorDef.iterator();i.hasNext();) {
      append(buf, ENV_PARAM_PROCESSOR, (String) i.next());
    }

    // Add all OS name defs
    for(Iterator i=osNameDef.iterator();i.hasNext();) {
      append(buf, ENV_PARAM_OSNAME, (String) i.next());
    }
    
    // Add all OS version defs
    for(Iterator i=osVersionDef.iterator();i.hasNext();) {
      append(buf, ENV_PARAM_OSVERSION, (String) i.next());
    }
    
    // Add all language defs
    for(Iterator i=languageDef.iterator();i.hasNext();) {
      append(buf, ENV_PARAM_LANGUAGE, (String) i.next());
    }
    
    return buf.toString();
  }
  
  /****************************************************************************
   * Native path methods
   ***************************************************************************/
  
  public void addNativePath(String path) {
    add(nativePaths, path);
  }
  
  public void removeNativePath(String path) {
    remove(nativePaths, path);
  }
  
  public String[] getNativePaths() {
    return getValues(nativePaths);
  }
  
  /****************************************************************************
   * Processor environment parameter methods
   ***************************************************************************/
  
  public void addProcessorDef(String def) {
    add(processorDef, def);
  }
  
  public void removeProcessorDef(String def) {
    remove(processorDef, def);
  }
  
  public String[] getProcessorDefs() {
    return getValues(processorDef);
  }
  
  /****************************************************************************
   * OS name environment parameter methods
   ***************************************************************************/
  
  public void addOSNameDef(String def) {
    add(osNameDef, def);
  }
  
  public void removeOSNameDef(String def) {
    remove(osNameDef, def);
  }
  
  public String[] getOSNameDefs() {
    return getValues(osNameDef);
  }
  
  /****************************************************************************
   * OS version environment parameter methods
   ***************************************************************************/
  
  public void addOSVersionDef(String def) {
    add(osVersionDef, def);
  }
  
  public void removeOSVersionDef(String def) {
    remove(osVersionDef, def);
  }
  
  public String[] getOSVersionDefs() {
    return getValues(osVersionDef);
  }
  
  /****************************************************************************
   * Language environment parameter methods
   ***************************************************************************/
  
  public void addLanguageDef(String def) {
    add(languageDef, def);
  }
  
  public void removeLanguageDef(String def) {
    remove(languageDef, def);
  }
  
  public String[] getLanguageDefs() {
    return getValues(languageDef);
  }
  
  /****************************************************************************
   * Private utility methods
   ***************************************************************************/
  
  private void add(List list, String value) {
    if (list == null || value == null) return;
    
    if (!list.contains(value)) {
      list.add(value);
    }
  }
  
  private void remove(List list, String value) {
    if (list == null || value == null) return;
    
    if (list.contains(value)) {
      list.remove(value);
    }
  }
  
  public String[] getValues(List list) {
    if (list == null) return new String[0];
    
    return (String[]) list.toArray(new String[list.size()]);
  }

  private void append(StringBuffer buf, String attr, String value) {
    
    if (buf.length() > 0) {
      buf.append(SEPARATOR);
    }
    
    buf.append(attr);
    buf.append("=");
    boolean qoute = value.indexOf(" ") != -1;
    if (qoute) {
      buf.append("\"");
    }
    buf.append(value);
    if (qoute) {
      buf.append("\"");
    }
  }
}
