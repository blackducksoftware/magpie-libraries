/*
 * Copyright 2016 Black Duck Software, Inc.
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

import static com.google.common.base.Ascii.toLowerCase;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Ascii;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableSet;
import com.google.common.net.UrlEscapers;

/**
 * A Hierarchical Identifier (HID). A HID is a special kind of identifier for uniquely identifying directory-like
 * structures found on file systems. HIDs are normalized so as to be unaffected by differences in path separators,
 * combinatorial characters, etc. They are also effectively two-dimensional as they can be used to identify structures
 * nested within other structures (such is the case with archives). The API allows for convenient traversal.
 *
 * @author jgustie
 */
public final class HID {

    /**
     * URI schemes that use the hierarchical archive scheme definition.
     */
    private static final ImmutableSet<String> HIERARCHICAL_FRAGMENT_SCHEMES = ImmutableSet.of("zip", "jar", "tar", "rpm", "ar", "arj", "cpio", "dump",
            "sevenz");

    /**
     * The standardized path separator character.
     */
    private static final char PATH_SEPARATOR_CHAR = '/';

    /**
     * The character used for joining paths.
     */
    private static final Joiner PATH_JOINER = Joiner.on(PATH_SEPARATOR_CHAR);

    /**
     * The character used for splitting paths.
     */
    private static final Splitter PATH_SPLITTER = Splitter.on(PATH_SEPARATOR_CHAR).omitEmptyStrings();

    /**
     * An empty path.
     */
    private static final String[] EMPTY_PATH = new String[0];

    /**
     * Static cache so the path arrays are re-used (i.e. this is more for memory then CPU savings).
     */
    private static final Map<String, String[]> PATH_CACHE = new LinkedHashMap<String, String[]>() {
        private static final long serialVersionUID = 1L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String[]> eldest) {
            return size() > 1000;
        }

