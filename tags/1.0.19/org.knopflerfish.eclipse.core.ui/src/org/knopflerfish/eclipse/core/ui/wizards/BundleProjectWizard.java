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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.knopflerfish.eclipse.core.Osgi;
import org.knopflerfish.eclipse.core.manifest.BundleManifest;
import org.knopflerfish.eclipse.core.manifest.PackageDescription;
import org.knopflerfish.eclipse.core.project.BuildPath;
import org.knopflerfish.eclipse.core.project.BundleProject;
import org.knopflerfish.eclipse.core.project.classpath.ExecutionEnvironmentContainer;
import org.knopflerfish.eclipse.core.project.classpath.FrameworkContainer;
import org.knopflerfish.eclipse.core.ui.OsgiUiPlugin;

/**
 * @author Anders Rim�n, Gatespace Telematics
 * @author Mats-Ola Persson, Gatespace Telematics
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
    IWorkspaceRunnable action = new IWorkspaceRunnable() {
      public void run(IProgressMonitor monitor) throws CoreException {
        try {
          doFinish(monitor);
        } finally {
          monitor.done();
        }
      }
    };
    
    try {
      ResourcesPlugin.getWorkspace().run(action, null);
      return true;
    } catch (Exception e) {
      MessageDialog.openError(getShell(), "Error", e.getMessage());
      return false;
    }
  }
  
  /****************************************************************************
   * Private worker methods
   ***************************************************************************/
  
  void doFinish(IProgressMonitor monitor) throws CoreException {
    // Create project
    String name = projectPage.getProjectName();
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IProject project = root.getProject(name);
    if (project.exists()) {
      OsgiUiPlugin.throwCoreException("Project \"" + name + "\" already exists.", null);
    }
    
    // Create project description
    IProjectDescription projectDescription = ResourcesPlugin.getWorkspace().newProjectDescription(name);
    
    // Set project location
    projectDescription.setLocation(projectPage.getProjectLocation());
    
    // Create project
    project.create(projectDescription, null);
    
    // Open project
    project.open(null);
    
    // Check if source folder exists otherwise create it
    IFolder srcFolder = project.getFolder(projectPage.getSourceFolder());
    if (!srcFolder.exists()) {
      // Create source folder
      srcFolder.create(IResource.NONE, true, null);
    }
    
    // Set Java project natures
    projectDescription = project.getDescription();
    projectDescription.setNatureIds(new String[] {Osgi.NATURE_ID, JavaCore.NATURE_ID});
    project.setDescription(projectDescription, null);
    
    // Create java project and set classpath and output location
    IJavaProject javaProject = JavaCore.create(project);

    // Create bundle project and add manifest and activator
    BundleProject bundleProject = new BundleProject(javaProject);

    // Set manifest attributes
    BundleManifest manifest = bundleProject.getBundleManifest();
    manifest.setSymbolicName(bundlePage.getBundleSymbolicName());
    manifest.setName(bundlePage.getBundleName());
    manifest.setVersion(bundlePage.getBundleVersion());
    manifest.setVendor(bundlePage.getBundleVendor());
    manifest.setDescription(bundlePage.getBundleDescription());
    if (!"".equals(bundlePage.getBundleManifestVersion())) {
      manifest.setManifestVersion(bundlePage.getBundleManifestVersion());
    }
    
    if (bundlePage.isCreateBundleActivator()) {
      String activator = bundlePage.getActivatorClassName();
      String packageName = bundlePage.getActivatorPackageName();
      if (packageName != null) {
        activator = packageName+"."+activator;
      }
      manifest.setActivator(activator);
    }
    bundleProject.setBundleManifest(manifest);
    
    // Create classpath
    ArrayList classPath = new ArrayList();
    
    // Source folder
    Path projectFolder = new Path("/"+project.getName());
    IPath sourcePath = projectFolder.append(projectPage.getSourceFolder());
    IPath[] inclusionPatterns = new IPath [] {
        new Path("**/*.java")
    };
    // Exclude subversion and CVS directories
    IPath[] exclusionPatterns = new IPath [] {
        new Path("**/.svn/**"),
        new Path("**/CVS/**")
    };
    
    IClasspathEntry sourceEntry = JavaCore.newSourceEntry(
        sourcePath,
        inclusionPatterns,
        exclusionPatterns,
        null);
    classPath.add(sourceEntry);
    
    // Execution environment container
    IPath containerPath = new Path(ExecutionEnvironmentContainer.CONTAINER_PATH);
    if (!projectPage.isDefaultEnvironment()) {
      containerPath = containerPath.append(projectPage.getEnvironmentName());
    }
    classPath.add(JavaCore.newContainerEntry(containerPath));
    
    // Framework container
    containerPath = new Path(FrameworkContainer.CONTAINER_PATH);
    if (!projectPage.isDefaultFramework()) {
      containerPath = containerPath.append(projectPage.getFrameworkName());
    }
    IAccessRule rule = JavaCore.newAccessRule(new Path("**/*"), IAccessRule.K_NON_ACCESSIBLE);
    IClasspathEntry frameworkContainer = JavaCore.newContainerEntry(
        containerPath,
        new IAccessRule[] {rule},
        new IClasspathAttribute[] {},
        false
        );
    classPath.add(frameworkContainer);
    
    // Set classpath and output location
    javaProject.setRawClasspath(
        (IClasspathEntry []) classPath.toArray(new IClasspathEntry[classPath.size()]),
        projectFolder.append(projectPage.getOutputFolder()),
        null);
    
    // Check if bundle activator shall be created
    if (bundlePage.isCreateBundleActivator()) {
      String className = bundlePage.getActivatorClassName();
      String packageName = bundlePage.getActivatorPackageName();
      createActivator(
          bundleProject.getJavaProject(), 
          bundleProject.getJavaProject().getProject().getFolder(projectPage.getSourceFolder()),
          packageName, 
          className);
      IPath path = new Path(FrameworkContainer.CONTAINER_PATH);
      PackageDescription pd = new PackageDescription("org.osgi.framework", null);
      BuildPath bp = new BuildPath(path, pd, null, "Framework");
      bundleProject.addBuildPath(bp, true);
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