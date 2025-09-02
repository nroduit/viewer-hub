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

package org.viewer.hub.back.enums;

import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.Objects;

@ToString
@AllArgsConstructor
public enum ViewerType {

    WEASIS("WEASIS"), OHIF("OHIF"), SLICER("SLICER"), MICRODICOM("MICRODICOM");

    private final String code;

    public static ViewerType fromCode(String code) {
        if (code != null) {
            return Arrays.stream(ViewerType.values())
                    .filter(i -> Objects.equals(code.trim(), i.toString()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

}
