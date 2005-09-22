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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
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
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.knopflerfish.eclipse.core.manifest.BundleManifest;
import org.knopflerfish.eclipse.core.manifest.ManifestUtil;
import org.knopflerfish.eclipse.core.manifest.PackageDescription;
import org.knopflerfish.eclipse.core.pkg.IPackage;
import org.knopflerfish.eclipse.core.pkg.PackageUtil;
import org.knopflerfish.eclipse.core.project.BundleProject;
import org.knopflerfish.eclipse.core.ui.OsgiUiPlugin;
import org.knopflerfish.eclipse.core.ui.SharedImages;
import org.knopflerfish.eclipse.core.ui.UiUtils;
import org.knopflerfish.eclipse.core.ui.dialogs.PackageListSelectionDialog;
import org.knopflerfish.eclipse.core.ui.dialogs.PropertyDialog;
import org.knopflerfish.eclipse.core.ui.editors.BundleDocument;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class PackageSection extends SectionPart {

  private static final int NUM_EXPORT_TABLE_ROWS = 5;
  private static final int NUM_IMPORT_TABLE_ROWS = 5;
  private static final int NUM_DYNAMIC_IMPORT_TABLE_ROWS = 4;

  // Dialog titles
  private static String TITLE_ADD_DYNAMIC_IMPORT = "Add Dynamic Import";
  
  // Column Properties
  public static String PROP_PACKAGE_NAME    = "name";
  public static String PROP_PACKAGE_VERSION = "version";
  public static String NO_VERSION_STR = "[No version]";

  
  // Section title and description
  private static final String TITLE = 
    "Packages";
  private static final String DESCRIPTION = 
    "This section lists packages that are exported and imported by this bundle.";

  // SWT Widgets
  private Button    wExportPackageRemoveButton;
  private Button    wExportPackageAddButton;
  private Button    wImportPackageRemoveButton;
  private Button    wImportPackageAddButton;
  private Button    wDynamicImportPackageRemoveButton;
  private Button    wDynamicImportPackageAddButton;
  
  // jFace Widgets 
  private TableViewer   wExportPackageTableViewer;
  private TableViewer   wImportPackageTableViewer;
  private TableViewer   wDynamicImportPackageTableViewer;
  
  // Model objects
  private BundleManifest manifest = null;
  private final BundleProject project;
  
  // Images 
  private Image imgPackageWarning;
  private Image imgPackageError;
  
  public PackageSection(Composite parent, FormToolkit toolkit, int style, BundleProject project) {
    super(parent, toolkit, style);
    
    // Create images
    imgPackageWarning = UiUtils.ovrImage(
        JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_PACKAGE),
        OsgiUiPlugin.getSharedImages().getImage(SharedImages.IMG_OVR_WARNING),
        UiUtils.LEFT, UiUtils.BOTTOM
        );
    imgPackageError = UiUtils.ovrImage(
        JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_PACKAGE),
        OsgiUiPlugin.getSharedImages().getImage(SharedImages.IMG_OVR_ERROR),
        UiUtils.LEFT, UiUtils.BOTTOM
        );

    this.project = project;
    Section section = getSection();
    createClient(section, toolkit);
    section.setDescription(DESCRIPTION);
    section.setText(TITLE);
  }
  
  public void setErrors(List errors) {
    wExportPackageTableViewer.refresh();
    wDynamicImportPackageTableViewer.refresh();
  }
  
  /****************************************************************************
   * org.eclipse.ui.forms.IFormPart methods
   ***************************************************************************/

  /*
   *  (non-Javadoc)
   * @see org.eclipse.ui.forms.IFormPart#dispose()
   */
  public void dispose() {
    if (imgPackageWarning != null) {
      imgPackageWarning.dispose();
      imgPackageWarning = null;
    }
    
    if (imgPackageError != null) {
      imgPackageError.dispose();
      imgPackageError = null;
    }
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.ui.forms.IFormPart#commit(boolean)
   */
  public void commit(boolean onSave) {
    // Commit values to document
    IDocument doc = ((BundleDocument) getManagedForm().getInput()).getManifestDocument();
    if (manifest == null) return;
    
    StringBuffer buf = new StringBuffer(doc.get());
    ManifestUtil.setManifestAttribute(buf, 
        BundleManifest.EXPORT_PACKAGE, manifest.getAttribute(BundleManifest.EXPORT_PACKAGE));
    ManifestUtil.setManifestAttribute(buf, 
        BundleManifest.IMPORT_PACKAGE, manifest.getAttribute(BundleManifest.IMPORT_PACKAGE));
    ManifestUtil.setManifestAttribute(buf, 
        BundleManifest.DYNAMIC_IMPORT_PACKAGE, manifest.getAttribute(BundleManifest.DYNAMIC_IMPORT_PACKAGE));
    doc.set(buf.toString());
    
    super.commit(onSave);
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.ui.forms.IFormPart#refresh()
   */
  public void refresh() {
    // Refresh values from document
    IDocument doc = ((BundleDocument) getManagedForm().getInput()).getManifestDocument();
    manifest = ManifestUtil.createManifest(doc.get().getBytes());
    
    // Refresh viewers
    if (wExportPackageTableViewer != null) {
      wExportPackageTableViewer.setInput(manifest);
    }
    UiUtils.packTableColumns(wExportPackageTableViewer.getTable());
    if (wImportPackageTableViewer != null) {
      wImportPackageTableViewer.setInput(manifest);
    }
    UiUtils.packTableColumns(wImportPackageTableViewer.getTable());
    if (wDynamicImportPackageTableViewer != null) {
      wDynamicImportPackageTableViewer.setInput(manifest);
    }
    UiUtils.packTableColumns(wDynamicImportPackageTableViewer.getTable());

    super.refresh();
  }    

  /****************************************************************************
   * Private utility methods
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
    layout.numColumns = 1;
    container.setLayout(layout);
    
    // Export section
    FormText exportText = toolkit.createFormText(container, false);
    exportText.setText(
        "<form><p><b>Exported Packages</b><br/>"+
        "List of package names that are exported by this bundle.</p></form>", true, true);
    TableWrapData td = new TableWrapData();
    td.align = TableWrapData.FILL;
    td.grabHorizontal = true;
    exportText.setLayoutData(td);
    
    Composite wExportComposite = toolkit.createComposite(container);
    td = new TableWrapData();
    td.align = TableWrapData.FILL;
    td.grabHorizontal = true;
    wExportComposite.setLayoutData(td);
    layout = new TableWrapLayout();
    layout.numColumns = 2;
    wExportComposite.setLayout(layout);
    
    // Create widgets
    Table wExportPackageTable = toolkit.createTable(wExportComposite, SWT.MULTI | SWT.FULL_SELECTION);
    wExportPackageTable.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
    
    wExportPackageTableViewer = new TableViewer(wExportPackageTable);
    ExportProvider exportProvider = new ExportProvider();
    wExportPackageTableViewer.setContentProvider(exportProvider);
    wExportPackageTableViewer.setLabelProvider(exportProvider);
    wExportPackageTableViewer.setSorter(exportProvider);
    wExportPackageTableViewer.setColumnProperties(new String[] {PROP_PACKAGE_NAME, PROP_PACKAGE_VERSION});
    wExportPackageTableViewer.setCellModifier(exportProvider);
    TextCellEditor packageVersionEditor = new TextCellEditor(wExportPackageTable, SWT.NONE);
    wExportPackageTableViewer.setCellEditors(
        new CellEditor[] {null, packageVersionEditor});
    wExportPackageTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = 
          (IStructuredSelection) wExportPackageTableViewer.getSelection();
        
        // Enable/disable remove button
        boolean enable = false;
        if (selection != null && !selection.isEmpty()) {
          enable = true;
        }
        wExportPackageRemoveButton.setEnabled(enable);
      }
    });
    // Package Name
    new TableColumn(wExportPackageTable, SWT.LEFT);
    // Version
    new TableColumn(wExportPackageTable, SWT.LEFT);
    
    TableWrapData wd = new TableWrapData();
    wd.rowspan = 2;
    wd.grabHorizontal = true;
    wd.grabVertical = true;
    wd.align = TableWrapData.FILL;
    wd.valign = TableWrapData.FILL;
    wd.heightHint = UiUtils.convertHeightInCharsToPixels(wExportPackageTable, NUM_EXPORT_TABLE_ROWS);
    wExportPackageTable.setLayoutData(wd);
    
    wExportPackageRemoveButton = toolkit.createButton(wExportComposite, "Remove", SWT.PUSH);
    wExportPackageRemoveButton.setEnabled(false);
    wExportPackageRemoveButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = 
          (IStructuredSelection) wExportPackageTableViewer.getSelection();
        
        if (selection == null || selection.isEmpty()) return;

        List packages = new ArrayList(Arrays.asList(manifest.getExportedPackages()));
        for (Iterator i = selection.iterator(); i.hasNext(); ) {
          PackageDescription pd = (PackageDescription) i.next();
          packages.remove(pd);
        }
        manifest.setExportedPackages((PackageDescription[]) packages.toArray(new PackageDescription[packages.size()]));
        
        UiUtils.packTableColumns(wExportPackageTableViewer.getTable());
        wExportPackageTableViewer.refresh();
        markDirty();
      }
    });
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wExportPackageRemoveButton.setLayoutData(wd);
    
    wExportPackageAddButton = toolkit.createButton(wExportComposite, "Add...", SWT.PUSH);
    wExportPackageAddButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        PackageDescription[] exportablePackages = project.getExportablePackages();
        ArrayList exportedPackages = new ArrayList(Arrays.asList(manifest.getExportedPackages()));
        
        // Remove already exported packages from list of exportable
        TreeMap map = new TreeMap();
        for (int i=0; i<exportablePackages.length; i++) {
          map.put(exportablePackages[i].getPackageName(), exportablePackages[i]);
        }
        for (Iterator i=exportedPackages.iterator(); i.hasNext();) {
          PackageDescription pd = (PackageDescription) i.next();
          map.remove(pd.getPackageName());
        }
        
        PackageListSelectionDialog dialog = new PackageListSelectionDialog(
            Display.getCurrent().getActiveShell());
        dialog.setElements(map.values().toArray(new PackageDescription[map.size()]));
        dialog.setMultipleSelection(true);
        dialog.setTitle("Select package");
        dialog.setImage(JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_PACKAGE));
        dialog.setMessage("Select package to export.");
        if (dialog.open() == Window.OK) {
          Object[] packages = (Object[]) dialog.getResult();
          for(int i=0; i<packages.length;i++) {
            if (packages[i] instanceof PackageDescription) {
              PackageDescription pd = (PackageDescription) packages[i];
              if (!exportedPackages.contains(pd)) {
                exportedPackages.add(pd);
              }
            }
          }
          manifest.setExportedPackages((PackageDescription[]) exportedPackages.toArray(new PackageDescription[exportedPackages.size()]));
          wExportPackageTableViewer.refresh();
          UiUtils.packTableColumns(wExportPackageTableViewer.getTable());
          markDirty();
        }
      }
    });
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wExportPackageAddButton.setLayoutData(wd);
    

    // Import section
    FormText importText = toolkit.createFormText(container, false);
    importText.setText(
        "<form><p><b>Imported Packages</b><br/>"+
        "List of package names that must be imported by this bundle.</p></form>", true, true);
    td = new TableWrapData();
    td.align = TableWrapData.FILL;
    td.grabHorizontal = true;
    importText.setLayoutData(td);
    
    Composite wImportComposite = toolkit.createComposite(container);
    td = new TableWrapData();
    td.align = TableWrapData.FILL;
    td.grabHorizontal = true;
    wImportComposite.setLayoutData(td);
    layout = new TableWrapLayout();
    layout.numColumns = 2;
    wImportComposite.setLayout(layout);
    
    // Create widgets
    Table wImportPackageTable = toolkit.createTable(wImportComposite, SWT.MULTI | SWT.FULL_SELECTION);
    wImportPackageTable.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
    
    wImportPackageTableViewer = new TableViewer(wImportPackageTable);
    ImportProvider importProvider = new ImportProvider();
    wImportPackageTableViewer.setContentProvider(importProvider);
    wImportPackageTableViewer.setLabelProvider(importProvider);
    wImportPackageTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = 
          (IStructuredSelection) wImportPackageTableViewer.getSelection();
        
        // Enable/disable remove button
        boolean enable = false;
        if (selection != null && !selection.isEmpty()) {
          enable = true;
        }
        wImportPackageRemoveButton.setEnabled(enable);
      }
    });
    // Package Name
    new TableColumn(wImportPackageTable, SWT.LEFT);
    // Version
    new TableColumn(wImportPackageTable, SWT.LEFT);
    // Bundle
    new TableColumn(wImportPackageTable, SWT.LEFT);
    
    wd = new TableWrapData();
    wd.rowspan = 2;
    wd.grabHorizontal = true;
    wd.grabVertical = true;
    wd.align = TableWrapData.FILL;
    wd.valign = TableWrapData.FILL;
    wd.heightHint = UiUtils.convertHeightInCharsToPixels(wImportPackageTable, NUM_IMPORT_TABLE_ROWS);
    wImportPackageTable.setLayoutData(wd);
    
    wImportPackageRemoveButton = toolkit.createButton(wImportComposite, "Remove", SWT.PUSH);
    wImportPackageRemoveButton.setEnabled(false);
    wImportPackageRemoveButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = 
          (IStructuredSelection) wImportPackageTableViewer.getSelection();
        
        if (selection == null || selection.isEmpty()) return;

        List packages = new ArrayList(Arrays.asList(manifest.getImportedPackages()));
        for (Iterator i = selection.iterator(); i.hasNext(); ) {
          PackageDescription pd = (PackageDescription) i.next();
          packages.remove(pd);
        }
        manifest.setImportedPackages((PackageDescription[]) packages.toArray(new PackageDescription[packages.size()]));
        
        UiUtils.packTableColumns(wImportPackageTableViewer.getTable());
        
        wImportPackageTableViewer.refresh();
        markDirty();
      }
    });
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wImportPackageRemoveButton.setLayoutData(wd);
    
    wImportPackageAddButton = toolkit.createButton(wImportComposite, "Add...", SWT.PUSH);
    wImportPackageAddButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        IPackage[] importablePackages = PackageUtil.getPackages(IPackage.ALL);
        ArrayList importedPackages = new ArrayList(Arrays.asList(manifest.getImportedPackages()));
        
        // Remove already imported packages from list of importable
        TreeMap map = new TreeMap();
        for (int i=0; i<importablePackages.length; i++) {
          PackageDescription packageDescription = importablePackages[i].getPackageDescription();
          packageDescription.setSpecificationVersion(null);
          map.put(packageDescription.getPackageName(), packageDescription);
        }
        for (Iterator i=importedPackages.iterator(); i.hasNext();) {
          PackageDescription pd = (PackageDescription) i.next();
          map.remove(pd.getPackageName());
        }
        
        
        PackageListSelectionDialog dialog = new PackageListSelectionDialog(
            Display.getCurrent().getActiveShell());
        dialog.setElements(map.values().toArray(new PackageDescription[map.size()]));
        dialog.setMultipleSelection(true);
        dialog.setTitle("Select package");
        dialog.setImage(JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_PACKAGE));
        dialog.setMessage("Select package to import.");
        if (dialog.open() == Window.OK) {
          Object[] packages = (Object[]) dialog.getResult();
          for(int i=0; i<packages.length;i++) {
            if (packages[i] instanceof PackageDescription) {
              PackageDescription pd = (PackageDescription) packages[i];
              if (!importedPackages.contains(pd)) {
                importedPackages.add(pd);
              }
            }
          }
          manifest.setImportedPackages((PackageDescription[]) importedPackages.toArray(new PackageDescription[importedPackages.size()]));
          wImportPackageTableViewer.refresh();
          UiUtils.packTableColumns(wImportPackageTableViewer.getTable());
          markDirty();
        }
      }
    });
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wImportPackageAddButton.setLayoutData(wd);
    
    // Dynamic Import section
    FormText dynamicImportText = toolkit.createFormText(container, false);
    dynamicImportText.setText(
        "<form><p><b>Dynamic Imported Packages</b><br/>"+
        "List of package names that can be imported at runtime by this bundle.</p></form>", true, true);
    td = new TableWrapData();
    td.align = TableWrapData.FILL;
    td.grabHorizontal = true;
    dynamicImportText.setLayoutData(td);
    
    Composite wDynamicImportComposite = toolkit.createComposite(container);
    td = new TableWrapData();
    td.align = TableWrapData.FILL;
    td.grabHorizontal = true;
    wDynamicImportComposite.setLayoutData(td);
    layout = new TableWrapLayout();
    layout.numColumns = 2;
    wDynamicImportComposite.setLayout(layout);
    
    // Create widgets
    Table wDynamicImportPackageTable = toolkit.createTable(wDynamicImportComposite, SWT.MULTI | SWT.FULL_SELECTION);
    wDynamicImportPackageTable.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
    
    wDynamicImportPackageTableViewer = new TableViewer(wDynamicImportPackageTable);
    DynamicImportProvider dynamicImportProvider = new DynamicImportProvider();
    wDynamicImportPackageTableViewer.setContentProvider(dynamicImportProvider);
    wDynamicImportPackageTableViewer.setLabelProvider(dynamicImportProvider);
    wDynamicImportPackageTableViewer.setSorter(dynamicImportProvider);
    wDynamicImportPackageTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = 
          (IStructuredSelection) wDynamicImportPackageTableViewer.getSelection();
        
        // Enable/disable remove button
        boolean enable = false;
        if (selection != null && !selection.isEmpty()) {
          enable = true;
        }
        wDynamicImportPackageRemoveButton.setEnabled(enable);
      }
    });
    // Package Name
    new TableColumn(wDynamicImportPackageTable, SWT.LEFT);
    
    wd = new TableWrapData();
    wd.rowspan = 2;
    wd.grabHorizontal = true;
    wd.grabVertical = true;
    wd.align = TableWrapData.FILL;
    wd.valign = TableWrapData.FILL;
    wd.heightHint = UiUtils.convertHeightInCharsToPixels(wDynamicImportPackageTable, NUM_DYNAMIC_IMPORT_TABLE_ROWS);
    wDynamicImportPackageTable.setLayoutData(wd);
    
    wDynamicImportPackageRemoveButton = toolkit.createButton(wDynamicImportComposite, "Remove", SWT.PUSH);
    wDynamicImportPackageRemoveButton.setEnabled(false);
    wDynamicImportPackageRemoveButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = 
          (IStructuredSelection) wDynamicImportPackageTableViewer.getSelection();
        
        if (selection == null || selection.isEmpty()) return;
        
        List packages = new ArrayList(Arrays.asList(manifest.getDynamicImportedPakages()));
        for (Iterator i = selection.iterator(); i.hasNext(); ) {
          String name = ((String) i.next()).trim();
          packages.remove(name);
        }
        manifest.setDynamicImportedPakages((String[]) packages.toArray(new String[packages.size()]));
        wDynamicImportPackageTableViewer.refresh();
        markDirty();
      }
    });
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wDynamicImportPackageRemoveButton.setLayoutData(wd);
    
    wDynamicImportPackageAddButton = toolkit.createButton(wDynamicImportComposite, "Add...", SWT.PUSH);
    wDynamicImportPackageAddButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        HashMap map = new HashMap();
        String key = "dynamicImport";
        PropertyDialog.Property p = new PropertyDialog.Property(key) {
          public boolean isValid(String value) {
            if (value.indexOf(",")!= -1) {
              return false;
            } else {
              return true;
            }
          }
        };
        p.setLabel("Dynamic Import:");
        map.put(key, p);
        PropertyDialog dialog = 
          new PropertyDialog(((Button) e.widget).getShell(), map, TITLE_ADD_DYNAMIC_IMPORT);
        
        if (dialog.open() == Window.OK) {
          map = dialog.getValues();
          p = (PropertyDialog.Property) map.get(key);
          List packages = new ArrayList(Arrays.asList(manifest.getDynamicImportedPakages()));
          String packageName = p.getValue();
          if (packageName != null && !packages.contains(p.getValue())) {
            packages.add(packageName);
            manifest.setDynamicImportedPakages((String[]) packages.toArray(new String[packages.size()]));
            wDynamicImportPackageTableViewer.refresh();
            UiUtils.packTableColumns(wDynamicImportPackageTableViewer.getTable());
            
            wDynamicImportPackageTableViewer.setSelection(new StructuredSelection(packageName), true);
            markDirty();
          }
        }
      }
    });
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wDynamicImportPackageAddButton.setLayoutData(wd);
    
    container.pack(true);
    container.layout(true);
    toolkit.paintBordersFor(container);
    toolkit.paintBordersFor(wExportComposite);
    toolkit.paintBordersFor(wImportComposite);
    toolkit.paintBordersFor(wDynamicImportComposite);
    section.setClient(container);
  }
  
  /****************************************************************************
   * ExportProvider Inner classes
   ***************************************************************************/
  class ExportProvider extends ViewerSorter implements IStructuredContentProvider, ITableLabelProvider, ITableColorProvider, ICellModifier {
    
    public int compare(Viewer viewer, Object o1, Object o2) {
      PackageDescription pd1 = (PackageDescription) o1;
      PackageDescription pd2 = (PackageDescription) o2;
      
      return pd1.getPackageName().compareTo(pd2.getPackageName());
    }
    
    public Object[] getElements(Object inputElement) {
      if ( !(inputElement instanceof BundleManifest)) return null;
      
      BundleManifest manifest = (BundleManifest) inputElement; 
      return manifest.getExportedPackages();
    }
    
    public void dispose() {
    }
    
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    
    public Image getColumnImage(Object element, int columnIndex) {
      if (columnIndex == 0) {
        PackageDescription desc = (PackageDescription) element;
        List packages = Arrays.asList(project.getExportablePackages());
        if (packages.contains(desc)) {
          return JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_PACKAGE);
        } else {
          return imgPackageError;
        }
      } else {
        return null;
      }
    }

    public String getColumnText(Object element, int columnIndex) {
      PackageDescription desc = (PackageDescription) element;
      
      switch(columnIndex){
      case 0:
        return desc.getPackageName();
      case 1:
        String version = desc.getSpecificationVersion();
        if (version == null) {
          version = NO_VERSION_STR;
        }
        return version;
      }
      return "";
    }

    public void addListener(ILabelProviderListener listener) {
    }

    public boolean isLabelProperty(Object element, String property) {
      return false;
    }

    public void removeListener(ILabelProviderListener listener) {
    }

    public boolean canModify(Object element, String property) {
      return PROP_PACKAGE_VERSION.equals(property);
    }

    public Object getValue(Object element, String property) {
      PackageDescription pd = (PackageDescription) element;
      if (PROP_PACKAGE_VERSION.equals(property)) {
        String version = pd.getSpecificationVersion();
        if (version == null) {
          version = "";
        }
        return version;
      } else if (PROP_PACKAGE_NAME.equals(property)) {
        return pd.getPackageName();
      } else {
        return null;
      }
    }

    public void modify(Object element, String property, Object value) {
      if (element instanceof Item && ((Item) element).getData() instanceof PackageDescription) {
        if (PROP_PACKAGE_VERSION.equals(property)) {
          List packages = new ArrayList(Arrays.asList(manifest.getExportedPackages()));
          PackageDescription pdElement = (PackageDescription) ((Item) element).getData();
          int idx = packages.indexOf(pdElement);
          if (idx != -1) {
            
            PackageDescription pd = (PackageDescription) packages.get(idx);
            if (value== null || NO_VERSION_STR.equals(value)) {
              pd.setSpecificationVersion(null);
            } else {
              pd.setSpecificationVersion(value.toString());
            }
            
            packages.remove(idx);
            packages.add(idx, pd);
            manifest.setExportedPackages((PackageDescription []) packages.toArray(new PackageDescription[packages.size()]));
            
            wExportPackageTableViewer.refresh();
            UiUtils.packTableColumns(wExportPackageTableViewer.getTable());
            markDirty();
          }
        }
      }
    }

    public Color getForeground(Object element, int columnIndex) {
      PackageDescription desc = (PackageDescription) element;
      List packages = Arrays.asList(project.getExportablePackages());
      if (packages.contains(desc)) {
        return null;
      } else {
        return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
      }
    }

    public Color getBackground(Object element, int columnIndex) {
      return null;
    }
  }
  
  /****************************************************************************
   * ImportProvider Inner classes
   ***************************************************************************/
  class ImportProvider implements IStructuredContentProvider, ITableLabelProvider {
    
    public Object[] getElements(Object inputElement) {
      if ( !(inputElement instanceof BundleManifest)) return null;
      
      BundleManifest manifest = (BundleManifest) inputElement; 
      
      return manifest.getImportedPackages();
    }
    
    public void dispose() {
    }
    
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    public Image getColumnImage(Object element, int columnIndex) {
      if (columnIndex == 0) {
        return JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_PACKAGE);
      } else {
        return null;
      }
    }

    public String getColumnText(Object element, int columnIndex) {
      PackageDescription desc = (PackageDescription) element;
      
      switch(columnIndex){
      case 0:
        return desc.getPackageName();
      case 1:
        String version = desc.getSpecificationVersion();
        if (version == null) {
          version = "";
        }
        return version;
      case 2:
        return "Framework [Oscar 1.0]";
      }
      return "";
    }

    public void addListener(ILabelProviderListener listener) {
    }

    public boolean isLabelProperty(Object element, String property) {
      return false;
    }

    public void removeListener(ILabelProviderListener listener) {
    }
  }
  
  /****************************************************************************
   * DynamicImportProvider Inner classes
   ***************************************************************************/
  class DynamicImportProvider extends ViewerSorter implements IStructuredContentProvider, ITableLabelProvider, ITableColorProvider {
    
    public int compare(Viewer viewer, Object o1, Object o2) {
      String s1 = (String) o1;
      String s2 = (String) o2;
      
      return s1.compareTo(s2);
    }
    
    public Object[] getElements(Object inputElement) {
      if ( !(inputElement instanceof BundleManifest)) return null;
      
      BundleManifest manifest = (BundleManifest) inputElement; 
      
      return manifest.getDynamicImportedPakages();
    }
    
    public void dispose() {
    }
    
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    public Image getColumnImage(Object element, int columnIndex) {
      String name = (String) element;
      if (checkPackageName(name)) {
        return JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_PACKAGE);
      } else {
        return imgPackageError;
      }
    }

    public String getColumnText(Object element, int columnIndex) {
      return (String) element;
    }

    public void addListener(ILabelProviderListener listener) {
    }

    public boolean isLabelProperty(Object element, String property) {
      return false;
    }

    public void removeListener(ILabelProviderListener listener) {
    }

    public Color getForeground(Object element, int columnIndex) {
      String name = (String) element;
      if (checkPackageName(name)) {
        return null;
      } else {
        return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
      }
    }

    public Color getBackground(Object element, int columnIndex) {
      return null;
    }
    
    private boolean checkPackageName(String name) {
      if (name == null) return false;
      
      if ("*".equals(name)) return true;
      
      if (name.endsWith(".*")) {
        name = name.substring(0, name.length()-2);
      }
      IStatus status = JavaConventions.validatePackageName(name);
      return (status.getCode() == IStatus.OK);
    }
  }
}
