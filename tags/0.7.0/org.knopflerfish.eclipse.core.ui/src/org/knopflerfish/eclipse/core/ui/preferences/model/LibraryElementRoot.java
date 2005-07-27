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

package org.knopflerfish.eclipse.core.ui.preferences.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.knopflerfish.eclipse.core.IOsgiBundle;
import org.knopflerfish.eclipse.core.IOsgiLibrary;
import org.knopflerfish.eclipse.core.OsgiBundle;
import org.knopflerfish.eclipse.core.OsgiLibrary;
import org.knopflerfish.eclipse.core.project.BundleManifest;

/**
 * @author Anders Rimén
 */
public class LibraryElementRoot implements ILibraryTreeElement {
  
  // Pathes relative knopflerfish osgi directory
  private static String REL_PATH_FRAMEWORK          = "framework.jar";
  private static String REL_PATH_FRAMEWORK_SRC_DIR  = "framework/src";
  private static String REL_PATH_JAR_DIR            ="jars";
  private static String REL_PATH_BUNDLE_DIR         ="bundles";

  private OsgiLibrary mainLib;
  
  private final LibraryElementRuntimeRoot runtimeElement;
  private final LibraryElementBuildRoot buildElement;
  private final LibraryElementBundleRoot bundleElement;
  
  public LibraryElementRoot() {
    runtimeElement = new LibraryElementRuntimeRoot(this);
    buildElement = new LibraryElementBuildRoot(this);
    bundleElement = new LibraryElementBundleRoot(this);
    
  }
  
  public void loadDefaultModel(File root) {
    // Clear data
    mainLib = null;
    runtimeElement.clear();
    bundleElement.clear();
    buildElement.clear();
    
    // Check root is ok
    if (root == null || !root.exists() || !root.isDirectory()) return;
    
    // Framework library
    try {
      File file = new File(root, REL_PATH_FRAMEWORK);
      mainLib = new OsgiLibrary(file);
      File srcDir = new File(root, REL_PATH_FRAMEWORK_SRC_DIR);
      if (srcDir.exists() && srcDir.isDirectory()) {
        mainLib.setSourceDirectory(srcDir.getAbsolutePath());
      }
      
      runtimeElement.addChild(new LibraryElementRuntime(runtimeElement, mainLib));
      buildElement.addChild(new LibraryElementBuild(buildElement, mainLib));
    } catch (IOException e) {
      // Failed to find framework library
    }
    
    // Bundles
    File jarDir = new File(root, REL_PATH_JAR_DIR);
    ArrayList jars = getBundleJars(jarDir);
    for (int i=0 ; i<jars.size(); i++) {
      try {
        OsgiBundle bundle = new OsgiBundle((File) jars.get(i));
        // Find source
        String builtFrom = null;
        if (bundle.getBundleManifest() != null) {
          builtFrom = bundle.getBundleManifest().getAttribute(BundleManifest.BUILT_FROM);
        }
        if (builtFrom != null) {
          int idx = builtFrom.lastIndexOf(REL_PATH_BUNDLE_DIR);
          if (idx != -1) {
            File dir = new File(root, builtFrom.substring(idx));
            File srcDir = new File(dir, "src");
            if (srcDir.exists() && srcDir.isDirectory()) {
              bundle.setSourceDirectory(srcDir.getAbsolutePath());
            }
          }
        }
        bundleElement.addChild(new LibraryElementBundle(bundleElement, bundle));
        if (bundle.hasCategory("api") || bundle.hasCategory("API")) {
          buildElement.addChild(new LibraryElementBuild(buildElement, bundle));
        }
      } catch(IOException e) {
        // Failed to create bundle from file
      }
    }
  }
  
  public IOsgiLibrary getMainLibrary() {
    return mainLib;
  }
  
  public LibraryElementBuildRoot getBuildRoot() {
   return buildElement; 
  }

  public LibraryElementBundleRoot getBundleRoot() {
    return bundleElement; 
   }
  
  public LibraryElementRuntimeRoot getRuntimeRoot() {
    return runtimeElement; 
   }
  
  public IOsgiLibrary[] getRuntimeLibraries() {
    ArrayList libs = new ArrayList();
    ILibraryTreeElement [] children = runtimeElement.getChildren();
    for (int i=0; i<children.length; i++) {
      libs.add( ((LibraryElementRuntime) children[i]).getLibrary());
    }
    return (IOsgiLibrary[]) libs.toArray(new IOsgiLibrary[libs.size()]);
  }
  
  public IOsgiLibrary[] getBuildLibraries() {
    ArrayList libs = new ArrayList();
    ILibraryTreeElement [] children = buildElement.getChildren();
    for (int i=0; i<children.length; i++) {
      libs.add( ((LibraryElementBuild) children[i]).getLibrary());
    }
    return (IOsgiLibrary[]) libs.toArray(new IOsgiLibrary[libs.size()]);
  }
  
  public IOsgiBundle[] getBundles() {
    ArrayList libs = new ArrayList();
    ILibraryTreeElement [] children = bundleElement.getChildren();
    for (int i=0; i<children.length; i++) {
      libs.add( ((LibraryElementBundle) children[i]).getBundle());
    }
    return (IOsgiBundle[]) libs.toArray(new IOsgiBundle[libs.size()]);
  }
  
  /****************************************************************************
   * org.knopflerfish.eclipse.core.ui.preferences.model.ILibraryTreeElement methods
   ***************************************************************************/
  
  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.ui.preferences.model.ILibraryTreeElement#getChildren()
   */
  public ILibraryTreeElement[] getChildren() {
    return new ILibraryTreeElement[] {
        runtimeElement,
        bundleElement,
        buildElement};
  }

  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.ui.preferences.model.ILibraryTreeElement#getParent()
   */
  public ILibraryTreeElement getParent() {
    return null;
  }

  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.ui.preferences.model.ILibraryTreeElement#hasChildren()
   */
  public boolean hasChildren() {
    return true;
  }
  
  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.ui.preferences.model.ILibraryTreeElement#getType()
   */
  public int getType() {
    return TYPE_ROOT;
  }
  
  /****************************************************************************
   * Private helper methods
   ***************************************************************************/
  private ArrayList getBundleJars(File f) { 
    ArrayList jars = new ArrayList();
    if (f.isFile() && f.getName().toLowerCase().endsWith("jar")) {
      jars.add(f); 
    } else if (f.isDirectory()) {
      File [] list = f.listFiles();
      for(int i=0; list != null && i<list.length; i++) {
        jars.addAll(getBundleJars(list[i]));
      }
    }
    return jars;
  }

}
