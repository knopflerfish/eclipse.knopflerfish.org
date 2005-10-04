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

package org.knopflerfish.eclipse.core.ui.editors.manifest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.knopflerfish.eclipse.core.manifest.BundleManifest;
import org.knopflerfish.eclipse.core.manifest.ManifestUtil;
import org.knopflerfish.eclipse.core.project.BundlePackDescription;
import org.knopflerfish.eclipse.core.project.BundleProject;
import org.knopflerfish.eclipse.core.project.BundleResource;
import org.knopflerfish.eclipse.core.ui.OsgiUiPlugin;
import org.knopflerfish.eclipse.core.ui.SharedImages;
import org.knopflerfish.eclipse.core.ui.UiUtils;
import org.knopflerfish.eclipse.core.ui.dialogs.FileTreeSelectionDialog;
import org.knopflerfish.eclipse.core.ui.editors.BundleDocument;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class ClasspathSection extends SectionPart {
  
  // Widget properties
  public static final String PROP_DIRTY = "dirty";
  public static final String PROP_NAME  = "name";
  
  
  // Section title and description
  private static final String TITLE = 
    "Bundle Classpath";
  private static final String DESCRIPTION = 
    "This section declares a bundle’s internal classpath.";
  
  // SWT Widgets
  private Button    wClassPathUpButton;
  private Button    wClassPathDownButton;
  private Button    wClassPathRemoveButton;
  private Button    wClassPathAddButton;
  
  TableViewer   wClassPathTableViewer;
  
  BundleManifest manifest = null;
  final BundleProject project;
  BundlePackDescription bundlePackDescription = null;
  Map bundleContents;
  
  // Images
  private Image imgJarWarning;
  Image imgJarError;
  
  public ClasspathSection(Composite parent, FormToolkit toolkit, int style, BundleProject project) {
    super(parent, toolkit, style);
    
    this.project = project;
    
    // Create images
    imgJarWarning = UiUtils.ovrImage(
        JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_JAR),
        OsgiUiPlugin.getSharedImages().getImage(SharedImages.IMG_OVR_WARNING),
        UiUtils.LEFT, UiUtils.BOTTOM
        );
    imgJarError = UiUtils.ovrImage(
        JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_JAR),
        OsgiUiPlugin.getSharedImages().getImage(SharedImages.IMG_OVR_ERROR),
        UiUtils.LEFT, UiUtils.BOTTOM
        );
    
    Section section = getSection();
    createClient(section, toolkit);
    section.setDescription(DESCRIPTION);
    section.setText(TITLE);
  }
  
  public void setErrors(List errors) {
    wClassPathTableViewer.refresh();
  }
  
  /****************************************************************************
   * org.eclipse.ui.forms.IFormPart methods
   ***************************************************************************/
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.ui.forms.IFormPart#dispose()
   */
  public void dispose() {
    if (imgJarWarning != null) {
      imgJarWarning.dispose();
      imgJarWarning = null;
    }
    
    if (imgJarError != null) {
      imgJarError.dispose();
      imgJarError = null;
    }
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.ui.forms.IFormPart#commit(boolean)
   */
  public void commit(boolean onSave) {
    setManifest(manifest);
    
    super.commit(onSave);
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.ui.forms.IFormPart#refresh()
   */
  public void refresh() {
    // Refresh values from document
    manifest = getManifest();
    bundlePackDescription = getBundlePackDescription();
    bundleContents = bundlePackDescription.getContentsMap(false);
    
    // Refresh viewer
    if (wClassPathTableViewer != null) {
      wClassPathTableViewer.setInput(manifest);
    }
    UiUtils.packTableColumns(wClassPathTableViewer.getTable());

    super.refresh();
  }
  
  /****************************************************************************
   * Private helper methods
   ***************************************************************************/
  
  /* (non-Javadoc)
   * @see org.knopflerfish.eclipse.core.ui.editors.ManifestSectionPart#createClient(org.eclipse.ui.forms.widgets.Section, org.eclipse.ui.forms.widgets.FormToolkit)
   */
  public void createClient(Section section, FormToolkit toolkit) {
    
    // Set section layout
    GridData data = new GridData(SWT.FILL);
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.FILL;
    data.verticalAlignment = SWT.TOP;
    section.setLayoutData(data);
    
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
    ClasspathViewerProvider provider = new ClasspathViewerProvider();
    wClassPathTableViewer.setContentProvider(provider);
    wClassPathTableViewer.setLabelProvider(provider);
    wClassPathTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        updateButtons();
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
        
        // Update bundle class path
        List paths = new ArrayList(Arrays.asList(manifest.getBundleClassPath()));
        String name = (String) selection.getFirstElement();
        int idx = paths.indexOf(name);
        if (idx > 0) {
          paths.remove(idx);
          paths.add(idx-1, name);
        }
        manifest.setBundleClassPath((String[]) paths.toArray(new String[paths.size()]));
        // Update document
        setManifest(manifest);
        
        wClassPathTableViewer.refresh();
        markDirty();
        updateButtons();
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
        
        // Update bundle class path
        List paths = new ArrayList(Arrays.asList(manifest.getBundleClassPath()));
        String name = (String) selection.getFirstElement();
        int idx = paths.indexOf(name);
        if (idx < wClassPathTableViewer.getTable().getItemCount()-1) {
          paths.remove(idx);
          paths.add(idx+1, name);
        }
        manifest.setBundleClassPath((String[]) paths.toArray(new String[paths.size()]));
        // Update document
        setManifest(manifest);
        
        wClassPathTableViewer.refresh();
        markDirty();
        updateButtons();
      }
    });
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wClassPathDownButton.setLayoutData(wd);
    
    wClassPathRemoveButton = toolkit.createButton(container, "Remove", SWT.PUSH);
    wClassPathRemoveButton.setEnabled(false);
    wClassPathRemoveButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        IStructuredSelection selection = 
          (IStructuredSelection) wClassPathTableViewer.getSelection();
        
        if (selection == null || selection.isEmpty()) return;
        
        // Update bundle class path and pack description
        List paths = new ArrayList(Arrays.asList(manifest.getBundleClassPath()));
        for (Iterator i = selection.iterator(); i.hasNext(); ) {
          String name = ((String) i.next()).trim();
          paths.remove(name);
          removeClasspathResource(name);
        }
        manifest.setBundleClassPath((String[]) paths.toArray(new String[paths.size()]));
        // Update document
        setManifest(manifest);
        wClassPathTableViewer.refresh();
        markDirty();
        updateButtons();
      }
    });
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wClassPathRemoveButton.setLayoutData(wd);
    
    wClassPathAddButton = toolkit.createButton(container, "Add...", SWT.PUSH);
    wClassPathAddButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        //try {
        IFile[] libraries = getAvailableLibraries();
        FileTreeSelectionDialog dialog = new FileTreeSelectionDialog(
            Display.getCurrent().getActiveShell(),
            libraries);
        dialog.setInput(project.getJavaProject().getProject());
        dialog.setTitle("Select library");
        dialog.setImage(JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_JAR));

        dialog.setMessage("Select library to add to bundle classpath.");
        if (dialog.open() == Window.OK && dialog.getResource() != null) {
          BundleResource resource = dialog.getResource();
          // Add resource to pack description
          addClasspathResource(resource.getSource());
          Map contents = bundlePackDescription.getContentsMap(true);
          String path = (String) contents.get(resource.getSource()); 
          
          String[] classPaths = manifest.getBundleClassPath();
          List paths = new ArrayList(Arrays.asList(classPaths));
          if (!paths.contains(path)) {
            // Update manifest
            if (paths.size()== 0) {
              paths.add(".");
            }
            paths.add(path);
            manifest.setBundleClassPath((String[]) paths.toArray(new String[paths.size()]));
            // Update document
            setManifest(manifest);
            wClassPathTableViewer.refresh();
            UiUtils.packTableColumns(wClassPathTableViewer.getTable());
            markDirty();
            updateButtons();
          }
        }
      }
    });
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wClassPathAddButton.setLayoutData(wd);
    
    toolkit.paintBordersFor(container);
    section.setClient(container);
  }
  
  void updateButtons() {
    IStructuredSelection selection = 
      (IStructuredSelection) wClassPathTableViewer.getSelection();
    
    // Enable/disable remove button
    boolean enableRemove = false;
    boolean enableUp = false;
    boolean enableDown = false;
    if (selection != null && !selection.isEmpty()) {
      enableRemove = true;
      if (selection.size() == 1) {
        String s = (String) selection.getFirstElement();
        if (".".equals(s) && wClassPathTableViewer.getTable().getItemCount() <= 1) {
          enableRemove = false;
        }
        
        int idx = wClassPathTableViewer.getTable().getSelectionIndex();
        enableUp = idx > 0;
        enableDown = idx < wClassPathTableViewer.getTable().getItemCount()-1;
      }
    }
    wClassPathUpButton.setEnabled(enableUp);
    wClassPathDownButton.setEnabled(enableDown);
    wClassPathRemoveButton.setEnabled(enableRemove);
  }
  
  /****************************************************************************
   * Private utility methods
   ***************************************************************************/

  private BundlePackDescription getBundlePackDescription() {
    try {
      BundleDocument buildDoc = (BundleDocument) getManagedForm().getInput();
      BundlePackDescription bundlePackDescription = new BundlePackDescription(
          project.getJavaProject().getProject(), 
          new ByteArrayInputStream(buildDoc.getPackDocument().get().getBytes()));
      
      return bundlePackDescription;
    } catch (Throwable t) {
      return new BundlePackDescription(project.getJavaProject().getProject());
    }
  }

  private void setBundlePackDescription(BundlePackDescription bundlePackDescription) {
    try {
      BundleDocument buildDoc = (BundleDocument) getManagedForm().getInput();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      bundlePackDescription.save(baos);
      baos.flush();
      buildDoc.getPackDocument().set(baos.toString());
    } catch (Throwable t) {
    }
  }

  private BundleManifest getManifest() {
    IDocument doc = ((BundleDocument) getManagedForm().getInput()).getManifestDocument();
    BundleManifest manifest = ManifestUtil.createManifest(doc.get().getBytes());
    
    return manifest;
  }

  void setManifest(BundleManifest manifest) {
    // Flush values to document
    IDocument doc = ((BundleDocument) getManagedForm().getInput()).getManifestDocument();
    
    if (manifest == null) return;
    
    StringBuffer buf = new StringBuffer(doc.get());
    ManifestUtil.setManifestAttribute(buf, 
        BundleManifest.BUNDLE_CLASSPATH, manifest.getAttribute(BundleManifest.BUNDLE_CLASSPATH));
    doc.set(buf.toString());
  }
  
  void removeClasspathResource(String name) {
    // Refresh values from document
    Map contents = bundlePackDescription.getContentsMap(false);
    bundlePackDescription.removeResource((IPath) contents.get(name));
    bundleContents = bundlePackDescription.getContentsMap(false);
    setBundlePackDescription(bundlePackDescription);
  }
  
  void addClasspathResource(IPath path) {
    if (path == null) return;
    bundlePackDescription.removeResource(path);
    BundleResource resource = new BundleResource(
        BundleResource.TYPE_CLASSPATH,
        path,
        path.removeFirstSegments(1).toString(),
        null);
    bundlePackDescription.addResource(resource);
    bundleContents = bundlePackDescription.getContentsMap(false);
    setBundlePackDescription(bundlePackDescription);
  }
  
  IFile[] getAvailableLibraries() {
    IFile[] files = project.getJars();
    HashMap fileMap = new HashMap();
    for (int i=0; i<files.length; i++) {
      fileMap.put(files[i].getFullPath(), files[i]);
    }
    
    String[] classPaths = manifest.getBundleClassPath();
    for (int i=0; i<classPaths.length; i++) {
      String path = classPaths[i];
      if (path.startsWith("/")) path = path.substring(1);
      
      if (bundleContents.containsKey(path)) {
        IPath location = (IPath) bundleContents.get(path);
        fileMap.remove(location);
      }
    }
    
    return (IFile []) fileMap.values().toArray(new IFile[fileMap.size()]);
  }
  
  /****************************************************************************
   * ClasspathViewerProvider Inner classes
   ***************************************************************************/
  class ClasspathViewerProvider implements IStructuredContentProvider, ITableLabelProvider, ITableColorProvider {

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement) {
      if ( !(inputElement instanceof BundleManifest)) return null;
      
      BundleManifest manifest = (BundleManifest) inputElement; 
      
      String [] classPath = manifest.getBundleClassPath();
      if (classPath.length == 0) {
        return new String[] {"."};
      }
      return manifest.getBundleClassPath();
    }

    public void dispose() {
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
    
    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
     */
    public Image getColumnImage(Object element, int columnIndex) {
      String path = (String) element;
      
      if (".".equals(path)) {
        return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS);
      }
      if (path.startsWith("/")) path = path.substring(1);
        
      if (bundleContents.containsKey(path)) {
        return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_JAR);
      }
      return imgJarError;
    }
    
    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
     */
    public String getColumnText(Object element, int columnIndex) {
      String path = (String) element;
      
      if (".".equals(path)) {
        return "Bundle´s classes"; 
      }
      return element.toString();
    }
    

    public void addListener(ILabelProviderListener listener) {
    }

    public boolean isLabelProperty(Object element, String property) {
      return false;
    }

    public void removeListener(ILabelProviderListener listener) {
    }

    public Color getForeground(Object element, int columnIndex) {
      String path = (String) element;
      
      if (".".equals(path)) {
        return null;
      }
      if (path.startsWith("/")) path = path.substring(1);
        
      if (bundleContents.containsKey(path)) {
        return null;
      }
      return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
    }

    public Color getBackground(Object element, int columnIndex) {
      return null;
    }
  }
}
