package org.knopflerfish.eclipse.core.ui.editors.manifest.form;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.knopflerfish.eclipse.core.manifest.BundleManifest;
import org.knopflerfish.eclipse.core.manifest.NativeCodeClause;
import org.knopflerfish.eclipse.core.project.BundlePackDescription;
import org.knopflerfish.eclipse.core.project.BundleProject;
import org.knopflerfish.eclipse.core.project.BundleResource;
import org.knopflerfish.eclipse.core.ui.UiUtils;
import org.knopflerfish.eclipse.core.ui.editors.BundleDocument;
import org.knopflerfish.eclipse.core.ui.editors.manifest.ManifestUtil;

public class NativeCodeSection extends SectionPart implements IStructuredContentProvider, ITableLabelProvider {
  
  // Section title and description
  private static final String TITLE = 
    "Native Code";
  private static final String DESCRIPTION = 
    "This section declares a bundle’s native code libraries.";
  
  // SWT Widgets
  private Button    wNativeCodeUpButton;
  private Button    wNativeCodeDownButton;
  private Button    wNativeCodeRemoveButton;
  private Button    wNativeCodeAddButton;
  
  // jFace Widgets 
  private TableViewer   wNativeCodeTableViewer;
  
  // Model objects
  private BundleManifest manifest = null;
  private final BundleProject project;
  
