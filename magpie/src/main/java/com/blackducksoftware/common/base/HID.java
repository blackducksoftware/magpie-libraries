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
package com.blackducksoftware.common.base;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
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
     * URI schemes that use the Black Duck archive scheme definition.
     */
    private static final Set<String> BLACK_DUCK_SCHEMES = ImmutableSet.of("zip", "jar", "bdjar", "tar", "rpm", "ar", "arj", "cpio", "dump", "sevenz");

    /**
     * URI schemes that use the Java archive scheme definition.
     */
    private static final Set<String> JAVA_SCHEMES = ImmutableSet.of("jar");

    /**
     * The character used for joining paths.
     */
    private static final Joiner PATH_JOINER = Joiner.on('/');

    /**
     * The character used for splitting paths.
     */
    private static final Splitter PATH_SPLITTER = Splitter.on('/').omitEmptyStrings();

    /**
     * An empty path.
     */
    private static final String[] EMPTY_PATH = new String[0];

    /**
     * Normalization procedure for path segments.
     */
    private static final Function<String, String> PATH_SEGMENT_NORMALIZER = new Function<String, String>() {
        @Override
        public String apply(String segment) {
            // Perform Unicode normalization on the text
            return Normalizer.normalize(segment, Normalizer.Form.NFC);
        }
    };

    /**
     * The ordering of individual "file name" segments.
     */
    // TODO Numeric/version smart sorting
    // TODO Case sensitivity?
    // TODO Locale specific?
    // TODO Work breaks?
    // TODO Share logic with version comparator?
    private static final Ordering<String> SEGMENT_ORDER = Ordering.natural();

    /**
     * Ordering that sorts HID according to a pre-order tree traversal.
     */
    private static final Ordering<HID> PRE_ORDER = new Ordering<HID>() {
        @Override
        public int compare(HID left, HID right) {
            ComparisonChain compare = ComparisonChain.start();
            for (int i = 0; i < Math.min(left.segments.length, right.segments.length) && compare.result() == 0; ++i) {
                String[] leftNested = left.segments[i], rightNested = right.segments[i];
                for (int j = 0; j < Math.min(leftNested.length, rightNested.length) && compare.result() == 0; ++j) {
                    compare = compare.compare(leftNested[j], rightNested[j], SEGMENT_ORDER);
                }
                compare = compare.compare(leftNested.length, rightNested.length);
            }
            compare = compare.compare(left.segments.length, right.segments.length);
            return compare.result();
        }
    };

    /**
     * Converts an archive entry name into a normalized path. Results are stored in a static cache so the arrays are
     * re-used (i.e. this is more for memory then CPU savings).
     */
    private static final LoadingCache<String, String[]> PATHS = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .build(new CacheLoader<String, String[]>() {
                @Override
                public String[] load(String entryName) {
                    // Detect empty paths
                    if (entryName.isEmpty()) {
                        return EMPTY_PATH;
                    }
                    boolean absolute = entryName.charAt(0) == '/';
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
                            path.add(segment);
                        }
                    }

                    // Copy and normalize the path segments
                    if (path.isEmpty()) {
                        return EMPTY_PATH;
                    } else {
                        String[] result = new String[path.size()];
                        for (int i = 0; i < result.length; ++i) {
                            result[i] = PATH_SEGMENT_NORMALIZER.apply(path.get(i));
                        }
                        return result;
                    }
                }
            });

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

    /**
     * Lazily computed URI representation.
     */
    private volatile URI uri;

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
        String[] path = PATHS.getUnchecked(entryName);
        if (container != null) {
            // Copy the container
            String[][] segments = Arrays.copyOf(container.segments, container.segments.length + 1);
            segments[segments.length - 1] = path;

            String[] schemes = Arrays.copyOf(container.schemes, container.schemes.length + 1);
            schemes[schemes.length - 1] = scheme;

            String[] authorities = Arrays.copyOf(container.authorities, container.authorities.length + 1);
            authorities[authorities.length - 1] = nullToEmpty(authority);

            return new HID(schemes, authorities, segments, segments.length - 1, -1);
        } else {
            // No container, this is a base HID
            return new HID(new String[] { scheme }, new String[] { nullToEmpty(authority) }, new String[][] { path }, 0, -1);
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

    public static HID from(URI uri) {
        checkArgument(uri.isAbsolute(), "URI must be absolute: '%s'", uri);
        String scheme = uri.getScheme().toLowerCase();
        if (BLACK_DUCK_SCHEMES.contains(scheme) && uri.getFragment() != null) {
            // Black Duck schemes use "<scheme>:<archiveUri>#<entryName>" for URIs
            // THESE SCHEMES MAY HAVE FRAGMENTS THAT DO NOT START WITH "/"
            return fromHierarchicalFragmentUri(uri);
        } else if (JAVA_SCHEMES.contains(scheme)) {
            // Java schemes use "<scheme>:<archiveUri>!/<entryName>" for URIs
            return fromJavaUri(uri);
        } else if (uri.getFragment() != null && uri.getFragment().startsWith("/")) {
            // Support arbitrary URI schemes like "<scheme>:<archiveUri>#/<entryName>"
            return fromHierarchicalFragmentUri(uri);
        }

        // For other URIs, we do not have any place to put non-path information
        checkArgument(isNullOrEmpty(uri.getQuery()) && isNullOrEmpty(uri.getFragment()),
                "cannot handle URIs with fragments or queries: %s", uri);

        // We must have a non-empty path at this point
        checkArgument(!isNullOrEmpty(uri.getPath()),
                "URI must have a path: %s", uri);

        return create(uri.getScheme(), uri.getAuthority(), null, uri.getPath());
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
    public static Ordering<HID> preOrder() {
        return PRE_ORDER;
    }

    // TODO postOrder? breadthFirstOrder?

    private static HID fromHierarchicalFragmentUri(URI uri) {
        // Recursively obtain the container HID from the SSP
        HID container = from(URI.create(uri.getSchemeSpecificPart()));
        return create(uri.getScheme(), null, container, uri.getFragment());
    }

    private static HID fromJavaUri(URI uri) {
        // See JarURLConnection#parseSpecs
        try {
            String spec = uri.toURL().getFile();
            int separator = spec.indexOf("!/");
            if (separator == -1) {
                throw new MalformedURLException("no !/ found in url spec:" + spec);
            }

            HID container = from(new URL(spec.substring(0, separator++)).toURI());
            String entryName = null;
            if (++separator != spec.length()) {
                entryName = spec.substring(separator, spec.length());
                entryName = URLDecoder.decode(entryName, "UTF-8"); // TODO Not quite right...
            }

            return create(uri.getScheme(), null, container, entryName);
        } catch (MalformedURLException | URISyntaxException | UnsupportedEncodingException e) {
            throw new IllegalArgumentException("bad URL", e);
        }
    }

    public static HID from(Path path) {
        // NOTE: Paths have an independent root that is not part of their path segments. Rather then attempt to
        // normalize the root and reconcile that, it is easier to just convert the Path into a URI and convert
        // that into a HID. To URI conversion will normalize the root handling across platforms.
        return from(path.toUri());
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
        return PATH_JOINER.appendTo(new StringBuilder().append('/'), segments[nesting()]).toString();
    }

    /**
     * Returns the individual names for the most specific path in this HID.
     */
    public List<String> getPathNames() {
        return Collections.unmodifiableList(Arrays.asList(segments[nesting()]));
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
     */
    @Nullable
    public HID getParent() {
        int depth = depth();
        if (depth > 0) {
            return new HID(schemes, authorities, segments, nesting(), depth - 1);
        } else {
            return getContainer();
        }
    }

    /**
     * Returns the directory level root identifier. This corresponds to the top-most directory (e.g. "/") that contains
     * this identifier.
     */
    public HID getRoot() {
        return isRoot() ? this : new HID(schemes, authorities, segments, nesting(), 0);
    }

    /**
     * Returns {@code true} if this HID represents a root.
     */
    public boolean isRoot() {
        return depth() == 0;
    }

    /**
     * Returns the container identifier. This corresponds to the "archive" that contains this identifier.
     */
    @Nullable
    public HID getContainer() {
        return hasContainer() ? new HID(schemes, authorities, segments, nesting() - 1, -1) : null;
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
        if (nesting() > 0) {
            return new HID(schemes, authorities, segments, 0, -1);
        } else {
            return this;
        }
    }

    /**
     * Determines if the supplied HID is an ancestor of this HID.
     */
    public boolean isAncestor(HID other) {
        // Compare all but the last segment (which is allowed to be different)
        int otherNesting = other.nesting();
        for (int i = 0; i < otherNesting; ++i) {
            if (!Arrays.equals(segments[i], other.segments[i])) {
                return false;
            }
        }

        // If we are at a lower nesting level, the other one cannot be an ancestor
        if (nesting() < other.nesting() || segments[otherNesting].length < other.segments[otherNesting].length) {
            return false;
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
     * Returns this HID as a URI useful for serialization.
     */
    public URI toUri() {
        URI uri = this.uri;
        if (uri == null) {
            synchronized (this) {
                if (this.uri == null) {
                    for (int i = 0; i < segments.length; ++i) {
                        if (uri != null) {
                            // Build an opaque URI with a fragment
                            uri = URI.create(new StringBuilder()
                                    .append(schemes[i])
                                    .append(':')
                                    .append(UrlEscapers.urlPathSegmentEscaper().escape(uri.toString()))
                                    .append("#/")
                                    .append(UrlEscapers.urlFragmentEscaper().escape(PATH_JOINER.join(segments[i])))
                                    .toString());
                        } else {
                            // The base URI is a little different
                            uri = URI.create(PATH_JOINER.appendTo(
                                    new StringBuilder().append(schemes[i]).append("://").append(authorities[i]).append('/'),
                                    FluentIterable.from(Arrays.asList(segments[i]))
                                            .transform(UrlEscapers.urlPathSegmentEscaper().asFunction()))
                                    .toString());
                        }
                    }
                    this.uri = uri;
                }
            }
        }
        return uri;
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
                    && Arrays.deepEquals(authorities, other.authorities)
                    && Arrays.deepEquals(segments, other.segments);
        }
        return false;
    }

}
