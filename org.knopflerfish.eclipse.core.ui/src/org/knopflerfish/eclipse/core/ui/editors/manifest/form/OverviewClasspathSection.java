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

package org.knopflerfish.eclipse.core.ui.editors.manifest.form;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.knopflerfish.eclipse.core.project.BundleManifest;
import org.knopflerfish.eclipse.core.project.BundlePackDescription;
import org.knopflerfish.eclipse.core.project.BundleProject;
import org.knopflerfish.eclipse.core.project.BundleResource;
import org.knopflerfish.eclipse.core.ui.OsgiUiPlugin;
import org.knopflerfish.eclipse.core.ui.UiUtils;
import org.knopflerfish.eclipse.core.ui.dialogs.FileTreeSelectionDialog;
import org.knopflerfish.eclipse.core.ui.editors.manifest.ManifestUtil;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class OverviewClasspathSection extends SectionPart {
  
  // Widget properties
  public static final String PROP_DIRTY = "dirty";
  public static final String PROP_NAME  = "name";
  
  
  // Section title and description
  private static final String TITLE = 
    "Bundle Classpath";
  private static final String DESCRIPTION = 
    "This section declares a bundle’s internal classpath using one or more JAR files that are contained in the bundle’s JAR file.";
  
  // SWT Widgets
  private Button    wClassPathUpButton;
  private Button    wClassPathDownButton;
  private Button    wClassPathRemoveButton;
  private Button    wClassPathAddButton;
  
  // jFace Widgets 
  private TableViewer   wClassPathTableViewer;
  
  // Model objects
  private BundleManifest manifest = null;
  private final BundleProject project;
  
  public OverviewClasspathSection(Composite parent, FormToolkit toolkit, int style, BundleProject project) {
    super(parent, toolkit, style);
    
    this.project = project;
    Section section = getSection();
    createClient(section, toolkit);
    section.setDescription(DESCRIPTION);
    section.setText(TITLE);
  }
  
  /****************************************************************************
   * org.eclipse.ui.forms.IFormPart methods
   ***************************************************************************/
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.ui.forms.IFormPart#commit(boolean)
   */
  public void commit(boolean onSave) {
    super.commit(onSave);
    
    // Flush values to document
    IManagedForm managedForm = getManagedForm();
    IDocument doc = (IDocument) managedForm.getInput();
    
    if (manifest == null) return;
    
    Table wClassPathTable = wClassPathTableViewer.getTable();
    try {
      String attribute = (String) wClassPathTable.getData(PROP_NAME);
      if (attribute != null) {
        String value = manifest.getAttribute(BundleManifest.BUNDLE_CLASSPATH);
        if (value == null) value = "";
        ManifestUtil.setManifestAttribute(doc, attribute, value);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.ui.forms.IFormPart#refresh()
   */
  public void refresh() {
    super.refresh();
    // Refresh values from document
    manifest = new BundleManifest(ManifestUtil.createManifest((IDocument) getManagedForm().getInput()));
    if (wClassPathTableViewer != null) {
      wClassPathTableViewer.setInput(manifest);
    }
    UiUtils.packTableColumns(wClassPathTableViewer.getTable());
  }
  
  /****************************************************************************
   * Private helper methods
   ***************************************************************************/
  
  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.ui.editors.ManifestSectionPart#createClient(org.eclipse.ui.forms.widgets.Section, org.eclipse.ui.forms.widgets.FormToolkit)
   */
  public void createClient(Section section, FormToolkit toolkit) {
    
    // Set section layout
    ColumnLayoutData data = new ColumnLayoutData();
    section.setLayoutData(data);
    section.addExpansionListener(new ExpansionAdapter() {
      public void expansionStateChanged(ExpansionEvent e) {
        getManagedForm().getForm().reflow(true);
      }
    });
    
    // Create section client
    Composite container = toolkit.createComposite(section);
    TableWrapLayout layout = new TableWrapLayout();
    layout.numColumns = 2;
    container.setLayout(layout);
    
    // Create widgets
    Table wClassPathTable = toolkit.createTable(container, SWT.MULTI | SWT.FULL_SELECTION);
    wClassPathTable.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
    wClassPathTable.setData(PROP_DIRTY, new Boolean(false));
    wClassPathTable.setData(PROP_NAME, BundleManifest.BUNDLE_CLASSPATH);
    
    wClassPathTableViewer = new TableViewer(wClassPathTable);
    wClassPathTableViewer.setContentProvider(new BundleClassPathContentProvider());
    wClassPathTableViewer.setLabelProvider(new BundleClassPathLabelProvider());
    wClassPathTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = 
          (IStructuredSelection) wClassPathTableViewer.getSelection();
        
        // Enable/disable remove button
        boolean enable = false;
        if (selection != null && !selection.isEmpty()) {
          enable = true;
        }
        wClassPathUpButton.setEnabled(enable && selection.size() == 1);
        wClassPathDownButton.setEnabled(enable && selection.size() == 1);
        wClassPathRemoveButton.setEnabled(enable);
      }
    });
    new TableColumn(wClassPathTable, SWT.LEFT);
    
    TableWrapData wd = new TableWrapData();
    wd.rowspan = 4;
    wd.grabHorizontal = true;
    wd.grabVertical = true;
    wd.align = TableWrapData.FILL;
    wd.valign = TableWrapData.FILL;
    //wd.heightHint = UiUtils.convertHeightInCharsToPixels(wClassPathTable, NUM_TABLE_ROWS);
    wClassPathTable.setLayoutData(wd);
    
    wClassPathUpButton = toolkit.createButton(container, "Up", SWT.PUSH);
    wClassPathUpButton.setEnabled(false);
    wClassPathUpButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = 
          (IStructuredSelection) wClassPathTableViewer.getSelection();
        
        if (selection == null || selection.isEmpty()) return;
        wClassPathTableViewer.refresh();
        markDirty();
      }
    });
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wClassPathUpButton.setLayoutData(wd);
    
    wClassPathDownButton = toolkit.createButton(container, "Down", SWT.PUSH);
    wClassPathDownButton.setEnabled(false);
    wClassPathDownButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = 
          (IStructuredSelection) wClassPathTableViewer.getSelection();
        
        if (selection == null || selection.isEmpty()) return;
        wClassPathTableViewer.refresh();
        markDirty();
      }
    });
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wClassPathDownButton.setLayoutData(wd);
    
    wClassPathRemoveButton = toolkit.createButton(container, "Remove", SWT.PUSH);
    wClassPathRemoveButton.setEnabled(false);
    wClassPathRemoveButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        try {
          IStructuredSelection selection = 
            (IStructuredSelection) wClassPathTableViewer.getSelection();
          
          if (selection == null || selection.isEmpty()) return;
          
          BundlePackDescription packDescription = project.getBundlePackDescription();
          Map map = packDescription.getContentsMap(false);
          
          // Update manifest
          HashMap resources = new HashMap();
          List pathList = new ArrayList(Arrays.asList(manifest.getBundleClassPath()));
          for (Iterator i = selection.iterator(); i.hasNext(); ) {
            String path = ((String) i.next()).trim();
            pathList.remove(path);
            if (path.startsWith("/")) path = path.substring(1);
            
            if (map.containsKey(path)) {
              BundleResource resource = new BundleResource(BundleResource.TYPE_CLASSPATH, (IPath) map.get(path), path, null);
              packDescription.removeResource(resource);
              resources.put(resource.getSource().makeAbsolute().toString(), resource);
            }
          }
          manifest.setBundleClassPath((String[]) pathList.toArray(new String[pathList.size()]));
          project.saveBundlePackDescription(packDescription);
          
          // Update classpath
          // TODO : Move this to when saved
          // project.updateClasspath();
          final IJavaProject javaProject = project.getJavaProject();
          final ArrayList entries = new ArrayList(Arrays.asList(javaProject.getRawClasspath()));
          boolean changed = false;
          for(Iterator i=entries.iterator();i.hasNext();) {
            IClasspathEntry entry = (IClasspathEntry) i.next();
            if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
              String path = entry.getPath().makeAbsolute().toString();
              if (resources.containsKey(path)) {
                i.remove();
                changed = true;
              }
            }
          }
          if (changed) {
            IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
              public void run(IProgressMonitor monitor) throws CoreException {
                javaProject.setRawClasspath(
                    (IClasspathEntry []) entries.toArray(new IClasspathEntry[entries.size()]),
                    null);
              }
            };
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            workspace.run(runnable,null, IWorkspace.AVOID_UPDATE, null);
          }
          wClassPathTableViewer.refresh();
          markDirty();
        } catch (CoreException e) {
          e.printStackTrace();
        }
      }
    });
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wClassPathRemoveButton.setLayoutData(wd);
    
    wClassPathAddButton = toolkit.createButton(container, "Add...", SWT.PUSH);
    wClassPathAddButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        try {
          // Determine which JAR files to include in the selection dialog
          IFile[] files = project.getJars();
          HashMap fileMap = new HashMap();
          for (int i=0; i<files.length; i++) {
            fileMap.put(files[i].getFullPath(), files[i]);
          }
          
          BundlePackDescription packDescription = project.getBundlePackDescription();
          Map map = packDescription.getContentsMap(false);
          String[] classPaths = manifest.getBundleClassPath();
          for (int i=0; i<classPaths.length; i++) {
            String path = classPaths[i];
            if (path.startsWith("/")) path = path.substring(1);
            
            if (map.containsKey(path)) {
              IPath location = (IPath) map.get(path);
              fileMap.remove(location);
            }
          }
          
          ArrayList fileList = new ArrayList(fileMap.values());
          FileTreeSelectionDialog dialog = new FileTreeSelectionDialog(
              Display.getCurrent().getActiveShell(),
              (IFile[]) fileList.toArray(new IFile[fileList.size()]));
          dialog.setInput(project.getJavaProject().getProject());
          dialog.setTitle("Select library");
          dialog.setMessage("Select library to add to bundle classpath.");
          if (dialog.open() == Window.OK && dialog.getResource() != null) {
            BundleResource resource = dialog.getResource();
            System.err.println("Source "+resource.getSource());
            System.err.println("Destination "+resource.getDestination());
            List pathList = new ArrayList(Arrays.asList(classPaths));
            if (!pathList.contains(resource.getDestination())) {
              // Update manifest
              pathList.add(resource.getDestination());
              manifest.setBundleClassPath((String[]) pathList.toArray(new String[pathList.size()]));
              // Update pack description
              packDescription.addResource(resource);
              project.saveBundlePackDescription(packDescription);
              // Update classpath
              IJavaProject javaProject = project.getJavaProject();
              ArrayList entries = new ArrayList(Arrays.asList(javaProject.getRawClasspath()));
              boolean exist = false;
              for(int i=0;i<entries.size();i++) {
                IClasspathEntry entry = (IClasspathEntry) entries.get(i);
                if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
                  IPath path = entry.getPath();
                  if (path.equals(resource.getSource())) {
                    exist = true;
                    break;
                  }
                }
              }
              if (!exist) {
                entries.add(JavaCore.newLibraryEntry(resource.getSource(),null,null));
                javaProject.setRawClasspath(
                    (IClasspathEntry []) entries.toArray(new IClasspathEntry[entries.size()]),
                    null);
              }
              
              wClassPathTableViewer.refresh();
              UiUtils.packTableColumns(wClassPathTableViewer.getTable());
              markDirty();
            }
          }          
        } catch (CoreException e) {
          e.printStackTrace();
        }
      }
    });
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wClassPathAddButton.setLayoutData(wd);
    
    toolkit.paintBordersFor(container);
    section.setClient(container);
  }
  
  /****************************************************************************
   * Inner classes
   ***************************************************************************/
  
  class BundleClassPathContentProvider  implements IStructuredContentProvider {
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement) {
      if ( !(inputElement instanceof BundleManifest)) return null;
      
      BundleManifest manifest = (BundleManifest) inputElement; 
      
      return manifest.getBundleClassPath();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
  }
  
  class BundleClassPathLabelProvider  implements ITableLabelProvider {
    
    private String IMAGE_BUNDLE = "icons/obj16/jar_b_obj.gif";
    private String IMAGE_LINK = "icons/obj16/link_obj.gif";
    private String IMAGE_BUNDLE_OVR  = "icons/ovr16/bundle_ovr.gif";
    
    private Image imageBundle = null;
    private Image imageLink = null;
    private Image imageJar  = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_JAR);
    private Image imageProject = null;
    
    public BundleClassPathLabelProvider() {
      ImageDescriptor id = OsgiUiPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui", IMAGE_BUNDLE);
      if (id != null) {
        imageBundle = id.createImage();
      }
      id = OsgiUiPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui", IMAGE_LINK);
      if (id != null) {
        imageLink = id.createImage();
      }
      id = OsgiUiPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui", IMAGE_BUNDLE_OVR);
      if (id != null) {
        Image bundleOvrImage = id.createImage();
        Image imageWorkspace  = PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT );
        imageProject = new Image(null, imageWorkspace.getBounds());
        GC gc = new GC(imageProject);
        gc.drawImage(imageWorkspace, 0, 0);
        gc.drawImage(bundleOvrImage, imageProject.getBounds().width-bundleOvrImage.getBounds().width, 0);
        gc.dispose();      
        bundleOvrImage.dispose();
      }
    }
    
    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
     */
    public void dispose() {
      if (imageBundle != null) {
        imageBundle.dispose();
        imageBundle = null;
      }
      if (imageLink != null) {
        imageLink.dispose();
        imageLink = null;
      }
      if (imageProject != null) {
        imageProject.dispose();
        imageProject = null;
      }
    }
    
    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
     */
    public Image getColumnImage(Object element, int columnIndex) {
      String path = (String) element;
      
      switch(columnIndex) {
      case 0:
        return imageBundle;
      case 1:
        if (!".".equals(path)) {
          return imageProject;
        }
        break;
      }
      
      return null;
    }
    
    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
     */
    public String getColumnText(Object element, int columnIndex) {
      String path = (String) element;
      
      switch(columnIndex) {
      case 0:
        if (".".equals(path)) {
          return "Bundle’s JAR file"; 
        } else  {
          return element.toString();
        }
      case 1:
        if (!".".equals(path)) {
          return "huh";
        }
      }
      return null;
    }
    
    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void addListener(ILabelProviderListener listener) {
    }
    
    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
     */
    public boolean isLabelProperty(Object element, String property) {
      return false;
    }
    
    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void removeListener(ILabelProviderListener listener) {
    }
  }
}
