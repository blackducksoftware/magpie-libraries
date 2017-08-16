/*
 * Copyright 2017 Black Duck Software, Inc.
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
package com.blackducksoftware.common.value;

import static com.google.common.base.Preconditions.checkArgument;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.IllformedLocaleException;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

/**
 * A collection of rules defined in the grammars of various specifications.
 *
 * @author jgustie
 */
final class Rules {

    /**
     * @see <a href="https://tools.ietf.org/html/rfc822">STANDARD FOR THE FORMAT OF ARPA INTERNET TEXT MESSAGES</a>
     */
    @VisibleForTesting
    static final class RFC822 {

        private static final CharMatcher CHAR = CharMatcher.inRange((char) 0, (char) 127);

        private static final CharMatcher SPACE = CharMatcher.is((char) 32);

        private static final CharMatcher CTL = CharMatcher.inRange((char) 0, (char) 31);

    }

    /**
     * @see <a href="https://tools.ietf.org/html/rfc2045">Multipurpose Internet Mail Extensions (MIME) Part One: Format
     *      of Internet Message Bodies</a>
     */
    @VisibleForTesting
    static final class RFC2045 {

        private static final CharMatcher tspecials = CharMatcher.anyOf("()<>@,;:\\\"/[]?=");

        public static boolean isToken(CharSequence input) {
            return input.length() > 0 && RFC822.CHAR.and(RFC822.SPACE.or(RFC822.CTL).or(tspecials).negate()).matchesAllOf(input);
        }

    }

    /**
     * @see <a href="https://tools.ietf.org/html/rfc2616">Hypertext Transfer Protocol -- HTTP/1.1</a>
     */
    @VisibleForTesting
    static final class RFC2616 {

        private static final CharMatcher LOALPHA = CharMatcher.inRange('a', 'z');

    }

    /**
     * @see <a href="https://tools.ietf.org/html/rfc3986">Uniform Resource Identifier (URI): Generic Syntax</a>
     */
    @VisibleForTesting
    static final class RFC3986 {

        public static boolean isURI(CharSequence input) {
            // TODO Lazy implementation...java.net.URI uses the RFC2396 definition...we need the RFC3986 definition
            try {
                return new URI(input.toString()).isAbsolute();
            } catch (URISyntaxException e) {
                return false;
            }
        }

        public static boolean isURIReference(CharSequence input) {
            // TODO Lazy implementation...java.net.URI uses the RFC2396 definition...we need the RFC3986 definition
            try {
                new URI(input.toString());
                return true;
            } catch (URISyntaxException e) {
                return false;
            }
        }

    }

    /**
     * @see <a href="https://tools.ietf.org/html/rfc4288">Media Type Specifications and Registration Procedures</a>
     */
    @VisibleForTesting
    static final class RFC4288 {
        private static final CharMatcher regNameChars = RFC5234.ALPHA.or(RFC5234.DIGIT).or(CharMatcher.anyOf("!#$&.+-^_"));

        public static boolean isRegName(CharSequence input) {
            return input.length() > 0 && input.length() < 128 && regNameChars.matchesAllOf(input);
        }

        public static boolean isTypeName(CharSequence input) {
            return isRegName(input);
        }

        public static boolean isSubtypeName(CharSequence input) {
            return isRegName(input);
        }

    }

    /**
     * @see <a href="https://tools.ietf.org/html/rfc5234">Augmented BNF for Syntax Specifications: ABNF</a>
     */
    @VisibleForTesting
    static final class RFC5234 {

        private static final CharMatcher ALPHA = CharMatcher.inRange('A', 'Z').or(CharMatcher.inRange('a', 'z'));

        private static final CharMatcher DIGIT = CharMatcher.inRange('0', '9');

        private static final CharMatcher DQUOTE = CharMatcher.is('"');

        private static final CharMatcher HEXDIG = DIGIT.or(CharMatcher.inRange('A', 'F'));

        private static final CharMatcher HTAB = CharMatcher.is('\t');

        private static final CharMatcher SP = CharMatcher.is(' ');

        private static final CharMatcher VCHAR = CharMatcher.inRange('!', '~');

    }

    /**
     * @see <a href="https://tools.ietf.org/html/rfc5646">Tags for Identifying Languages</a>
     */
    @VisibleForTesting
    static final class RFC5646 {

