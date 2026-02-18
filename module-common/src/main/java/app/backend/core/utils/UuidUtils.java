package app.backend.core.utils;

import java.util.UUID;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UuidUtils {
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
