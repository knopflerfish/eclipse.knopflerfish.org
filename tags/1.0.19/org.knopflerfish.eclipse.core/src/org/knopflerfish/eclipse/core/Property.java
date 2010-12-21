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

package org.knopflerfish.eclipse.core;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Anders Rimén, Gatespace Telematics
 * @see http://www.gatespacetelematics.com/
 */
public class Property {

  public static final String SYSTEM_PROPERTY = "System";
  public static final String FRAMEWORK_PROPERTY = "Framework";
  public static final String[] TYPES = new String[] { FRAMEWORK_PROPERTY,
      SYSTEM_PROPERTY };

  private String name;
  private String type = SYSTEM_PROPERTY;
  private PropertyGroup group;
  private String value;
  private String defaultValue;
  private String description;
  private final List<String> allowedValues = new ArrayList<String>();

  public Property(String name)
  {
    this.name = name;
  }

  // ***************************************************************************
  // Getters and setters
  // ***************************************************************************

  public List<String> getAllowedValues()
  {
    return allowedValues;
  }

  public void setAllowedValues(List<String> values)
  {
    allowedValues.clear();
    allowedValues.addAll(values);
  }

  public String getDefaultValue()
  {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue)
  {
    this.defaultValue = defaultValue;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public void setSystemPropertyGroup(PropertyGroup group)
  {
    this.group = group;
  }

  public PropertyGroup getSystemPropertyGroup()
  {
    return group;
  }

  public String getValue()
  {
    return value;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public boolean isDefaultValue()
  {
    if (defaultValue == null) {
      return value == null || value.length() == 0;
    }
    return defaultValue.equals(value);
  }

  // ***************************************************************************
  // java.lang.Object methods
  // ***************************************************************************

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    StringBuffer buf = new StringBuffer(type);
    buf.append(' ');
    buf.append(name);
    buf.append('=');
    buf.append(value);
    return buf.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object o)
  {
    if (o == null || !(o instanceof Property))
      return false;

    return ((Property) o).getName().equals(getName());
  }
}
