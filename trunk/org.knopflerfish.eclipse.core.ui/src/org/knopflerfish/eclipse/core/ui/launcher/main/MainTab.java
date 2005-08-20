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

package org.knopflerfish.eclipse.core.ui.launcher.main;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputer;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.knopflerfish.eclipse.core.IOsgiInstall;
import org.knopflerfish.eclipse.core.Osgi;
import org.knopflerfish.eclipse.core.SystemProperty;
import org.knopflerfish.eclipse.core.SystemPropertyGroup;
import org.knopflerfish.eclipse.core.launcher.IOsgiLaunchConfigurationConstants;
import org.knopflerfish.eclipse.core.launcher.SourcePathComputer;
import org.knopflerfish.eclipse.core.ui.OsgiUiPlugin;
import org.knopflerfish.eclipse.core.ui.UiUtils;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class MainTab extends AbstractLaunchConfigurationTab {
  private static String IMAGE = "icons/obj16/osgi_obj.gif";
  
  // Default values
  private static final String DEFAULT_RUNTIME_PATH = "runtime-osgi";
  private static final int DEFAULT_START_LEVEL     = 7;
  
  // Column Properties
  public static String PROP_NAME  = "name";
  public static String PROP_VALUE = "value";
  
  private static final int NUM_ROWS_DESCRIPTION = 5;
  private int MARGIN = 5;
  
  protected static final String USER_GROUP                 = "User Defined";
  protected static final String DEFAULT_USER_PROPERTY_NAME = "user.property.";
  
  // Widgets
  private Composite wPageComposite;
  private Combo     wOsgiInstallCombo;
  private Text      wInstanceDirText;
  private Spinner   wStartLevelSpinner;
  private Button    wInitButton;
  private Button    wAddPropertyButton;
  private Button    wRemovePropertyButton;
  private Label     wDescriptionLabel;
  private Label     wDescriptionText;
  private Label     wDefaultLabel;
  private Label     wDefaultText;
  
  // jFace Widgets 
  private TreeViewer    wPropertyTreeViewer;
  private int treeWidth = -1;

  // Resources
  private Image imageTab = null;
  
  private SystemPropertyGroup userGroup = new SystemPropertyGroup(USER_GROUP);
  private Map systemProperties;
  
  public MainTab() {
    ImageDescriptor id = OsgiUiPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui", IMAGE);
    if (id != null) {
      imageTab = id.createImage();
    }
  }
  
  /****************************************************************************
   * org.eclipse.debug.ui.ILaunchConfigurationTab Methods
   ***************************************************************************/
  /* (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
   */
  public String getName() {
    return "Main";
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
   */
  public Image getImage() {
    return imageTab;
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
   */
  public void dispose() {
    if (imageTab != null) {
      imageTab.dispose();
      imageTab = null;
    }
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl(Composite parent) {
    
    wPageComposite = new Composite(parent, 0);
    setControl(wPageComposite);
    GridLayout layout = new GridLayout();
    layout.marginHeight = MARGIN;
    layout.marginWidth = MARGIN;
    layout.numColumns = 1;
    wPageComposite.setLayout(layout);
    
    // Location Group
    Group wLocationGroup = new Group(wPageComposite, SWT.SHADOW_IN);
    layout = new GridLayout();
    layout.marginHeight = MARGIN;
    layout.marginWidth = MARGIN;
    layout.numColumns = 3;
    wLocationGroup.setLayout(layout);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    wLocationGroup.setLayoutData(gd);
    wLocationGroup.setText("Instance Data");
    Label wLocationLabel = new Label(wLocationGroup, SWT.LEFT | SWT.WRAP);
    wLocationLabel.setText("Location:");
    //wLocationLabel.setToolTipText(TOOLTIP_LOCATION);
    wInstanceDirText = new Text(wLocationGroup, SWT.SINGLE | SWT.BORDER);
    wInstanceDirText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        updateLaunchConfigurationDialog();
      }
    });
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wInstanceDirText.setLayoutData(gd);
    Button wBrowseLocationButton = new Button(wLocationGroup, SWT.CENTER);
    wBrowseLocationButton.setText("Browse...");
    wBrowseLocationButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        DirectoryDialog dialog = new DirectoryDialog(((Button) e.widget).getShell());
        dialog.setFilterPath(wInstanceDirText.getText());
        String path = dialog.open();
        if (path != null) {
          wInstanceDirText.setText(path);
          updateLaunchConfigurationDialog();
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
    wOsgiInstallCombo = new Combo(wFrameworkGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
    wOsgiInstallCombo.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        updateLaunchConfigurationDialog();
        // Update System Properties accepted by this framework
        initializeSystemProperties(systemProperties);
      }
    });
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wOsgiInstallCombo.setLayoutData(gd);
    Label wStartLevelLabel = new Label(wFrameworkGroup, SWT.LEFT | SWT.WRAP);
    wStartLevelLabel.setText("Initial Start Level:");
    wStartLevelSpinner = new Spinner(wFrameworkGroup, SWT.READ_ONLY);
    wStartLevelSpinner.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        updateLaunchConfigurationDialog();
      }
    });
    wStartLevelSpinner.setMinimum(0);
    wStartLevelSpinner.setMaximum(99);

    wInitButton = new Button(wFrameworkGroup, SWT.CHECK);
    wInitButton.setText("Clear bundle cache when starting framework");
    wInitButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        updateLaunchConfigurationDialog();
      }
    });
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=2;
    wInitButton.setLayoutData(gd);
    
    // Properties Group
    Group wPropertyGroup = new Group(wPageComposite, SWT.SHADOW_IN);
    FormLayout formLayout = new FormLayout();
    formLayout.marginHeight = MARGIN;
    formLayout.marginWidth = MARGIN;
    wPropertyGroup.setLayout(formLayout);
    gd = new GridData(GridData.FILL_BOTH);
    wPropertyGroup.setLayoutData(gd);
    wPropertyGroup.setText("System Properties");
    
    Tree wPropertyTree = new Tree(wPropertyGroup, SWT.BORDER | SWT.FULL_SELECTION);
    wPropertyTreeViewer = new TreeViewer(wPropertyTree);
    wPropertyTreeViewer.setContentProvider(new SystemPropertyContentProvider());
    wPropertyTreeViewer.setLabelProvider(new SystemPropertyLabelProvider());
    wPropertyTreeViewer.setSorter(new SystemPropertySorter());
    wPropertyTreeViewer.setColumnProperties(new String[] {PROP_NAME, PROP_VALUE});
    wPropertyTreeViewer.setCellModifier(new SystemPropertyCellModifier(this));
    TextCellEditor propertyNameEditor = new TextCellEditor(wPropertyTree, SWT.NONE);
    TextCellEditor propertyValueEditor = new TextCellEditor(wPropertyTree, SWT.NONE);
    wPropertyTreeViewer.setCellEditors(
        new CellEditor[] {propertyNameEditor, propertyValueEditor});

    wPropertyTree.setHeaderVisible(true);
    wPropertyTree.setLinesVisible(true);
    // Tree columns
    TreeColumn wPropertyTreeColumn = new TreeColumn(wPropertyTree, SWT.LEFT);
    wPropertyTreeColumn.setText("Property");
    TreeColumn wValueTreeColumn = new TreeColumn(wPropertyTree, SWT.LEFT);
    wValueTreeColumn.setText("Value");
    wPropertyTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        // Show property info
        showPropertyInfo();
      }
    });
    wPropertyTreeViewer.getTree().addControlListener(new ControlListener() {

      public void controlMoved(ControlEvent e) {
      }

      public void controlResized(ControlEvent e) {
        Tree tree = wPropertyTreeViewer.getTree();
        int width = tree.getBounds().width;
        int borderWidth = tree.getBorderWidth();
        int gridLineWidth = tree.getGridLineWidth();
        int barWidth = 0;
        ScrollBar bar = tree.getVerticalBar();
        if (bar != null && bar.isVisible()) {
          barWidth = bar.getSize().x;
        }        
        
        TreeColumn [] columns = tree.getColumns();
        width = width-2*borderWidth-(columns.length-1)*gridLineWidth-barWidth;
        if (width == treeWidth) return;
        treeWidth = width;
        
        int colWidth = width / 3;
        // Name columm
        columns[0].setWidth(2*colWidth);
        // Value columm
        columns[1].setWidth(colWidth);
      }
      
    });
    
    wAddPropertyButton = new Button(wPropertyGroup, SWT.PUSH);
    wAddPropertyButton.setText("Add...");
    wAddPropertyButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        // Find unused property name
        StringBuffer buf = new StringBuffer(DEFAULT_USER_PROPERTY_NAME);
        int idx = 0;
        SystemProperty property = null;
        do {
          buf.setLength(DEFAULT_USER_PROPERTY_NAME.length());
          buf.append(idx++);
          property = new SystemProperty(buf.toString());
        } while(userGroup.contains(property)); 

        // System property and start edit property name
        userGroup.addSystemProperty(property);
        wPropertyTreeViewer.refresh();
        wPropertyTreeViewer.editElement(property, 0);

        updateLaunchConfigurationDialog();
      }
    });
    wRemovePropertyButton = new Button(wPropertyGroup, SWT.PUSH);
    wRemovePropertyButton.setText("Remove");
    wRemovePropertyButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = 
          (IStructuredSelection) wPropertyTreeViewer.getSelection();

        SystemProperty property = null;
        if (selection != null && !selection.isEmpty() && selection.getFirstElement() instanceof SystemProperty) {
          property = (SystemProperty) selection.getFirstElement();
        }
        
        if (property != null) {
          userGroup.removeSystemProperty(property);
          wPropertyTreeViewer.refresh();
          updateLaunchConfigurationDialog();
        }
      }
    });
    
    //Description
    wDescriptionLabel = new Label(wPropertyGroup, SWT.LEFT);
    wDescriptionLabel.setText("Description:");
    wDescriptionText = new Label(wPropertyGroup, SWT.LEFT| SWT.WRAP);
    wDefaultLabel = new Label(wPropertyGroup, SWT.LEFT);
    wDefaultLabel.setText("Default Value:");
    wDefaultText = new Label(wPropertyGroup, SWT.LEFT);

    // Layout
    FormData fd = new FormData();
    fd.left = new FormAttachment(0,0);
    fd.top = new FormAttachment(0,0);
    fd.right = new FormAttachment(wRemovePropertyButton,-5,SWT.LEFT);
    fd.bottom = new FormAttachment(wDescriptionLabel, -5, SWT.TOP);
    wPropertyTree.setLayoutData(fd);
    
    fd = new FormData();
    //data.left = new FormAttachment(wBundleAttachButton, 0, SWT.LEFT);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(wPropertyTree,0,SWT.TOP);
    wRemovePropertyButton.setLayoutData(fd);
    
    fd = new FormData();
    fd.left = new FormAttachment(wRemovePropertyButton, 0, SWT.LEFT);
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(wRemovePropertyButton,5,SWT.BOTTOM);
    wAddPropertyButton.setLayoutData(fd);

    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(wPropertyTree, 0, SWT.RIGHT);
    fd.bottom = new FormAttachment(wDescriptionText, -5,SWT.TOP);
    wDescriptionLabel.setLayoutData(fd);

    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(wPropertyTree, 0, SWT.RIGHT);
    fd.bottom = new FormAttachment(wDefaultText, -5,SWT.TOP);
    fd.height = UiUtils.convertHeightInCharsToPixels(wDescriptionText, NUM_ROWS_DESCRIPTION);
    wDescriptionText.setLayoutData(fd);

    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.bottom = new FormAttachment(100, 0);
    fd.top = new FormAttachment(wDefaultText, 0, SWT.CENTER);
    wDefaultLabel.setLayoutData(fd);

    fd = new FormData();
    fd.left = new FormAttachment(wDefaultLabel, 5, SWT.RIGHT);
    fd.right = new FormAttachment(wPropertyTree, 0, SWT.RIGHT);
    fd.bottom = new FormAttachment(100, 0);
    wDefaultText.setLayoutData(fd);

    showPropertyInfo();
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
   */
  public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
    // New configuration created, set default values

    // Set default framework
    IOsgiInstall osgiInstall = Osgi.getDefaultOsgiInstall();
    if (osgiInstall != null) {
      configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_FRAMEWORK, osgiInstall.getName());
    }
    
    // Set default instance directory 
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IPath path = root.getLocation();
    path = path.append(DEFAULT_RUNTIME_PATH);
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_INSTANCE_DIR, path.toString());
    
    // Set default instance settings
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_CLEAR_CACHE, true);
    
    // Set default start level
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_START_LEVEL, DEFAULT_START_LEVEL);
    
    // Set default properties 
    HashMap properties = new HashMap();
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_PROPERTIES, properties);   
    
    configuration.setAttribute(ISourcePathComputer.ATTR_SOURCE_PATH_COMPUTER_ID, SourcePathComputer.ID);
  }

  /* (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
   */
  public void initializeFrom(ILaunchConfiguration configuration) {
    // Set values to GUI widgets

    // OSGi install
    updateOsgiInstalls();
    String installName = null;
    try {
      installName = configuration.getAttribute(IOsgiLaunchConfigurationConstants.ATTR_FRAMEWORK, (String) null);
    } catch (CoreException e) {
      e.printStackTrace();
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
      instanceDir = configuration.getAttribute(IOsgiLaunchConfigurationConstants.ATTR_INSTANCE_DIR, "");
    } catch (CoreException e) {
      e.printStackTrace();
    }
    wInstanceDirText.setText(instanceDir);
    
    // Clear bundle cache
    boolean instanceInit = false;
    try {
      // Instance settings
      instanceInit = configuration.getAttribute(IOsgiLaunchConfigurationConstants.ATTR_CLEAR_CACHE, false);
    } catch (CoreException e) {
      e.printStackTrace();
    }
    wInitButton.setSelection(instanceInit);
    
    // Start level
    int startLevel = DEFAULT_START_LEVEL;
    try {
      startLevel = configuration.getAttribute(IOsgiLaunchConfigurationConstants.ATTR_START_LEVEL, DEFAULT_START_LEVEL);
    } catch (CoreException e) {
      e.printStackTrace();
    }
    wStartLevelSpinner.setSelection(startLevel);
    
    // Initialize default system properties list
    try {
      systemProperties = configuration.getAttribute(IOsgiLaunchConfigurationConstants.ATTR_PROPERTIES, (Map) null);
      initializeSystemProperties(systemProperties);
    } catch (CoreException e) {
      e.printStackTrace();
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
   */
  public void performApply(ILaunchConfigurationWorkingCopy configuration) {

    // Read values from GUI widgets
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_FRAMEWORK, 
        wOsgiInstallCombo.getText());
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_INSTANCE_DIR, 
        wInstanceDirText.getText());
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_CLEAR_CACHE, 
        wInitButton.getSelection());
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_START_LEVEL, 
        wStartLevelSpinner.getSelection());
    configuration.setAttribute(ISourcePathComputer.ATTR_SOURCE_PATH_COMPUTER_ID, SourcePathComputer.ID);
    
    // System Properties
    systemProperties = getSystemProperties();
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_PROPERTIES, systemProperties);
  }

  /*
   *  (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
   */
  public boolean isValid(ILaunchConfiguration configuration) {
    // Verify Osgi install
    String name = null;
    try {
      name = configuration.getAttribute(IOsgiLaunchConfigurationConstants.ATTR_FRAMEWORK, (String) null);
    } catch (CoreException e) {
      e.printStackTrace();
    }
    if(Osgi.getOsgiInstall(name) == null) {
      setErrorMessage("No framework selected.");
      return false;
    }
    
    
    // TODO: Verify instance directory
    
    setErrorMessage(null);
    return true;
  }
  
  /****************************************************************************
   * Private utility methods
   ***************************************************************************/
  private void updateOsgiInstalls() {
    wOsgiInstallCombo.removeAll();
    List l = Osgi.getOsgiInstalls();
    
    if (l != null) {
      for(int i=0; i<l.size();i++) {
        wOsgiInstallCombo.add( ((IOsgiInstall) l.get(i)).getName());
      }
    }
  }
  
  private void initializeSystemProperties(Map properties) {
    IOsgiInstall osgiInstall = Osgi.getOsgiInstall(wOsgiInstallCombo.getText());
    if (osgiInstall == null) return;
    osgiInstall.addSystemPropertyGroup(userGroup);
    userGroup.clear();
    if (properties != null) {
      for(Iterator i=properties.entrySet().iterator(); i.hasNext();) {
        Map.Entry entry = (Map.Entry) i.next();
        SystemProperty property = osgiInstall.findSystemProperty((String) entry.getKey());
        if (property != null) {
          property.setValue((String) entry.getValue());
        } else {
          property = new SystemProperty((String) entry.getKey());
          property.setValue((String) entry.getValue());
          userGroup.addSystemProperty(property);
        }
      }
    }
    wPropertyTreeViewer.setInput(osgiInstall);
  }

  private Map getSystemProperties() {
    IOsgiInstall osgiInstall = (IOsgiInstall) wPropertyTreeViewer.getInput();
    if (osgiInstall == null) return null;
    
    SystemPropertyGroup[] groups = osgiInstall.getSystemPropertyGroups();
    if (groups == null) return null;
    
    HashMap map = new HashMap();
    for (int i=0; i<groups.length; i++) {
      SystemProperty[] properties = groups[i].getProperties();
      if (properties == null) continue;
      for (int j=0; j<properties.length; j++) {
        if (!isDefaultProperty(properties[j])) {
          map.put(properties[j].getName(), properties[j].getValue());
        }
      }
    }
    return map;
  }
  
  protected static boolean isDefaultProperty(SystemProperty property) {
    if (property == null) return false;
    
    if (MainTab.USER_GROUP.equals(property.getSystemPropertyGroup().getName())) return false;
      
    String value = property.getValue();
    if (value == null) value = "";
    String defaultValue = property.getDefaultValue();
    if (defaultValue == null) defaultValue = "";
    
    if (value.equals(defaultValue)) return true;
    
    return false;
  }
  
  private void showPropertyInfo() {
    IStructuredSelection selection = 
      (IStructuredSelection) wPropertyTreeViewer.getSelection();

    SystemProperty property = null;
    if (selection != null && !selection.isEmpty() && selection.getFirstElement() instanceof SystemProperty) {
      property = (SystemProperty) selection.getFirstElement();
    }
    
    if (property == null) {
      wDescriptionLabel.setEnabled(false);
      wDescriptionText.setText("");
      wDefaultLabel.setEnabled(false);
      wDefaultText.setText("");
      
      wRemovePropertyButton.setEnabled(false);
    } else {
      // Description
      wDescriptionLabel.setEnabled(true);
      if (property.getDescription() != null) {
        wDescriptionText.setText(property.getDescription());
      } else {
        wDescriptionText.setText("");
      }
      
      // Default value
      wDefaultLabel.setEnabled(true);
      if (property.getDefaultValue() != null) {
        wDefaultText.setText(property.getDefaultValue());
      } else {
        wDefaultText.setText("");
      }
      
      // Buttons
      if (USER_GROUP.equals(property.getSystemPropertyGroup().getName())) {
        wRemovePropertyButton.setEnabled(true);
      } else {
        wRemovePropertyButton.setEnabled(false);
      }
    }
  }

  protected void update(Object element) {
    wPropertyTreeViewer.update(element, null);
    updateLaunchConfigurationDialog();
  }
}
