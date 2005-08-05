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

package org.knopflerfish.eclipse.core.ui.editors.jar.form;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
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
import org.knopflerfish.eclipse.core.project.BundleJar;
import org.knopflerfish.eclipse.core.project.BundleJarResource;
import org.knopflerfish.eclipse.core.ui.UiUtils;
import org.knopflerfish.eclipse.core.ui.dialogs.BundleJarResourceDialog;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class ContentsSection extends SectionPart {

  private static final int NUM_TABLE_ROWS    = 5;

  // Widget properties
  public static final String PROP_DIRTY = "dirty";
  public static final String PROP_NAME  = "name";
  
  // Section title and description
  private static final String TITLE = 
    "Contents";
  private static final String DESCRIPTION = 
    "This section lists the contents to be included in bundle JAR-file.";

  // SWT Widgets
  private Button    wResourceAddButton;
  private Button    wResourceRemoveButton;
  
  // jFace Widgets 
  private TableViewer   wResourceTableViewer;
  
  private final IProject project;
  
  // Model objects
  private BundleJar bundleJar = null;

  public ContentsSection(Composite parent, FormToolkit toolkit, int style, IProject project) {
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

    if (bundleJar == null) return;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      bundleJar.save(baos);
      baos.flush();
      doc.set(baos.toString());
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
    IDocument doc = (IDocument) getManagedForm().getInput();
    
    try {
      bundleJar = new BundleJar(project, new ByteArrayInputStream(doc.get().getBytes()));
    } catch (Exception e) {
      e.printStackTrace();
      bundleJar = new BundleJar();
    }
    if (wResourceTableViewer != null) {
      wResourceTableViewer.setInput(bundleJar);
    }
    UiUtils.packTableColumns(wResourceTableViewer.getTable());
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
    Table wResourceTable = toolkit.createTable(container, SWT.MULTI | SWT.FULL_SELECTION);
    wResourceTable.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
    wResourceTable.setData(PROP_DIRTY, new Boolean(false));
    //wResourceTable.setData(PROP_NAME, BundleManifest.BUNDLE_CATEGORY);

    wResourceTableViewer = new TableViewer(wResourceTable);
    wResourceTableViewer.setContentProvider(new ResourceContentProvider());
    wResourceTableViewer.setLabelProvider(new ResourceLabelProvider());
    
    //wCategoryTable.setHeaderVisible(true);
    //wCategoryTable.setLinesVisible(true);
    wResourceTableViewer.addOpenListener(new IOpenListener() {

      public void open(OpenEvent event) {
        IStructuredSelection selection = 
          (IStructuredSelection) event.getSelection();
        
        if (selection.size() == 1) {
          BundleJarResource r = (BundleJarResource) selection.getFirstElement();
          BundleJarResourceDialog dialog =
            new BundleJarResourceDialog(
                Display.getCurrent().getActiveShell(),
                r, 
                "Edit resource");
          if (dialog.open() == Window.OK) {
            BundleJarResource resource = dialog.getResource();
            bundleJar.updateResource(resource);
            wResourceTableViewer.refresh();
            UiUtils.packTableColumns(wResourceTableViewer.getTable());
            markDirty();
          }
        }
      }
    });
    wResourceTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = 
          (IStructuredSelection) wResourceTableViewer.getSelection();
        
        // Enable/disable remove button
        boolean enable = false;
        if (selection != null && !selection.isEmpty()) {
          enable = true;
        }
        wResourceRemoveButton.setEnabled(enable);
      }
    });
    TableColumn wSrcTableColumn = new TableColumn(wResourceTable, SWT.LEFT);
    wSrcTableColumn.setText("Source");
    TableColumn wDstTableColumn = new TableColumn(wResourceTable, SWT.LEFT);
    wDstTableColumn.setText("Destination");
    TableColumn wPatternTableColumn = new TableColumn(wResourceTable, SWT.LEFT);
    wPatternTableColumn.setText("Pattern");
    TableColumn wErrorTableColumn = new TableColumn(wResourceTable, SWT.LEFT);
    wErrorTableColumn.setText("Error");
    
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
        BundleJarResourceDialog dialog =
          new BundleJarResourceDialog(
              Display.getCurrent().getActiveShell(),
              null, 
              "Add resource");
        if (dialog.open() == Window.OK) {
          BundleJarResource resource = dialog.getResource();
          bundleJar.addResource(resource);
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
        for (Iterator i = selection.iterator(); i.hasNext(); ) {
          BundleJarResource resource = (BundleJarResource) i.next();
          bundleJar.removeResource(resource);
        }
        wResourceTableViewer.refresh();
        UiUtils.packTableColumns(wResourceTableViewer.getTable());
        markDirty();
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
   * Inner classes
   ***************************************************************************/

  class ResourceContentProvider  implements IStructuredContentProvider {

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement) {
      if ( !(inputElement instanceof BundleJar)) return null;
        
      BundleJar bundleJar = (BundleJar) inputElement; 
      
      return bundleJar.getResources();
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
  
  class ResourceLabelProvider implements ITableLabelProvider {

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
     */
    public void dispose() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
     */
    public Image getColumnImage(Object o, int columnIndex) {
      BundleJarResource e = (BundleJarResource) o;
      
      if (columnIndex == 0) {
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
      } else {
        return null;
      }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
     */
    public String getColumnText(Object o, int columnIndex) {
      BundleJarResource e = (BundleJarResource) o;

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
  }
  
}
