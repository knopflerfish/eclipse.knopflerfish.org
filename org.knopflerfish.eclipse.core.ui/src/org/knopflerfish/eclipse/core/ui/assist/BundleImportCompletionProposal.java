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

package org.knopflerfish.eclipse.core.ui.assist;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.knopflerfish.eclipse.core.IOsgiBundle;
import org.knopflerfish.eclipse.core.Osgi;
import org.knopflerfish.eclipse.core.PackageDescription;

/**
 * @author ar
 */
public class BundleImportCompletionProposal implements IJavaCompletionProposal {

  private final PackageDescription pkg;
  private final IOsgiBundle bundle;
  private final IJavaProject project;
  
  public BundleImportCompletionProposal(IJavaProject project, PackageDescription pkg, IOsgiBundle bundle) {
    this.pkg = pkg;
    this.bundle = bundle;
    this.project = project;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.ui.text.java.IJavaCompletionProposal#getRelevance()
   */
  public int getRelevance() {
    return 10;
  }

  /****************************************************************************
   * org.eclipse.jface.text.contentassist.ICompletionProposal methods
   ***************************************************************************/
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.text.contentassist.ICompletionProposal#apply(org.eclipse.jface.text.IDocument)
   */
  public void apply(IDocument document) {
    System.err.println("apply bundle");
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getSelection(org.eclipse.jface.text.IDocument)
   */
  public Point getSelection(IDocument document) {
    System.err.println("getSelection");
    return null;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getAdditionalProposalInfo()
   */
  public String getAdditionalProposalInfo() {
    if (Osgi.isBundleProject(project)) {
      return "Bundle '"+bundle.getName()+
        "' will be added to this projects classpath and "+
        "the package '"+pkg.getPackageName()+"' will be added to the manifest Import-Package";
    } else {
      return "Bundle '"+bundle.getName()+
      "' will be added to this projects classpath.";
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getDisplayString()
   */
  public String getDisplayString() {
    return "Import package '"+pkg.getPackageName()+ "' from bundle '"+bundle.getName()+"'";
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getImage()
   */
  public Image getImage() {
    return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_EXTERNAL_ARCHIVE);
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getContextInformation()
   */
  public IContextInformation getContextInformation() {
    System.err.println("getContextInformation");
    return null;
  }

}
