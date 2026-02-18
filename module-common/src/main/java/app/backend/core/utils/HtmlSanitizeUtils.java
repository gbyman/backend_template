package app.backend.core.utils;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import lombok.experimental.UtilityClass;

/**
 * HTML Sanitization Utility Prevents XSS attacks by cleaning HTML content from WYSIWYG editors.
 *
 * <p>Uses JSoup library for HTML sanitization.
 */
@UtilityClass
public class HtmlSanitizeUtils {

    /**
     * Sanitize HTML for WYSIWYG editor content (recommended for blog posts, articles)
     *
     * <p>Allows: - Text formatting: b, em, i, strong, u, strike - Headings: h1, h2, h3, h4, h5, h6
     * - Paragraphs: p, br, blockquote - Lists: ul, ol, li - Links: a (with href) - Images: img
     * (with src, alt) - Tables: table, thead, tbody, tr, th, td - Code: pre, code
     *
     * <p>Example:
     *
     * <pre>
     * String userHtml = "&lt;script&gt;alert('XSS')&lt;/script&gt;&lt;p&gt;Hello&lt;/p&gt;";
     * String safe = HtmlSanitizeUtils.sanitizeForEditor(userHtml);
     * // Result: "&lt;p&gt;Hello&lt;/p&gt;"
     * </pre>
     *
     * @param html HTML content from WYSIWYG editor
     * @return Sanitized HTML (safe to store and display)
     */
    public String sanitizeForEditor(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }

        return Jsoup.clean(
                html,
                Safelist.relaxed()
                        .addTags("h1", "h2", "h3", "h4", "h5", "h6")
                        .addTags("pre", "code")
                        .addAttributes("img", "width", "height", "style")
                        .addAttributes("a", "target", "rel")
                        .addProtocols("a", "href", "http", "https", "mailto")
                        .addProtocols("img", "src", "http", "https", "data"));
    }

    /**
     * Sanitize HTML with basic formatting only (recommended for comments, descriptions)
     *
     * <p>Allows: - Text formatting: b, em, i, strong, u - Line breaks: br - Links: a (with href)
     *
     * <p>Example:
     *
     * <pre>
     * String comment = "&lt;p&gt;Hello &lt;strong&gt;World&lt;/strong&gt;&lt;/p&gt;";
     * String safe = HtmlSanitizeUtils.sanitizeBasic(comment);
     * // Result: "Hello &lt;strong&gt;World&lt;/strong&gt;"
     * </pre>
     *
     * @param html HTML content
     * @return Sanitized HTML with basic formatting only
     */
    public String sanitizeBasic(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }

        return Jsoup.clean(html, Safelist.basic());
    }

    /**
     * Sanitize HTML with simple text formatting only (recommended for user profiles, short texts)
     *
     * <p>Allows: - Text formatting: b, em, i, strong, u - Line breaks: br
     *
     * <p>Example:
     *
     * <pre>
     * String bio = "&lt;p&gt;&lt;a href='evil.com'&gt;Click&lt;/a&gt; &lt;strong&gt;Developer&lt;/strong&gt;&lt;/p&gt;";
     * String safe = HtmlSanitizeUtils.sanitizeSimple(bio);
     * // Result: "Click &lt;strong&gt;Developer&lt;/strong&gt;"
     * </pre>
     *
     * @param html HTML content
     * @return Sanitized HTML with simple text formatting
     */
    public String sanitizeSimple(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }

        return Jsoup.clean(html, Safelist.simpleText());
    }

    /**
     * Remove all HTML tags and return plain text only
     *
     * <p>Use for: - Search indexing - Plain text previews - SMS/Email content
     *
     * <p>Example:
     *
     * <pre>
     * String html = "&lt;p&gt;Hello &lt;strong&gt;World&lt;/strong&gt;&lt;/p&gt;";
     * String text = HtmlSanitizeUtils.toPlainText(html);
     * // Result: "Hello World"
     * </pre>
     *
     * @param html HTML content
     * @return Plain text without any HTML tags
     */
    public String toPlainText(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }

        return Jsoup.clean(html, Safelist.none());
    }

    /**
     * Sanitize HTML with custom safelist
     *
     * <p>Example:
     *
     * <pre>
     * Safelist custom = Safelist.none()
     *     .addTags("p", "br", "strong")
     *     .addAttributes("p", "class");
     *
     * String safe = HtmlSanitizeUtils.sanitizeWithCustomPolicy(html, custom);
     * </pre>
     *
     * @param html HTML content
     * @param safelist Custom JSoup Safelist
     * @return Sanitized HTML according to custom policy
     */
    public String sanitizeWithCustomPolicy(String html, Safelist safelist) {
        if (html == null || html.isEmpty()) {
            return html;
        }

        return Jsoup.clean(html, safelist);
    }

    /**
     * Check if HTML contains dangerous content (script, iframe, etc.)
     *
     * <p>Example:
     *
     * <pre>
     * String dangerous = "&lt;script&gt;alert('XSS')&lt;/script&gt;&lt;p&gt;Hello&lt;/p&gt;";
     * boolean isDangerous = HtmlSanitizeUtils.containsDangerousContent(dangerous);
     * // Result: true
     * </pre>
     *
     * @param html HTML content to check
     * @return true if HTML contains dangerous tags
     */
    public boolean containsDangerousContent(String html) {
        if (html == null || html.isEmpty()) {
            return false;
        }

        String sanitized = Jsoup.clean(html, Safelist.relaxed());
        return !sanitized.equals(html);
    }

    /**
     * Validate HTML against specific safelist
     *
     * <p>Example:
     *
     * <pre>
     * String html = "&lt;p&gt;Hello &lt;strong&gt;World&lt;/strong&gt;&lt;/p&gt;";
     * boolean isValid = HtmlSanitizeUtils.isValid(html, Safelist.basic());
     * // Result: true
     * </pre>
     *
     * @param html HTML content
     * @param safelist Safelist to validate against
     * @return true if HTML is valid according to safelist
     */
    public boolean isValid(String html, Safelist safelist) {
        if (html == null || html.isEmpty()) {
            return true;
        }

        String sanitized = Jsoup.clean(html, safelist);
        return sanitized.equals(html);
    }
}
