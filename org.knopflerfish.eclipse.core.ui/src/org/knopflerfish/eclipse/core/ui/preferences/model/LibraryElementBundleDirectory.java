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

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Anders Rimén, Makewave
 * @see http://www.makewave.com/
 */
public class LibraryElementBundleDirectory implements ILibraryTreeElement {

  private final ILibraryTreeElement parent;
  private String bundleDir;
  private final Set children = new TreeSet(new BundleElementComparator());
  
  public LibraryElementBundleDirectory(ILibraryTreeElement parent, String dir) {
    this.parent = parent;
    bundleDir = dir;
  }
  
  public String getBundleDirectory() {
    return bundleDir;
  }

  public void setBundleDirectory(String dir) {
    this.bundleDir = dir;
  }
  
  public void clear() {
    children.clear();
  }
  
  public void addChild(LibraryElementBundle e) {
    if (e != null && !children.contains(e)) {
      children.add(e);
    }
  }

  public boolean remove(ILibraryTreeElement e) {
    return children.remove(e);
  }
  


  /****************************************************************************
   * org.knopflerfish.eclipse.core.ui.preferences.model.ILibraryTreeElement methods
   ***************************************************************************/
  
  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.ui.preferences.model.ILibraryTreeElement#getChildren()
   */
  public ILibraryTreeElement[] getChildren() {
    return (ILibraryTreeElement[]) children.toArray(new ILibraryTreeElement[children.size()]);
  }

  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.ui.preferences.model.ILibraryTreeElement#getParent()
   */
  public ILibraryTreeElement getParent() {
    return parent;
  }

  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.ui.preferences.model.ILibraryTreeElement#hasChildren()
   */
  public boolean hasChildren() {
    return (children.size() > 0);
  }

  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.ui.preferences.model.ILibraryTreeElement#getType()
   */
  public int getType() {
    return TYPE_BUNDLE_DIR;
  }

  /****************************************************************************
   * java.lang.Object methods
   ***************************************************************************/
  
  /*
   *  (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof LibraryElementBundleDirectory)) return false;
    
    return ((LibraryElementBundleDirectory) obj).getBundleDirectory().equals(getBundleDirectory()); 
  }
  
  class BundleElementComparator implements Comparator {

    public int compare(Object arg0, Object arg1)
    {
      if (arg0 instanceof LibraryElementBundleDirectory && arg1 instanceof LibraryElementBundleDirectory) {
        LibraryElementBundleDirectory dir0 = (LibraryElementBundleDirectory) arg0;
        LibraryElementBundleDirectory dir1 = (LibraryElementBundleDirectory) arg1;
        return dir0.getBundleDirectory().compareTo(dir1.getBundleDirectory());
      } else if (arg0 instanceof LibraryElementBundle && arg1 instanceof LibraryElementBundle) {
        LibraryElementBundle b0 = (LibraryElementBundle) arg0;
        LibraryElementBundle b1 = (LibraryElementBundle) arg1;
        return b0.getBundle().getName().compareTo(b1.getBundle().getName());
      } else if (arg0 instanceof LibraryElementBundle && arg1 instanceof LibraryElementBundleDirectory) {
        return 1;
      } else {
        return -1;
      }
    } 
  }
  

}
