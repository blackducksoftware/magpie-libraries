/*
 * Copyright 2019 Synopsys, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blackducksoftware.common.io;

import static com.blackducksoftware.common.base.ExtraStrings.padBoth;
import static com.blackducksoftware.common.base.ExtraStrings.removePrefix;
import static com.blackducksoftware.common.base.ExtraStrings.truncateEnd;
import static com.blackducksoftware.common.base.ExtraStrings.truncateMiddle;
import static com.blackducksoftware.common.base.ExtraStrings.truncateStart;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.padEnd;
import static com.google.common.base.Strings.padStart;
import static java.nio.charset.StandardCharsets.US_ASCII;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntBiFunction;

import javax.annotation.Nullable;

import com.google.common.annotations.Beta;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * A formatter intended for end user customization when rendering instances of a particular type.
 *
 * @author jgustie
 */
@Beta
public class Formatter<T> {

    // TODO This should become part of a larger "Terminal" class

    /**
     * Defines the logic to handle a placeholder in the format string.
     */
    public interface Placeholder<T> {

        /**
         * Given the entire format string and the offset of this placeholder, returns the number of additional
         * characters this placeholder wishes to consume.
         */
        default int optionLength(CharSequence format, int offset) {
            return 0;
        }

        /**
         * Given the value being rendered and the options extracted from the placeholder, returns the rendered value.
         */
        String extract(T value, String options);

        /**
         * Returns the length of an options group or zero if the offset does not point to an options group.
         */
        static int optionsGroupLength(CharSequence format, int offset) {
            if (format.charAt(offset) == '(') {
                for (int i = offset; i < format.length(); ++i) {
                    if (format.charAt(i) == ')') {
                        return i - offset + 1;
                    }
                }
                throw new IllegalArgumentException("invalid format, missing ')': " + format);
            } else {
                return 0;
            }
        }

        /**
         * Splits a group of options into individual values. An option group is a comma or space delimited list of
         * values
         * enclosed in parenthesis.
         */
        @Nullable
        static List<String> optionsGroup(String options) {
            if (options.startsWith("(") && options.endsWith(")")) {
                return Splitter.on(CharMatcher.anyOf(" ,")).omitEmptyStrings().splitToList(options.substring(1, options.length() - 1));
            } else {
                return null;
            }
        }
    }

    /**
     * A placeholder backed by functions.
     */
    private static class LambdaPlaceholder<T> implements Placeholder<T> {
        private final ToIntBiFunction<CharSequence, Integer> optionLength;

        private final BiFunction<T, String, String> extract;

        public LambdaPlaceholder(ToIntBiFunction<CharSequence, Integer> optionLength, BiFunction<T, String, String> extract) {
            this.optionLength = Objects.requireNonNull(optionLength);
            this.extract = Objects.requireNonNull(extract);
        }

        public LambdaPlaceholder(Function<T, String> extract) {
            this((f, o) -> 0, (v, o) -> extract.apply(v));
        }

        @Override
        public int optionLength(CharSequence format, int offset) {
            return optionLength.applyAsInt(format, offset);
        }

        @Override
        public String extract(T value, String options) {
            return extract.apply(value, options);
        }
    }

    /**
     * A placeholder that emits a raw hex encoded byte.
     */
    private static class HexBytePlaceholder implements Placeholder<Object> {
        private static final HexBytePlaceholder INSTANCE = new HexBytePlaceholder();

        @SuppressWarnings("unchecked")
        public static <T> Placeholder<T> getInstance() {
            return (Placeholder<T>) INSTANCE;
        }

        @Override
        public int optionLength(CharSequence format, int offset) {
            return 2;
        }

        @Override
        public String extract(Object value, String options) {
            return new String(new byte[] { Byte.parseByte(options, 16) }, US_ASCII);
        }
    }

    /**
     * A placeholder that emits ANSI color sequences.
     */
    private static class ColorPlaceholder<T> implements Placeholder<T> {

        private static final ImmutableSet<String> OPTIONS = ImmutableSet.of("red", "green", "blue", "reset");

        private static final ImmutableList<String> COLORS = ImmutableList.of("black", "red", "green", "yellow", "blue", "magenta", "cyan", "white");

        private static final ImmutableList<String> ATTRIBUTES = ImmutableList.of("reset", "bold", "dim", "italic", "ul", "blink", "", "", "", "strike");

        private Boolean enabled;

        private boolean useColor() {
            if (enabled == null) {
                // `System.console != null` is the closest thing to isTTY we have
                // TODO Also check System.getenv("TERM")?
                return System.console() != null;
            }
            return enabled.booleanValue();
        }

