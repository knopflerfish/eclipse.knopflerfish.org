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
import java.util.Map;
import java.util.TreeSet;
import java.util.jar.Attributes;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.knopflerfish.eclipse.core.manifest.BundleManifest;
import org.knopflerfish.eclipse.core.manifest.ManifestUtil;
import org.knopflerfish.eclipse.core.preferences.EnvironmentPreference;
import org.knopflerfish.eclipse.core.preferences.OsgiPreferences;
import org.knopflerfish.eclipse.core.project.BundleProject;
import org.knopflerfish.eclipse.core.ui.OsgiUiPlugin;
import org.knopflerfish.eclipse.core.ui.SharedImages;
import org.knopflerfish.eclipse.core.ui.StatusLabel;
import org.knopflerfish.eclipse.core.ui.UiUtils;
import org.knopflerfish.eclipse.core.ui.dialogs.PropertyDialog;
import org.knopflerfish.eclipse.core.ui.dialogs.TypeSelectionDialog;
import org.knopflerfish.eclipse.core.ui.editors.BundleDocument;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class GeneralSection extends SectionPart {
  
  private static final int NUM_CATEGORY_TABLE_ROWS = 5;
  private static final int NUM_ENVIRONMENT_TABLE_ROWS = 5;
  
  // Dialog titles
  static String TITLE_ADD_CATEGORY = "Add Category";
  static String TITLE_ADD_ENVIRONMENT = "Add Execution Environment";
  
  // Section title and description
  private static final String TITLE = "General Information";
  private static final String DESCRIPTION = "This section describes general information about this bundle.";
  
  // Widget strings 
  private static final String BUNDLE_SYMBOLIC_NAME_LABEL = 
    "Symbolic Name:";
  private static final String BUNDLE_SYMBOLIC_NAME_TOOLTIP = 
    "A unique, non localizable, name for this bundle. This name should be based on the reverse domain name convention.";
  private static final String BUNDLE_VERSION_LABEL = 
    "Version:";
  private static final String BUNDLE_VERSION_TOOLTIP = 
    "The version of this bundle.";
  private static final String BUNDLE_NAME_LABEL = 
    "Name:";
  private static final String BUNDLE_NAME_TOOLTIP = 
    "Name for this bundle. This should be a short, human readable, name and should contain no spaces.";
  private static final String BUNDLE_UPDATELOCATION_LABEL = 
    "Update Location:";
  private static final String BUNDLE_UPDATELOCATION_TOOLTIP = 
    "If the bundle is updated, this location should be used (if present) to retrieve the updated JAR file.";
  private static final String BUNDLE_ACTIVATOR_LABEL = 
    "Activator:";
  private static final String BUNDLE_ACTIVATOR_TOOLTIP = 
    "The name of the class that is used to start and stop the bundle.";
  
  private static final String BUNDLE_DESCRIPTION_LABEL = 
    "Description:";
  private static final String BUNDLE_DESCRIPTION_TOOLTIP = 
    "A short description of this bundle.";
  private static final String BUNDLE_DOCURL_LABEL = 
    "Documentation:";
  private static final String BUNDLE_DOCURL_TOOLTIP = 
    "A URL to documentation about this bundle.";
  
  private static final String BUNDLE_VENDOR_LABEL = 
    "Vendor Name:";
  private static final String BUNDLE_VENDOR_TOOLTIP = 
    "A text description of the vendor.";
  private static final String BUNDLE_CONTACT_LABEL = 
    "Contact Address:";
  private static final String BUNDLE_CONTACT_TOOLTIP = 
    "Contact address if it is necessary to contact the vendor.";
  private static final String BUNDLE_COPYRIGHT_LABEL = 
    "Copyright:";
  private static final String BUNDLE_COPYRIGHT_TOOLTIP = 
    "Copyright specification for this bundle.";
  
  // SWT Widgets
  private StatusLabel wSymbolicNameStatusLabel;
  private Text wSymbolicNameText;
  private StatusLabel wVersionStatusLabel;
  private Text wVersionText;
  private StatusLabel wNameStatusLabel;
  private Text wNameText;
  private Text wUpdateLocationText;
  private StatusLabel wUpdateLocationStatusLabel;
  Text wActivatorText;
  private StatusLabel wActivatorStatusLabel;
  private Text wDescriptionText;
  private StatusLabel wDocUrlStatusLabel;
  private Text wDocUrlText;
  private Text wVendorText;
  private Text wContactText;
  private Text wCopyrightText;
  private Button wCategoryAddButton;
  Button wCategoryRemoveButton;
  Button wEnvironmentRemoveButton;
  private Button wEnvironmentAddButton;
  
  TableViewer wCategoryTableViewer;
  TableViewer wEnvironmentTableViewer;
  
  // Model
  final BundleProject project;
  BundleManifest manifest = null;

  Image imgLibraryWarning;
  
  public GeneralSection(Composite parent, FormToolkit toolkit, int style, BundleProject project) {
    super(parent, toolkit, style);
    
    // Create images
    imgLibraryWarning = UiUtils.ovrImage(
        JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_LIBRARY),
        OsgiUiPlugin.getSharedImages().getImage(SharedImages.IMG_OVR_WARNING),
        UiUtils.LEFT, UiUtils.BOTTOM
        );
    
    this.project = project;
    Section section = getSection();
    createClient(section, toolkit);
    section.setDescription(DESCRIPTION);
    section.setText(TITLE);
  }
  
  public void setErrors(Map errors) {
    
    IMarker marker = (IMarker) errors.get(BundleManifest.BUNDLE_ACTIVATOR);
    updateStatus(marker, wActivatorStatusLabel, wActivatorText);
    marker = (IMarker) errors.get(BundleManifest.BUNDLE_UPDATELOCATION);
    updateStatus(marker, wUpdateLocationStatusLabel, wUpdateLocationText);
    marker = (IMarker) errors.get(BundleManifest.BUNDLE_DOCURL);
    updateStatus(marker, wDocUrlStatusLabel, wDocUrlText);
    marker = (IMarker) errors.get(BundleManifest.BUNDLE_NAME);
    updateStatus(marker, wNameStatusLabel, wNameText);
    marker = (IMarker) errors.get(BundleManifest.BUNDLE_VERSION);
    updateStatus(marker, wVersionStatusLabel, wVersionText);
    marker = (IMarker) errors.get(BundleManifest.BUNDLE_SYMBOLIC_NAME);
    updateStatus(marker, wSymbolicNameStatusLabel, wSymbolicNameText);
    
    if (wEnvironmentTableViewer != null) {
      wEnvironmentTableViewer.refresh();
    }
  }
  
  private void updateStatus(IMarker marker, StatusLabel wLabel, Text wText) {
    Color c = null;
    Image img = null;
    
    if (marker != null) {
      int severity = marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
      img = OsgiUiPlugin.getSharedImages().getImage(SharedImages.IMG_OVR_ERROR);
      c = Display.getDefault().getSystemColor(SWT.COLOR_RED);
      if (severity == IMarker.SEVERITY_WARNING) {
        img = OsgiUiPlugin.getSharedImages().getImage(SharedImages.IMG_OVR_WARNING);
        c = null;
      }
    }
    
    // Update label
    if (wLabel != null && !wLabel.isDisposed()) {
      wLabel.setStatusImage(img, UiUtils.LEFT, UiUtils.BOTTOM);
      wLabel.redraw();
    }
    
    // Update text
    if (wText != null && !wText.isDisposed()) {
      wText.setForeground(c);
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
    if (imgLibraryWarning != null) {
      imgLibraryWarning.dispose();
      imgLibraryWarning = null;
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
        BundleManifest.BUNDLE_SYMBOLIC_NAME, wSymbolicNameText.getText());
    ManifestUtil.setManifestAttribute(buf, 
        BundleManifest.BUNDLE_VERSION, wVersionText.getText());
    ManifestUtil.setManifestAttribute(buf, 
        BundleManifest.BUNDLE_NAME, wNameText.getText());
    ManifestUtil.setManifestAttribute(buf, 
        BundleManifest.BUNDLE_UPDATELOCATION, wUpdateLocationText.getText());
    ManifestUtil.setManifestAttribute(buf, 
        BundleManifest.BUNDLE_ACTIVATOR, wActivatorText.getText());
    ManifestUtil.setManifestAttribute(buf, 
        BundleManifest.BUNDLE_DESCRIPTION, wDescriptionText.getText());
    ManifestUtil.setManifestAttribute(buf, 
        BundleManifest.BUNDLE_DOCURL, wDocUrlText.getText());
    ManifestUtil.setManifestAttribute(buf, 
        BundleManifest.BUNDLE_VENDOR, wVendorText.getText());
    ManifestUtil.setManifestAttribute(buf, 
        BundleManifest.BUNDLE_CONTACT, wContactText.getText());
    ManifestUtil.setManifestAttribute(buf, 
        BundleManifest.BUNDLE_COPYRIGHT, wCopyrightText.getText());
    ManifestUtil.setManifestAttribute(buf, 
        BundleManifest.BUNDLE_CATEGORY, manifest.getAttribute(BundleManifest.BUNDLE_CATEGORY));
    ManifestUtil.setManifestAttribute(buf, 
        BundleManifest.BUNDLE_EXEC_ENV, manifest.getAttribute(BundleManifest.BUNDLE_EXEC_ENV));
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
    Attributes attributes = manifest.getMainAttributes();
    
    setText(wSymbolicNameText, attributes, BundleManifest.BUNDLE_SYMBOLIC_NAME);
    setText(wVersionText, attributes, BundleManifest.BUNDLE_VERSION);
    setText(wNameText, attributes, BundleManifest.BUNDLE_NAME);
    setText(wUpdateLocationText, attributes, BundleManifest.BUNDLE_UPDATELOCATION);
    setText(wActivatorText, attributes, BundleManifest.BUNDLE_ACTIVATOR);
    setText(wDescriptionText, attributes, BundleManifest.BUNDLE_DESCRIPTION);
    setText(wDocUrlText, attributes, BundleManifest.BUNDLE_DOCURL);
    setText(wVendorText, attributes, BundleManifest.BUNDLE_VENDOR);
    setText(wContactText, attributes, BundleManifest.BUNDLE_CONTACT);
    setText(wCopyrightText, attributes, BundleManifest.BUNDLE_COPYRIGHT);
    
    if (wCategoryTableViewer != null) {
      wCategoryTableViewer.setInput(manifest);
    }
    UiUtils.packTableColumns(wCategoryTableViewer.getTable());
    if (wEnvironmentTableViewer != null) {
      wEnvironmentTableViewer.setInput(manifest);
    }
    UiUtils.packTableColumns(wEnvironmentTableViewer.getTable());
    
    super.refresh();
  }
  
  /****************************************************************************
   * Private methods
   ***************************************************************************/
  
  private void createClient(Section section, FormToolkit toolkit) {
    
    // Set section layout
    GridData data = new GridData(SWT.FILL);
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.FILL;
    data.verticalAlignment = SWT.TOP;
    data.verticalSpan = 2;
    section.setLayoutData(data);
    
    // Create section client
    Composite container = toolkit.createComposite(section);
    TableWrapLayout layout = new TableWrapLayout();
    layout.numColumns = 3;
    container.setLayout(layout);
    
    // Basic Information section
    FormText basicText = toolkit.createFormText(container, false);
    basicText.setText("<form><p><b>Basic Information</b></p></form>", true, true);
    TableWrapData td = new TableWrapData();
    td.align = TableWrapData.FILL;
    td.grabHorizontal = true;
    td.colspan = 3;
    basicText.setLayoutData(td);
    
    wSymbolicNameStatusLabel = 
      createLabel(container, toolkit, BUNDLE_SYMBOLIC_NAME_LABEL, BUNDLE_SYMBOLIC_NAME_TOOLTIP);
    wSymbolicNameText = createText(container, toolkit, 2);
    wVersionStatusLabel = 
      createLabel(container, toolkit, BUNDLE_VERSION_LABEL, BUNDLE_VERSION_TOOLTIP);
    wVersionText = createText(container, toolkit, 2);
    wNameStatusLabel = 
      createLabel(container, toolkit, BUNDLE_NAME_LABEL, BUNDLE_NAME_TOOLTIP);
    wNameText = createText(container, toolkit, 2);
    wUpdateLocationStatusLabel =
      createLabel(container, toolkit, BUNDLE_UPDATELOCATION_LABEL, BUNDLE_UPDATELOCATION_TOOLTIP);
    wUpdateLocationText = createText(container, toolkit, 2);
    wActivatorStatusLabel = 
      createLabel(container, toolkit, BUNDLE_ACTIVATOR_LABEL, BUNDLE_ACTIVATOR_TOOLTIP);
    wActivatorText = createText(container, toolkit, 1);
    
    Button wBrowseActivatorButton = toolkit.createButton(container, "Browse...", SWT.PUSH);
    wBrowseActivatorButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        IType[] activators = project.getBundleActivators();
        TypeSelectionDialog dialog =
          new TypeSelectionDialog(Display.getCurrent().getActiveShell(), activators, "BundleActivator");
        
        if (dialog.open() == Window.OK && dialog.getResult() != null && dialog.getResult().length > 0) {
          // Set activator
          TypeSelectionDialog.Selection selection = (TypeSelectionDialog.Selection) dialog.getResult()[0];
          IType activator = selection.getType();
          if (activator != null) {
            wActivatorText.setText(activator.getFullyQualifiedName());
          } else {
            wActivatorText.setText("");
          }
          markDirty();
        }
      }
    });
    td = new TableWrapData();
    wBrowseActivatorButton.setLayoutData(td);
    
    // Documentation section
    FormText docText = toolkit.createFormText(container, false);
    docText.setText("<form><p><b>Documentation</b></p></form>", true, true);
    td = new TableWrapData();
    td.align = TableWrapData.FILL;
    td.grabHorizontal = true;
    td.colspan = 3;
    docText.setLayoutData(td);
    
    createLabel(container, toolkit, BUNDLE_DESCRIPTION_LABEL, BUNDLE_DESCRIPTION_TOOLTIP);
    wDescriptionText = createText(container, toolkit, 2);
    wDocUrlStatusLabel = 
      createLabel(container, toolkit, BUNDLE_DOCURL_LABEL, BUNDLE_DOCURL_TOOLTIP);
    wDocUrlText = createText(container, toolkit, 2);
    
    // Vendor Information section
    FormText vendorText = toolkit.createFormText(container, false);
    vendorText.setText("<form><p><b>Vendor Information</b></p></form>", true, true);
    td = new TableWrapData();
    td.align = TableWrapData.FILL;
    td.grabHorizontal = true;
    td.colspan = 3;
    vendorText.setLayoutData(td);
    
    createLabel(container, toolkit, BUNDLE_VENDOR_LABEL, BUNDLE_VENDOR_TOOLTIP);
    wVendorText = createText(container, toolkit, 2);
    createLabel(container, toolkit, BUNDLE_CONTACT_LABEL, BUNDLE_CONTACT_TOOLTIP);
    wContactText = createText(container, toolkit, 2);
    createLabel(container, toolkit, BUNDLE_COPYRIGHT_LABEL, BUNDLE_COPYRIGHT_TOOLTIP);
    wCopyrightText = createText(container, toolkit, 2);
    
    // Categories section
    FormText catText = toolkit.createFormText(container, false);
    catText.setText(
        "<form><p><b>Categories</b><br/>"+
        "List of category names.</p></form>", true, true);
    td = new TableWrapData();
    td.align = TableWrapData.FILL;
    td.grabHorizontal = true;
    td.colspan = 3;
    catText.setLayoutData(td);
    
    Composite wCategoryComposite = toolkit.createComposite(container);
    td = new TableWrapData();
    td.align = TableWrapData.FILL;
    td.grabHorizontal = true;
    td.colspan = 3;
    wCategoryComposite.setLayoutData(td);
    layout = new TableWrapLayout();
    layout.numColumns = 2;
    wCategoryComposite.setLayout(layout);
    
    // Create widgets
    Table wCategoryTable = toolkit.createTable(wCategoryComposite, SWT.MULTI | SWT.FULL_SELECTION);
    wCategoryTable.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
    
    wCategoryTableViewer = new TableViewer(wCategoryTable);
    wCategoryTableViewer.setContentProvider(new CategoryContentProvider());
    wCategoryTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = 
          (IStructuredSelection) wCategoryTableViewer.getSelection();
        
        // Enable/disable remove button
        boolean enable = false;
        if (selection != null && !selection.isEmpty()) {
          enable = true;
        }
        wCategoryRemoveButton.setEnabled(enable);
      }
    });
    new TableColumn(wCategoryTable, SWT.LEFT);
    
    TableWrapData wd = new TableWrapData();
    wd.rowspan = 2;
    wd.grabHorizontal = true;
    wd.grabVertical = true;
    wd.align = TableWrapData.FILL;
    wd.valign = TableWrapData.FILL;
    wd.heightHint = UiUtils.convertHeightInCharsToPixels(wCategoryTable, NUM_CATEGORY_TABLE_ROWS);
    wCategoryTable.setLayoutData(wd);
    
    wCategoryRemoveButton = toolkit.createButton(wCategoryComposite, "Remove", SWT.PUSH);
    wCategoryRemoveButton.setEnabled(false);
    wCategoryRemoveButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = 
          (IStructuredSelection) wCategoryTableViewer.getSelection();
        
        if (selection == null || selection.isEmpty()) return;
        
        List categories = new ArrayList(Arrays.asList(manifest.getCategories()));
        for (Iterator i = selection.iterator(); i.hasNext(); ) {
          String name = ((String) i.next()).trim();
          categories.remove(name);
        }
        manifest.setCategories((String[]) categories.toArray(new String[categories.size()]));
        wCategoryTableViewer.refresh();
        markDirty();
      }
    });
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wCategoryRemoveButton.setLayoutData(wd);
    
    wCategoryAddButton = toolkit.createButton(wCategoryComposite, "Add...", SWT.PUSH);
    wCategoryAddButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        HashMap map = new HashMap();
        String key = "category";
        PropertyDialog.Property p = new PropertyDialog.Property(key) {
          public boolean isValid(String value) {
            if (value.indexOf(",")!= -1) {
              return false;
            }
            return true;
          }
        };
        p.setLabel("Category:");
        map.put(key, p);
        PropertyDialog dialog = 
          new PropertyDialog(((Button) e.widget).getShell(), map, TITLE_ADD_CATEGORY);
        
        if (dialog.open() == Window.OK) {
          map = dialog.getValues();
          p = (PropertyDialog.Property) map.get(key);
          List categories = new ArrayList(Arrays.asList(manifest.getCategories()));
          if (p.getValue() != null && !categories.contains(p.getValue())) {
            categories.add(p.getValue());
            manifest.setCategories((String[]) categories.toArray(new String[categories.size()]));
            wCategoryTableViewer.refresh();
            UiUtils.packTableColumns(wCategoryTableViewer.getTable());
            markDirty();
          }
        }
      }
    });
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wCategoryAddButton.setLayoutData(wd);
    
    // Execution environment section
    FormText environmentText = toolkit.createFormText(container, false);
    environmentText.setText(
        "<form><p><b>Execution Environment</b><br/>"+
        "List of execution environments that must be present on the Service Platform.</p></form>", true, true);
    td = new TableWrapData();
    td.align = TableWrapData.FILL;
    td.grabHorizontal = true;
    td.colspan = 3;
    environmentText.setLayoutData(td);
    
    Composite wEnvironmentComposite = toolkit.createComposite(container);
    td = new TableWrapData();
    td.align = TableWrapData.FILL;
    td.grabHorizontal = true;
    td.colspan = 3;
    wEnvironmentComposite.setLayoutData(td);
    layout = new TableWrapLayout();
    layout.numColumns = 2;
    wEnvironmentComposite.setLayout(layout);
    
    // Create widgets
    Table wEnvironmentTable = toolkit.createTable(wEnvironmentComposite, SWT.MULTI | SWT.FULL_SELECTION);
    wEnvironmentTable.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
    
    wEnvironmentTableViewer = new TableViewer(wEnvironmentTable);
    EnvironmentContentProvider environmentProvider = new EnvironmentContentProvider();
    wEnvironmentTableViewer.setContentProvider(environmentProvider);
    wEnvironmentTableViewer.setLabelProvider(environmentProvider);
    wEnvironmentTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = 
          (IStructuredSelection) wEnvironmentTableViewer.getSelection();
        
        // Enable/disable remove button
        boolean enable = false;
        if (selection != null && !selection.isEmpty()) {
          enable = true;
        }
        wEnvironmentRemoveButton.setEnabled(enable);
      }
    });
    new TableColumn(wEnvironmentTable, SWT.LEFT);
    
    wd = new TableWrapData();
    wd.rowspan = 2;
    wd.grabHorizontal = true;
    wd.grabVertical = true;
    wd.align = TableWrapData.FILL;
    wd.valign = TableWrapData.FILL;
    wd.heightHint = UiUtils.convertHeightInCharsToPixels(wEnvironmentTable, NUM_ENVIRONMENT_TABLE_ROWS);
    wEnvironmentTable.setLayoutData(wd);
    
    wEnvironmentRemoveButton = toolkit.createButton(wEnvironmentComposite, "Remove", SWT.PUSH);
    wEnvironmentRemoveButton.setEnabled(false);
    wEnvironmentRemoveButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = 
          (IStructuredSelection) wEnvironmentTableViewer.getSelection();
        
        if (selection == null || selection.isEmpty()) return;
        
        List environments = new ArrayList(Arrays.asList(manifest.getExecutionEnvironments()));
        for (Iterator i = selection.iterator(); i.hasNext(); ) {
          String name = ((String) i.next()).trim();
          environments.remove(name);
        }
        manifest.setExecutionEnvironments((String[]) environments.toArray(new String[environments.size()]));
        wEnvironmentTableViewer.refresh();
        markDirty();
      }
    });
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wEnvironmentRemoveButton.setLayoutData(wd);
    
    wEnvironmentAddButton = toolkit.createButton(wEnvironmentComposite, "Add...", SWT.PUSH);
    wEnvironmentAddButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        EnvironmentPreference[] prefEnvironments = OsgiPreferences.getExecutionEnvironments();
        TreeSet dialogEnvironments = new TreeSet();
        for (int i=0; i<prefEnvironments.length;i++){
          if (prefEnvironments[i].getType() != EnvironmentPreference.TYPE_JRE) {
            dialogEnvironments.add(prefEnvironments[i].getName());
          }
        }
        // Remove already selected environments
        ArrayList environments = new ArrayList(Arrays.asList(manifest.getExecutionEnvironments()));
        dialogEnvironments.removeAll(environments);
        
        EnvironmentContentProvider environmentProvider = new EnvironmentContentProvider();
        ElementListSelectionDialog dialog = new ElementListSelectionDialog(
            Display.getDefault().getActiveShell(),
            environmentProvider);
        dialog.setElements(dialogEnvironments.toArray());
        dialog.setMultipleSelection(true);
        dialog.setTitle(TITLE_ADD_ENVIRONMENT);
        dialog.setImage(JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_LIBRARY));
        dialog.setMessage("Select the execution environments to add.");
        
        if (dialog.open() == Window.OK) {
          Object [] result = dialog.getResult();
          for (int i=0; i<result.length;i++) {
            environments.add(result[i]);
          }
          if (result.length > 0) {
            manifest.setExecutionEnvironments((String[]) environments.toArray(new String[environments.size()]));
            wEnvironmentTableViewer.refresh();
            UiUtils.packTableColumns(wEnvironmentTableViewer.getTable());
            markDirty();
          }
        }
      }
    });
    wd = new TableWrapData();
    wd.align = TableWrapData.FILL;
    wEnvironmentAddButton.setLayoutData(wd);
    
    container.pack(true);
    container.layout(true);
    toolkit.paintBordersFor(container);
    toolkit.paintBordersFor(wCategoryComposite);
    toolkit.paintBordersFor(wEnvironmentComposite);
    section.setClient(container);
  }
  
  private StatusLabel createLabel(Composite parent, FormToolkit toolkit, String label, String tooltip) {
    // Create label
    StatusLabel wStatusLabel = new StatusLabel(parent, SWT.NONE);
    toolkit.adapt(wStatusLabel, false, false);
    wStatusLabel.setText(label);
    wStatusLabel.setToolTipText(tooltip);

    TableWrapData td = new TableWrapData();
    td.valign = TableWrapData.MIDDLE;
    wStatusLabel.setLayoutData(td);
    
    return wStatusLabel;
  }
  
  private Text createText(Composite parent, FormToolkit toolkit, int colSpan) {
    
    // Create text
    Text wText = toolkit.createText(parent, "");
    wText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        markDirty();
      }
    });
    
    TableWrapData td = new TableWrapData();
    td.valign = TableWrapData.MIDDLE;
    td.align = TableWrapData.FILL;
    td.grabHorizontal = true;
    td.colspan = colSpan;
    wText.setLayoutData(td);
    
    return wText;
  }
  
  private void setText(Text wText, Attributes attributes, String key) {
    String value = null;
    if (attributes != null && key != null) {
      value = attributes.getValue(key);
    }
    if (value == null) {
      value = "";
    }
    if (!value.equals(wText.getText())) {
      wText.setText(value);
    }
  }
  
  /****************************************************************************
   * CategoryContentProvider Inner classes
   ***************************************************************************/
  class CategoryContentProvider implements IStructuredContentProvider {
    
    public Object[] getElements(Object inputElement) {
      if ( !(inputElement instanceof BundleManifest)) return null;
      
      BundleManifest manifest = (BundleManifest) inputElement; 
      
      return manifest.getCategories();
    }
    
    public void dispose() {
    }
    
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
  }
  
  /****************************************************************************
   * EnvironmentContentProvider Inner classes
   ***************************************************************************/
  class EnvironmentContentProvider implements IStructuredContentProvider, ILabelProvider {
    
    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement) {
      if (inputElement instanceof BundleManifest) {
        BundleManifest manifest = (BundleManifest) inputElement; 
        return manifest.getExecutionEnvironments();
      } else if (inputElement instanceof TreeSet) {
        TreeSet set = (TreeSet) inputElement;
        return set.toArray(new String[set.size()]);
      } else {
        return null;
      }
    }
    
    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
     */
    public Image getImage(Object element) {
      EnvironmentPreference[] prefEnv = OsgiPreferences.getExecutionEnvironments();
      ArrayList list = new ArrayList();
      for(int i=0; i<prefEnv.length; i++) {
        list.add(prefEnv[i].getName());
      }
      
      if (list.contains(element)){
        return JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_LIBRARY);
      }
      return imgLibraryWarning;
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element) {
      return (String) element;
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
     */
    public void dispose() {
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
