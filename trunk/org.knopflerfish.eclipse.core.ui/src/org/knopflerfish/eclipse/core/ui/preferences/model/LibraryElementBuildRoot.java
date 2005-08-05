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

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class LibraryElementBuildRoot implements ILibraryTreeElement {

  private final ILibraryTreeElement parent;
  private final ArrayList children = new ArrayList();
  
  LibraryElementBuildRoot(ILibraryTreeElement parent) {
    this.parent = parent;
  }
  
  public void clear() {
    children.clear();
  }
  
  public void addChild(LibraryElementBuild e) {
    if (e != null && !children.contains(e)) {
      children.add(e);
    }
  }
  
  public int indexOf(ILibraryTreeElement e) {
    return children.indexOf(e);
  }

  public boolean remove(ILibraryTreeElement e) {
    return children.remove(e);
  }

  public int size() {
    return children.size();
  }
  
  public void moveUp(ILibraryTreeElement e) {
    int idx = indexOf(e);
    if (idx < 1) return;
    
    if (children.remove(e)) {
      children.add(idx-1, e);
    }
  }

  public void moveDown(ILibraryTreeElement e) {
    int idx = indexOf(e);
    if (idx == -1 || idx >= size()-1) return;
    
    if (children.remove(e)) {
      children.add(idx+1, e);
    }
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
    return TYPE_BUILD_ROOT;
  }
}
