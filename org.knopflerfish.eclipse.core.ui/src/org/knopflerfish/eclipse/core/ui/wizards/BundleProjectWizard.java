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

package org.knopflerfish.eclipse.core.ui.wizards;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.knopflerfish.eclipse.core.project.BundleManifest;
import org.knopflerfish.eclipse.core.project.BundleProject;
import org.knopflerfish.eclipse.core.project.OsgiContainerInitializer;
import org.knopflerfish.eclipse.core.ui.OsgiUiPlugin;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class BundleProjectWizard extends Wizard implements INewWizard {
  
  private ProjectWizardPage projectPage;
  private BundleWizardPage bundlePage;
  private ISelection selection;
  
  /****************************************************************************
   * org.eclipse.ui.IWorkbenchWizard methods
   ***************************************************************************/
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
   */
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    this.selection = selection;
    setWindowTitle("New Bundle Project");
    setDefaultPageImageDescriptor(OsgiUiPlugin.BUNDLE_WIZARD_BANNER);
  }
  
  /****************************************************************************
   * org.eclipse.jface.wizard.IWizard methods
   ***************************************************************************/
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.wizard.IWizard#addPages()
   */
  public void addPages() {
    // Project settings wizard Page (e.g. name, src and bin folders)
    projectPage = new ProjectWizardPage(selection);
    addPage(projectPage);
    
    // Bundle settings wizard page (e.g. package, activator and manifest entries)
    bundlePage = new BundleWizardPage(selection, projectPage);
    addPage(bundlePage);
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.wizard.IWizard#performFinish()
   */
  public boolean performFinish() {
    try {
      doFinish();
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
  
  /****************************************************************************
   * Private worker methods
   ***************************************************************************/
  
  private void doFinish() throws CoreException {
    // Create project
    BundleProject project = BundleProject.create(
        projectPage.getProjectName(),
        projectPage.getProjectLocation(),
        projectPage.getSourceFolder(),
        projectPage.getOutputFolder());
    
    // Set manifest attributes
    BundleManifest manifest = project.getBundleManifest();
    manifest.setSymbolicName(bundlePage.getBundleSymbolicName());
    manifest.setName(bundlePage.getBundleName());
    manifest.setVersion(bundlePage.getBundleVersion());
    manifest.setVendor(bundlePage.getBundleVendor());
    manifest.setDescription(bundlePage.getBundleDescription());
    if (bundlePage.isCreateBundleActivator()) {
      String activator = bundlePage.getActivatorClassName();
      String packageName = bundlePage.getActivatorPackageName();
      if (packageName != null) {
        activator = packageName+"."+activator;
      }
      manifest.setActivator(activator);
    }
    project.saveManifest();
    
    // Create classpath
    ArrayList classPath = new ArrayList();
    
    // Source folder
    Path projectFolder = new Path("/"+project.getJavaProject().getProject().getName());
    classPath.add(JavaCore.newSourceEntry(projectFolder.append(projectPage.getSourceFolder())));
    
    // JRE container
    classPath.add(JavaRuntime.getDefaultJREContainerEntry());
    
    // Knopflerfish container
    IPath containerPath = new Path(OsgiContainerInitializer.KF_CONTAINER);
    if (!projectPage.isDefaultProjectLibrary()) {
      containerPath = containerPath.append(projectPage.getEnvironmentName());
    }
    classPath.add(JavaCore.newContainerEntry(containerPath));
    
    // Set classpath
    project.getJavaProject().setRawClasspath((IClasspathEntry []) classPath.toArray(new IClasspathEntry[classPath.size()]), null);
    
    // Check if bundle activator shall be created
    if (bundlePage.isCreateBundleActivator()) {
      String className = bundlePage.getActivatorClassName();
      String packageName = bundlePage.getActivatorPackageName();
      createActivator(
          project.getJavaProject(), 
          project.getJavaProject().getProject().getFolder(projectPage.getSourceFolder()),
          packageName, 
          className);
    }
  }
  
  /**
   * Creates a file containing skeleton for bundle activator. The file 
   * "resources/BundleSkeleton.java" is used as template.
   * 
   * @param project the Java project in which the file shall be created.
   * @param srcFolder the source folder that shall be used.
   * @param packageName package name. If packageName is null then the 
   *  default package is used.
   * @param unitName class name for the activator.
   * @param monitor progress monitor
   */
  private void createActivator(IJavaProject project, IFolder srcFolder, String packageName, String unitName) throws CoreException {
    
    // Read bundle activator template
    InputStream is = null;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      try {
        is = OsgiUiPlugin.getDefault().openStream(new Path("resources/BundleSkeleton.java"));
        
        byte [] buf = new byte[256];
        int numRead = 0;
        while( (numRead = is.read(buf)) != -1) {
          baos.write(buf, 0, numRead);
        }
      } finally {
        if (is != null) is.close();
        baos.flush();
        baos.close();
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
    String skeleton = baos.toString();
    
    // Replace placeholders
    String date = new Date().toString();
    skeleton = skeleton.replaceAll("%DATE%", date);
    String pkg = "";
    if (packageName != null) {
      pkg = "package "+packageName+";";
    }
    skeleton = skeleton.replaceAll("%PACKAGE%", pkg);
    skeleton = skeleton.replaceAll("%BUNDLE_NAME%", unitName);
    
    // Create package fragment
    IPackageFragmentRoot rootFragment = project.findPackageFragmentRoot(srcFolder.getFullPath());
    IPackageFragment fragment = rootFragment.createPackageFragment((packageName == null ? "":packageName), true, null);
    
    // Create compilation unit
    fragment.createCompilationUnit(unitName+".java", skeleton, true, null);
  }
}