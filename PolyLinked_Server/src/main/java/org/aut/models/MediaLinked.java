package org.aut.models;

import org.jetbrains.annotations.NotNull;


public interface MediaLinked extends JsonSerializable, Comparable<MediaLinked> {
    String SERVER_PREFIX = "http://localhost:8080/resources/";

    String getMediaURL();

    @Override
    default int compareTo(@NotNull MediaLinked o) {
        return getMediaURL().compareTo(o.getMediaURL());
    }
}
