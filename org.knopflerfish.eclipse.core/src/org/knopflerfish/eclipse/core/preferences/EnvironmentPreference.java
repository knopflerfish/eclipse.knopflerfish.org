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

package org.knopflerfish.eclipse.core.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.knopflerfish.eclipse.core.IOsgiLibrary;
import org.knopflerfish.eclipse.core.OsgiLibrary;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class EnvironmentPreference {
  
  // Preferences keys
  private static final String PREF_DEFAULT_ENVIRONMENT = "DefaultEnvironment"; // true, false
  private static final String PREF_TYPE                = "Type"; // osgi, user 
  private static final String PREF_LIBRARIES           = "Libraries";
  private static final String PREF_JAR                 = "Jar";
  private static final String PREF_SOURCE              = "Source";
  private static final String PREF_NAME                = "Name";
  
  
  // Environment types
  public static final int TYPE_JRE  = 0;
  public static final int TYPE_OSGI = 1;
  public static final int TYPE_USER = 2;
  
  private String name;
  private int type;
  private boolean defaultEnvironment;
  private ArrayList libraries = new ArrayList();  
  
  public EnvironmentPreference() {
  }
  
  public EnvironmentPreference(Preferences node) throws BackingStoreException {
    load(node);
  }
  
  private void load(Preferences node) throws BackingStoreException {
    // Load preferences
    //name = node.name();
    
    // Name
    name = node.get(PREF_NAME, "");
    
    // Type
    type = node.getInt(PREF_TYPE, TYPE_USER);
    
    // Default environment 
    defaultEnvironment = "true".equalsIgnoreCase(node.get(PREF_DEFAULT_ENVIRONMENT, "false"));
    
    // Libraries
    Preferences librariesNode = node.node(PREF_LIBRARIES);
    libraries.clear();
    int idx = 0;
    while (librariesNode.nodeExists("Library "+idx)) {
      Preferences libraryNode = librariesNode.node("Library "+idx);
      try {
        OsgiLibrary library = new OsgiLibrary(new File(libraryNode.get(PREF_JAR, "")));
        library.setSource(libraryNode.get(PREF_SOURCE, null));
        libraries.add(library);
      } catch (Exception e) {}
      idx++;
    }
  }

  protected void save(Preferences node) throws BackingStoreException {
    // Save preferences in node
    Preferences subNode = node.node(name.replace('/','_'));
    
    // Name
    subNode.put(PREF_NAME, name);
    
    // Type
    subNode.putInt(PREF_TYPE, type);

    // Default Environment 
    subNode.putBoolean(PREF_DEFAULT_ENVIRONMENT, defaultEnvironment);

    // Libraries
    subNode.node(PREF_LIBRARIES).removeNode();
    Preferences librariesNode = subNode.node(PREF_LIBRARIES);
    int idx = 0;
    for (Iterator i=libraries.iterator(); i.hasNext(); ) {
      OsgiLibrary library = (OsgiLibrary) i.next();
      Preferences libraryNode = librariesNode.node("Library "+idx);
      libraryNode.put(PREF_JAR, library.getPath());
      if (library.getSource() != null) {
        libraryNode.put(PREF_SOURCE, library.getSource());
      }
      idx++;
    }
  }
  
  /****************************************************************************
   * Getters and Setters
   ***************************************************************************/
  
  public boolean isDefaultEnvironment() {
    return defaultEnvironment;
  }
  
  public void setDefaultEnvironment(boolean defaultEnvironment) {
    this.defaultEnvironment = defaultEnvironment;
  }

  public int getType() {
    return type;
  }
  
  public void setType(int type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public IOsgiLibrary[] getLibraries() {
    return (OsgiLibrary[]) libraries.toArray(new OsgiLibrary[libraries.size()]);
  }

  public void setLibraries(IOsgiLibrary[] libraries) {
    this.libraries.clear();
    if (libraries == null) return;
    for(int i=0; i<libraries.length; i++) {
      this.libraries.add(libraries[i]);
    }
  }
  
  /****************************************************************************
   * java.lang.Object methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object o) {
    if (o == null || !(o instanceof EnvironmentPreference)) {
      return false;
    }
    EnvironmentPreference env = (EnvironmentPreference) o;
    
    if (name == null) {
      return false;
    }
    
    return name.equals(env.getName());
  }
}
