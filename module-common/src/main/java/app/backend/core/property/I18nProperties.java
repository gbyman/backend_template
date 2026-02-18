package app.backend.core.property;

import java.util.Locale;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.i18n")
public class I18nProperties {

    private String defaultLang = Locale.KOREAN.getLanguage();

    private Set<String> supportedLangs =
            Set.of(Locale.KOREAN.getLanguage(), Locale.ENGLISH.getLanguage());
}
