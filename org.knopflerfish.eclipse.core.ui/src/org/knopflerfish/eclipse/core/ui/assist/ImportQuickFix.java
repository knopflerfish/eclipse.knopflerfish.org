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

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;
import org.knopflerfish.eclipse.core.IOsgiBundle;
import org.knopflerfish.eclipse.core.PackageDescription;
import org.knopflerfish.eclipse.core.pkg.PackageUtil;
import org.knopflerfish.eclipse.core.project.IBundleProject;

public class ImportQuickFix implements IQuickFixProcessor {

  /* (non-Javadoc)
   * @see org.eclipse.jdt.ui.text.java.IQuickFixProcessor#hasCorrections(org.eclipse.jdt.core.ICompilationUnit, int)
   */
  public boolean hasCorrections(ICompilationUnit unit, int problemId) {
    if (problemId == IProblem.ImportNotFound) {
      return true;
    } else {
      return false;
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.jdt.ui.text.java.IQuickFixProcessor#getCorrections(org.eclipse.jdt.ui.text.java.IInvocationContext, org.eclipse.jdt.ui.text.java.IProblemLocation[])
   */
  public IJavaCompletionProposal[] getCorrections(IInvocationContext context,
      IProblemLocation[] locations) throws CoreException {
    if (locations == null) return null;
    
    IJavaProject project = context.getCompilationUnit().getJavaProject();
    
    ArrayList proposals = new ArrayList();
    for(int i=0; i<locations.length; i++) {
      System.err.println("problemId="+locations[i].getProblemId());
      if (locations[i].getProblemId() != IProblem.ImportNotFound) continue;

      // Import declaration
      ASTNode node = context.getCoveredNode();
      while ( !(node instanceof ImportDeclaration)) {
        node = node.getParent();
      }
      ImportDeclaration importDecl = (ImportDeclaration) node;
      String importLine = importDecl.toString().trim();
      StringTokenizer st = new StringTokenizer(importLine, " ");
      if (st.countTokens() < 2) continue;
      st.nextToken(); // Import statement
      String name = st.nextToken().trim();
      if (name.endsWith(";")) name = name.substring(0, name.length()-1);
      int idx = name.lastIndexOf(".");
      if (idx != -1) {
        name = name.substring(0, idx);
      }
      PackageDescription pkg = new PackageDescription(name, null);
      System.err.println("Missing package :"+pkg.getPackageName());

      IOsgiBundle[] bundles = PackageUtil.findExportingBundles(pkg);
      for (int j=0; bundles != null && j<bundles.length; j++) {
        proposals.add(new BundleImportCompletionProposal(project, pkg, bundles[j]));
      }

      IBundleProject[] projects = PackageUtil.findExportingProjects(pkg);
      for (int j=0; projects != null && j<projects.length; j++) {
        proposals.add(new ProjectImportCompletionProposal(project, pkg, projects[j]));
      }
    }
    
    return (IJavaCompletionProposal[]) proposals.toArray(new IJavaCompletionProposal[proposals.size()]);
  }
}
