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
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
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

        private static final CharMatcher CTL = CharMatcher.inRange((char) 0, (char) 31).or(CharMatcher.is((char) 127));

    }

    /**
     * @see <a href="https://tools.ietf.org/html/rfc2045">Multipurpose Internet Mail Extensions (MIME) Part One: Format
     *      of Internet Message Bodies</a>
     */
    @VisibleForTesting
    static final class RFC2045 {

        private static final CharMatcher tspecials = CharMatcher.anyOf("()<>@,;:\\\"/[]?=");

        // This isn't a real rule, but we needed a composed matcher
        private static final CharMatcher TOKEN_CHAR_MATCHER = RFC822.CHAR.and(RFC822.SPACE.or(RFC822.CTL).or(tspecials).negate());

        public static boolean isToken(CharSequence input) {
            return input.length() > 0 && TOKEN_CHAR_MATCHER.matchesAllOf(input);
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

        public static boolean isScheme(CharSequence input) {
            return RFC5234.ALPHA.or(RFC5234.DIGIT).or(CharMatcher.anyOf("+-.")).matchesAllOf(input)
                    && input.length() > 0 && RFC5234.ALPHA.matches(input.charAt(0));
        }

    }

    /**
     * @see <a href="https://tools.ietf.org/html/rfc4288">Media Type Specifications and Registration Procedures</a>
     */
    @VisibleForTesting
    static final class RFC4288 {

        // NOTE: See RFC6838 for restrictions on "." and "+" (facet and suffix)
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

    static final class RFC5234 {

        private static final CharMatcher ALPHA = CharMatcher.inRange('A', 'Z').or(CharMatcher.inRange('a', 'z'));

        private static final CharMatcher DIGIT = CharMatcher.inRange('0', '9');

        private static final CharMatcher DQUOTE = CharMatcher.is('"');

        private static final CharMatcher HEXDIG = DIGIT.or(CharMatcher.inRange('A', 'F'));

        private static final CharMatcher HTAB = CharMatcher.is('\t');

        private static final CharMatcher SP = CharMatcher.is(' ');

        private static final CharMatcher VCHAR = CharMatcher.inRange('!', '~');

        private static final CharMatcher WSP = SP.or(HTAB);

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
            return isRelationType(input) || matchesWithQuotes(input, '"', '"', i -> Splitter.on(RFC5234.SP)
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
            return input.length() > 0 && tchar.matchesAllOf(input);
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

    public static String checkRelationTypes(@Nullable CharSequence input) {
        checkArgument(input != null, "null relation-types");
        checkArgument(RFC5988.isRelationTypes(input), "invalid relation-types: %s", input);
        return input.toString();
    }

    public static String checkURIReference(@Nullable CharSequence input) {
        checkArgument(input != null, "null URI-Reference");
        checkArgument(matchesWithQuotes(input, '"', '"', RFC3986::isURIReference));
        return input.toString();
    }

    public static String checkScheme(@Nullable CharSequence input) {
        checkArgument(input != null, "null scheme");
        checkArgument(RFC3986.isScheme(input), "invalid scheme: %s", input);
        return input.toString();
    }

    public static String checkLanguageTag(@Nullable CharSequence input) {
        checkArgument(input != null, "null Language-Tag");
        checkArgument(RFC5646.isLanguageTag(input), "invalid Language-Tag: %s", input);
        return input.toString();
    }

    public static String checkExtValue(@Nullable CharSequence input) {
        checkArgument(input != null, "null ext-value");
        checkArgument(RFC5987.isExtValue(input), "invalid ext-value: %s", input);
        return input.toString();
    }

    public static String checkMediaDesc(@Nullable CharSequence input) {
        checkArgument(input != null, "null MediaDesc");
        checkArgument(REChtml401.isMediaDesc(input) || matchesWithQuotes(input, '"', '"', REChtml401::isMediaDesc), "invalid MediaDesc: %s", input);
        return input.toString();
    }

    public static String checkMediaTypeOrQuotedMt(@Nullable CharSequence input) {
        checkArgument(input != null, "null media-type");
        checkArgument(RFC5988.isMediaType(input) || matchesWithQuotes(input, '"', '"', RFC5988::isMediaType), "invalid media-type or quoted-mt: %s", input);
        return input.toString();
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
    public static boolean matchesWithQuotes(CharSequence input, char quoteStart, char quoteEnd, Predicate<CharSequence> predicate) {
        int limit = input.length() - 1;
        return input.length() > 2
                && input.charAt(0) == quoteStart
                && input.charAt(limit) == quoteEnd
                && predicate.test(input.subSequence(1, limit));
    }

    /**
     * Tokens are typically defined as one or more character of a specific character set. This allows us to vary the
     * definition for the different definitions of what a token character is.
     */
    public enum TokenType {
        RFC2045(Rules.RFC2045.TOKEN_CHAR_MATCHER, Rules.RFC2045.TOKEN_CHAR_MATCHER),
        RFC5988(Rules.RFC5987.attrChar, Rules.RFC5988.ptokenchar),
        RFC7230(Rules.RFC7230.tchar, Rules.RFC7230.tchar);

        private final CharMatcher keyTokenChar;

        private final CharMatcher valueTokenChar;

        private TokenType(CharMatcher keyTokenChar, CharMatcher valueTokenChar) {
            this.keyTokenChar = Objects.requireNonNull(keyTokenChar);
            this.valueTokenChar = Objects.requireNonNull(valueTokenChar);
        }
    }

    /**
     * Returns the index of the first non-matching character.
     */
    public static int next(CharSequence input, CharMatcher matcher, int start) {
        int pos = start, length = input.length();
        while (pos < length && matcher.matches(input.charAt(pos))) {
            ++pos;
        }
        return pos;
    }

    /**
     * Returns the end index of the token in the character sequence given the specified starting point. If the sequence
     * does not start with a token, the starting position is returned.
     */
    public static int nextToken(TokenType tokenType, CharSequence input, int start) {
        return next(input, tokenType.keyTokenChar, start);
    }

    /**
     * Returns the end index of the next reg-name in the character sequence given the specified starting point.
     */
    public static int nextRegName(CharSequence input, int start) {
        if (input.length() > start + 127) {
            return next(input.subSequence(start, start + 127), RFC4288.regNameChars, start);
        } else {
            return next(input, RFC4288.regNameChars, start);
        }
    }

    /**
     * Returns the end index of the next digit in the character sequence given the specified starting point.
     */
    public static int nextDigit(CharSequence input, int start) {
        return next(input, RFC5234.DIGIT, start);
    }

    /**
     * Returns the start index of the next non-WSP in the character sequence.
     */
    public static int nextNonWsp(CharSequence input, int start) {
        return next(input, RFC5234.WSP, start);
    }

    /**
     * Returns the end index of the next matching character. The input must have at least enough characters to perform
     * the check, if the character matches then this method will effectively return {@code start + 1}.
     */
    // TODO If we can toggle optional/required then this would work for Product as well
    public static int nextChar(CharSequence input, int start, char expected) {
        checkArgument(start < input.length(), "missing %s: %s", expected, input);
        return input.charAt(start) == expected ? start + 1 : start;
    }

    /**
     * Returns the end index of the next quoted string or quoted string. The unquoted (effective) value is accumulated
     * in the supplied buffer.
     */
    public static int nextTokenOrQuotedString(TokenType tokenType, CharSequence input, int start) {
        if (tokenType.valueTokenChar.matches(input.charAt(start))) {
            return nextToken(tokenType, input, start);
        } else if (input.charAt(start) == '"') {
            int pos = start + 1, length = input.length();
            while (pos < length) {
                char c = input.charAt(pos++);
                if (c == '"') {
                    return pos;
                } else if (c == '\\' && pos < length) {
                    ++pos;
                }
            }
            throw new IllegalArgumentException("missing DQUOTE: " + input);
        } else {
            return start;
        }
    }

    /**
     * Reads ";" delimited name/value pairs until the end of input.
     */
    public static int remainingNameValues(TokenType tokenType, CharSequence input, int start, BiConsumer<CharSequence, CharSequence> consumer) {
        int pos = start, length = input.length();
        int end = pos;
        while (end < length) {
            pos = Rules.nextNonWsp(input, end);
            checkArgument(pos < length && input.charAt(pos) == ';', "missing pair delimiter (;): %s", input);
            end = Rules.nextNonWsp(input, pos + 1);

            pos = end;
            end = Rules.nextToken(tokenType, input, pos);
            checkArgument(end > pos, "missing pair name: %s", input);
            CharSequence name = input.subSequence(pos, end);

            pos = Rules.nextNonWsp(input, end);
            checkArgument(pos < input.length() && input.charAt(pos) == '=', "missing pair splitter (=): %s", input);
            end = Rules.nextNonWsp(input, pos + 1);

            pos = end;
            end = Rules.nextTokenOrQuotedString(tokenType, input, pos);
            checkArgument(end > pos, "missing pair value: %s", input);
            consumer.accept(name, input.subSequence(pos, end));
        }
        return end;
    }

    /**
     * Reads whitespace delimited tokens until the end of input. Treats entire comments as a single token.
     */
    public static int remainingTokens(CharSequence input, int start, Consumer<CharSequence> consumer) {
        int pos = start, length = input.length();
        StringBuilder buffer = new StringBuilder();
        int commentDepth = 0;
        while (pos < length) {
            char c = input.charAt(pos++);
            if (c == '\\' && pos < length) {
                buffer.append(input.charAt(++pos));
            } else if (c == '(') {
                commentDepth++;
                buffer.append(c);
            } else if (c == ')') {
                commentDepth--;
                buffer.append(c);
                checkArgument(commentDepth >= 0, "unbalanced '(...)': %s", input);
                if (commentDepth == 0) {
                    consumer.accept(buffer.toString());
                    buffer.setLength(0);
                }
            } else if (RFC5234.WSP.matches(c) && commentDepth == 0) {
                if (buffer.length() > 0) {
                    consumer.accept(buffer.toString());
                    buffer.setLength(0);
                }
            } else {
                buffer.append(c);
            }
        }
        if (buffer.length() > 0) {
            consumer.accept(buffer.toString());
        }
        return pos;
    }

    /**
     * Helper to make a string into a conforming token by stripping away non-token characters.
     */
    @Nullable
    public static String retainTokenChars(TokenType tokenType, @Nullable CharSequence input) {
        if (input != null) {
            String result = tokenType.keyTokenChar.retainFrom(input);
            checkArgument(input.length() == 0 || !result.isEmpty(), "input contained no token characters: " + input);
            return result;
        } else {
            return null;
        }
    }

}