        @Override
        public String[] computeIfAbsent(String key, Function<? super String, ? extends String[]> mappingFunction) {
            synchronized (this) {
                return super.computeIfAbsent(key, mappingFunction);
            }
        }
    };

    /**
     * Converts an archive entry name into a normalized path.
     */
    private static String[] toPath(String entryName) {
        // Detect empty paths
        if (entryName.isEmpty()) {
            return EMPTY_PATH;
        }
        boolean absolute = entryName.charAt(0) == PATH_SEPARATOR_CHAR;
        if (entryName.length() == 1 && (entryName.charAt(0) == '.' || absolute)) {
            return EMPTY_PATH;
        }

        // Split the path into a list that contains the relevant bits
        List<String> path = new ArrayList<>();
        for (String segment : PATH_SPLITTER.split(entryName)) {
            if (segment.equals("..")) {
                if (path.size() > 0 && !path.get(path.size() - 1).equals("..")) {
                    path.remove(path.size() - 1);
                } else if (!absolute || !path.isEmpty()) {
                    path.add("..");
                }
            } else if (!segment.equals(".")) {
                path.add(normalizePathSegment(segment));
            }
        }
        return path.toArray(EMPTY_PATH);
    }

    /**
     * Normalization procedure for path segments.
     */
    private static String normalizePathSegment(String segment) {
        // Perform Unicode normalization on the text
        return Normalizer.normalize(segment, Normalizer.Form.NFC);
    }

    /**
     * Ordering that sorts HID according to a pre-order tree traversal.
     */
    private static int preOrderTraversal(HID left, HID right) {
        ComparisonChain compare = ComparisonChain.start();
        for (int i = 0; i < Math.min(left.segments.length, right.segments.length) && compare.result() == 0; ++i) {
            String[] leftNested = left.segments[i], rightNested = right.segments[i];
            for (int j = 0; j < Math.min(leftNested.length, rightNested.length) && compare.result() == 0; ++j) {
                compare = compare.compare(leftNested[j], rightNested[j], HID::comparePathSegments);
            }
            compare = compare.compare(leftNested.length, rightNested.length);
        }
        compare = compare.compare(left.segments.length, right.segments.length);
        return compare.result();
    }

    // TODO postOrderTraversal? (abcdefgh)
    // TODO breadthFirstTraversal? (hdegabcf)

    /**
     * The ordering of individual "file name" segments.
     */
    // TODO Numeric/version smart sorting
    // TODO Case sensitivity?
    // TODO Locale specific (collate)?
    // TODO Work breaks?
    // TODO Share logic with version comparator?
    private static int comparePathSegments(String left, String right) {
        return left.compareTo(right);
    }

    /**
     * Returns an ordering that imposes a pre-order tree traversal.
     * <p>
     * For example, given the tree:
     *
     * <pre>
     *          h
     *        / | \
     *       /  e  \
     *      d       g
     *     /|\      |
     *    / | \     f
     *   a  b  c
     * </pre>
     *
     * Given a collection of unordered HID's, this ordering would produce {@code hdabcegf}.
     */
    public static Comparator<HID> preOrder() {
        return HID::preOrderTraversal;
    }

    /**
     * Returns an ordering that ignores all path information and only considers the name.
     */
    public static Comparator<HID> ignorePathOrder() {
        return Comparator.comparing(HID::getName, HID::comparePathSegments);
    }

    /**
     * The URI scheme associated with each segment level.
     */
    private final String[] schemes;

    /**
     * The URI authority associated with each segment level.
     */
    private final String[] authorities;

    /**
     * The segments representing this HID. The first dimension is the "nesting" by container dimension, the second
     * dimension is the "depth" by path dimension.
     * <p>
     * It is assumed that the segments are fully normalized before being passed in.
     */
    private final String[][] segments;

    private HID(String[] schemes, String[] authorities, String[][] segments, int nesting, int depth) {
        checkArgument(schemes.length == authorities.length, "schemes and authorities lengths must match");
        checkArgument(schemes.length == segments.length, "schemes and segment lengths must match");
        checkArgument(segments.length > 0, "segements must not be empty");
        checkArgument(nesting >= 0, "nesting must be >= 0");
        checkArgument(depth <= segments[segments.length - 1].length, "depth cannot exceed existing segment count");

        this.schemes = Arrays.copyOf(schemes, nesting + 1);
        this.authorities = Arrays.copyOf(authorities, nesting + 1);

        // Copy the segments over
        this.segments = new String[nesting + 1][];
        this.segments[nesting] = new String[depth < 0 ? segments[nesting].length : depth];

        System.arraycopy(segments, 0, this.segments, 0, nesting);
        System.arraycopy(segments[nesting], 0, this.segments[nesting], 0, this.segments[nesting].length);
    }

    /**
     * Internal use only factory method.
     */
    private static HID create(String scheme, @Nullable String authority, @Nullable HID container, String entryName) {
        Objects.requireNonNull(scheme);
        String[] path = PATH_CACHE.computeIfAbsent(entryName, HID::toPath);
        if (container != null) {
            // Copy the container
            String[][] segments = Arrays.copyOf(container.segments, container.segments.length + 1);
            segments[segments.length - 1] = path;

            String[] schemes = Arrays.copyOf(container.schemes, container.schemes.length + 1);
            schemes[schemes.length - 1] = toLowerCase(scheme);

            String[] authorities = Arrays.copyOf(container.authorities, container.authorities.length + 1);
            authorities[authorities.length - 1] = nullToEmpty(authority);

            return new HID(schemes, authorities, segments, segments.length - 1, -1);
        } else {
            // No container, this is a base HID
            return new HID(new String[] { toLowerCase(scheme) }, new String[] { nullToEmpty(authority) }, new String[][] { path }, 0, -1);
        }
    }

    /**
     * Creates a new HID from a non-null supported object. A HID can be created from a {@code String}, {@code URI} or
     * {@code Path} instance.
     */
    public static HID from(Object obj) {
        if (obj instanceof HID) {
            return (HID) obj;
        }
        // TODO How can we implement this to not use a URI? For performance and RFC 3986...
        URI uri;
        if (obj instanceof URI) {
            uri = (URI) obj;
        } else if (obj instanceof Path) {
            // NOTE: Paths have an independent root that is not part of their path segments. Rather then attempt to
            // normalize the root and reconcile that, it is easier to just convert the Path into a URI and convert
            // that into a HID. To URI conversion will normalize the root handling across platforms.
            uri = ((Path) obj).toUri();
        } else if (obj instanceof String) {
            uri = URI.create((String) obj);
        } else {
            throw new IllegalArgumentException("unexpected input: " + obj);
        }

        checkArgument(uri.isAbsolute(), "URI must be absolute: %s", uri);
        String scheme = Ascii.toLowerCase(uri.getScheme());
        // NOTE: "jar" can be a hierarchical scheme if it has a fragment!
        if (uri.getFragment() != null
                && (HIERARCHICAL_FRAGMENT_SCHEMES.contains(scheme) || uri.getFragment().startsWith("/"))) {
            // Hierarchical schemes use "<scheme>:<archiveUri>#<entryName>" for URIs
            HID container = from(URI.create(uri.getSchemeSpecificPart()));
            return create(uri.getScheme(), null, container, uri.getFragment());
        } else if (scheme.equals("jar")) {
            // Java's JAR scheme uses "<scheme>:<archiveUri>!/<entryName>" for URLs
            return fromJarUrl(uri.toString());
        } else {
            // For other URIs, we do not have any place to put non-path information
            checkArgument(!isNullOrEmpty(uri.getPath()), "path must not be empty: %s", uri);
            checkArgument(isNullOrEmpty(uri.getQuery()), "query must be empty: %s", uri);
            checkArgument(isNullOrEmpty(uri.getFragment()), "fragment must be empty or start with '/': %s", uri);
            return create(uri.getScheme(), uri.getAuthority(), null, uri.getPath());
        }
    }

    private static HID fromJarUrl(String url) {
        // See java.net.JarURLConnection#parseSpecs
        try {
            String spec = new URL(url).getFile();
            int separator = spec.indexOf("!/");
            if (separator == -1) {
                throw new MalformedURLException("no !/ found in url spec:" + spec);
            }

            HID container = from(new URL(spec.substring(0, separator++)).toString());
            String entryName = null;
            if (++separator != spec.length()) {
                entryName = spec.substring(separator, spec.length());
                entryName = URLDecoder.decode(entryName, "UTF-8");
            }

            return create("jar", null, container, entryName);
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            throw new IllegalArgumentException("bad JAR URL", e);
        }
    }

    @VisibleForTesting
    int nesting() {
        return segments.length - 1;
    }

    @VisibleForTesting
    int depth() {
        return segments[nesting()].length;
    }

    private HID parent() {
        assert depth() > 0;
        return new HID(schemes, authorities, segments, nesting(), depth() - 1);
    }

    private HID container() {
        assert nesting() > 0;
        return new HID(schemes, authorities, segments, nesting() - 1, -1);
    }

    /**
     * Returns the most specific scheme in this HID.
     */
    public String getScheme() {
        return schemes[nesting()];
    }

    /**
     * Returns the most specific name in this HID.
     */
    public String getName() {
        return segments[nesting()].length > 0 ? segments[nesting()][depth() - 1] : "/";
    }

    /**
     * Returns the most specific path in this HID.
     */
    public String getPath() {
        return PATH_JOINER.appendTo(new StringBuilder().append(PATH_SEPARATOR_CHAR), segments[nesting()]).toString();
    }

    /**
     * Returns the individual names for the most specific path in this HID.
     */
    public List<String> getPathNames() {
        return Collections.unmodifiableList(Arrays.asList(segments[nesting()]));
    }

    /**
     * Returns the individual names of each container in this HID. The most specific container is first.
     */
    public List<String> getContainerNames() {
        // This is small enough, just copy the data so it can be accessed randomly.
        String[] result = new String[nesting()];
        for (int i = 0; i < result.length; ++i) {
            int j = segments.length - i - 2;
            result[i] = segments[j][segments[j].length - 1];
        }
        return Collections.unmodifiableList(Arrays.asList(result));
    }

    /**
     * Returns {@code true} if the most specific path in this HID contains exactly the specified name components.
     */
    public boolean containsPathNames(String... names) {
        // Looks a lot like `Collections.indexOfSublist`
        int max = depth() - names.length;
        next: for (int index = 0; index <= max; ++index) {
            for (int i = 0, j = index; i < names.length; ++i, ++j) {
                if (!names[i].equals(segments[nesting()][j])) {
                    continue next;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the path of the supplied HID relativized against this HID. Both HIDs must be at the same nesting level
     * and this HID must be an ancestor of the supplied HID; otherwise this method just returns {@code null}.
     */
    @Nullable
    public String getRelativizedPath(HID other) {
        if (nesting() == other.nesting() && other.isAncestor(this)) {
            return PATH_JOINER.join(Arrays.asList(other.segments[nesting()]).subList(depth(), other.depth()));
        } else {
            return null;
        }
    }

    /**
     * Returns the directory level parent identifier. This corresponds to the "directory" that contains this identifier.
     * <p>
     * Note that if this HID is a "{@linkplain #getRoot() root}", this method will return the container HID.
     */
    @Nullable
    public HID getParent() {
        if (isRoot()) {
            return getContainer();
        } else {
            return parent();
        }
    }

    /**
     * Returns the directory level parent identifier. This corresponds to the "directory" that contains this identifier.
     * <p>
     * Note that if this HID is a "{@linkplain #getRoot() root}", this method will return the container HID.
     */
    public Optional<HID> tryParent() {
        if (isRoot()) {
            return tryContainer();
        } else {
            return Optional.of(parent());
        }
    }

    /**
     * Checks to see if this HID has a parent.
     * <p>
     * <em>IMPORTANT</em> This check is not consistent with {@code getParent() != null} as it only considers parents at
     * the current nesting level. The {@code getParent()} method could still return the container even if this method
     * returns {@code false}.
     *
     * @deprecated Use {@code !isRoot()} instead.
     */
    @Deprecated
    public boolean hasParent() {
        // Note that there is no intention on removing this, this method serves as documentation
        return !isRoot();
    }

    /**
     * Returns the directory level root identifier. This corresponds to the top-most directory (e.g. "/") at the current
     * nesting level.
     */
    public HID getRoot() {
        if (isRoot()) {
            return this;
        } else {
            return new HID(schemes, authorities, segments, nesting(), 0);
        }
    }

    /**
     * Returns {@code true} if this HID represents a root at the current nesting level.
     */
    public boolean isRoot() {
        return depth() == 0;
    }

    /**
     * Returns the container identifier. This corresponds to the "archive" that contains this identifier.
     */
    @Nullable
    public HID getContainer() {
        if (hasContainer()) {
            return container();
        } else {
            return null;
        }
    }

    /**
     * Returns the container identifier. This corresponds to the "archive" that contains this identifier.
     */
    public Optional<HID> tryContainer() {
        if (hasContainer()) {
            return Optional.of(container());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Returns {@code true} if this HID has a container.
     */
    public boolean hasContainer() {
        return nesting() > 0;
    }

    /**
     * Returns the base identifier. This corresponds to the "outermost path" (i.e. the file system path the scan started
     * from).
     */
    public HID getBase() {
        if (hasContainer()) {
            return new HID(schemes, authorities, segments, 0, -1);
        } else {
            return this;
        }
    }

    /**
     * Determines if the supplied HID is an ancestor of this HID.
     */
    public boolean isAncestor(HID other) {
        int otherNesting = other.nesting();

        // If we are at a lower nesting level, the other one cannot be an ancestor
        if (nesting() < otherNesting || segments[otherNesting].length < other.segments[otherNesting].length) {
            return false;
        }

        // Compare all but the last segment (which is allowed to be different)
        for (int i = 0; i < otherNesting; ++i) {
            if (!Arrays.equals(segments[i], other.segments[i])) {
                return false;
            }
        }

        // Compare all segments at the other nesting level
        int otherDepth = other.depth();
        for (int i = 0; i < otherDepth; ++i) {
            if (!segments[otherNesting][i].equals(other.segments[otherNesting][i])) {
                return false;
            }
        }

        // An equal cannot be an ancestor, compare depths and nesting
        return depth() > other.depth() || nesting() > other.nesting();
    }

    /**
     * Returns this HID as a URI string useful for serialization.
     */
    public String toUriString() {
        StringBuilder buffer = new StringBuilder(128).append(schemes[0]).append("://").append(authorities[0]).append('/');
        String result = PATH_JOINER.appendTo(buffer, Stream.of(segments[0]).map(UrlEscapers.urlPathSegmentEscaper()::escape).iterator()).toString();
        for (int i = 1; i < segments.length; ++i) {
            buffer.setLength(0);
            result = buffer.append(schemes[i]).append(':')
                    .append(UrlEscapers.urlPathSegmentEscaper().escape(result))
                    .append("#/")
                    .append(UrlEscapers.urlFragmentEscaper().escape(PATH_JOINER.join(segments[i])))
                    .toString();
        }
        return result;
    }

    /**
     * Returns this HID as a URI useful for serialization.
     * <p>
     * <em>WARNING</em> This method will fail for non-RFC 2396 URIs.
     *
     * @deprecated Use {@link #toUriString()} instead. This method will be removed in a future release.
     */
    @Deprecated
    public URI toUri() {
        return URI.create(toUriString());
    }

    /**
     * Returns the HID as a string useful for debugging.
     */
    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append("[ ");
        for (int i = 0; i < segments.length; ++i) {
            if (i > 0) {
                string.append(", ");
            }
            string.append(schemes[i]).append(":/");
            if (!authorities[i].isEmpty()) {
                string.append('/').append(authorities[i]).append('/');
            }
            PATH_JOINER.appendTo(string, segments[i]);
        }
        string.append(" ]");
        return string.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(schemes), Arrays.hashCode(authorities), Arrays.deepHashCode(segments));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HID) {
            HID other = (HID) obj;
            return Arrays.equals(schemes, other.schemes)
                    && Arrays.equals(authorities, other.authorities)
                    && Arrays.deepEquals(segments, other.segments);
        }
        return false;
    }

}
