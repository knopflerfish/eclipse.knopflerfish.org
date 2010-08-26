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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.knopflerfish.eclipse.core.manifest.BundleManifest;
import org.knopflerfish.eclipse.core.manifest.ManifestUtil;
import org.knopflerfish.eclipse.core.manifest.PackageDescription;
import org.knopflerfish.eclipse.core.pkg.PackageUtil;
import org.knopflerfish.eclipse.core.project.BuildPath;
import org.knopflerfish.eclipse.core.project.BundleProject;
import org.knopflerfish.eclipse.core.project.classpath.FrameworkContainer;
import org.knopflerfish.eclipse.core.ui.OsgiUiPlugin;
import org.knopflerfish.eclipse.core.ui.UiUtils;
import org.knopflerfish.eclipse.core.ui.dialogs.PackageLabelProvider;
import org.knopflerfish.eclipse.core.ui.dialogs.PackageSelectionDialog;
import org.knopflerfish.eclipse.core.ui.dialogs.PropertyDialog;
import org.knopflerfish.eclipse.core.ui.editors.BundleDocument;
import org.osgi.framework.Version;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class PackageSection extends SectionPart {

  private static final int NUM_EXPORT_TABLE_ROWS = 4;
  private static final int NUM_IMPORT_TABLE_ROWS = 6;
  private static final int NUM_DYNAMIC_IMPORT_TABLE_ROWS = 4;

  // Dialog titles
  static String TITLE_ADD_DYNAMIC_IMPORT = "Add Dynamic Import";
  
  // Column Properties
  public static String PROP_PACKAGE_NAME       = "name";
  public static String PROP_PACKAGE_VERSION    = "version";
  public static String PROP_PACKAGE_CONTAINER  = "container";
  public static String NO_VERSION_STR = "[No version]";
  
  // Section title and description
  private static final String TITLE = 
    "Packages";
  private static final String DESCRIPTION = 
    "This section lists packages that are exported and imported by this bundle.";

  Button    wExportPackageRemoveButton;
  private Button    wExportPackageAddButton;
  Button    wImportPackageRemoveButton;
  private Button    wImportPackageAddButton;
  Button    wDynamicImportPackageRemoveButton;
  private Button    wDynamicImportPackageAddButton;
  
  // jFace Widgets 
  TableViewer   wExportPackageTableViewer;
  TableViewer   wImportPackageTableViewer;
  TableViewer   wDynamicImportPackageTableViewer;
  ImportDialogEditor importPackageBundleEditor;
  ComboBoxCellEditor importPackageVersionEditor;
  
  BundleManifest manifest = null;
  ImportPackageModel importPackageModel = null;
  final BundleProject project;
  
  public PackageSection(Composite parent, FormToolkit toolkit, int style, BundleProject project) {
    super(parent, toolkit, style);
    
    this.project = project;
    Section section = getSection();
    createClient(section, toolkit);
    section.setDescription(DESCRIPTION);
    section.setText(TITLE);
  }
  
  public void setErrors(List errors) {
    if (wExportPackageTableViewer != null) { 
      wExportPackageTableViewer.refresh();
    }
    if (wImportPackageTableViewer != null) { 
      wImportPackageTableViewer.refresh();
    }
    if (wDynamicImportPackageTableViewer != null) { 
      wDynamicImportPackageTableViewer.refresh();
    }
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
    // Commit values to document
    BundleDocument bundleDocument = (BundleDocument) getManagedForm().getInput();
    IDocument doc = bundleDocument.getManifestDocument();
    if (manifest == null) return;
    
    StringBuffer buf = new StringBuffer(doc.get());
    ManifestUtil.setManifestAttribute(buf, 
        BundleManifest.EXPORT_PACKAGE, manifest.getAttribute(BundleManifest.EXPORT_PACKAGE));
    ManifestUtil.setManifestAttribute(buf, 
        BundleManifest.IMPORT_PACKAGE, manifest.getAttribute(BundleManifest.IMPORT_PACKAGE));
    ManifestUtil.setManifestAttribute(buf, 
        BundleManifest.DYNAMIC_IMPORT_PACKAGE, manifest.getAttribute(BundleManifest.DYNAMIC_IMPORT_PACKAGE));
    doc.set(buf.toString());
    
    bundleDocument.setImportPackageModel(importPackageModel);
    
    super.commit(onSave);
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.ui.forms.IFormPart#refresh()
   */
  public void refresh() {
    // Refresh values from document
    BundleDocument bundleDocument = (BundleDocument) getManagedForm().getInput();
    IDocument doc = bundleDocument.getManifestDocument();
    manifest = ManifestUtil.createManifest(doc.get().getBytes());
    importPackageModel = bundleDocument.getImportPackageModel();
    importPackageModel.updateManifest(manifest);
    
    // Refresh viewers
    if (wExportPackageTableViewer != null) {
      wExportPackageTableViewer.setInput(manifest);
    }
    UiUtils.packTableColumns(wExportPackageTableViewer.getTable());
    if (wImportPackageTableViewer != null) {
      wImportPackageTableViewer.setInput(importPackageModel);
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
    ExportProvider exportProvider = new ExportProvider(project);
    wExportPackageTableViewer.setContentProvider(exportProvider);
    wExportPackageTableViewer.setLabelProvider(exportProvider);
    wExportPackageTableViewer.setSorter(exportProvider);
    wExportPackageTableViewer.setColumnProperties(new String[] {PROP_PACKAGE_NAME, PROP_PACKAGE_VERSION});
    ExportCellModifier exportCellModifier = new ExportCellModifier();
    wExportPackageTableViewer.setCellModifier(exportCellModifier);
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
        ArrayList exportablePackageNames = new ArrayList();
        try {
          exportablePackageNames.addAll(Arrays.asList(project.getExportablePackageNames()));
        } catch (JavaModelException jme) {
          OsgiUiPlugin.log(jme.getStatus());
        }
        ArrayList exportedPackages = new ArrayList(Arrays.asList(manifest.getExportedPackages()));
        
        // Remove already exported packages from list of exportable
        for (Iterator i=exportedPackages.iterator(); i.hasNext();) {
          PackageDescription pd = (PackageDescription) i.next();
          exportablePackageNames.remove(pd.getPackageName());
        }
        
        ElementListSelectionDialog dialog = new ElementListSelectionDialog(
            Display.getCurrent().getActiveShell(),
            new PackageLabelProvider());
        dialog.setElements(exportablePackageNames.toArray(new String[exportablePackageNames.size()]));
        dialog.setMultipleSelection(true);
        dialog.setTitle("Select package");
        dialog.setImage(JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_PACKAGE));
        dialog.setMessage("Select package to export.");
        if (dialog.open() == Window.OK) {
          Object[] packageNames = dialog.getResult();
          for(int i=0; i<packageNames.length;i++) {
            PackageDescription pd = new PackageDescription((String) packageNames[i], null);
            if (!exportedPackages.contains(pd)) {
              exportedPackages.add(pd);
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
    ImportProvider importProvider = new ImportProvider(project);
    wImportPackageTableViewer.setContentProvider(importProvider);
    wImportPackageTableViewer.setLabelProvider(importProvider);
    wImportPackageTableViewer.setSorter(importProvider);
    wImportPackageTableViewer.setColumnProperties(new String[] {PROP_PACKAGE_NAME, PROP_PACKAGE_VERSION, PROP_PACKAGE_CONTAINER});
    ImportCellModifier importCellModifier = new ImportCellModifier();
    wImportPackageTableViewer.setCellModifier(importCellModifier);
    importPackageBundleEditor = new ImportDialogEditor(wImportPackageTable);
    importPackageVersionEditor = new ComboBoxCellEditor(
        wImportPackageTable, 
        new String[] {},
        SWT.DROP_DOWN);
    wImportPackageTableViewer.setCellEditors(
        new CellEditor[] {null, importPackageVersionEditor, importPackageBundleEditor});
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
    wd.rowspan = 3;
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
          BuildPath path = (BuildPath) i.next();
          packages.remove(path.getPackageDescription());
        }
        manifest.setImportedPackages((PackageDescription[]) packages.toArray(new PackageDescription[packages.size()]));
        importPackageModel.updateManifest(manifest);
        wImportPackageTableViewer.refresh();
        UiUtils.packTableColumns(wImportPackageTableViewer.getTable());
        markDirty();
      }
    });
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wImportPackageRemoveButton.setLayoutData(wd);
    
    wImportPackageAddButton = toolkit.createButton(wImportComposite, "Add...", SWT.PUSH);
    wImportPackageAddButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        PackageDescription[] importablePackages = PackageUtil.getExportedPackages(project);
        ArrayList importedPackages = new ArrayList(Arrays.asList(manifest.getImportedPackages()));
        
        // Remove already imported packages from list of importable
        TreeMap map = new TreeMap();
        for (int i=0; i<importablePackages.length; i++) {
          PackageDescription pd = importablePackages[i];
          String name = pd.getPackageName();
          ArrayList list = (ArrayList) map.get(name);
          if (list == null) {
            list = new ArrayList();
          }
          if (!list.contains(pd)) {
            list.add(pd);
          }
          map.put(name, list);
        }
        for (Iterator i=importedPackages.iterator(); i.hasNext();) {
          PackageDescription pd = (PackageDescription) i.next();
          map.remove(pd.getPackageName());
        }
        
        PackageSelectionDialog dialog = new PackageSelectionDialog(
            Display.getCurrent().getActiveShell(), new PackageLabelProvider());
        dialog.setPackages(map);
        dialog.setMultipleSelection(true);
        dialog.setTitle("Select package");
        dialog.setImage(JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_PACKAGE));
        dialog.setMessage("Select package to import.");
        if (dialog.open() == Window.OK) {
          Object[] packages = dialog.getResult();
          Version version = dialog.getVersion();
          for(int i=0; i<packages.length;i++) {
            PackageDescription pd = new PackageDescription((String) packages[i], version);
            if (!importedPackages.contains(pd)) {
              importedPackages.add(pd);
            }
          }
          manifest.setImportedPackages((PackageDescription[]) importedPackages.toArray(new PackageDescription[importedPackages.size()]));
          importPackageModel.updateManifest(manifest);
          wImportPackageTableViewer.refresh();
          UiUtils.packTableColumns(wImportPackageTableViewer.getTable());
          markDirty();
        }
      }
    });
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wImportPackageAddButton.setLayoutData(wd);
    
    Button wImportPackageAutoButton = toolkit.createButton(wImportComposite, "Auto", SWT.PUSH);
    wImportPackageAutoButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        ArrayList neededPackageNames = new ArrayList();
        try {
          neededPackageNames.addAll(Arrays.asList(project.getReferencedPackageNames()));
        } catch (JavaModelException jme) {
          OsgiUiPlugin.log(jme.getStatus());
        }
        ArrayList importedPackages = new ArrayList(Arrays.asList(manifest.getImportedPackages()));
        ArrayList importedPackageNames = new ArrayList();
        for (int i=0; i<importedPackages.size(); i++) {
          importedPackageNames.add(((PackageDescription) importedPackages.get(i)).getPackageName());
        }
        // Add missing imports
        boolean changed = false;
        for(Iterator i=neededPackageNames.iterator();i.hasNext();) {
          String name = (String) i.next();
          if (!importedPackageNames.contains(name)) {
            importedPackages.add(new PackageDescription(name, null));
            changed = true;
          }
        }
        // Remove unneeded imports
        for(Iterator i=importedPackages.iterator();i.hasNext();) {
          PackageDescription pd = (PackageDescription) i.next();
          if (!neededPackageNames.contains(pd.getPackageName())) {
            i.remove();
            changed = true;
          }
        }

        if (changed) {
          manifest.setImportedPackages((PackageDescription[]) importedPackages.toArray(new PackageDescription[importedPackages.size()]));
          importPackageModel.updateManifest(manifest);
          wImportPackageTableViewer.refresh();
          UiUtils.packTableColumns(wImportPackageTableViewer.getTable());
          markDirty();
        }
      }
    });
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wImportPackageAutoButton.setLayoutData(wd);
    
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
            }
            return true;
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
   * ExportCellModifier Inner classes
   ***************************************************************************/
  class ExportCellModifier implements ICellModifier {
    
    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
     */
    public boolean canModify(Object element, String property) {
      return PackageSection.PROP_PACKAGE_VERSION.equals(property);
    }
    
    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
     */
    public Object getValue(Object element, String property) {
      PackageDescription pd = (PackageDescription) element;
      if (PackageSection.PROP_PACKAGE_VERSION.equals(property)) {
        Version version = pd.getSpecificationVersion();
        return version.toString();
      } else if (PackageSection.PROP_PACKAGE_NAME.equals(property)) {
        return pd.getPackageName();
      } else {
        return null;
      }
    }
    
    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
     */
    public void modify(Object element, String property, Object value) {
      if (element instanceof Item && ((Item) element).getData() instanceof PackageDescription) {
        if (PackageSection.PROP_PACKAGE_VERSION.equals(property)) {
          List packages = new ArrayList(Arrays.asList(manifest.getExportedPackages()));
          PackageDescription pdElement = (PackageDescription) ((Item) element).getData();
          int idx = packages.indexOf(pdElement);
          if (idx != -1) {
            
            PackageDescription pd = (PackageDescription) packages.get(idx);
            if (value== null || NO_VERSION_STR.equals(value)) {
              pd.setSpecificationVersion(null);
            } else {
              try {
                pd.setSpecificationVersion(Version.parseVersion(value.toString()));
              } catch (IllegalArgumentException e) {
                MessageDialog msgDialog = new MessageDialog(
                    Display.getCurrent().getActiveShell(),
                    "Version Error",
                    null,
                    "Version improperly formatted, format major('.'minor('.'micro('.'qualifier)?)?)?",
                    MessageDialog.ERROR,
                    new String[] {"Ok"},
                    0);
                msgDialog.setBlockOnOpen(true);
                msgDialog.open();
                pd.setSpecificationVersion(null);
              }
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
  }

  /****************************************************************************
   * ImportCellModifier Inner classes
   ***************************************************************************/

  class ImportCellModifier implements ICellModifier {
    
    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
     */
    public boolean canModify(Object o, String property) {
      BuildPath element = (BuildPath) o;
      if (PackageSection.PROP_PACKAGE_CONTAINER.equals(property)) {
        PackageDescription pd = element.getPackageDescription();
        return !project.getBundleManifest().hasExportedPackage(pd);
      } else if (PackageSection.PROP_PACKAGE_VERSION.equals(property)) {
        return true;
      } else {
        return false;
      }
    }
    
    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
     */
    public Object getValue(Object o, String property) {
      BuildPath element = (BuildPath) o;
      

      if (PackageSection.PROP_PACKAGE_CONTAINER.equals(property)) {
        // Update cell editor
        PackageDescription pd = element.getPackageDescription();

        ArrayList items = new ArrayList();
        
        // Add framework entry
        if (PackageUtil.frameworkExportsPackage(project, pd)) {
          IPath path = new Path(FrameworkContainer.CONTAINER_PATH);
          items.add(new BuildPath(path, pd, null, "Framework"));
        }
        
        // Add bundle entries from projects
        BuildPath[] projectBuildPaths = PackageUtil.getExportingProjectBundles(pd);
        for (int i=0; i<projectBuildPaths.length; i++) {
          if (!items.contains(projectBuildPaths[i])) {
            items.add(projectBuildPaths[i]);
          }
        }
  
        // Add bundle entries from repositories
        BuildPath[] repositoryBuildPaths = PackageUtil.getExportingRepositoryBundles(pd);
        for (int i=0; i<repositoryBuildPaths.length; i++) {
          if (!items.contains(repositoryBuildPaths[i])) {
            items.add(repositoryBuildPaths[i]);
          }
        }
        
        // Set elements to show in dialog box
        importPackageBundleEditor.setElements((BuildPath[]) items.toArray(new BuildPath[items.size()]));
        
        // Return current selected bundle
        if (element.getContainerPath() == null) {
          return "";
        } else if (FrameworkContainer.CONTAINER_PATH.equals(element.getContainerPath().toString())) {
          return "Framework";
        } 
        
        String name= element.getBundleName();
        if (name == null || name.trim().length() == 0) {
          name = element.getBundleIdentity().getSymbolicName().toString();
        }
        return name;
      } else if (PackageSection.PROP_PACKAGE_VERSION.equals(property)) {
        // Update cell editor
        PackageDescription pd = element.getPackageDescription();
        
        Version version = pd.getSpecificationVersion();
        
        ArrayList versions = new ArrayList();
        versions.add(Version.emptyVersion.toString());
        
        // Add package versions available from framework runtime
        Version[] frameworkPackageVersions = 
          PackageUtil.getFrameworkPackageVersions(project, pd.getPackageName());
        for(int i=0; i<frameworkPackageVersions.length; i++) {
          if (!versions.contains(frameworkPackageVersions[i].toString())) {
            versions.add(frameworkPackageVersions[i].toString());
          }
        }
        
        // Add package versions available from bundle projects
        Version[] projectPackageVersions = 
          PackageUtil.getProjectPackageVersions(pd.getPackageName());
        for(int i=0; i<projectPackageVersions.length; i++) {
          if (!versions.contains(projectPackageVersions[i].toString())) {
            versions.add(projectPackageVersions[i].toString());
          }
        }
        
        // Add package versions available from repositories
        Version[] repositoryPackageVersions = 
          PackageUtil.getRepositoryPackageVersions(pd.getPackageName());
        for(int i=0; i<repositoryPackageVersions.length; i++) {
          if (!versions.contains(repositoryPackageVersions[i].toString())) {
            versions.add(repositoryPackageVersions[i].toString());
          }
        }
        
        // Set elements to show in combo box
        importPackageVersionEditor.setItems((String[]) versions.toArray(new String[versions.size()]));
        
        int idx = versions.indexOf(version.toString());
        return new Integer(idx);
      } else {
        return null;
      }
    }
    
    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
     */
    public void modify(Object element, String property, Object value) {
      
      if (element instanceof Item && ((Item) element).getData() instanceof BuildPath) {
        BuildPath oldBuildPath = (BuildPath) ((Item) element).getData();
        if (PackageSection.PROP_PACKAGE_CONTAINER.equals(property) && value instanceof BuildPath) {
          BuildPath newBuildPath = (BuildPath) value;
  
          oldBuildPath.setBundleIdentity(newBuildPath.getBundleIdentity());
          oldBuildPath.setContainerPath(newBuildPath.getContainerPath());
          oldBuildPath.setBundleName(newBuildPath.getBundleName());
        } else if (PackageSection.PROP_PACKAGE_VERSION.equals(property) && value instanceof Integer) {
          Version version = Version.emptyVersion;
          try {
            int idx = ((Integer) value).intValue();
            Control control = importPackageVersionEditor.getControl();
            if (control instanceof CCombo) {
              version = Version.parseVersion(((CCombo) control).getText());
            } else if (idx != -1) {
              String[] items = importPackageVersionEditor.getItems();
              version = Version.parseVersion(items[idx]);
            }
          } catch (IllegalArgumentException e) {
            version = Version.emptyVersion;
          }
          String packageName = oldBuildPath.getPackageDescription().getPackageName();
          List packages = new ArrayList(Arrays.asList(manifest.getImportedPackages()));
          int idx = -1;
          for(int i=0; i<packages.size(); i++) {
            PackageDescription pd = (PackageDescription) packages.get(i);
            if (packageName.equals(pd.getPackageName())) {
              idx = i;
              break;
            }
          }
          
          PackageDescription pd = new PackageDescription(packageName, version);
          if (idx != -1) {
            packages.remove(idx);
            packages.add(idx, pd);
          } else {
            packages.add(pd);
          }
          
          manifest.setImportedPackages((PackageDescription[]) packages.toArray(new PackageDescription[packages.size()]));
          importPackageModel.updateManifest(manifest);
        }
        //wImportPackageTableViewer.update(oldBuildPath, null);
        wImportPackageTableViewer.refresh();
        UiUtils.packTableColumns(wImportPackageTableViewer.getTable());
        markDirty();
      }
    }   
  }
}
