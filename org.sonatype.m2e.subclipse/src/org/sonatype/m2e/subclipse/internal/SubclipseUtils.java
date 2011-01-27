/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sonatype.m2e.subclipse.internal;

import java.util.Properties;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.repo.SVNRepositories;

/**
 * @author Eugene Kuleshov
 */
public class SubclipseUtils {

  public static ISVNRepositoryLocation getRepositoryLocation(String url) {
    SVNProviderPlugin provider = SVNProviderPlugin.getPlugin();
    SVNRepositories repositories = provider.getRepositories();

    ISVNRepositoryLocation location = null;
    
    ISVNRepositoryLocation[] locations = repositories.getKnownRepositories(new NullProgressMonitor());
    for(int i = 0; i < locations.length; i++ ) {
      String currentUrl = locations[i].getUrl().toString();
      if(url.startsWith(currentUrl)) {
        if(location==null || currentUrl.length()>location.getUrl().toString().length())
        location = locations[i];
      }
    }

    return location;
  }

  public static ISVNRepositoryLocation createRepositoryLocation(String url) throws SVNException {
    return createRepositoryLocation(url, null, null);
  }

  public static ISVNRepositoryLocation createRepositoryLocation(String url, String username, String password) throws SVNException {
    Properties properties = new Properties();
    properties.setProperty("url", url); //$NON-NLS-1$
    if (username != null) {
      properties.setProperty("user", username);
    }
    if (password != null) {
      properties.setProperty("password", password);
    }

    SVNProviderPlugin provider = SVNProviderPlugin.getPlugin();
    return provider.getRepositories().createRepository(properties);
    // provider.getRepositories().addOrUpdateRepository(location);
  }

}
