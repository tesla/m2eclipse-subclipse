/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.subclipse;

import org.eclipse.core.runtime.IAdapterFactory;

import org.tigris.subversion.subclipse.core.ISVNRemoteFolder;
import org.tigris.subversion.svnclientadapter.SVNUrl;

import org.maven.ide.eclipse.scm.ScmUrl;

/**
 * @author Eugene Kuleshov
 */
public class SubclipseUrlAdapterFactory implements IAdapterFactory {

  @SuppressWarnings("unchecked")
  private static final Class[] ADAPTER_TYPES = new Class[] { ScmUrl.class };
  
  @SuppressWarnings("unchecked")
  public Class[] getAdapterList() {
    return ADAPTER_TYPES;
  }
  
  @SuppressWarnings("unchecked")
  public Object getAdapter(Object adaptable, Class adapterType) {
    if(ScmUrl.class.equals(adapterType)) {
      if(adaptable instanceof ISVNRemoteFolder) {
        SVNUrl svnUrl = ((ISVNRemoteFolder) adaptable).getUrl();

        String scmUrl = SubclipseHandler.SCM_SVN_PREFIX + svnUrl.toString();
        
        SVNUrl parent = svnUrl.getParent();
        String scmParentUrl = null;
        if(parent!=null) {
          scmParentUrl = SubclipseHandler.SCM_SVN_PREFIX + parent.toString();
        }
        
        return new ScmUrl(scmUrl, scmParentUrl);
      }
    }
    return null;
  }

}
