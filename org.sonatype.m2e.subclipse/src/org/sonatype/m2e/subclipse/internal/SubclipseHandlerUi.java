/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sonatype.m2e.subclipse.internal;

import java.net.MalformedURLException;
import java.text.ParseException;

import org.eclipse.jface.window.Window;
import org.eclipse.m2e.scm.ScmUrl;
import org.eclipse.m2e.scm.spi.ScmHandlerUi;
import org.eclipse.swt.widgets.Shell;

import org.tigris.subversion.subclipse.core.ISVNRemoteFolder;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.history.ILogEntry;
import org.tigris.subversion.subclipse.ui.dialogs.ChooseUrlDialog;
import org.tigris.subversion.subclipse.ui.dialogs.HistoryDialog;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * An SCM handler UI for Subclipse team provider
 * 
 * @author Eugene Kuleshov
 */
@SuppressWarnings("restriction")
public class SubclipseHandlerUi extends ScmHandlerUi {

  public boolean canSelectUrl() {
    return true;
  }
  
  public boolean canSelectRevision() {
    return true;
  }
  
  public ScmUrl selectUrl(Shell shell, ScmUrl scmUrl) {
    ChooseUrlDialog dialog = new ChooseUrlDialog(shell, null);
    dialog.setFoldersOnly(true);
    dialog.setMultipleSelect(false);
    if(dialog.open() == Window.OK) {
      return new ScmUrl(SubclipseHandler.SCM_SVN_PREFIX + dialog.getUrl());
    }
    return null;
  }
  
  public String selectRevision(Shell shell, ScmUrl scmUrl, String scmRevision) {
    String svnUrl = getSvnUrl(scmUrl.getUrl());
    ISVNRepositoryLocation location = SubclipseUtils.getRepositoryLocation(svnUrl);
    if(location==null) {
      // TODO ask if we should add new repository
      return null;
    }
    
    String path = svnUrl.substring(location.getUrl().toString().length());
    ISVNRemoteFolder remoteFolder = location.getRemoteFolder(path);
    
    HistoryDialog dialog = new HistoryDialog(shell, remoteFolder);
    // dialog.getShell().setSize(300, 200);

    if(dialog.open() != Window.CANCEL) {
      ILogEntry[] selectedEntries = dialog.getSelectedLogEntries();
      if(selectedEntries.length > 0) {
        return Long.toString(selectedEntries[0].getRevision().getNumber());
      }
    }
    
    return null;
  }
  
  public boolean isValidUrl(String scmUrl) {
    if(scmUrl==null) {
      return false;
    }
    if(!scmUrl.startsWith(SubclipseHandler.SCM_SVN_PREFIX)) {
      return false;
    }
    
    try {
      new SVNUrl(getSvnUrl(scmUrl));
      return true;
    } catch(MalformedURLException ex) {
      return false;
    }
  }

  /**
   * Verify that SVN revision is one of the following:
   * 
   * <ul>
   * <li>HEAD</li>
   * <li>a revision number</li>
   * <li>a date with the following format : MM/DD/YYYY HH:MM AM_PM</li>
   * </ul>
   * 
   * @param scmUrl
   * @param scmRevision
   * @return
   */
  public boolean isValidRevision(ScmUrl scmUrl, String scmRevision) {
    try {
      SVNRevision revision = SVNRevision.getRevision(scmRevision);
      int kind = revision.getKind();
      return kind == SVNRevision.Kind.head || kind == SVNRevision.Kind.number || kind == SVNRevision.Kind.date;
    } catch(ParseException ex) {
      return false;
    }
  }
  
  private String getSvnUrl(String scmUrl) {
    return scmUrl.substring(SubclipseHandler.SCM_SVN_PREFIX.length());
  }
  
}

