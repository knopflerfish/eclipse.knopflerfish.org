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

package org.knopflerfish.eclipse.core.ui.editors.packaging.form;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.ui.JavaUI;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.knopflerfish.eclipse.core.manifest.BundleManifest;
import org.knopflerfish.eclipse.core.project.BundlePackDescription;
import org.knopflerfish.eclipse.core.project.BundleResource;
import org.knopflerfish.eclipse.core.ui.UiUtils;
import org.knopflerfish.eclipse.core.ui.dialogs.BundleResourceDialog;
import org.knopflerfish.eclipse.core.ui.editors.BundleDocument;
import org.knopflerfish.eclipse.core.ui.editors.manifest.ManifestUtil;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class ContentsSection extends SectionPart implements ITableLabelProvider, IStructuredContentProvider {
  
  private static final int NUM_TABLE_ROWS = 15;
  
  // Widget properties
  public static final String PROP_DIRTY = "dirty";
  public static final String PROP_NAME  = "name";
  
  // Section title and description
  private static final String TITLE = 
    "Contents";
  private static final String DESCRIPTION = 
    "This section shows the contents of the bundle JAR-file.";
  
  // SWT Widgets
  private Button    wResourceAddButton;
  private Button    wResourceRemoveButton;
  
  // jFace Widgets 
  private TableViewer   wResourceTableViewer;
  
  // Model objects
  private BundlePackDescription bundlePackDescription = null;
  private final IProject project;
  private final PackagingFormEditor editor;
  
  // Graphic Resources
  private Image imageJar  = JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_JAR);
  private Image imageClass = JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_CLASS);
  //private Image imageNative = JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_LIBRARY);
  
  public ContentsSection(Composite parent, FormToolkit toolkit, int style, IProject project, PackagingFormEditor editor) {
    super(parent, toolkit, style);
    
    this.project = project;
    this.editor = editor;
    
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
    // Flush values to document
    setBundlePackDescription(bundlePackDescription);
    
    // Marks section as not dirty
    super.commit(onSave);
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.ui.forms.IFormPart#refresh()
   */
  public void refresh() {
    super.refresh();
    
    // Refresh values from document
    bundlePackDescription = getBundlePackDescription();
    if (wResourceTableViewer != null) {
      wResourceTableViewer.setInput(bundlePackDescription);
    }
    UiUtils.packTableColumns(wResourceTableViewer.getTable());
  }
  
  /****************************************************************************
   * org.eclipse.jface.viewers.ITableLabelProvider methods
   ***************************************************************************/
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
   */
  public Image getColumnImage(Object o, int columnIndex) {
    BundleResource e = (BundleResource) o;
    
    if (columnIndex == 0) {
      switch (e.getType()) {
      case BundleResource.TYPE_CLASSES:
        return imageClass;
      case BundleResource.TYPE_CLASSPATH:
        return imageJar;
      case BundleResource.TYPE_USER:
        IPath src = e.getSource();
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot(); 
        
        IResource res = root.findMember(src);
        
        if (res == null) {
          // Does not exist
          return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
        } else if (res.getType() == IResource.FILE) {
          return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
        } else if (res.getType() == IResource.FOLDER) {
          return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
        } else {
          return null;
        }
      }
      return null;
    } else {
      return null;
    }
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
   */
  public String getColumnText(Object o, int columnIndex) {
    BundleResource e = (BundleResource) o;
    
    switch (columnIndex) {
    case 0:
      // Source
      return e.getSource().toString();
    case 1:
      // Destination
      return e.getDestination() == null ? "":e.getDestination();
    case 2:
      // Destination
      return e.getPattern() == null ? "":e.getPattern().pattern();
    case 3:
      // Error
      IPath src = e.getSource();
      IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot(); 
      
      IResource res = root.findMember(src);
      
      if (res == null) {
        return "Resource does not exist";
      } else {
        return "";
      }
    default:
      return "";
    }
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
   */
  public void addListener(ILabelProviderListener listener) {
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
   */
  public boolean isLabelProperty(Object element, String property) {
    return false;
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
   */
  public void removeListener(ILabelProviderListener listener) {
  }
  
  /****************************************************************************
   * org.eclipse.jface.viewers.IStructuredContentProvider methods
   ***************************************************************************/
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  public Object[] getElements(Object inputElement) {
    if ( !(inputElement instanceof BundlePackDescription)) return null;
    
    BundlePackDescription bundlePackDescription = (BundlePackDescription) inputElement; 
    
    return bundlePackDescription.getResources();
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
   */
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
  }

  /****************************************************************************
   * Private helper methods
   ***************************************************************************/
  
  private void createClient(Section section, FormToolkit toolkit) {
    
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
    Table wResourceTable = toolkit.createTable(container, SWT.MULTI | SWT.FULL_SELECTION);
    wResourceTable.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
    wResourceTable.setData(PROP_DIRTY, new Boolean(false));
    
    wResourceTableViewer = new TableViewer(wResourceTable);
    wResourceTableViewer.setContentProvider(this);
    wResourceTableViewer.setLabelProvider(this);
    wResourceTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = 
          (IStructuredSelection) wResourceTableViewer.getSelection();
        
        // Enable/disable remove button
        boolean enable = false;
        for (Iterator i = selection.iterator(); i.hasNext(); ) {
          BundleResource resource = (BundleResource) i.next();
          if (resource.getType() == BundleResource.TYPE_USER) {
            enable = true;
          }
        }
        wResourceRemoveButton.setEnabled(enable);
      }
    });
    TableColumn wSrcTableColumn = new TableColumn(wResourceTable, SWT.LEFT);
    wSrcTableColumn.setText("Project Path");
    TableColumn wDstTableColumn = new TableColumn(wResourceTable, SWT.LEFT);
    wDstTableColumn.setText("Bundle Path");
    TableColumn wPatternTableColumn = new TableColumn(wResourceTable, SWT.LEFT);
    wPatternTableColumn.setText("Pattern");
    TableColumn wErrorTableColumn = new TableColumn(wResourceTable, SWT.LEFT);
    wErrorTableColumn.setText("Error");
    
    wResourceTable.setHeaderVisible(true);
    wResourceTable.setLinesVisible(true);
    
    TableWrapData wd = new TableWrapData();
    wd.rowspan = 2;
    wd.grabHorizontal = true;
    wd.grabVertical = true;
    wd.align = TableWrapData.FILL;
    wd.heightHint = UiUtils.convertHeightInCharsToPixels(wResourceTable, NUM_TABLE_ROWS);
    wResourceTable.setLayoutData(wd);
    
    // Resource Buttons
    wResourceAddButton = toolkit.createButton(container, "Add...", SWT.PUSH);
    wResourceAddButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        BundleResourceDialog dialog =
          new BundleResourceDialog(Display.getCurrent().getActiveShell(),null,project);
        if (dialog.open() == Window.OK) {
          BundleResource resource = dialog.getResource();
          updateResources(resource, false);

          wResourceTableViewer.refresh();
          UiUtils.packTableColumns(wResourceTableViewer.getTable());
          markDirty();
        }
      }
    });
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wResourceAddButton.setLayoutData(wd);
    
    wResourceRemoveButton = toolkit.createButton(container, "Remove", SWT.PUSH);
    wResourceRemoveButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = 
          (IStructuredSelection) wResourceTableViewer.getSelection();
        
        if (selection == null || selection.isEmpty()) return;
        
        //List categories = new ArrayList(Arrays.asList(manifest.getCategories()));
        boolean changed = false;
        for (Iterator i = selection.iterator(); i.hasNext(); ) {
          BundleResource resource = (BundleResource) i.next();
          if (resource.getType() == BundleResource.TYPE_USER) {
            updateResources(resource, true);
            changed = true;
          }
        }
        if (changed) {
          wResourceTableViewer.refresh();
          UiUtils.packTableColumns(wResourceTableViewer.getTable());
          markDirty();
        }
      }
    });
    wResourceRemoveButton.setEnabled(false);
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wResourceRemoveButton.setLayoutData(wd);
    
    toolkit.paintBordersFor(container);
    section.setClient(container);
  }

  /****************************************************************************
   * Private utility methods
   ***************************************************************************/

  private BundlePackDescription getBundlePackDescription() {
    try {
      BundleDocument buildDoc = (BundleDocument) getManagedForm().getInput();
      BundlePackDescription bundlePackDescription = new BundlePackDescription(
          project, 
          new ByteArrayInputStream(buildDoc.getPackDocument().get().getBytes()));
      
      return bundlePackDescription;
    } catch (Throwable t) {
      return new BundlePackDescription(project);
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

  private void updateResources(BundleResource resource, boolean remove) {
    // Flush values to document
    IManagedForm managedForm = getManagedForm();
    BundleDocument doc = (BundleDocument) managedForm.getInput();

    // Update manifest classpath
    BundleManifest manifest = new BundleManifest(ManifestUtil.createManifest(doc.getManifestDocument()));
    String [] oldClassPath = manifest.getBundleClassPath();
    ArrayList newClassPath = new ArrayList();
    Map oldContents = bundlePackDescription.getContentsMap(false);
    
    if (remove) {
      bundlePackDescription.removeResource(resource);
    } else {
      bundlePackDescription.addResource(resource);
    }
    
    Map newContents = bundlePackDescription.getContentsMap(true);
    for (int i=0; i<oldClassPath.length;i++) {
      if (".".equals(oldClassPath[i])) {
        newClassPath.add(oldClassPath[i]);
      } else {
        IPath path = (IPath) oldContents.get(oldClassPath[i]);
        if (path != null) {
          String newPath = (String) newContents.get(path);
          if (newPath != null) {
            newClassPath.add(newPath);
          }
        }
      }
    }
    manifest.setBundleClassPath((String[]) newClassPath.toArray(new String[newClassPath.size()]));
    
    // Update document
    setBundlePackDescription(bundlePackDescription);

    // Update manifest document
    try {
      String value = manifest.getAttribute(BundleManifest.BUNDLE_CLASSPATH);
      if (value == null) value = "";
      ManifestUtil.setManifestAttribute(
          doc.getManifestDocument(), 
          BundleManifest.BUNDLE_CLASSPATH, 
          value);
    } catch (Throwable t) {
      t.printStackTrace();
    }
    editor.markClasspathStale();
  }
}
