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

import java.util.ArrayList;

import org.knopflerfish.eclipse.core.IOsgiBundle;
import org.knopflerfish.eclipse.core.IOsgiLibrary;

/**
 * @author Anders Rimén
 */
public class LibraryElementRoot implements ILibraryTreeElement {
  
  private final LibraryElementRuntimeRoot runtimeElement;
  private final LibraryElementBuildRoot buildElement;
  private final LibraryElementBundleRoot bundleElement;
  
  public LibraryElementRoot() {
    runtimeElement = new LibraryElementRuntimeRoot(this);
    buildElement = new LibraryElementBuildRoot(this);
    bundleElement = new LibraryElementBundleRoot(this);
  }
  
  public void clear() {
    runtimeElement.clear();
    bundleElement.clear();
    buildElement.clear();
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
        buildElement,
        bundleElement};
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
  
}
