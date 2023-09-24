/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.subsystem.bridge.impl;

import java.io.IOException;

import org.jboss.as.controller.ModelVersion;
import org.jboss.as.model.test.ModelTestOperationValidatorFilter;
import org.jboss.as.subsystem.bridge.local.ClassLoaderObjectConverter;
import org.jboss.as.subsystem.bridge.shared.ObjectSerializer;
import org.jboss.as.subsystem.test.AdditionalInitialization;
import org.jboss.dmr.ModelNode;

/**
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
public class ClassLoaderObjectConverterImpl implements ClassLoaderObjectConverter{
    private final ObjectSerializer local;
    private final ObjectSerializer remote;

    public ClassLoaderObjectConverterImpl(final ClassLoader local, final ClassLoader remote) {
        this.local = ObjectSerializer.FACTORY.createSerializer(local);
        this.remote = ObjectSerializer.FACTORY.createSerializer(remote);

        if (this.local.getClass().getClassLoader() != local) {
            throw new IllegalStateException("Wrong classloader");
        }
        if (this.remote.getClass().getClassLoader() != remote) {
            throw new IllegalStateException("Wrong classloader");
        }
    }

    public Object convertModelNodeToChildCl(ModelNode modelNode) {
        if (modelNode == null) {
            return null;
        }
        try {
            return remote.deserializeModelNode(local.serializeModelNode(modelNode));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public ModelNode convertModelNodeFromChildCl(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return (ModelNode)local.deserializeModelNode(remote.serializeModelNode(object));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object convertModelVersionToChildCl(ModelVersion modelVersion) {
        if (modelVersion == null) {
            return null;
        }
        return remote.deserializeModelVersion(local.serializeModelVersion(modelVersion));
    }

    @Override
    public Object convertAdditionalInitializationToChildCl(AdditionalInitialization additionalInit) {
        try {
            return remote.deserializeAdditionalInitialization(local.serializeAdditionalInitialization(additionalInit));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object convertValidateOperationsFilterToChildCl(ModelTestOperationValidatorFilter validateOpsFilter) {
        try {
            return remote.deserializeModelTestOperationValidatorFilter(local.serializeModelTestOperationValidatorFilter(validateOpsFilter));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
