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

package org.knopflerfish.eclipse.core.ui.launcher;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.knopflerfish.eclipse.core.IOsgiBundle;
import org.knopflerfish.eclipse.core.IOsgiInstall;
import org.knopflerfish.eclipse.core.IOsgiVendor;
import org.knopflerfish.eclipse.core.Osgi;
import org.knopflerfish.eclipse.core.OsgiBundle;
import org.knopflerfish.eclipse.core.OsgiVendor;
import org.knopflerfish.eclipse.core.launcher.IOsgiLaunchConfigurationConstants;
import org.knopflerfish.eclipse.core.ui.OsgiUiPlugin;

/**
 * @author ar
 */
public class BundleTab extends AbstractLaunchConfigurationTab {
  private static final String ITEM_TYPE = "item_type";
  private static final String TYPE_BUNDLE_PROJECT = "bundle_project";
  private static final String TYPE_KF_BUNDLE = "kf_bundle";
  
  private static String IMAGE_BUNDLE = "icons/obj16/bundle_obj.gif";
  private static String IMAGE_FISH = "icons/obj16/knopflerfish_obj.gif";
  private int MARGIN = 5;

  private static final int DEFAULT_STARTLEVEL = 1;
  private static final int DEFAULT_STARTLEVEL_BUNDLE_PROJECT = 2;
  
  // Widgets
  private Composite wPageComposite;
  private Button    wAutoBundleButton;
  private Button    wManualBundleButton;
  private Tree      wBundleAllTree;
  private Button    wBundleAddButton;
  private Table     wBundleSelectedTable;
  private Button    wBundleRemoveButton;
  private Group     wBundleInfoGroup;
  private Text      wBundleInfoNameText;
  private Text      wBundleInfoVersionText;
  private Text      wBundleInfoPathText;

  private HashMap selectedBundles = new HashMap();
  private HashMap selectedBundleProjects = new HashMap();
  
  private Image imageBundle = null;
  private Image imageFish = null;
  
  public BundleTab() {
    ImageDescriptor id = OsgiUiPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui", IMAGE_BUNDLE);
    if (id != null) {
      imageBundle = id.createImage();
    }
    id = OsgiUiPlugin.imageDescriptorFromPlugin("org.knopflerfish.eclipse.core.ui", IMAGE_FISH);
    if (id != null) {
      imageFish = id.createImage();
    }
  }
    
