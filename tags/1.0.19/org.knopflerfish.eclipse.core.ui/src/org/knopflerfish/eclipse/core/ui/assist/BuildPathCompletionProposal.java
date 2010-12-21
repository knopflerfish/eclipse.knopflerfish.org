/*
 * Copyright (c) 2003-2010, KNOPFLERFISH project
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.knopflerfish.eclipse.core.project.BuildPath;
import org.knopflerfish.eclipse.core.project.BundleProject;
import org.knopflerfish.eclipse.core.project.classpath.FrameworkContainer;
import org.knopflerfish.eclipse.core.ui.OsgiUiPlugin;
import org.osgi.framework.Version;

/**
 * @author Anders Rimén, Makewave
 * @see http://www.makewave.com/
 */
public class BuildPathCompletionProposal implements IJavaCompletionProposal {

  private final BundleProject bundleProject;
  private final BuildPath buildPath;

  public BuildPathCompletionProposal(BundleProject bundleProject,
      BuildPath buildPath)
  {
    this.bundleProject = bundleProject;
    this.buildPath = buildPath;
    this.buildPath.getPackageDescription().setVersion(Version.emptyVersion);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jdt.ui.text.java.IJavaCompletionProposal#getRelevance()
   */
  public int getRelevance()
  {
    if (buildPath.getContainerPath().toString()
        .equals(FrameworkContainer.CONTAINER_PATH)) {
      return 20;
    }
    return 10;
  }

  /****************************************************************************
   * org.eclipse.jface.text.contentassist.ICompletionProposal methods
   ***************************************************************************/

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.jface.text.contentassist.ICompletionProposal#apply(org.eclipse
   * .jface.text.IDocument)
   */
  public void apply(IDocument document)
  {
    try {
      bundleProject.addBuildPath(buildPath, true);
    } catch (CoreException e) {
      OsgiUiPlugin.log(e.getStatus());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.jface.text.contentassist.ICompletionProposal#getSelection(org
   * .eclipse.jface.text.IDocument)
   */
  public Point getSelection(IDocument document)
  {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.contentassist.ICompletionProposal#
   * getAdditionalProposalInfo()
   */
  public String getAdditionalProposalInfo()
  {
    StringBuffer info = new StringBuffer();
    info.append("The package ");
    info.append(buildPath.getPackageDescription().getPackageName());
    info.append(" will be added to this projects classpath and also added to the");
    info.append(" Import-Package header in the bundle manifest");
    return info.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.jface.text.contentassist.ICompletionProposal#getDisplayString()
   */
  public String getDisplayString()
  {
    StringBuffer info = new StringBuffer("Import package ");
    info.append(buildPath.getPackageDescription().getPackageName());
    if (buildPath.getContainerPath().toString()
        .equals(FrameworkContainer.CONTAINER_PATH)) {
      info.append(" from framework");
    } else {
      info.append(" from bundle ");
      info.append(buildPath.getBundleIdentity().getSymbolicName()
          .getSymbolicName());
    }
    return info.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getImage()
   */
  public Image getImage()
  {
    return JavaUI.getSharedImages().getImage(
        ISharedImages.IMG_OBJS_EXTERNAL_ARCHIVE);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.jface.text.contentassist.ICompletionProposal#getContextInformation
   * ()
   */
  public IContextInformation getContextInformation()
  {
    return null;
  }

}
