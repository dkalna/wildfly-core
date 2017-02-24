/*
 * Copyright 2017 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.server.deploymentoverlay;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DEPLOYMENT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DEPLOYMENTS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DEPLOYMENT_OVERLAY;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DEPLOYMENT_OVERLAY_LINK_REMOVAL;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REDEPLOY_AFFECTED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REDEPLOY_LINKS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REMOVE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SERVER_GROUP;

import java.util.Collections;
import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationDefinition;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleOperationDefinitionBuilder;
import org.jboss.as.controller.descriptions.common.ControllerResolver;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * Handler to remove a link between an overlay and a deployment with support to redeploy deployments thus affected.
 * @author Emmanuel Hugonnet (c) 2017 Red Hat, inc.
 */
public class DeploymentOverlayDeploymentRemoveHandler extends AbstractRemoveStepHandler {

    public static final AttributeDefinition REDEPLOY_AFFECTED_DEFINITION
            = SimpleAttributeDefinitionBuilder.create(REDEPLOY_AFFECTED, ModelType.BOOLEAN)
                    .setRequired(false)
                    .setDefaultValue(new ModelNode(false))
                    .build();
    public static final OperationDefinition REMOVE_DEFINITION
            = new SimpleOperationDefinitionBuilder(REMOVE, ControllerResolver.getResolver(DEPLOYMENT_OVERLAY + '.' + DEPLOYMENT))
                    .addParameter(REDEPLOY_AFFECTED_DEFINITION)
                    .build();

    public static final DeploymentOverlayDeploymentRemoveHandler INSTANCE = new DeploymentOverlayDeploymentRemoveHandler();

    @Override
    protected void performRemove(OperationContext context, final ModelNode operation, final ModelNode model) throws OperationFailedException {
        final String runtimeName = context.getCurrentAddressValue();
        if (REDEPLOY_AFFECTED_DEFINITION.resolveModelAttribute(context, operation).asBoolean()) {
            if (SERVER_GROUP.equals(context.getCurrentAddress().getElement(0).getKey())) {
                PathAddress overlayAddress = context.getCurrentAddress().getParent();
                OperationStepHandler handler = context.getRootResourceRegistration().getOperationHandler(overlayAddress, REDEPLOY_LINKS);
                ModelNode redeployAffectedOperation = Util.createOperation(REDEPLOY_LINKS, overlayAddress);
                redeployAffectedOperation.get(DEPLOYMENTS).setEmptyList().add(runtimeName);
                redeployAffectedOperation.get(DEPLOYMENT_OVERLAY_LINK_REMOVAL).set(true);
                assert handler != null;
                assert redeployAffectedOperation.isDefined();
                context.addStep(redeployAffectedOperation, handler, OperationContext.Stage.MODEL);
            } else {
                AffectedDeploymentOverlay.redeployLinks(context, PathAddress.EMPTY_ADDRESS, Collections.singleton(runtimeName));
            }
        }
        super.performRemove(context, operation, model);
    }

    /**
     * Throws {@link UnsupportedOperationException}.
     *
     * {@inheritDoc}
     */
    @Override
    protected void performRuntime(final OperationContext context, final ModelNode operation, final ModelNode model) throws OperationFailedException {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws {@link UnsupportedOperationException}.
     *
     * {@inheritDoc}
     */
    @Override
    protected void recoverServices(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns {@code false}.
     *
     * {@inheritDoc}
     */
    @Override
    protected final boolean requiresRuntime(OperationContext context) {
        return false;
    }
}