        public static boolean isLanguageTag(CharSequence input) {
            // TODO Lazy implementation...BCP 47 is defined by RFC5646, this just uses Java's registry on top of that
            try {
                new Locale.Builder().setLanguageTag(input.toString());
                return true;
            } catch (IllformedLocaleException e) {
                return false;
            }
        }

    }

    /**
     * @see <a href="https://tools.ietf.org/html/rfc5987">Character Set and Language Encoding for Hypertext Transfer
     *      Protocol (HTTP) Header Field Parameters</a>
     */
    @VisibleForTesting
    static final class RFC5987 {

        private static final CharMatcher attrChar = RFC5234.ALPHA.or(RFC5234.DIGIT).or(CharMatcher.anyOf("!#$&+-.^_`|~"));

        private static final CharMatcher mimeCharsetc = RFC5234.ALPHA.or(RFC5234.DIGIT).or(CharMatcher.anyOf("!#$%&+-^_`{}~"));

        public static boolean isValueChars(CharSequence input) {
            int length = input.length();
            for (int index = 0; index < length; ++index) {
                if (input.charAt(index) == '%') {
                    if (index + 2 > length
                            || !RFC5234.HEXDIG.matches(input.charAt(index + 1))
                            || !RFC5234.HEXDIG.matches(input.charAt(index + 2))) {
                        return false;
                    }
                } else if (!attrChar.matches(input.charAt(index))) {
                    return false;
                }
            }
            return true;
        }

        public static boolean isCharset(CharSequence input) {
            return "UTF-8".contentEquals(input)
                    || "ISO-8859-1".contentEquals(input)
                    || (input.length() > 0 && mimeCharsetc.matchesAllOf(input));
        }

        public static boolean isParmname(CharSequence input) {
            return input.length() > 0 && attrChar.matchesAllOf(input);
        }

        public static boolean isExtValue(CharSequence input) {
            List<String> extValue = Splitter.on('\'').limit(3).splitToList(input);
            return extValue.size() == 3
                    && isCharset(extValue.get(0))
                    && (extValue.get(1).isEmpty() || RFC5646.isLanguageTag(extValue.get(1)))
                    && isValueChars(extValue.get(2));
        }

    }

    /**
     * @see <a href="https://tools.ietf.org/html/rfc5988">Web Linking</a>
     */
    @VisibleForTesting
    static final class RFC5988 {

        private static final CharMatcher ptokenchar = RFC5234.ALPHA.or(RFC5234.DIGIT).or(CharMatcher.anyOf("!#$%&'()*+-./:<=>?@[]^_`{|}~"));

        public static boolean isExtNameStar(CharSequence input) {
            return input.length() > 1 && input.charAt(input.length() - 1) == '*'
                    && RFC5987.isParmname(input.subSequence(0, input.length() - 1));
        }

        public static boolean isPtoken(CharSequence input) {
            return input.length() > 0 && ptokenchar.matchesAllOf(input);
        }

        public static boolean isRegRelType(CharSequence input) {
            return input.length() > 0 && RFC2616.LOALPHA.matches(input.charAt(0))
                    && RFC2616.LOALPHA.or(RFC5234.DIGIT).or(CharMatcher.is('.')).or(CharMatcher.is('-')).matchesAllOf(input);
        }

        public static boolean isRelationType(CharSequence input) {
            return isRegRelType(input) || RFC3986.isURI(input);
        }

        public static boolean isRelationTypes(CharSequence input) {
            return isRelationType(input) || matchesWithQuotes(input, '"', i -> Splitter.on(RFC5234.SP)
                    .omitEmptyStrings()
                    .splitToList(i)
                    .stream()
                    .allMatch(RFC5988::isRelationType));

        }

        public static boolean isMediaType(CharSequence input) {
            List<String> mediaType = Splitter.on('/').limit(2).splitToList(input);
            return mediaType.size() == 2
                    && RFC4288.isTypeName(mediaType.get(0))
                    && RFC4288.isSubtypeName(mediaType.get(1));
        }

        public static boolean isLinkExtension(CharSequence parmname, CharSequence value) {
            if (RFC5987.isParmname(parmname)) {
                return isPtoken(value) || RFC7230.isQuotedString(value);
            } else if (isExtNameStar(parmname)) {
                return RFC5987.isExtValue(value);
            } else {
                return false;
            }
        }

    }

