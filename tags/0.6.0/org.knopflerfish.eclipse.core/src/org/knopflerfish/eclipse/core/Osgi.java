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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jdt.core.IJavaProject;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * @author Anders Rimén
 */
public class Osgi {
  public static String NATURE_ID = "org.knopflerfish.eclipse.core.bundlenature";
  public static String BUILDER_ID = "org.knopflerfish.eclipse.core.bundlebuilder";
  
  private static String EXTENSION_POINT_VENDORS = "org.knopflerfish.eclipse.core.vendors";
  private static String PREFERENCE_NODE = "org.knopflerfish.eclipse.core.bundlesets";
                                                   
  public static List getVendorNames()  {
    ArrayList vendors = new ArrayList();
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint point = registry.getExtensionPoint(EXTENSION_POINT_VENDORS);
    if (point != null) {
      IExtension[] extensions = point.getExtensions();
      for (int i = 0; i < extensions.length; i++) {
        IConfigurationElement [] configs = extensions[i].getConfigurationElements();
        if (configs == null) continue;
        
        for (int j=0; j<configs.length; j++) {
          if ("vendor".equals(configs[i].getName())) {
            vendors.add( configs[j].getAttribute("name") );
          }
        }
      }
    }
    
    return vendors;
  }
  
  public static IOsgiVendor getVendor(String name) {
    if (name == null) return null;
    
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint point = registry.getExtensionPoint(EXTENSION_POINT_VENDORS);
    if (point != null) {
      IExtension[] extensions = point.getExtensions();
      for (int i = 0; i < extensions.length; i++) {
        IConfigurationElement [] configs = extensions[i].getConfigurationElements();
        if (configs == null) continue;
        
        for (int j=0; j<configs.length; j++) {
          if ("vendor".equals(configs[i].getName()) && 
              name.equals(configs[j].getAttribute("name"))) { 
            try {
              return (IOsgiVendor) configs[j].createExecutableExtension("class");
            } catch (CoreException e) {
              System.err.println("Failed to create executable extension / "+e);
            }
          }
        }
      }
    }
    
    return null;
  }

  /****************************************************************************
   * Bundle Set Methods
   ***************************************************************************/

  public static List getBundleSets() {
    // Load all osgi install definitions from preferences
    Preferences node = new ConfigurationScope().getNode(PREFERENCE_NODE);
    ArrayList bundleSets = new ArrayList();
    
    try {
      String [] children = node.childrenNames();
      for (int i=0; i<children.length; i++) {
        bundleSets.add(new BundleSet(node.node(children[i])));
      }
    } catch (BackingStoreException e) {
      e.printStackTrace();
    }
    
    return bundleSets;
  }

  public BundleSet getBundleSet(String name) {
    Preferences node = new ConfigurationScope().getNode(PREFERENCE_NODE);
    BundleSet bundleSet = null;
    try  {
      if (node.nodeExists(name)) {
        bundleSet = new BundleSet(node.node(name));
      }
    } catch (BackingStoreException e) {
      e.printStackTrace();
    }
    
    return bundleSet;
  }

  public BundleSet getDefaultBundleSet() {
    List l = getBundleSets();
    BundleSet defaultSet = null;
    for (int i=0; i<l.size(); i++) {
      BundleSet bundleSet = (BundleSet) l.get(i);
      if (bundleSet.isDefaultDefinition()) {
        defaultSet = bundleSet;
      }
    }
    
    return defaultSet;
  }

  public void setBundleSets(List bundleSets) throws BackingStoreException {
    // Remove previous bundle sets
    Preferences node = new ConfigurationScope().getNode(PREFERENCE_NODE);
    String [] names = node.childrenNames();
    if (names != null) {
      for(int i=0; i<names.length; i++) {
        node.node(names[i]).removeNode();
      }
    }
    
    // Save bundle sets
    if (bundleSets == null) return;
    for(int i=0; i<bundleSets.size();i++) {
      BundleSet bundleSet = (BundleSet) bundleSets.get(i);
      bundleSet.save(node);
    }

    node.flush();
    
  }

  /****************************************************************************
   * Project methods
   ***************************************************************************/
  public static boolean isBundleProject(IJavaProject project) {
    try {
      return project.getProject().hasNature(Osgi.NATURE_ID);
    } catch (CoreException e) {
      return false;
    }
  }
}
