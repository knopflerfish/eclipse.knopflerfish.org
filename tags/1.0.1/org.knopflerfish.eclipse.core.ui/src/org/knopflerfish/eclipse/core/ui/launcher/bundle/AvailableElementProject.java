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

package org.knopflerfish.eclipse.core.ui.launcher.bundle;

import java.util.ArrayList;

import org.knopflerfish.eclipse.core.project.IBundleProject;
import org.osgi.framework.Version;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class AvailableElementProject implements IAvailableTreeElement {

  private final IAvailableTreeElement parent;
  private final ArrayList children = new ArrayList();
  private final IBundleProject project;

  AvailableElementProject(IAvailableTreeElement parent,  IBundleProject project) {
    this.parent = parent;
    this.project = project;
  }

  /****************************************************************************
   * org.knopflerfish.eclipse.core.ui.launcher.IAvailableTreeElement methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.ui.launcher.IAvailableTreeElement#getParent()
   */
  public IAvailableTreeElement getParent() {
    return parent;
  }
  
  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.ui.launcher.IAvailableTreeElement#getChildren()
   */
  public IAvailableTreeElement[] getChildren() {
    return (IAvailableTreeElement[]) children.toArray(new IAvailableTreeElement[children.size()]);
  }
  
  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.ui.launcher.IAvailableTreeElement#hasChildren()
   */
  public boolean hasChildren() {
    return (children.size() > 0);
  }
  
  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.ui.launcher.IAvailableTreeElement#getType()
   */
  public int getType() {
    return TYPE_PROJECT;
  }
  
  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.ui.launcher.IAvailableTreeElement#getName()
   */
  public String getName() {
    return project.getBundleManifest().getName();
  }
  
  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.ui.launcher.IAvailableTreeElement#getVersion()
   */
  public Version getVersion() {
    return project.getBundleManifest().getVersion();
  }
  
  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.ui.launcher.IAvailableTreeElement#getPath()
   */
  public String getPath() {
    return project.getJavaProject().getProject().getName();
  }
  
  /*
   *  (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.ui.launcher.IAvailableTreeElement#getData()
   */
  public Object getData() {
    return project;
  }

  /****************************************************************************
   * java.lang.Object methods
   ***************************************************************************/
  /*
   *  (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return getName();
  }
}