        @Override
        public int optionLength(CharSequence format, int offset) {
            int len = Placeholder.optionsGroupLength(format, offset);
            if (len > 0) {
                return len;
            }
            for (String option : OPTIONS) {
                if (option.contentEquals(format.subSequence(offset, Math.min(offset + option.length(), format.length())))) {
                    return option.length();
                }
            }
            throw new IllegalArgumentException("invalid format, incorrect color: " + format);
        }

        @Override
        public String extract(Object value, String options) {
            if (OPTIONS.contains(options)) {
                return ansi(options, false);
            } else {
                List<String> optionsGroup = Placeholder.optionsGroup(options);
                checkArgument(optionsGroup != null, "invalid format, unsupported options: %s", options);

                StringBuilder result = new StringBuilder();
                int colorIndex = 0;
                for (String option : optionsGroup) {
                    if (COLORS.contains(option) || option.startsWith("#")) {
                        checkArgument(colorIndex < 2, "invalid format, too many colors: " + options);
                        result.append(ansi(option, colorIndex++ == 1));
                    } else if (option.equals("auto") && optionsGroup.size() == 1) {
                        enabled = null;
                    } else if (option.equals("always")) {
                        enabled = Boolean.TRUE;
                    } else {
                        boolean invert = option.startsWith("no");
                        result.append(ansi(invert ? removePrefix(removePrefix(option, "no"), "-") : option, invert));
                    }
                }
                return result.toString();
            }
        }

        private String ansi(String value, boolean toggle) {
            checkArgument(!value.equals("reset") || !toggle, "invalid ANSI tag: " + value);
            if (!useColor()) {
                return "";
            } else if (value.startsWith("#") && value.length() == 7) {
                int color = Integer.parseInt(value.substring(1), 16);
                return "\033[" + (toggle ? 48 : 38) + ";2;" + ((color >> 16) & 0xFF) + ";" + ((color >> 8) & 0xFF) + ";" + (color & 0xFF) + "m";
            } else {
                int color = COLORS.indexOf(value);
                if (color >= 0) {
                    return "\033[" + (color + (toggle ? 40 : 30)) + "m";
                } else {
                    int attribute = ATTRIBUTES.indexOf(value);
                    if (attribute >= 0) {
                        return "\033[" + (attribute + (toggle ? 20 : 0)) + "m";
                    }
                }
            }
            throw new IllegalArgumentException("unknown ANSI tag: " + value);
        }
    }

    /**
     * A special placeholder that instead provides an adjustment for the subsequent placeholder.
     */
    private static class PaddingPlaceholder<T> implements Placeholder<T> {
        /**
         * The character used to invoke this placeholder. Necessary to determine the direction of padding.
         */
        private final char placeholder;

        public PaddingPlaceholder(char placeholder) {
            this.placeholder = placeholder;
        }

        /**
         * Reconstructs the complete padding mode from the original format.
         */
        private String padding(CharSequence format, int offset) {
            StringBuilder result = new StringBuilder(3).append(placeholder);
            char c = format.charAt(offset++);
            while (c == '|' || c == '<' || c == '>') {
                result.append(c);
                c = format.charAt(offset++);
            }
            return result.toString();
        }

        @Override
        public int optionLength(CharSequence format, int offset) {
            int l = padding(format, offset).length() - 1;
            return Placeholder.optionsGroupLength(format, offset + l) + l;
        }

        public Function<String, String> adjust(String options) {
            String padding = padding(options, 0);
            List<String> optionsGroup = Placeholder.optionsGroup(options.substring(padding.length() - 1));
            checkArgument(optionsGroup.size() == 1 || optionsGroup.size() == 2, "invalid format, bad padding options", options);
            int n = Integer.parseInt(optionsGroup.get(0));

            // Enforce maximum width by truncating
            Function<String, String> truncate;
            switch (padding.endsWith("|") || optionsGroup.size() < 2 ? "" : optionsGroup.get(1)) {
            case "":
                truncate = Function.identity();
                break;
            case "trunc":
                truncate = v -> truncateEnd(v, n);
                break;
            case "ltrunc":
                truncate = v -> truncateStart(v, n);
                break;
            case "mtrunc":
                truncate = v -> truncateMiddle(v, n);
                break;
            default:
                throw new IllegalArgumentException("invalid format, bad truncate option: " + options);
            }

            // Enforce minimum width by padding
            Function<String, String> pad;
            if (padding.startsWith("><")) {
                pad = v -> padBoth(v, n, ' ');
            } else if (padding.startsWith("<")) {
                pad = v -> padEnd(v, n, ' ');
            } else {
                pad = v -> padStart(v, n, ' ');
            }

            return truncate.andThen(pad);
        }

