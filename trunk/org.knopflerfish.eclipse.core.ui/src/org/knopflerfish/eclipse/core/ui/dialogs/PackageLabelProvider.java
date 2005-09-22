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

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.knopflerfish.eclipse.core.manifest.PackageDescription;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class PackageLabelProvider implements ILabelProvider {

  /****************************************************************************
   * org.eclipse.ui.forms.IFormPart methods
   ***************************************************************************/
  public Image getImage(Object element) {
    return JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_PACKAGE);
  }

  public String getText(Object element) {
    PackageDescription pkgDescription = (PackageDescription) element;
    return pkgDescription.getPackageName();
    /*
    if (element instanceof PackageDescription) {
    } else if (element instanceof IPackage) {
      IPackage pkg = (IPackage) element;
      StringBuffer buf = new StringBuffer(pkg.getPackageDescription().getPackageName());
      if (pkg instanceof FrameworkPackage) {
        FrameworkPackage fwPkg = (FrameworkPackage) pkg;
        buf.append(" [Framework ");
        buf.append(fwPkg.getFramework().getType());
        buf.append("]");
      } else if (pkg instanceof BundlePackage) {
        BundlePackage bundlePkg = (BundlePackage) pkg;
        buf.append(" [Bundle ");
        buf.append(bundlePkg.getBundle().getName());
        buf.append("]");
      } else if (pkg instanceof ProjectPackage) {
        ProjectPackage projectPkg = (ProjectPackage) pkg;
        buf.append(" [Project ");
        buf.append(projectPkg.getProject().getJavaProject().getProject().getName());
        buf.append("]");
      }
      return buf.toString();
    } else {
      return "";
    }
    */
  }

  public void addListener(ILabelProviderListener listener) {
  }

  public void dispose() {
  }

  public boolean isLabelProperty(Object element, String property) {
    return false;
  }

  public void removeListener(ILabelProviderListener listener) {
  }
}
