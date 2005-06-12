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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.knopflerfish.eclipse.core.IOsgiInstall;
import org.knopflerfish.eclipse.core.IOsgiLibrary;
import org.knopflerfish.eclipse.core.Osgi;
import org.knopflerfish.eclipse.core.OsgiBundle;
import org.knopflerfish.eclipse.core.ui.OsgiUiPlugin;

public class BundleProjectWizard extends Wizard implements INewWizard {
  private static String EXTENSION_POINT_BUILDERS = "org.eclipse.core.resources.builders";

  private static final String MANIFEST_FILE = "bundle.manifest";
  
  private ProjectWizardPage projectPage;
  private BundleWizardPage bundlePage;
	private ISelection selection;


	public BundleProjectWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

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
    final BundleProject project = bundlePage.getProject();
    
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(project, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
 			}
		};
    
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}
	
  /****************************************************************************
   * Private worker methods
   ***************************************************************************/
  
	private void doFinish(BundleProject bundleProject, IProgressMonitor monitor) throws CoreException {
		// Create project
    String projectName = bundleProject.getName();
		monitor.beginTask("Creating " + projectName, 5);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IProject project = root.getProject(projectName);
    if (project.exists()) {
      throwCoreException("Project \"" + projectName + "\" already exists.");
    }
    
    // Create project description
    IProjectDescription projectDescription = ResourcesPlugin.getWorkspace().newProjectDescription(projectName);
    
    // Set project location
    projectDescription.setLocation(bundleProject.getLocation());
    IPath location = projectDescription.getLocation();

    // Create project
    project.create(projectDescription, monitor);
    monitor.worked(1);
   
    // Open project
    project.open(null);
    //monitor.worked(1);

    // Set project natures
    projectDescription = project.getDescription();
    projectDescription.setNatureIds(new String[] {Osgi.NATURE_ID, JavaCore.NATURE_ID});
    project.setDescription(projectDescription, monitor);
    
    // Create source folder
    IFolder srcFolder = project.getFolder(bundleProject.getSourceFolder());
    srcFolder.create(true, true, monitor);
    monitor.worked(1);

    Path projectFolder = new Path("/"+projectName);
    // Set default classpath
    IJavaProject javaProject = JavaCore.create(project);
    // Set output location 
    javaProject.setOutputLocation(projectFolder.append(bundleProject.getOutputFolder()), monitor);
    monitor.worked(1);
    
    // Source folder
    IClasspathEntry srcEntry = JavaCore.newSourceEntry(projectFolder.append(bundleProject.getSourceFolder()));
    // Lib folder
    IOsgiInstall osgiInstall = bundleProject.getOsgiInstall();
    IOsgiLibrary [] osgiLibraries = osgiInstall.getLibraries();
    
    IClasspathEntry[] newClasspath = new IClasspathEntry[2+(osgiLibraries != null ? osgiLibraries.length : 0)];
    newClasspath[0] = srcEntry;
    newClasspath[1] = JavaRuntime.getDefaultJREContainerEntry();
    
    if (osgiLibraries != null) {
      for (int i=0; i<osgiLibraries.length; i++) {
        newClasspath[i+2]=  JavaCore.newLibraryEntry(
            new Path(osgiLibraries[i].getPath()), 
            new Path(osgiLibraries[i].getSourceDirectory()),
            null, //no source
            false); //not exported
      }
    }
    // Set build path
    javaProject.setRawClasspath(newClasspath, monitor);
    monitor.worked(1);
    
    
    // Check if bundle activator shall be created
    if (bundleProject.isCreateBundleActivator()) {
      String className = bundleProject.getActivatorClassName();
      String packageName = bundleProject.getActivatorPackageName();
      createActivator(javaProject, srcFolder, packageName, className, monitor);
    }
    
    // Create manifest
    final IFile manifest = createManifest(javaProject, bundleProject, monitor);
    
    // Open manifest
    getShell().getDisplay().asyncExec(new Runnable() {
      public void run() {
        IWorkbenchPage page =
          PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        try {
          IDE.openEditor(page, manifest, true);
        } catch (PartInitException e) {
        }
      }
    });
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
  private void createActivator(IJavaProject project, IFolder srcFolder, String packageName, String unitName, IProgressMonitor monitor) throws CoreException {
    
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
    IPackageFragment fragment = rootFragment.createPackageFragment((packageName == null ? "":packageName), true, monitor);
    
    // Create compilation unit
    ICompilationUnit unit = fragment.createCompilationUnit(unitName+".java", skeleton, true, monitor);
  }
  
  /**
   * Creates a manifest file. The manifest is primed with attributes
   * taken from the BundleProject.
   * 
   * @param project the Java project in which the file shall be created.
   * @param bundleProject project settings used to set attributes in manifest.
   * @param monitor progress monitor
   */
  private IFile createManifest(IJavaProject javaProject, BundleProject bundleProject, IProgressMonitor monitor) throws CoreException {
    IProject project = javaProject.getProject();

    // Create manifest and set attributes
    Manifest manifest = new Manifest();
    Attributes attributes = manifest.getMainAttributes();
    // Manifest version
    attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
    // Bundle Symbolic Name
    String value = bundleProject.getBundleSymbolicName();
    if (value != null && value.trim().length() > 0) {
      attributes.putValue(OsgiBundle.BUNDLE_SYMBOLIC_NAME, value);
    }
    // Bundle Name
    value = bundleProject.getBundleName();
    if (value != null && value.trim().length() > 0) {
      attributes.putValue(OsgiBundle.BUNDLE_NAME, value);
    }
    // Bundle Version
    value = bundleProject.getBundleVersion();
    if (value != null && value.trim().length() > 0) {
      attributes.putValue(OsgiBundle.BUNDLE_VERSION, value);
    }
    // Bundle Vendor
    value = bundleProject.getBundleVendor();
    if (value != null && value.trim().length() > 0) {
      attributes.putValue(OsgiBundle.BUNDLE_VENDOR, value);
    }
    // Bundle Description
    value = bundleProject.getBundleDescription();
    if (value != null && value.trim().length() > 0) {
      attributes.putValue(OsgiBundle.BUNDLE_DESCRIPTION, value);
    }
    
    // Bundle Activator
    if (bundleProject.isCreateBundleActivator()) {
      value = bundleProject.getActivatorClassName();
      String packageName = bundleProject.getActivatorPackageName();
      if (packageName != null) {
        value = packageName+"."+value;
      }
      if (value != null && value.trim().length() > 0) {
        attributes.putValue(OsgiBundle.BUNDLE_ACTIVATOR, value);
      }
    }
    
    // Write manifest to file
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ByteArrayInputStream bais = null;
    IFile file = null;
    try {
      try {
        manifest.write(baos);
        baos.flush();
        bais = new ByteArrayInputStream(baos.toByteArray());
        file = project.getFile(MANIFEST_FILE);
        file.create(bais, true, monitor);
      } finally {
        baos.close();
        bais.close();
      }
    } catch (IOException e) {
      throwCoreException("Failed to create manifest file ("+e+")");
    }
    
    return file;
  }
  
	private void throwCoreException(String message) throws CoreException {
		IStatus status =
			new Status(IStatus.ERROR, "org.gstproject.eclipse.osgi.ui", IStatus.OK, message, null);
		throw new CoreException(status);
	}


  public static List getBuilders()  {
    ArrayList builders = new ArrayList();
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint point = registry.getExtensionPoint(EXTENSION_POINT_BUILDERS);
    if (point != null) {
      IExtension[] extensions = point.getExtensions();
      for (int i = 0; i < extensions.length; i++) {
        System.err.println("Builder :"+extensions[i]);
        System.err.println("Simple id:"+extensions[i].getSimpleIdentifier());
        System.err.println("Unique id:"+extensions[i].getUniqueIdentifier());
      }
    }
    
    return builders;
  }
}