/*
 *  Copyright (c) 2022-2025 Weasis Team and other contributors.
 *
 *  This program and the accompanying materials are made available under the terms of the Eclipse
 *  Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 *  License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package org.viewer.hub.back.service.impl;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.viewer.hub.back.model.ArchiveSearchCriteria;
import org.viewer.hub.back.service.DisplaySelectViewerRuleService;

@Service
public class DisplaySelectViewerRuleServiceImpl implements DisplaySelectViewerRuleService {

    // TODO: process rules to select the viewer to launch
    // Redirect to  WeasisDisplayService/OHIFDisplayService
    //=> Which will use WeasisConnectorQueryService / OHifConnectorQueryService
    //=> Which will use DBConnectorService / DicomConnectorService


    public DisplaySelectViewerRuleServiceImpl() {
    }

    @Override
    public String displayViewer(Authentication authentication, ArchiveSearchCriteria archiveSearchCriteria) {
        return ""; // weasisDisplayService.xxx ou ohifDisplayService.yyyy
    }
}
