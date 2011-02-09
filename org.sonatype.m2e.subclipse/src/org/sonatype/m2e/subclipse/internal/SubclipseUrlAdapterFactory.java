/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sonatype.m2e.subclipse.internal;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.m2e.scm.ScmUrl;

import org.tigris.subversion.subclipse.core.ISVNRemoteFolder;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * @author Eugene Kuleshov
 */
@SuppressWarnings("restriction")
public class SubclipseUrlAdapterFactory implements IAdapterFactory {

  private static final Class<?>[] ADAPTER_TYPES = new Class[] { ScmUrl.class };
  
  public Class<?>[] getAdapterList() {
    return ADAPTER_TYPES;
  }
  
  @SuppressWarnings("rawtypes")
  public Object getAdapter(Object adaptable, Class adapterType) {
    if(ScmUrl.class.equals(adapterType)) {
      if(adaptable instanceof ISVNRemoteFolder) {
        SVNUrl svnUrl = ((ISVNRemoteFolder) adaptable).getUrl();

        return adapt(svnUrl);
      } else if(adaptable instanceof ISVNRepositoryLocation) {
        SVNUrl svnUrl = ((ISVNRepositoryLocation) adaptable).getUrl();

        return adapt(svnUrl);
      }
    }
    return null;
  }

  private ScmUrl adapt(SVNUrl svnUrl) {
    String scmUrl = SubclipseHandler.SCM_SVN_PREFIX + svnUrl.toString();

    SVNUrl parent = svnUrl.getParent();
    String scmParentUrl = null;
    if(parent != null) {
      scmParentUrl = SubclipseHandler.SCM_SVN_PREFIX + parent.toString();
    }

    return new ScmUrl(scmUrl, scmParentUrl);
  }

}