  /****************************************************************************
   * org.eclipse.debug.ui.ILaunchConfigurationTab Methods
   ***************************************************************************/
  /* (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
   */
  public String getName() {
    return "Bundles";
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
   */
  public Image getImage() {
    return imageBundle;
  }
  
  /*
   *  (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
   */
  public void dispose() {
    if (imageBundle != null) {
      imageBundle.dispose();
      imageBundle = null;
    }
  }
  

  /* (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl(Composite parent) {
    
    wPageComposite = new Composite(parent, 0);
    setControl(wPageComposite);
    FormLayout layout = new FormLayout();
    layout.marginHeight = MARGIN;
    layout.marginWidth = MARGIN;
    wPageComposite.setLayout(layout);
    
    // Auto Button
    wAutoBundleButton = new Button(wPageComposite, SWT.RADIO);
    wAutoBundleButton.setText("Automatic selection of bundles");
    wAutoBundleButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        updateLaunchConfigurationDialog();
      }
    });

    // Manual Button
    wManualBundleButton = new Button(wPageComposite, SWT.RADIO);
    wManualBundleButton.setText("Manual selection of bundles");
    wManualBundleButton.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e) {
        updateLaunchConfigurationDialog();
      }
    });
    
    
    // All bundle tree
    Label wBundleAllLabel = new Label(wPageComposite, SWT.LEFT);
    wBundleAllLabel.setText("All bundles:");
    wBundleAddButton = new Button(wPageComposite, SWT.CENTER);
    wBundleAddButton.setText("Add");
    wBundleAddButton.setEnabled(false);
    wBundleAddButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        TreeItem [] treeItems = wBundleAllTree.getSelection();
        String itemType = (String) treeItems[0].getData(ITEM_TYPE);
        if (itemType == null) return;

        if (TYPE_KF_BUNDLE.equals(itemType)) {
          IOsgiBundle bundle = (IOsgiBundle) treeItems[0].getData();
          addBundleToSelected(bundle, new Integer(DEFAULT_STARTLEVEL));
        } else if(TYPE_BUNDLE_PROJECT.equals(itemType)) {
          addBundleToSelected(treeItems[0].getText(), new Integer(DEFAULT_STARTLEVEL_BUNDLE_PROJECT));
        }
        packTableColumns(wBundleSelectedTable);
      }
      
    });
    wBundleAllTree =  new Tree(wPageComposite, SWT.SINGLE | SWT.BORDER);
    wBundleAllTree.addSelectionListener(new SelectionListener() {
      public void widgetSelected(SelectionEvent e) {
        if (e.item instanceof TreeItem) {
          treeItemSelected((TreeItem) e.item);
        }
      }
      public void widgetDefaultSelected(SelectionEvent e) {
        if (e.item instanceof TreeItem) {
          TreeItem treeItem = (TreeItem) e.item;
          //treeItemSelected((TreeItem) e.item);
          String itemType = (String) treeItem.getData(ITEM_TYPE);
          if (itemType == null) return;
          
          if (TYPE_KF_BUNDLE.equals(itemType)) {
            IOsgiBundle bundle = (IOsgiBundle) treeItem.getData();
            addBundleToSelected(bundle, new Integer(DEFAULT_STARTLEVEL));
          } else if(TYPE_BUNDLE_PROJECT.equals(itemType)) {
            addBundleToSelected(treeItem.getText(), new Integer(DEFAULT_STARTLEVEL_BUNDLE_PROJECT));
          }
          packTableColumns(wBundleSelectedTable);
        }
      }
    });
    
    // Workspace bundles
    addBundleProjects(wBundleAllTree);

    // Knopflerfish bundles
    IOsgiVendor osgiVendor = Osgi.getVendor(OsgiVendor.VENDOR_NAME);
    List installList = osgiVendor == null ? null: osgiVendor.getOsgiInstalls();
    if (installList != null) {
      for(int i=0; i<installList.size(); i++) {
        addOsgiDistribution(wBundleAllTree, (IOsgiInstall) installList.get(i));
      }
    }
    
    // Bundle Info Group
    wBundleInfoGroup = new Group(wPageComposite, SWT.NONE);
    wBundleInfoGroup.setText("Bundle Info");
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    wBundleInfoGroup.setLayout(gridLayout);
    
    Label wBundleInfoNameLabel = new Label(wBundleInfoGroup, SWT.LEFT);
    wBundleInfoNameLabel.setText("Name:");
    wBundleInfoNameText = new Text(wBundleInfoGroup, SWT.LEFT);
    wBundleInfoNameText.setEditable(false);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    wBundleInfoNameText.setLayoutData(gd);
    
    Label wBundleInfoVersionLabel = new Label(wBundleInfoGroup, SWT.LEFT);
    wBundleInfoVersionLabel.setText("Version:");
    wBundleInfoVersionText = new Text(wBundleInfoGroup, SWT.LEFT);
    wBundleInfoVersionText.setEditable(false);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wBundleInfoVersionText.setLayoutData(gd);

    Label wBundleInfoPathLabel = new Label(wBundleInfoGroup, SWT.LEFT);
    wBundleInfoPathLabel.setText("Path:");
    wBundleInfoPathText = new Text(wBundleInfoGroup, SWT.LEFT);
    wBundleInfoPathText.setEditable(false);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    wBundleInfoPathText.setLayoutData(gd);

    wBundleInfoGroup.setEnabled(false);
    
    // Selected bundle tree
    Label wBundleSelectedLabel = new Label(wPageComposite, SWT.LEFT);
    wBundleSelectedLabel.setText("Selected bundles:");
    wBundleSelectedTable = new Table(wPageComposite, SWT.FULL_SELECTION | SWT.BORDER);
    wBundleSelectedTable.setHeaderVisible(true);
    wBundleSelectedTable.setLinesVisible(true);
    wBundleSelectedTable.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        if (e.item instanceof TableItem) {
          wBundleRemoveButton.setEnabled(true);
        }
      }
    });

    // Table columns
    TableColumn colName = new TableColumn(wBundleSelectedTable, SWT.LEFT);
    colName.setText("Name");
    TableColumn colVersion = new TableColumn(wBundleSelectedTable, SWT.LEFT);
    colVersion.setText("Version");
    TableColumn colLocation = new TableColumn(wBundleSelectedTable, SWT.LEFT);
    colLocation.setText("Start Level");
    
    wBundleRemoveButton = new Button(wPageComposite, SWT.CENTER);
    wBundleRemoveButton.setText("Remove");
    wBundleRemoveButton.setEnabled(false);
    wBundleRemoveButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        removeBundleFromSelected();
        packTableColumns(wBundleSelectedTable);
        if (wBundleSelectedTable.getSelectionCount()==0) {
          wBundleRemoveButton.setEnabled(false);
        }
      }
      
    });
    Button wBundleAddExternalButton = new Button(wPageComposite, SWT.CENTER);
    wBundleAddExternalButton.setText("Add external bundle...");
    
    
    // Layout radio buttons
    FormData data = new FormData();
    data.left = new FormAttachment(0,0);
    data.top = new FormAttachment(0,0);
    wAutoBundleButton.setLayoutData(data);

    data = new FormData();
    data.left = new FormAttachment(0,0);
    data.top = new FormAttachment(wAutoBundleButton,5, SWT.BOTTOM);
    wManualBundleButton.setLayoutData(data);
    
    // Layout All Bundles Tree
    data = new FormData();
    data.left = new FormAttachment(0,0);
    data.top = new FormAttachment(wManualBundleButton,5, SWT.BOTTOM);
    wBundleAllLabel.setLayoutData(data);

    data = new FormData();
    data.left = new FormAttachment(0,0);
    data.top = new FormAttachment(wBundleAllLabel,5, SWT.BOTTOM);
    data.right = new FormAttachment(wBundleAddButton,-5,SWT.LEFT);
    data.bottom = new FormAttachment(50,0);
    wBundleAllTree.setLayoutData(data);
    
    data = new FormData();
    data.right = new FormAttachment(100,0);
    data.left = new FormAttachment(wBundleAddExternalButton, 0, SWT.LEFT);
    data.top = new FormAttachment(wBundleAllTree,0, SWT.TOP);
    wBundleAddButton.setLayoutData(data);
    
    // Layout Bundle Info Group
    data = new FormData();
    data.left = new FormAttachment(0,0);
    data.right = new FormAttachment(wBundleAllTree,0, SWT.RIGHT);
    data.top = new FormAttachment(wBundleAllTree,5, SWT.BOTTOM);
    wBundleInfoGroup.setLayoutData(data);

    // Layout Selected Bundles Table
    data = new FormData();
    data.left = new FormAttachment(0,0);
    data.top = new FormAttachment(wBundleInfoGroup,5, SWT.BOTTOM);
    wBundleSelectedLabel.setLayoutData(data);

    data = new FormData();
    data.left = new FormAttachment(0,0);
    data.top = new FormAttachment(wBundleSelectedLabel,5, SWT.BOTTOM);
    data.right = new FormAttachment(wBundleAddExternalButton,-5,SWT.LEFT);
    data.bottom = new FormAttachment(100,0);
    wBundleSelectedTable.setLayoutData(data);

    data = new FormData();
    data.right = new FormAttachment(100,0);
    data.left = new FormAttachment(wBundleAddExternalButton, 0, SWT.LEFT);
    data.top = new FormAttachment(wBundleSelectedTable,0, SWT.TOP);
    wBundleRemoveButton.setLayoutData(data);

    data = new FormData();
    data.right = new FormAttachment(100,0);
    data.top = new FormAttachment(wBundleRemoveButton,5, SWT.BOTTOM);
    wBundleAddExternalButton.setLayoutData(data);
  }

  /* (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
   */
  public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
    // New configuration created, set default values
    
    // Set default bundle select type
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_BUNDLE_SELECT_TYPE, 
        IOsgiLaunchConfigurationConstants.BUNDLE_SELECT_TYPE_AUTO);

