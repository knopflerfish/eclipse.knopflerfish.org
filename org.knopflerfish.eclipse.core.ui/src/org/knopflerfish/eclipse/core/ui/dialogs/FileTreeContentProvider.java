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

package org.knopflerfish.eclipse.core.ui.dialogs;

import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class FileTreeContentProvider implements ITreeContentProvider {

  private IFile[] files;
  
  public FileTreeContentProvider(IFile[] files) {
    this.files = files;
  }

  public Object[] getChildren(Object element) {
    if (element instanceof IContainer) {
      IContainer container = (IContainer) element;
      // Only show children which contain the files
      return getResources(container, files);
    } else {
      return new Object[0];
    }
  }

  public Object getParent(Object element) {
    if (element instanceof IResource) {
      IResource resource = (IResource) element;
      return resource.getParent();
    } else {
      return null;
    }
  }

  public boolean hasChildren(Object element) {
    if (element instanceof IContainer) {
      IContainer container = (IContainer) element;
      // Only show children which contain the files
      return getResources(container, files) != null;
    } else {
      return false;
    }
  }

  public Object[] getElements(Object element) {
    if (element instanceof IContainer) {
      IContainer container = (IContainer) element;
      // Only show children which contain the files
      return getResources(container, files);
    } else {
      return null;
    }
  }

  public void dispose() {
  }

  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
  }

  /****************************************************************************
   * Private methods
   ***************************************************************************/
  
  private IResource[] getResources(IContainer container, IFile[] files) {
    if (files == null || files.length == 0) return new IResource[0];
    
    ArrayList resources = new ArrayList();
    try {
      IResource[] children = container.members();
      for (int i=0; i<children.length;i++) {
        for (int j=0; j<files.length; j++) {
          if (children[i] instanceof IContainer) {
            IContainer c = (IContainer) children[i];
            IPath cp = c.getFullPath();
            IPath fp = files[j].getFullPath();
            if (cp.segmentCount() >= fp.segmentCount()) {
              continue;
            }
            IPath path = fp.removeLastSegments(fp.segmentCount()-cp.segmentCount());
            if (!path.equals(cp)) {
              continue;
            }
            fp = fp.removeFirstSegments(cp.segmentCount());
            if (c.exists(fp)) {
              resources.add(c);
              break;
            }
          } else if (children[i] instanceof IFile) {
            IFile f = (IFile) children[i];
            if (f.equals(files[j])) {
              resources.add(f);
              break;
            }
          }
        }
      }
      
    } catch (CoreException e) {
    }
    
    if (resources.size() > 0) {
      return (IResource[]) resources.toArray(new IResource[resources.size()]);
    } else {
      return null;
    }
  }
}