    /**
     * @see <a href="https://tools.ietf.org/html/rfc7230">Hypertext Transfer Protocol (HTTP/1.1): Message Syntax and
     *      Routing</a>
     */
    @VisibleForTesting
    static final class RFC7230 {

        private static final CharMatcher tchar = CharMatcher.anyOf("!#$%&'*+-.^_`|~").or(RFC5234.DIGIT).or(RFC5234.ALPHA);

        private static final CharMatcher obsText = CharMatcher.inRange((char) 0x80, (char) 0xFF);

        private static final CharMatcher qdtext = RFC5234.HTAB.or(RFC5234.SP)
                .or(CharMatcher.is('!'))
                .or(CharMatcher.inRange('#', '['))
                .or(CharMatcher.inRange(']', '~'))
                .or(obsText);

        private static final CharMatcher ctext = RFC5234.HTAB.or(RFC5234.SP)
                .or(CharMatcher.inRange('!', '\''))
                .or(CharMatcher.inRange('*', '['))
                .or(CharMatcher.inRange(']', '~'))
                .or(obsText);

        public static boolean isCtext(char c) {
            return ctext.matches(c);
        }

        public static boolean isToken(CharSequence input) {
            return tchar.matchesAllOf(input);
        }

        public static boolean isQuotedPair(CharSequence input) {
            return input.length() == 2
                    && input.charAt(0) == '\\'
                    && RFC5234.HTAB.or(RFC5234.SP).or(RFC5234.VCHAR).or(obsText).matches(input.charAt(1));
        }

