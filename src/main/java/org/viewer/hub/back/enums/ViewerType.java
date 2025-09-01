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
