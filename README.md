README - Knopflerfish Eclipse Plugin
======================================================================

The Knopflerfish Eclipse Plug-in is a tool for launching and debugging
the Knopflerfish OSGi distribution. The goal with the plug-in is to
simply the use of Knopflerfish for developers using Eclipse as their
IDE.

The Knopflerfish Eclipse Plugin is available at the update site:
http://www.knopflerfish.org/eclipse-update/

Contents
----------------------------------------------------------------------

There are five different plug-ins:
* org.knopflerfish.eclipse.core
* org.knopflerfish.eclipse.core.ui
* org.knopflerfish.eclipse.framework.knopflerfish13
* org.knopflerfish.eclipse.repository.directory
* org.knopflerfish.eclipse.repository.framework

The plugin is defined as a feature in the
`org.knopflerfish.eclipse.ide-feature` eclipse project.

How to build and prepare the feature for the update site is defined 
in the `org.knopflerfish.eclipse.ide-update-site` project.

The following plug-ins are no longer used and are scheduled for removal:
* org.knopflerfish.eclipse.framework.eclipse31
* org.knopflerfish.eclipse.framework.oscar10
* org.knopflerfish.eclipse.repository.obr


Building
----------------------------------------------------------------------

The plug-in is currently built by hand from eclipse.

Release
----------------------------------------------------------------------

The plug-in is released at Knopflerfish's eclipse-update site:
http://www.knopflerfish.org/eclipse-update/

License
----------------------------------------------------------------------

The Knopflerfish Eclipse plugin is licensed under a BSD 3-Clause
license, see License.txt.