  public NativeCodeSection(Composite parent, FormToolkit toolkit, int style, BundleProject project) {
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
   * @see org.eclipse.ui.forms.IFormPart#dispose()
   */
  public void dispose() {
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
    
    // Refresh viewer
    if (wNativeCodeTableViewer != null) {
      wNativeCodeTableViewer.setInput(manifest);
    }
    UiUtils.packTableColumns(wNativeCodeTableViewer.getTable());
    
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
    data.horizontalSpan = 2;
    section.setLayoutData(data);
    
    // Create section client
    Composite container = toolkit.createComposite(section);
    TableWrapLayout layout = new TableWrapLayout();
    layout.numColumns = 2;
    container.setLayout(layout);
    
    // Create widgets
    Table wClassPathTable = toolkit.createTable(container, SWT.MULTI | SWT.FULL_SELECTION);
    wClassPathTable.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
    
    wNativeCodeTableViewer = new TableViewer(wClassPathTable);
    wNativeCodeTableViewer.setContentProvider(this);
    wNativeCodeTableViewer.setLabelProvider(this);
    wNativeCodeTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        updateButtons();
      }
    });
    TableColumn pathsCol = new TableColumn(wClassPathTable, SWT.LEFT);
    pathsCol.setText("Paths");
    TableColumn processorCol = new TableColumn(wClassPathTable, SWT.LEFT);
    processorCol.setText("Processor");
    TableColumn osNameCol = new TableColumn(wClassPathTable, SWT.LEFT);
    osNameCol.setText("OS Name");
    TableColumn osVersionCol = new TableColumn(wClassPathTable, SWT.LEFT);
    osVersionCol.setText("OS Version");
    TableColumn languageCol = new TableColumn(wClassPathTable, SWT.LEFT);
    languageCol.setText("Language");
    
    wClassPathTable.setHeaderVisible(true);
    wClassPathTable.setLinesVisible(true);
    
    TableWrapData wd = new TableWrapData();
    wd.rowspan = 4;
    wd.grabHorizontal = true;
    wd.grabVertical = true;
    wd.align = TableWrapData.FILL;
    wd.valign = TableWrapData.FILL;
    //wd.heightHint = UiUtils.convertHeightInCharsToPixels(wClassPathTable, NUM_TABLE_ROWS);
    wClassPathTable.setLayoutData(wd);
    
    wNativeCodeUpButton = toolkit.createButton(container, "Up", SWT.PUSH);
    wNativeCodeUpButton.setEnabled(false);
    wNativeCodeUpButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = 
          (IStructuredSelection) wNativeCodeTableViewer.getSelection();
        
        if (selection == null || selection.isEmpty()) return;
    
      }
    });
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wNativeCodeUpButton.setLayoutData(wd);
    
    wNativeCodeDownButton = toolkit.createButton(container, "Down", SWT.PUSH);
    wNativeCodeDownButton.setEnabled(false);
    wNativeCodeDownButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = 
          (IStructuredSelection) wNativeCodeTableViewer.getSelection();
        
        if (selection == null || selection.isEmpty()) return;
        
      }
    });
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wNativeCodeDownButton.setLayoutData(wd);
    
    wNativeCodeRemoveButton = toolkit.createButton(container, "Remove", SWT.PUSH);
    wNativeCodeRemoveButton.setEnabled(false);
    wNativeCodeRemoveButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        IStructuredSelection selection = 
          (IStructuredSelection) wNativeCodeTableViewer.getSelection();
        
        if (selection == null || selection.isEmpty()) return;
        
      }
    });
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wNativeCodeRemoveButton.setLayoutData(wd);
    
    wNativeCodeAddButton = toolkit.createButton(container, "Add...", SWT.PUSH);
    wNativeCodeAddButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
      }
    });
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wNativeCodeAddButton.setLayoutData(wd);
    
    toolkit.paintBordersFor(container);
    section.setClient(container);
  }
  
  private void updateButtons() {
    IStructuredSelection selection = 
      (IStructuredSelection) wNativeCodeTableViewer.getSelection();
    
    // Enable/disable remove button
    boolean enableRemove = false;
    boolean enableUp = false;
    boolean enableDown = false;
    if (selection != null && !selection.isEmpty()) {
      enableRemove = true;
      if (selection.size() == 1) {
        int idx = wNativeCodeTableViewer.getTable().getSelectionIndex();
        enableUp = idx > 0;
        enableDown = idx < wNativeCodeTableViewer.getTable().getItemCount()-1;
      }
    }
    wNativeCodeUpButton.setEnabled(enableUp);
    wNativeCodeDownButton.setEnabled(enableDown);
    wNativeCodeRemoveButton.setEnabled(enableRemove);
  }
  
  /****************************************************************************
   * org.eclipse.jface.viewers.IStructuredContentProvider methods
   ***************************************************************************/
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  public Object[] getElements(Object inputElement) {
    if ( !(inputElement instanceof BundleManifest)) return null;
    
    BundleManifest manifest = (BundleManifest) inputElement; 
    NativeCodeClause[] clauses = manifest.getNativeCodeClauses();
    
    return clauses;
  }
  
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
   */
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
  }
  
  /****************************************************************************
   * org.eclipse.jface.viewers.ITableLabelProvider methods
   ***************************************************************************/
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
   */
  public Image getColumnImage(Object element, int columnIndex) {
    //NativeCodeClause clause = (NativeCodeClause) element;
    
    return null;
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
   */
  public String getColumnText(Object element, int columnIndex) {
    NativeCodeClause clause = (NativeCodeClause) element;
    
    switch(columnIndex) {
    case 0:
      // Paths
      return toString(clause.getNativePaths());
    case 1:
      // Processor defs
      return toString(clause.getProcessorDefs());
    case 2:
      // OS name defs
      return toString(clause.getOSNameDefs());
    case 3:
      // OS version defs
      return toString(clause.getOSVersionDefs());
    case 4:
      // Language defs
      return toString(clause.getLanguageDefs());
    }
    return null;
  }
  
  /****************************************************************************
   * org.eclipse.jface.viewers.IBaseLabelProvider methods
   ***************************************************************************/
  
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
    BundleDocument buildDoc = (BundleDocument) getManagedForm().getInput();
    BundleManifest manifest = new BundleManifest(ManifestUtil.createManifest(buildDoc.getManifestDocument()));
    
    return manifest;
  }

  private void setManifest(BundleManifest manifest) {
    // Flush values to document
    IManagedForm managedForm = getManagedForm();
    BundleDocument doc = (BundleDocument) managedForm.getInput();
    
    if (manifest == null) return;
    
    try {
      String value = manifest.getAttribute(BundleManifest.BUNDLE_NATIVECODE);
      if (value == null) value = "";
      ManifestUtil.setManifestAttribute(doc.getManifestDocument(), BundleManifest.BUNDLE_NATIVECODE, value);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private void removeClasspathResource(String name) {
    // Refresh values from document
    BundlePackDescription bundlePackDescription = getBundlePackDescription();
    Map contents = bundlePackDescription.getContentsMap(false);
    bundlePackDescription.removeResource((IPath) contents.get(name));
    setBundlePackDescription(bundlePackDescription);
  }
  
  private void addClasspathResource(IPath path) {
    if (path == null) return;
    BundlePackDescription bundlePackDescription = getBundlePackDescription();
    bundlePackDescription.removeResource(path);
    BundleResource resource = new BundleResource(
        BundleResource.TYPE_CLASSPATH,
        path,
        path.removeFirstSegments(1).toString(),
        null);
    bundlePackDescription.addResource(resource);
    setBundlePackDescription(bundlePackDescription);
  }
  
  private String toString(String[] strs) {
    if (strs == null) return null;
    
    StringBuffer buf = new StringBuffer();
    for(int i=0; i<strs.length;i++) {
      if (i > 0) {
        if (i == strs.length-1) {
          buf.append(" or ");
        } else {
          buf.append(", ");
        }
      }
      buf.append(strs[i]);
    }
    return buf.toString();
  }
}
