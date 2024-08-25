//
//  ========================================================================
//  Copyright (c) 1995-2013 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//
package speck.resource;

import java.net.MalformedURLException;
import java.util.Objects;


import speck.staticfiles.DirectoryTraversal;
import speck.utils.Assert;

/**
 * Locates resources in classpath
 * Code snippets copied from Eclipse Jetty source. Modifications made by Per Wendel.
 */
public class ClassPathResourceHandler extends AbstractResourceHandler {
    private static final System.Logger LOG = System.getLogger(ClassPathResourceHandler.class.getName());

    private final String baseResource;
    private String welcomeFile;

    /**
     * Constructor
     *
     * @param baseResource the base resource path
     */
    public ClassPathResourceHandler(String baseResource) {
        this(baseResource, null);
    }

    /**
     * Constructor
     *
     * @param baseResource the base resource path
     * @param welcomeFile  the welcomeFile
     */
    public ClassPathResourceHandler(String baseResource, String welcomeFile) {
        Objects.requireNonNull(baseResource);

        this.baseResource = baseResource;
        this.welcomeFile = welcomeFile;
    }

    @Override
    protected AbstractFileResolvingResource getResource(String path) throws MalformedURLException {
        if (path == null || !path.startsWith("/")) {
            throw new MalformedURLException(path);
        }

        try {
            path = UriPath.canonical(path);

            final String addedPath = addPaths(baseResource, path);

            ClassPathResource resource = new ClassPathResource(addedPath);

            if (resource.exists() && path.endsWith("/")) {
                if (welcomeFile != null) {
                    resource = new ClassPathResource(addPaths(resource.getPath(), welcomeFile));
                } else {
                    // No welcome file configured, serve nothing since it's a directory
                    resource = null;
                }
            }

            if (resource != null && resource.exists()) {
                DirectoryTraversal.protectAgainstInClassPath(resource.getPath(), baseResource);
                return resource;
            } else {
                return null;
            }

        } catch (DirectoryTraversal.DirectoryTraversalDetection directoryTraversalDetection) {
            throw directoryTraversalDetection;
        } catch (Exception e) {
            if (LOG.isLoggable(System.Logger.Level.DEBUG)) {
                LOG.log(System.Logger.Level.DEBUG,e.getClass().getSimpleName() + " when trying to get resource. " + e.getMessage());
            }
        }
        return null;
    }

}