    // Set default bundles
    HashMap bundles = new HashMap();
    // TODO: Calculate default bundles
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_BUNDLES, bundles);   

    // Set default bundle projects
    HashMap bundleProjects = new HashMap();
    // TODO: Calculate default bundles
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_BUNDLE_PROJECTS, bundleProjects);   
  }

  /* (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
   */
  public void initializeFrom(ILaunchConfiguration configuration) {
    // Set values to GUI widgets

    // Bundle select type
    int selectType = IOsgiLaunchConfigurationConstants.BUNDLE_SELECT_TYPE_AUTO;
    try {
      selectType = configuration.getAttribute(IOsgiLaunchConfigurationConstants.ATTR_BUNDLE_SELECT_TYPE, 
          IOsgiLaunchConfigurationConstants.BUNDLE_SELECT_TYPE_AUTO);
    } catch (CoreException e) {
      e.printStackTrace();
    }
    wAutoBundleButton.setSelection(selectType == IOsgiLaunchConfigurationConstants.BUNDLE_SELECT_TYPE_AUTO);
    wManualBundleButton.setSelection(selectType == IOsgiLaunchConfigurationConstants.BUNDLE_SELECT_TYPE_MANUAL);
    
    wBundleSelectedTable.removeAll();
    // Bundles
    Map bundles = null;
    try {
      bundles = configuration.getAttribute(IOsgiLaunchConfigurationConstants.ATTR_BUNDLES, (Map) null);
    } catch (CoreException e) {
      e.printStackTrace();
    }
    selectedBundles.clear();
    if (bundles != null) {
      for(Iterator i=bundles.keySet().iterator();i.hasNext();) {
        String path = (String) i.next();
        Integer startLevel = new Integer((String) bundles.get(path));
        try {
          addBundleToSelected(new OsgiBundle(new File(path)), startLevel);
        } catch (IOException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
      }
    }
    // Bundle projects
    Map bundleProjects = null;
    try {
      bundleProjects = configuration.getAttribute(IOsgiLaunchConfigurationConstants.ATTR_BUNDLE_PROJECTS, (Map) null);
    } catch (CoreException e) {
      e.printStackTrace();
    }
    selectedBundleProjects.clear();
    if (bundleProjects != null) {
      for(Iterator i=bundleProjects.keySet().iterator();i.hasNext();) {
        String name = (String) i.next();
        Integer startLevel = new Integer((String) bundleProjects.get(name));
        addBundleToSelected(name, startLevel);
      }
    }

    packTableColumns(wBundleSelectedTable);
  }

  /* (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
   */
  public void performApply(ILaunchConfigurationWorkingCopy configuration) {
    // Read values from GUI widgets

    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_BUNDLE_SELECT_TYPE, 
        wAutoBundleButton.getSelection() ? 
            IOsgiLaunchConfigurationConstants.BUNDLE_SELECT_TYPE_AUTO : 
            IOsgiLaunchConfigurationConstants.BUNDLE_SELECT_TYPE_MANUAL);

    // Bundles
    HashMap bundles = new HashMap();
    for(Iterator i=selectedBundles.keySet().iterator();i.hasNext();) {
      String path = (String) i.next();
      bundles.put(path, selectedBundles.get(path));
    }
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_BUNDLES, bundles);   

    // Bundle projects
    HashMap bundleProjects = new HashMap();
    for(Iterator i=selectedBundleProjects.keySet().iterator();i.hasNext();) {
      String name = (String) i.next();
      bundleProjects.put(name, selectedBundleProjects.get(name));
    }
    configuration.setAttribute(IOsgiLaunchConfigurationConstants.ATTR_BUNDLE_PROJECTS, bundleProjects);   
  }

  /****************************************************************************
   * Private utility methods
   ***************************************************************************/

  /**
   * Adds the bundles from a OSGi distribution to a tree widget. 
   * The bundles are sorted after bundle name.
   * 
   * @param tree Tree widget
   * @param osgiInstall OSGi distribution. 
   */
  private void addOsgiDistribution(Tree tree, IOsgiInstall osgiInstall) {

    // Create tree item for OSGi distribution
    TreeItem treeDistribution = new TreeItem(tree, 0);
    treeDistribution.setText(osgiInstall.getName());
    treeDistribution.setImage(imageFish);

    IOsgiBundle[] bundles = osgiInstall.getBundles();
    if (bundles != null) {
      // Sort bundles
      TreeSet sortedSet = new TreeSet(new Comparator() {
        public int compare(Object b0, Object b1) {
          String s0 = ((IOsgiBundle) b0).getName();
          String s1 = ((IOsgiBundle) b1).getName();
          return s0.toLowerCase().compareTo(s1.toLowerCase());
        }
      });

      for (int i=0; i<bundles.length; i++) {
        sortedSet.add(bundles[i]);
      }
      
      // Add bundles to tree
      for (Iterator i=sortedSet.iterator(); i.hasNext();) {
        IOsgiBundle bundle = (IOsgiBundle)i.next(); 
        TreeItem treeBundle = new TreeItem(treeDistribution, 0);
        treeBundle.setText(bundle.getName());
        treeBundle.setImage(imageBundle);
        treeBundle.setData(bundle);
        treeBundle.setData(ITEM_TYPE, TYPE_KF_BUNDLE);
      }
    }
  }
 
  /**
   * Adds the bundle projects for the current workspace to a
   * tree widget. The bundles are sorted after bundle name.
   * 
   * @param tree Tree widget
   */
  private void addBundleProjects(Tree tree) {

    // Create parent tree item for workspace bundles
    TreeItem treeWorkspace = new TreeItem(tree, 0);
    treeWorkspace.setText("Workspace Bundles");
    
    // Get workspace bundle projects
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IProject [] projects = root.getProjects();
    
    
    // Add bundles to tree
    if (projects != null) {
      for(int i=0; i<projects.length; i++) {
        try {
          if (projects[i].hasNature(Osgi.NATURE_ID)) {
            IJavaProject javaProject = JavaCore.create(projects[i]);
            TreeItem treeBundle = new TreeItem(treeWorkspace, 0);
            treeBundle.setText(javaProject.getProject().getName());
            treeBundle.setImage(imageBundle);
            treeBundle.setData(ITEM_TYPE, TYPE_BUNDLE_PROJECT);
          }
        } catch (CoreException e) {
          // Failed to check project nature.
        }
      }
    }
  }
  
  /**
   * Callback used when a item in the bundle tree has been selected.
   * Widget are enabled/disabled depending on the type of item that
   * has been selected.
   * 
   * @param item Tree item selected
   */
  private IOsgiBundle treeItemSelected(TreeItem item) {
    IOsgiBundle bundle = null;
    
    String itemType = (String) item.getData(ITEM_TYPE);
    
    if (itemType != null) {
      wBundleAddButton.setEnabled(true);
      wBundleInfoGroup.setEnabled(true);
      // Enable children
      Control [] children = wBundleInfoGroup.getChildren();
      for(int i=0; i<children.length; i++) {
        children[i].setEnabled(true);
      }

      // Check what type of bundle has been selected
      String name = "";
      String version = "";
      String path = "";
      if(TYPE_BUNDLE_PROJECT.equals(itemType)) {
        name = item.getText();
      } else if(TYPE_KF_BUNDLE.equals(itemType)) {
        bundle = (IOsgiBundle) item.getData();
        name = bundle.getName();
        version = bundle.getVersion();
        path = bundle.getPath();
      }
      wBundleInfoNameText.setText(name);
      wBundleInfoVersionText.setText(version);
      wBundleInfoPathText.setText(path);
    } else {
      wBundleAddButton.setEnabled(false);
      wBundleInfoGroup.setEnabled(false);
      // Disable children
      Control [] children = wBundleInfoGroup.getChildren();
      for(int i=0; i<children.length; i++) {
        children[i].setEnabled(false);
      }
      wBundleInfoNameText.setText("");
      wBundleInfoVersionText.setText("");
      wBundleInfoPathText.setText("");
    }
    return bundle;
  }

  private void removeBundleFromSelected() {
    int idx  = wBundleSelectedTable.getSelectionIndex();
    if (idx == -1) return;

    TableItem item = wBundleSelectedTable.getItem(idx);
    String itemType = (String) item.getData(ITEM_TYPE);
    if (TYPE_BUNDLE_PROJECT.equals(itemType)) {
      selectedBundleProjects.remove(item.getText());
    } else if (TYPE_KF_BUNDLE.equals(itemType)) {
      selectedBundles.remove(((IOsgiBundle) item.getData()).getPath());
    }
    wBundleSelectedTable.remove(idx);

    updateLaunchConfigurationDialog();
  }
  
  private void addBundleToSelected(IOsgiBundle bundle, Integer startLevel) {
    // Check that bundle is not already selected
    if (selectedBundles.containsKey(bundle.getPath())) return;
    
    TableItem item = new TableItem(wBundleSelectedTable, 0);
    item.setData(bundle);
    item.setText(0, bundle.getName());
    item.setImage(0, imageBundle);
    item.setText(1, bundle.getVersion());
    item.setText(2, startLevel.toString());
    item.setData(ITEM_TYPE, TYPE_KF_BUNDLE);
    
    selectedBundles.put(bundle.getPath(), startLevel.toString());

    updateLaunchConfigurationDialog();
  }
  
  private void addBundleToSelected(String name, Integer startLevel) {
    // Check that bundle is not already selected
    if (selectedBundleProjects.containsKey(name)) return;
    
    TableItem item = new TableItem(wBundleSelectedTable, 0);
    item.setText(0, name);
    item.setImage(0, imageBundle);
    item.setText(1, "");
    item.setText(2, startLevel.toString());
    item.setData(ITEM_TYPE, TYPE_BUNDLE_PROJECT);
    
    selectedBundleProjects.put(name, startLevel.toString());

    updateLaunchConfigurationDialog();
  }
 
  private void packTableColumns(Table table) {
    if(table == null) return;
    TableColumn [] columns = table.getColumns();
    if (columns == null) return;
    for(int i=0;i<columns.length;i++) {
      columns[i].pack();
    }
  }
}
