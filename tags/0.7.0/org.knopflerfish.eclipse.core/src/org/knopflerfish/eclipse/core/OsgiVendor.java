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

package org.knopflerfish.eclipse.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * Knopflerfish implementation of IOsgiVendor
 */
public class OsgiVendor implements IOsgiVendor {

  public static String VENDOR_NAME = "Knopflerfish";

  /****************************************************************************
   * org.gstproject.eclipse.osgi.IOsgiVendor Methods
   ***************************************************************************/
  /* (non-Javadoc)
   * @see org.gstproject.eclipse.osgi.IOsgiVendor#getName()
   */
  public String getName() {
    return VENDOR_NAME;
  }

  /* (non-Javadoc)
   * @see org.gstproject.eclipse.osgi.IOsgiVendor#getOsgiInstalls()
   */
  public List getOsgiInstalls() {
    // Load all osgi install definitions from preferences
    //Preferences node = new ConfigurationScope().getNode(PREFERENCE_NODE);
    Preferences node = new InstanceScope().getNode(Osgi.PREFERENCE_ROOT_NODE).node(Osgi.PREFERENCE_FRAMEWORKS_NODE);
    ArrayList osgiInstalls = new ArrayList();
    
    try {
      String [] children = node.childrenNames();
      for (int i=0; i<children.length; i++) {
        osgiInstalls.add(new OsgiInstall(node.node(children[i])));
      }
    } catch (BackingStoreException e) {
      e.printStackTrace();
    }
    
    return osgiInstalls;
  }

  /* (non-Javadoc)
   * @see org.gstproject.eclipse.osgi.IOsgiVendor#getOsgiInstall(java.lang.String)
   */
  public IOsgiInstall getOsgiInstall(String name) {
    if (name == null || name.length() == 0) return null;
    
    //Preferences node = new ConfigurationScope().getNode(PREFERENCE_NODE);
    Preferences node = new InstanceScope().getNode(Osgi.PREFERENCE_ROOT_NODE).node(Osgi.PREFERENCE_FRAMEWORKS_NODE);
    IOsgiInstall osgiInstall = null;
    try  {
      if (node.nodeExists(name)) {
        osgiInstall = new OsgiInstall(node.node(name));
      }
    } catch (BackingStoreException e) {
      e.printStackTrace();
    }
    
    return osgiInstall;
  }

  /* (non-Javadoc)
   * @see org.gstproject.eclipse.osgi.IOsgiVendor#getDefaultOsgiInstall()
   */
  public IOsgiInstall getDefaultOsgiInstall() {
    List l = getOsgiInstalls();
    IOsgiInstall defaultInstall = null;
    for (int i=0; i<l.size(); i++) {
      OsgiInstall osgiInstall = (OsgiInstall) l.get(i);
      if (osgiInstall.isDefaultDefinition()) {
        defaultInstall = osgiInstall;
      }
    }
    
    return defaultInstall;
  }

  /* (non-Javadoc)
   * @see org.gstproject.eclipse.osgi.IOsgiVendor#createConfiguration(java.io.File, java.util.Map)
   */
  public IOsgiConfiguration createConfiguration(File dir, Map attributes) {
    OsgiConfiguration conf = new OsgiConfiguration(dir, attributes);
    return conf;
  }

  /****************************************************************************
   * Preference storage methods
   ***************************************************************************/
  public void setOsgiInstalls(List osgiInstalls) throws BackingStoreException {
    // Remove previous definitions
    //Preferences node = new ConfigurationScope().getNode(PREFERENCE_NODE);
    Preferences node = new InstanceScope().getNode(Osgi.PREFERENCE_ROOT_NODE).node(Osgi.PREFERENCE_FRAMEWORKS_NODE);
    String [] names = node.childrenNames();
    if (names != null) {
      for(int i=0; i<names.length; i++) {
        node.node(names[i]).removeNode();
      }
    }
    
    // Save framework definitions
    if (osgiInstalls == null) return;
    for(int i=0; i<osgiInstalls.size();i++) {
      OsgiInstall osgiInstall = (OsgiInstall) osgiInstalls.get(i);
      osgiInstall.save(node);
    }

    node.flush();
  }
}