        public static boolean isQuotedString(CharSequence input) {
            int limit = input.length() - 1;
            if (input.length() >= 2
                    && RFC5234.DQUOTE.matches(input.charAt(0))
                    && RFC5234.DQUOTE.matches(input.charAt(limit))) {
                int pos = 1;
                while (pos < limit) {
                    if (qdtext.matches(input.charAt(pos))) {
                        pos = pos + 1;
                    } else if (isQuotedPair(input.subSequence(pos, Math.min(pos + 2, limit)))) {
                        pos = pos + 2;
                    } else {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        }

        public static boolean isComment(CharSequence input) {
            int limit = input.length() - 1;
            if (input.length() >= 2
                    && input.charAt(0) == '('
                    && input.charAt(limit) == ')') {
                int pos = 1;
                while (pos < limit) {
                    if (ctext.matches(input.charAt(pos))) {
                        pos = pos + 1;
                    } else if (isQuotedPair(input.subSequence(pos, Math.min(pos + 2, limit)))) {
                        pos = pos + 2;
                    } else {
                        int end = nextComment(input, pos, limit);
                        if (isComment(input.subSequence(pos, end))) {
                            pos = end;
                        } else {
                            return false;
                        }
                    }
                }
                return true;
            } else {
                return false;
            }
        }

        /**
         * Because comments can exist next to one another (e.g. "(()())" is valid), we need to be able to find the end
         * of the current comment within the limit and honoring quoted-pairs.
         */
        private static int nextComment(CharSequence input, int start, int limit) {
            for (int end = start; end < limit; ++end) {
                if (input.charAt(end) == '\\') {
                    end = end + 1;
                } else if (input.charAt(end) == ')') {
                    return end + 1;
                }
            }
            return limit;
        }

    }

    /**
     * @see <a href="https://tools.ietf.org/html/rfc7233">Hypertext Transfer Protocol (HTTP/1.1): Range Requests</a>
     */
    @VisibleForTesting
    static final class RFC7233 {

        private static final String bytesUnit = "bytes";

        public static final boolean isBytesUnit(CharSequence input) {
            return bytesUnit.contentEquals(input);
        }

        public static final boolean isOtherRangeUnit(CharSequence input) {
            return RFC7230.isToken(input);
        }

    }

    /**
     * @see <a href="https://www.w3.org/TR/1999/REC-html401-19991224/">HTML 4.01 Specification</a>
     */
    static final class REChtml401 {

        public static final boolean isMediaDesc(CharSequence input) {
            // Technically this is just a CDATA field; which we know because it's passed as a sequence of characters...
            // That said, it _should_ be a "single or comma-separated list of media descriptors" (ALPHA/DIGIT/"-").
            return true;
        }

    }

    public static String checkToken(@Nullable CharSequence input) {
        checkArgument(input != null, "null token");
        checkArgument(RFC7230.isToken(input), "invalid token: %s", input);
        return input.toString();
    }

    public static String checkComment(@Nullable CharSequence input) {
        checkArgument(input != null, "null comment");
        checkArgument(RFC7230.isComment(input), "invalid comment: %s", input);
        return input.toString();
    }

    public static String checkQuotedString(@Nullable CharSequence input) {
        checkArgument(input != null, "null comment");
        checkArgument(RFC7230.isQuotedString(input), "invalid quoted-string: %s", input);
        return input.toString();
    }

    public static void checkRelationTypes(@Nullable CharSequence input) {
        checkArgument(input != null, "null relation-types");
        checkArgument(RFC5988.isRelationTypes(input), "invalid relation-types: %s", input);
    }

    public static void checkURIReference(@Nullable CharSequence input) {
        checkArgument(input != null, "null URI-Reference");
        checkArgument(matchesWithQuotes(input, '"', RFC3986::isURIReference));
    }

    public static void checkLanguageTag(@Nullable CharSequence input) {
        checkArgument(input != null, "null Language-Tag");
        checkArgument(RFC5646.isLanguageTag(input), "invalid Language-Tag: %s", input);
    }

    public static void checkExtValue(@Nullable CharSequence input) {
        checkArgument(input != null, "null ext-value");
        checkArgument(RFC5987.isExtValue(input), "invalid ext-value: %s", input);
    }

    public static void checkMediaDesc(@Nullable CharSequence input) {
        checkArgument(input != null, "null MediaDesc");
        checkArgument(REChtml401.isMediaDesc(input) || matchesWithQuotes(input, '"', REChtml401::isMediaDesc), "invalid MediaDesc: %s", input);
    }

    public static void checkMediaTypeOrQuotedMt(@Nullable CharSequence input) {
        checkArgument(input != null, "null media-type");
        checkArgument(RFC5988.isMediaType(input) || matchesWithQuotes(input, '"', RFC5988::isMediaType), "invalid media-type or quoted-mt: %s", input);
    }

    public static void checkLinkExtension(@Nullable CharSequence parmname, @Nullable CharSequence value) {
        checkArgument(parmname != null, "null parmname");
        checkArgument(value != null, "null value");
        checkArgument(RFC5988.isLinkExtension(parmname, value), "invalid link-extension: %s=%s", parmname, value);
    }

    public static String checkAttribute(@Nullable CharSequence input) {
        checkArgument(input != null, "null attribute");
        checkArgument(RFC2045.isToken(input), "invalid attribute: %s", input);
        return input.toString().toLowerCase();
    }

    public static String checkValue(@Nullable CharSequence input) {
        checkArgument(input != null, "null value");
        checkArgument(RFC2045.isToken(input) || RFC7230.isQuotedString(input), "invalid value: %s", input);
        return input.toString();
    }

    public static String checkType(@Nullable CharSequence input) {
        checkArgument(input != null, "null type");
        checkArgument(RFC4288.isTypeName(input), "invalid type: %s", input);
        return input.toString().toLowerCase();
    }

    public static String checkSubtype(@Nullable CharSequence input) {
        checkArgument(input != null, "null subtype");
        checkArgument(RFC4288.isSubtypeName(input), "invalid subtype: %s", input);
        return input.toString().toLowerCase();
    }

    public static boolean isBytesUnit(@Nullable CharSequence input) {
        checkArgument(input != null, "null unit");
        return RFC7233.isBytesUnit(input);
    }

    public static String checkOtherRangeUnit(@Nullable CharSequence input) {
        checkArgument(input != null, "null unit");
        checkArgument(RFC7233.isOtherRangeUnit(input), "invalid unit: %s", input);
        return input.toString();
    }

    /**
     * Common use case where we need to check quotes and further validate the content
     */
    private static boolean matchesWithQuotes(CharSequence input, char quote, Predicate<CharSequence> predicate) {
        int limit = input.length() - 1;
        return input.length() > 2
                && input.charAt(0) == quote
                && input.charAt(limit) == quote
                && predicate.test(input.subSequence(1, limit));
    }

}
