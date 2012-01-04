/*
 * Copyright (c) 2003-2012, KNOPFLERFISH project
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

package org.knopflerfish.eclipse.core.ui.launcher.main;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputer;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.knopflerfish.eclipse.core.IXArgsBundle;
import org.knopflerfish.eclipse.core.IXArgsFile;
import org.knopflerfish.eclipse.core.IXArgsProperty;
import org.knopflerfish.eclipse.core.Property;
import org.knopflerfish.eclipse.core.PropertyGroup;
import org.knopflerfish.eclipse.core.XArgsFile;
import org.knopflerfish.eclipse.core.launcher.IOsgiLaunchConfigurationConstants;
import org.knopflerfish.eclipse.core.launcher.SourcePathComputer;
import org.knopflerfish.eclipse.core.preferences.FrameworkPreference;
import org.knopflerfish.eclipse.core.preferences.OsgiPreferences;
import org.knopflerfish.eclipse.core.ui.OsgiUiPlugin;
import org.knopflerfish.eclipse.core.ui.dialogs.ImportXargsDialog;
import org.knopflerfish.eclipse.core.ui.launcher.CustomJavaArgumentsTab;
import org.knopflerfish.eclipse.core.ui.launcher.bundle.BundleTab;

/**
 * @author Anders Rimén, Makewave
 * @see http://www.makewave.com/
 */
public class MainTab extends AbstractLaunchConfigurationTab
{
  private static String                IMAGE                      =
                                                                    "icons/obj16/kf-16x16.png";

  // Default values
  private static final String          DEFAULT_RUNTIME_PATH       =
                                                                    "runtime-osgi";
  public static final int              DEFAULT_START_LEVEL        = 10;

  // Column Properties
  public static String                 PROP_NAME                  = "name";
  public static String                 PROP_VALUE                 = "value";
  public static String                 PROP_TYPE                  = "type";

  private int                          MARGIN                     = 5;

  protected static final String        USER_GROUP                 =
                                                                    "User Defined";
  protected static final String        DEFAULT_USER_PROPERTY_NAME =
                                                                    "user.property.";

  // Widgets
  private Composite                    wPageComposite;
  private Combo                        wOsgiInstallCombo;
  Text                                 wInstanceDirText;
  private Spinner                      wStartLevelSpinner;
  private Button                       wInitButton;
  private Button                       wAddPropertyButton;
  private Button                       wRemovePropertyButton;
  private Label                        wErrorXargs;

  TreeViewer                           wPropertyTreeViewer;
  int                                  treeWidth                  = -1;

  // Resources
  private Image                        imageTab                   = null;
  private final CustomJavaArgumentsTab argTab;
  private final BundleTab              bundleTab;

  private final PropertyGroup          userGroup                  =
                                                                    new PropertyGroup(
                                                                                      USER_GROUP);
  Map<String, String>                  systemProperties;

