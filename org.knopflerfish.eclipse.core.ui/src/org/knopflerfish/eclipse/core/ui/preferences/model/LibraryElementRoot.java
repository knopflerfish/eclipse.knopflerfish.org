/*
 * Copyright (c) 2003-2011, KNOPFLERFISH project
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
import java.util.List;

import org.knopflerfish.eclipse.core.IOsgiBundle;
import org.knopflerfish.eclipse.core.IOsgiLibrary;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class LibraryElementRoot implements ILibraryTreeElement {
  
  private final LibraryElementRuntimeRoot runtimeElement;
  private final LibraryElementBundleRoot bundleElement;
  
  public LibraryElementRoot() {
    runtimeElement = new LibraryElementRuntimeRoot(this);
    bundleElement = new LibraryElementBundleRoot(this);
  }
  
  public void clear() {
    runtimeElement.clear();
    bundleElement.clear();
  }
  
  public LibraryElementBundleRoot getBundleRoot() {
    return bundleElement; 
   }
  
  public LibraryElementRuntimeRoot getRuntimeRoot() {
    return runtimeElement; 
   }
  
  public IOsgiLibrary[] getRuntimeLibraries() {
    List<IOsgiLibrary> libs = new ArrayList<IOsgiLibrary>();
    ILibraryTreeElement [] children = runtimeElement.getChildren();
    for (int i=0; i<children.length; i++) {
      libs.add( ((LibraryElementRuntime) children[i]).getLibrary());
    }
    return libs.toArray(new IOsgiLibrary[libs.size()]);
  }
  
  public IOsgiBundle[] getBundles(String dir) {
    List<IOsgiBundle> libs = new ArrayList<IOsgiBundle>();
    if (dir == null) {
      LibraryElementBundle [] children = bundleElement.getBundles();
      for (int i=0; i<children.length; i++) {
          libs.add(children[i].getBundle());
      }
    } else if (dir != null) {
      LibraryElementBundleDirectory [] children = bundleElement.getBundleDirectories();
      for (int i=0; i<children.length; i++) {
        if (!dir.equals(children[i].getBundleDirectory())) {
          continue;
        }
        ILibraryTreeElement [] dirChildren = children[i].getChildren();
        for (int j=0; j<dirChildren.length; j++) {
          LibraryElementBundle bundleElem = (LibraryElementBundle) dirChildren[j];
          libs.add(bundleElem.getBundle());
        }
      }
    }
    return libs.toArray(new IOsgiBundle[libs.size()]);
  }
  
  public String[] getBundleDirectories()
  {
    List<String> dirs = new ArrayList<String>();
    LibraryElementBundleDirectory [] children = bundleElement.getBundleDirectories();
    for (int i=0; i<children.length; i++) {
      dirs.add(children[i].getBundleDirectory());
    }
    return dirs.toArray(new String[dirs.size()]);
  }
  
  public LibraryElementBundleDirectory getBundleDirectory(String dir)
  {
    if (dir == null) {
      return null;
    }
    
    LibraryElementBundleDirectory [] children = bundleElement.getBundleDirectories();
    for (int i=0; i<children.length; i++) {
      if (dir.equals(children[i].getBundleDirectory())) {
        return children[i];
      }
    }
    return null;
  }
  
  //***************************************************************************
  // org.knopflerfish.eclipse.core.ui.preferences.model.ILibraryTreeElement methods
  //***************************************************************************
  
  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.ui.preferences.model.ILibraryTreeElement#getChildren()
   */
  public ILibraryTreeElement[] getChildren() {
    return new ILibraryTreeElement[] {
        runtimeElement,
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
