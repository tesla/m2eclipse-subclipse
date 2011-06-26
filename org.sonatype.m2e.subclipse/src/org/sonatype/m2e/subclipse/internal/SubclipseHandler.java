/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sonatype.m2e.subclipse.internal;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.ParseException;

import org.sonatype.m2e.subclipse.MavenSubclipsePlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.scm.MavenProjectScmInfo;
import org.eclipse.m2e.scm.spi.ScmHandler;

import org.tigris.subversion.subclipse.core.ISVNRemoteFolder;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.client.OperationManager;
import org.tigris.subversion.subclipse.core.client.OperationProgressNotifyListener;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;


/**
 * An SCM handler based on Subclipse Team provider
 * 
 * @author Eugene Kuleshov
 */
@SuppressWarnings("restriction")
public class SubclipseHandler extends ScmHandler {

  public static final String SCM_SVN_PREFIX = "scm:svn:";

  public InputStream open(String url, String revision) throws CoreException {
    if(!url.startsWith(SCM_SVN_PREFIX)) {
      throw new CoreException(new Status(IStatus.WARNING, MavenSubclipsePlugin.PLUGIN_ID, -1, //
          "Not supported SCM type " + url, null));
    }

    String svnUrl = url.trim().substring(SCM_SVN_PREFIX.length());

    try {
      ISVNRepositoryLocation location = SubclipseUtils.getRepositoryLocation(svnUrl);
      if(location == null) {
        location = SubclipseUtils.createRepositoryLocation(svnUrl);
      }

      ISVNClientAdapter client = location.getSVNClient();

      // TODO follow svn:externals property
      // ISVNProperty[] properties = client.getProperties(folderUrl);

      SVNUrl folderUrl = new SVNUrl(svnUrl);
      SVNUrl pomUrl = folderUrl.appendPath("/" + IMavenConstants.POM_FILE_NAME);

      return client.getContent(pomUrl, SVNRevision.getRevision(revision));
      
    } catch(SVNException ex) {
      throw new CoreException(new Status(IStatus.ERROR, IMavenConstants.PLUGIN_ID, 0, ex.getMessage(), ex));

    } catch(SVNClientException ex) {
      throw new CoreException(new Status(IStatus.ERROR, IMavenConstants.PLUGIN_ID, 0, ex.getMessage(), ex));

    } catch(ParseException ex) {
      throw new CoreException(new Status(IStatus.ERROR, IMavenConstants.PLUGIN_ID, 0, "Invalid revision " + revision , ex));

    } catch(MalformedURLException ex) {
      throw new CoreException(new Status(IStatus.ERROR, IMavenConstants.PLUGIN_ID, 0, "Invalid url " + svnUrl, ex));

    }
  }

  public void checkoutProject(MavenProjectScmInfo info, File dest, IProgressMonitor monitor) throws CoreException,
      InterruptedException {
    ISVNRemoteFolder folder = getRemoteFolder(info);
    
    ISVNClientAdapter svnClient = folder.getRepository().getSVNClient();
    
    try {
      OperationManager.getInstance().beginOperation(svnClient, new OperationProgressNotifyListener(monitor));
      
      svnClient.checkout(folder.getUrl(), dest, //
          SVNRevision.getRevision(info.getRevision()), true);
      
//      RepositoryProvider.map(project, SVNProviderPlugin.getTypeId());
//      RepositoryProvider.getProvider(project, SVNProviderPlugin.getTypeId());
      
    } catch(ParseException ex) {
      throw new CoreException(new Status(IStatus.ERROR, IMavenConstants.PLUGIN_ID, 0, //
          "Invalid revision " + info.getRevision(), ex));
    
    } catch (SVNClientException e) {
      String msg = null;
      Throwable cause = e.getCause();
      if(cause!=null) {
        msg = cause.getMessage();
      }
      if(msg==null) {
        msg = e.getMessage();
      }
      if(msg==null) {
        msg = e.toString();
      }
      
      throw new SVNException("Checkout error; " + msg);
      
    } finally {
      OperationManager.getInstance().endOperation();
    
    }
    
//    CheckoutAsProjectOperation operation = new CheckoutAsProjectOperation(null, //
//        new ISVNRemoteFolder[] {folder}, //
//        new IProject[] {project}, locationPath);
//    try {
//      operation.setSvnRevision(SVNRevision.getRevision(info.getRevision()));
//      operation.run(monitor);
//
//    } catch(ParseException ex) {
//      throw new CoreException(new Status(IStatus.ERROR, MavenPlugin.PLUGIN_ID, 0, "Invalid revision " + info.getRevision(), ex));
//      
//    } catch(InvocationTargetException ex) {
//      Throwable t = ex.getTargetException()==null ? ex : ex.getTargetException();
//      if(t instanceof CoreException) {
//        throw (CoreException) t;
//      }
//      throw new CoreException(new Status(IStatus.ERROR, MavenPlugin.PLUGIN_ID, 0, "Invalid revision " + info.getRevision(), t));
//      
//    }
  }

  private ISVNRemoteFolder getRemoteFolder(MavenProjectScmInfo info) throws SVNException {
    String folderUrl = info.getFolderUrl();

    String repositoryUrl = info.getRepositoryUrl();

    String svnUrl = repositoryUrl.substring(SCM_SVN_PREFIX.length());
    ISVNRepositoryLocation repository = SubclipseUtils.getRepositoryLocation(svnUrl);
    if(repository==null) {
      repository = SubclipseUtils.createRepositoryLocation(svnUrl, info.getUsername(), info.getPassword());
    }

    String folderPath = folderUrl.substring(SCM_SVN_PREFIX.length() + repository.getUrl().toString().length());
    return repository.getRemoteFolder(folderPath);
  }

}