        @Override
        public String extract(T value, String options) {
            // This placeholder doesn't actually work as a placeholder
            throw new UnsupportedOperationException();
        }
    }

    /**
     * The mapping of placeholder characters to their logic.
     */
    private final Map<Character, Placeholder<T>> placeholders = new HashMap<>();

    /**
     * The mapping of supported format aliases.
     */
    private final Map<String, String> prettyFormats = new HashMap<>();

    /**
     * The delimiter printed after formatting.
     */
    private String delimiter = "\n";

    public Formatter() {
        placeholders.put('n', (v, o) -> "\n");
        placeholders.put('%', (v, o) -> "%");
        placeholders.put('x', HexBytePlaceholder.getInstance());
        placeholders.put('C', new ColorPlaceholder<>());
        placeholders.put('<', new PaddingPlaceholder<>('<'));
        placeholders.put('>', new PaddingPlaceholder<>('>'));
    }

    /**
     * Adds custom placeholder logic at the specified placeholder character.
     */
    public void setPlaceholder(char c, @Nullable Placeholder<T> placeholder) {
        placeholders.put(c, placeholder);
    }

    /**
     * Invokes the supplied function on the object being formated at the specified placeholder character.
     */
    public void setPlaceholder(char c, Function<T, String> extractor) {
        setPlaceholder(c, new LambdaPlaceholder<>(extractor));
    }

    /**
     * Adds a pretty format mapping to this formatter.
     */
    public void setPrettyFormat(String pretty, String format) {
        prettyFormats.put(Objects.requireNonNull(pretty), Objects.requireNonNull(format));
    }

    /**
     * Adds three common pretty format mappings of progressively increasing detail.
     */
    public void setPrettyFormats(String shortFormat, String mediumFormat, String fullFormat) {
        prettyFormats.put("short", Objects.requireNonNull(shortFormat));
        prettyFormats.put("medium", Objects.requireNonNull(mediumFormat));
        prettyFormats.put("full", Objects.requireNonNull(fullFormat));
    }

    /**
     * Sets the delimiter to use for terminated formats.
     */
    public void setDelimiter(CharSequence delimiter) {
        this.delimiter = delimiter.toString();
    }

    /**
     * Formats the supplied object to a string.
     *
     * @see #formatTo(Appendable, Object, String)
     */
    public String format(T obj, String pretty) {
        return formatTo(new StringBuilder(), obj, pretty).toString();
    }

    /**
     * Formats the supplied object.
     *
     * @throws UncheckedIOException
     *             if appending to the supplied target fails
     */
    public <A extends Appendable> A formatTo(A target, T obj, String pretty) {
        Objects.requireNonNull(obj);
        try {
            // Determine what the format should be
            boolean terminate = false;
            String format;
            if (pretty.startsWith("tformat:")) {
                format = pretty.substring(8);
                terminate = true;
            } else if (pretty.startsWith("format:")) {
                format = pretty.substring(7);
            } else {
                format = prettyFormats.get(pretty);
                checkArgument(format != null, "invalid format: %s", format);
            }

            // Iterate over the format, replacing the placeholders
            int start = 0;
            int end = format.indexOf('%');
            Function<String, String> adjustment = Function.identity();
            while (end >= 0) {
                target.append(format, start, end);

                // Look for special placeholder flags
                char p = format.charAt(++end);
                if (p == '+' || p == '-' || p == ' ') {
                    if (p == '+') {
                        adjustment = v -> v.isEmpty() ? "" : "\n" + v;
                    } else if (p == ' ') {
                        adjustment = v -> v.isEmpty() ? "" : " " + v;
                    } else {
                        // TODO How do we take back previous newlines for '-'?
                        throw new UnsupportedOperationException("cannot take back newlines");
                    }
                    p = format.charAt(++end);
                }

                // Resolve the placeholder logic and collection the "options"
                Placeholder<T> placeholder = placeholders.get(p);
                checkArgument(placeholder != null, "invalid placeholder: %s", p);
                String options = format.substring(++end, end + placeholder.optionLength(format, end));

                // Handle the placeholder
                if (placeholder instanceof PaddingPlaceholder) {
                    adjustment = ((PaddingPlaceholder<?>) placeholder).adjust(options);
                } else {
                    String value = placeholder.extract(obj, options);
                    value = adjustment.apply(value);
                    adjustment = Function.identity();
                    target.append(value);
                }

                // Iterate
                start = end + options.length();
                end = format.indexOf('%', start);
            }

            // Finish
            target.append(format, start, format.length());
            if (terminate) {
                target.append(delimiter);
            }
            return target;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