  public MainTab(CustomJavaArgumentsTab argTab, BundleTab bundleTab)
  {
    this.argTab = argTab;
    this.bundleTab = bundleTab;
    ImageDescriptor id =
      AbstractUIPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui",
                                                 IMAGE);
    if (id != null) {
      imageTab = id.createImage();
    }
  }

  // ***************************************************************************
  // org.eclipse.debug.ui.ILaunchConfigurationTab Methods
  // ***************************************************************************
  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
   */
  public String getName()
  {
    return "Main";
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
   */
  public Image getImage()
  {
    return imageTab;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
   */
  public void dispose()
  {
    if (imageTab != null) {
      imageTab.dispose();
      imageTab = null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.
   * swt.widgets.Composite)
   */
  public void createControl(Composite parent)
  {

    wPageComposite = new Composite(parent, 0);
    setControl(wPageComposite);
    GridLayout layout = new GridLayout();
    layout.marginHeight = MARGIN;
    layout.marginWidth = MARGIN;
    layout.numColumns = 1;
    wPageComposite.setLayout(layout);

    // Knopflerfish xargs Group
    Group wXargsGroup = new Group(wPageComposite, SWT.SHADOW_IN);
    layout = new GridLayout();
    layout.marginHeight = MARGIN;
    layout.marginWidth = MARGIN;
    layout.numColumns = 3;
    wXargsGroup.setLayout(layout);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    wXargsGroup.setLayoutData(gd);
    wXargsGroup.setText("Knopflerfish Xargs");
    Button wImportButton = new Button(wXargsGroup, SWT.CENTER);
    wImportButton.setText("Import...");
    wImportButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e)
      {
        ImportXargsDialog dialog =
          new ImportXargsDialog(((Button) e.widget).getShell(),
                                "Import xargs file");
        if (dialog.open() == Window.OK) {
          File f = dialog.getXargsFile();
          boolean merge = dialog.merge();
          try {
            XArgsFile xargsFile = new XArgsFile(f.getParentFile(), f.getName());
            if (!merge) {
              // Clear
              clearConfiguration();
            }
            if (importXargs(xargsFile)) {
              wErrorXargs.setText("");
            } else {
              wErrorXargs.setText("Error when importing xargs file, could not resolve all bundles. See log for more info.");
            }
          } catch (Exception ioe) {
            // Failed to parse xargs file
            OsgiUiPlugin.log(new Status(IStatus.ERROR,
                                        "org.knopflerfish.eclipse.core",
                                        IStatus.OK,
                                        "Failed to import xargs file.", ioe));
            wErrorXargs.setText("Error when importing xargs file, failed to read file. See log for more info.");
          }
        }
      }

    });
    // Do we need to export
    /*
    Button wExportButton = new Button(wXargsGroup, SWT.CENTER);
    wExportButton.setText("Export...");
    wExportButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e)
      {
        FileDialog dialog =
          new FileDialog(((Button) e.widget).getShell(), SWT.SAVE);

        dialog.setText("Export xargs");
        String[] filterExt = {
          "*.xargs", "*.*"
        };
        dialog.setFilterExtensions(filterExt);
        // dialog.setFilterPath(wInstanceDirText.getText());
        String path = dialog.open();
        if (path != null) {
          // wInstanceDirText.setText(path);
          // updateDialog();
        }
      }
    });
    // TBI : Not yet implemented
    wExportButton.setEnabled(false);
    */

    // Import/export error messages
    wErrorXargs = new Label(wXargsGroup, SWT.LEFT | SWT.WRAP);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wErrorXargs.setForeground(new Color(null, 255, 0, 0));
    wErrorXargs.setLayoutData(gd);

    // Location Group
    Group wLocationGroup = new Group(wPageComposite, SWT.SHADOW_IN);
    layout = new GridLayout();
    layout.marginHeight = MARGIN;
    layout.marginWidth = MARGIN;
    layout.numColumns = 3;
    wLocationGroup.setLayout(layout);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wLocationGroup.setLayoutData(gd);
    wLocationGroup.setText("Instance Data");
    Label wLocationLabel = new Label(wLocationGroup, SWT.LEFT | SWT.WRAP);
    wLocationLabel.setText("Location:");
    // wLocationLabel.setToolTipText(TOOLTIP_LOCATION);
    wInstanceDirText = new Text(wLocationGroup, SWT.SINGLE | SWT.BORDER);
    wInstanceDirText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e)
      {
        updateDialog();
      }
    });
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wInstanceDirText.setLayoutData(gd);
    Button wBrowseLocationButton = new Button(wLocationGroup, SWT.CENTER);
    wBrowseLocationButton.setText("Browse...");
    wBrowseLocationButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e)
      {
        DirectoryDialog dialog =
          new DirectoryDialog(((Button) e.widget).getShell());
        dialog.setFilterPath(wInstanceDirText.getText());
        String path = dialog.open();
        if (path != null) {
          wInstanceDirText.setText(path);
          updateDialog();
        }
      }
    });

    // Framework Group
    Group wFrameworkGroup = new Group(wPageComposite, SWT.SHADOW_IN);
    layout = new GridLayout();
    layout.marginHeight = MARGIN;
    layout.marginWidth = MARGIN;
    layout.numColumns = 2;
    wFrameworkGroup.setLayout(layout);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wFrameworkGroup.setLayoutData(gd);
    wFrameworkGroup.setText("Framework to Run");

    // OSGi framework
    Label wFrameworkLabel = new Label(wFrameworkGroup, SWT.LEFT | SWT.WRAP);
    wFrameworkLabel.setText("Framework:");
    wOsgiInstallCombo =
      new Combo(wFrameworkGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
    wOsgiInstallCombo.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e)
      {
        updateDialog();
        // Update System Properties accepted by this framework
        initializeSystemProperties(systemProperties);
      }
    });
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wOsgiInstallCombo.setLayoutData(gd);
    Label wStartLevelLabel = new Label(wFrameworkGroup, SWT.LEFT | SWT.WRAP);
    wStartLevelLabel.setText("Initial Start Level:");
    wStartLevelSpinner = new Spinner(wFrameworkGroup, SWT.READ_ONLY);
    wStartLevelSpinner.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e)
      {
        updateDialog();
      }
    });
    wStartLevelSpinner.setMinimum(0);
    wStartLevelSpinner.setMaximum(99);

    wInitButton = new Button(wFrameworkGroup, SWT.CHECK);
    wInitButton.setText("Clear bundle cache when starting framework");
    wInitButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e)
      {
        argTab.setInitFlag(wInitButton.getSelection());
        updateDialog();
      }
    });
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 2;
    wInitButton.setLayoutData(gd);

    // System Properties Group
    Group wPropertyGroup = new Group(wPageComposite, SWT.SHADOW_IN);
    FormLayout formLayout = new FormLayout();
    formLayout.marginHeight = MARGIN;
    formLayout.marginWidth = MARGIN;
    wPropertyGroup.setLayout(formLayout);
    gd = new GridData(GridData.FILL_BOTH);
    wPropertyGroup.setLayoutData(gd);
    wPropertyGroup.setText("Properties");

    Tree wPropertyTree =
      new Tree(wPropertyGroup, SWT.BORDER | SWT.FULL_SELECTION);
    wPropertyTreeViewer = new TreeViewer(wPropertyTree);
    ColumnViewerToolTipSupport.enableFor(wPropertyTreeViewer);
    wPropertyTreeViewer.setContentProvider(new SystemPropertyContentProvider());
    wPropertyTreeViewer.setLabelProvider(new SystemPropertyLabelProvider());
    wPropertyTreeViewer.setSorter(new SystemPropertySorter());
    wPropertyTreeViewer.setColumnProperties(new String[]{
      PROP_NAME, PROP_VALUE, PROP_TYPE
    });
    wPropertyTreeViewer.setCellModifier(new SystemPropertyCellModifier(this));
    TextCellEditor propertyNameEditor =
      new TextCellEditor(wPropertyTree, SWT.NONE);
    TextCellEditor propertyValueEditor =
      new TextCellEditor(wPropertyTree, SWT.NONE);
    ComboBoxCellEditor propertyTypeEditor =
      new ComboBoxCellEditor(wPropertyTree, Property.TYPES, SWT.DROP_DOWN
                                                            | SWT.READ_ONLY);
    wPropertyTreeViewer.setCellEditors(new CellEditor[]{
      propertyNameEditor, propertyValueEditor, propertyTypeEditor
    });

    wPropertyTree.setHeaderVisible(true);
    wPropertyTree.setLinesVisible(true);
    // Tree columns
    TreeColumn wPropertyTreeColumn = new TreeColumn(wPropertyTree, SWT.LEFT);
    wPropertyTreeColumn.setText("Property");
    TreeColumn wValueTreeColumn = new TreeColumn(wPropertyTree, SWT.LEFT);
    wValueTreeColumn.setText("Value");
    TreeColumn wTypeTreeColumn = new TreeColumn(wPropertyTree, SWT.LEFT);
    wTypeTreeColumn.setText("Type");
    wPropertyTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event)
      {
        // Show property info
        showPropertyInfo();
      }
    });
    wPropertyTreeViewer.getTree().addControlListener(new ControlListener() {

      public void controlMoved(ControlEvent e)
      {
      }

      public void controlResized(ControlEvent e)
      {
        Tree tree = wPropertyTreeViewer.getTree();
        int width = tree.getBounds().width;
        int borderWidth = tree.getBorderWidth();
        int gridLineWidth = tree.getGridLineWidth();
        int barWidth = 0;
        ScrollBar bar = tree.getVerticalBar();
        if (bar != null && bar.isVisible()) {
          barWidth = bar.getSize().x;
        }

        TreeColumn[] columns = tree.getColumns();
        width =
          width - 2 * borderWidth - (columns.length - 1) * gridLineWidth
              - barWidth;
        if (width == treeWidth) return;
        treeWidth = width;

        int colWidth = width / 4;
        // Name columm
        columns[0].setWidth(2 * colWidth);
        // Value columm
        columns[1].setWidth(colWidth);
        // Type columm
        columns[2].setWidth(colWidth);
      }

    });

    wAddPropertyButton = new Button(wPropertyGroup, SWT.PUSH);
    wAddPropertyButton.setText("Add");
    wAddPropertyButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e)
      {
        // Find unused property name
        StringBuffer buf = new StringBuffer(DEFAULT_USER_PROPERTY_NAME);
        int idx = 0;
        Property property = null;
        do {
          buf.setLength(DEFAULT_USER_PROPERTY_NAME.length());
          buf.append(idx++);
          property = new Property(buf.toString());
        } while (userGroup.contains(property));

        // System property and start edit property name
        userGroup.addSystemProperty(property);
        wPropertyTreeViewer.refresh();
        wPropertyTreeViewer.editElement(property, 0);

        updateDialog();
      }
    });
    wRemovePropertyButton = new Button(wPropertyGroup, SWT.PUSH);
    wRemovePropertyButton.setText("Remove");
    wRemovePropertyButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e)
      {
        IStructuredSelection selection =
          (IStructuredSelection) wPropertyTreeViewer.getSelection();

        Property property = null;
        if (selection != null && !selection.isEmpty()
            && selection.getFirstElement() instanceof Property) {
          property = (Property) selection.getFirstElement();
        }

        if (property != null) {
          userGroup.removeSystemProperty(property);
          wPropertyTreeViewer.refresh();
          updateDialog();
        }
      }
    });

    // Layout
    FormData fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(0, 0);
    fd.right = new FormAttachment(wRemovePropertyButton, -5, SWT.LEFT);
    fd.bottom = new FormAttachment(100, 0);
    wPropertyTree.setLayoutData(fd);

    fd = new FormData();
    // data.left = new FormAttachment(wBundleAttachButton, 0, SWT.LEFT);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(wPropertyTree, 0, SWT.TOP);
    wRemovePropertyButton.setLayoutData(fd);

    fd = new FormData();
    fd.left = new FormAttachment(wRemovePropertyButton, 0, SWT.LEFT);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(wRemovePropertyButton, 5, SWT.BOTTOM);
    wAddPropertyButton.setLayoutData(fd);

    showPropertyInfo();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug
   * .core.ILaunchConfigurationWorkingCopy)
   */
  public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
  {
    // New configuration created, set default values

    // Set default framework
    FrameworkPreference distribution = OsgiPreferences.getDefaultFramework();
    if (distribution != null) {
      configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_FRAMEWORK,
                                 distribution.getName());
    }

    // Set default instance directory
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IPath path = root.getLocation();
    path = path.append(DEFAULT_RUNTIME_PATH);
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_INSTANCE_DIR,
                               path.toString());

    // Set default instance settings, add program argument "-init"
    /*
     * String programArgs = ""; try { programArgs =
     * configuration.getAttribute(IJavaLaunchConfigurationConstants
     * .ATTR_PROGRAM_ARGUMENTS, ""); } catch (CoreException e) { }
     * ExecutionArguments execArgs = new ExecutionArguments("", programArgs); if
     * (!Arrays.asList(execArgs.getProgramArgumentsArray()).contains("-init")) {
     * configuration
     * .setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
     * "-init "+programArgs); }
     */

    // Set default start level
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_START_LEVEL,
                               DEFAULT_START_LEVEL);

    // Set default properties
    HashMap<String, String> properties = new HashMap<String, String>();
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_PROPERTIES,
                               properties);

    configuration.setAttribute(ISourcePathComputer.ATTR_SOURCE_PATH_COMPUTER_ID,
                               SourcePathComputer.ID);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse
   * .debug.core.ILaunchConfiguration)
   */
  public void initializeFrom(ILaunchConfiguration configuration)
  {
    // Set values to GUI widgets
    wErrorXargs.setText("");

    // OSGi install
    updateOsgiInstalls();
    String installName = null;
    try {
      installName =
        configuration.getAttribute(IOsgiLaunchConfigurationConstants.ATTR_FRAMEWORK,
                                   (String) null);
    } catch (CoreException e) {
      OsgiUiPlugin.log(e.getStatus());
    }
    int idx = 0;
    if (installName != null) {
      idx = wOsgiInstallCombo.indexOf(installName);
      if (idx < 0) idx = 0;
    }
    wOsgiInstallCombo.select(idx);

    // Instance directory
    String instanceDir = null;
    try {
      instanceDir =
        configuration.getAttribute(IOsgiLaunchConfigurationConstants.ATTR_INSTANCE_DIR,
                                   "");
    } catch (CoreException e) {
      OsgiUiPlugin.log(e.getStatus());
    }
    wInstanceDirText.setText(instanceDir);

    // Clear bundle cache
    /*
     * String programArgs = ""; try { programArgs =
     * configuration.getAttribute(IJavaLaunchConfigurationConstants
     * .ATTR_PROGRAM_ARGUMENTS, ""); } catch (CoreException e) { }
     * ExecutionArguments execArgs = new ExecutionArguments("", programArgs);
     * wInitButton
     * .setSelection(Arrays.asList(execArgs.getProgramArgumentsArray()
     * ).contains("-init"));
     */
    argTab.initializeFrom(configuration);
    wInitButton.setSelection(argTab.getInitFlag());

    // Start level
    int startLevel = DEFAULT_START_LEVEL;
    try {
      startLevel =
        configuration.getAttribute(IOsgiLaunchConfigurationConstants.ATTR_START_LEVEL,
                                   DEFAULT_START_LEVEL);
    } catch (CoreException e) {
      OsgiUiPlugin.log(e.getStatus());
    }
    wStartLevelSpinner.setSelection(startLevel);

    // Initialize default system properties list
    try {
      systemProperties =
        configuration.getAttribute(IOsgiLaunchConfigurationConstants.ATTR_PROPERTIES,
                                   (Map<String, String>) null);
      initializeSystemProperties(systemProperties);
    } catch (CoreException e) {
      OsgiUiPlugin.log(e.getStatus());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug
   * .core.ILaunchConfigurationWorkingCopy)
   */
  public void performApply(ILaunchConfigurationWorkingCopy configuration)
  {

    // Read values from GUI widgets
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_FRAMEWORK,
                               wOsgiInstallCombo.getText());
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_INSTANCE_DIR,
                               wInstanceDirText.getText());

    /*
     * String programArgs = ""; boolean programArgsChanged = false; try {
     * programArgs =
     * configuration.getAttribute(IJavaLaunchConfigurationConstants
     * .ATTR_PROGRAM_ARGUMENTS, ""); } catch (CoreException e) { }
     * ExecutionArguments execArgs = new ExecutionArguments("", programArgs);
     * List argsList = new
     * ArrayList(Arrays.asList(execArgs.getProgramArgumentsArray())); if
     * (!argsList.contains("-init")) { if (wInitButton.getSelection()) {
     * argsList.add(0, "-init"); programArgsChanged = true; } } else { if
     * (!wInitButton.getSelection()) { while(argsList.contains("-init")) {
     * argsList.remove("-init"); programArgsChanged = true; } } } if
     * (programArgsChanged) { StringBuffer buf = new StringBuffer(); for
     * (Iterator i=argsList.iterator(); i.hasNext(); ) { String a = (String)
     * i.next(); if (buf.length() > 0) { buf.append(" "); } buf.append(a); } if
     * (buf.length() == 0) {
     * configuration.setAttribute(IJavaLaunchConfigurationConstants
     * .ATTR_PROGRAM_ARGUMENTS, (String) null); } else {
     * configuration.setAttribute
     * (IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
     * buf.toString()); } }
     */
    argTab.performApply(configuration);
    bundleTab.performApply(configuration);

    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_START_LEVEL,
                               wStartLevelSpinner.getSelection());
    configuration.setAttribute(ISourcePathComputer.ATTR_SOURCE_PATH_COMPUTER_ID,
                               SourcePathComputer.ID);

    // System Properties
    systemProperties = getSystemProperties();
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_PROPERTIES,
                               systemProperties);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.
   * core.ILaunchConfiguration)
   */
  public boolean isValid(ILaunchConfiguration configuration)
  {
    // Verify Osgi install
    String name = null;
    try {
      name =
        configuration.getAttribute(IOsgiLaunchConfigurationConstants.ATTR_FRAMEWORK,
                                   (String) null);
    } catch (CoreException e) {
      OsgiUiPlugin.log(e.getStatus());
    }
    if (OsgiPreferences.getFramework(name) == null) {
      setErrorMessage("No framework selected.");
      return false;
    }

    // TODO: Verify instance directory

    setErrorMessage(null);
    return true;
  }

  // ***************************************************************************
  // Private utility methods
  // ***************************************************************************
  private void updateOsgiInstalls()
  {
    wOsgiInstallCombo.removeAll();
    FrameworkPreference[] distributions = OsgiPreferences.getFrameworks();

    for (int i = 0; i < distributions.length; i++) {
      wOsgiInstallCombo.add(distributions[i].getName());
    }
  }

  void initializeSystemProperties(Map<String, String> properties)
  {
    FrameworkPreference distribution =
      OsgiPreferences.getFramework(wOsgiInstallCombo.getText());
    if (distribution == null) return;
    distribution.addSystemPropertyGroup(userGroup);
    userGroup.clear();
    if (properties != null) {
      for (Map.Entry<String, String> element : properties.entrySet()) {
        String name = element.getKey();
        Property property = distribution.findSystemProperty(name);
        String value = element.getValue();
        String type = Property.SYSTEM_PROPERTY;
        if (value.startsWith(Property.SYSTEM_PROPERTY + ":")) {
          type = Property.SYSTEM_PROPERTY;
          value = value.substring(Property.SYSTEM_PROPERTY.length() + 1);
        } else if (value.startsWith(Property.FRAMEWORK_PROPERTY + ":")) {
          type = Property.FRAMEWORK_PROPERTY;
          value = value.substring(Property.FRAMEWORK_PROPERTY.length() + 1);
        }
        if (property != null) {
          property.setValue(value);
          property.setType(type);
        } else {
          property = new Property(name);
          property.setValue(value);
          property.setType(type);
          userGroup.addSystemProperty(property);
        }
      }
    }
    wPropertyTreeViewer.setInput(distribution);
  }

  private Map<String, String> getSystemProperties()
  {
    FrameworkPreference distribution =
      (FrameworkPreference) wPropertyTreeViewer.getInput();
    if (distribution == null) return null;

    PropertyGroup[] groups = distribution.getSystemPropertyGroups();
    if (groups == null) return null;

    HashMap<String, String> map = new HashMap<String, String>();
    for (int i = 0; i < groups.length; i++) {
      Property[] properties = groups[i].getProperties();
      if (properties == null) continue;
      for (int j = 0; j < properties.length; j++) {
        if (!isDefaultProperty(properties[j])) {
          String name = properties[j].getName();
          StringBuffer value = new StringBuffer(properties[j].getType());
          value.append(':');
          value.append(properties[j].getValue());
          map.put(name, value.toString());
        }
      }
    }
    return map;
  }

  protected static boolean isDefaultProperty(Property property)
  {
    if (property == null) return false;

    if (MainTab.USER_GROUP.equals(property.getSystemPropertyGroup().getName()))
      return false;

    String value = property.getValue();
    if (value == null) value = "";
    String defaultValue = property.getDefaultValue();
    if (defaultValue == null) defaultValue = "";

    if (value.equals(defaultValue)) return true;

    return false;
  }

  void showPropertyInfo()
  {
    IStructuredSelection selection =
      (IStructuredSelection) wPropertyTreeViewer.getSelection();

    Property property = null;
    if (selection != null && !selection.isEmpty()
        && selection.getFirstElement() instanceof Property) {
      property = (Property) selection.getFirstElement();
    }

    if (property == null) {
      wRemovePropertyButton.setEnabled(false);
    } else {
      // Buttons
      if (USER_GROUP.equals(property.getSystemPropertyGroup().getName())) {
        wRemovePropertyButton.setEnabled(true);
      } else {
        wRemovePropertyButton.setEnabled(false);
      }
    }
  }

  protected void update(Object element)
  {
    wPropertyTreeViewer.update(element, null);
    // scheduleUpdateJob();
    updateLaunchConfigurationDialog();
  }

  protected void updateDialog()
  {
    // scheduleUpdateJob();
    updateLaunchConfigurationDialog();
  }

  private boolean importXargs(IXArgsFile xArgsFile)
  {
    FrameworkPreference distribution =
      OsgiPreferences.getFramework(wOsgiInstallCombo.getText());

    boolean importOk = true;

    if (distribution != null) {
      // Set system properties
      Set<String> systemPropertyNames = xArgsFile.getSystemPropertyNames();
      for (Iterator<String> i = systemPropertyNames.iterator(); i.hasNext();) {
        String name = i.next();
        IXArgsProperty p = xArgsFile.getSystemProperty(name);
        Property property = distribution.findSystemProperty(name);
        if (property == null) {
          property = userGroup.findSystemProperty(name);
          if (property == null) {
            property = new Property(name);
            userGroup.addSystemProperty(property);
          }
        }
        property.setType(Property.SYSTEM_PROPERTY);
        property.setValue(p.getValue());

      }
      // Set framework properties
      Set<String> frameworkPropertyNames =
        xArgsFile.getFrameworkPropertyNames();
      for (Iterator<String> i = frameworkPropertyNames.iterator(); i.hasNext();) {
        String name = i.next();
        IXArgsProperty p = xArgsFile.getFrameworkProperty(name);
        Property property = distribution.findSystemProperty(name);
        if (property == null) {
          property = userGroup.findSystemProperty(name);
          if (property == null) {
            property = new Property(name);
            userGroup.addSystemProperty(property);
          }
        }
        property.setType(Property.FRAMEWORK_PROPERTY);
        property.setValue(p.getValue());

      }
      distribution.addSystemPropertyGroup(userGroup);
      wPropertyTreeViewer.setInput(distribution);
    }

    // Set start level
    wStartLevelSpinner.setSelection(xArgsFile.getStartLevel());

    // Read init flag from xargs file
    wInitButton.setSelection(xArgsFile.clearPersistentData());
    argTab.setInitFlag(xArgsFile.clearPersistentData());

    // Add bundles
    for (Iterator<IXArgsBundle> i = xArgsFile.getBundles().iterator(); i.hasNext();) {
      IXArgsBundle xArgsBundle = i.next();
      try {
        bundleTab.addBundle(xArgsBundle);
      } catch (Throwable t) {
        importOk = false;
        OsgiUiPlugin.log(new Status(IStatus.ERROR,
                                    "org.knopflerfish.eclipse.core",
                                    IStatus.OK,
                                    "Failed to find bundle ["
                                        + xArgsBundle.getLocation()
                                        + "] when importing xargs file.", t));

      }
    }
    bundleTab.refresh();

    return importOk;
  }

  private void clearConfiguration()
  {
    // Set default instance location
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IPath path = root.getLocation();
    path = path.append(DEFAULT_RUNTIME_PATH);
    wInstanceDirText.setText(path.toString());

    // Set default framework

    // Set default start level
    wStartLevelSpinner.setSelection(DEFAULT_START_LEVEL);

    // Set clear bundle cache
    wInitButton.setSelection(false);
    argTab.setInitFlag(false);

    // Clear properties
    systemProperties.clear();
    initializeSystemProperties(systemProperties);

    // Clear bundles
    bundleTab.clearBundles();

    updateDialog();
  }

}
