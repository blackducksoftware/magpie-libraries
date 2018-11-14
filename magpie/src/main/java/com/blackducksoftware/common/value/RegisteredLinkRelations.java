package com.blackducksoftware.common.value;

import java.util.Objects;
import javax.annotation.Generated;

@Generated(value = "com.blackducksoftware.common.value.LinkBuilderRelationsGenerator", date = "2017-10-26")
public class RegisteredLinkRelations {

    private final Link.Builder builder;

    RegisteredLinkRelations(Link.Builder builder) {
        this.builder = Objects.requireNonNull(builder);
    }

    private Link.Builder rel(String rel) {
        return builder.rel(rel);
    }

    /**
     * Refers to a resource that is the subject of the link's context.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc6903">RFC6903</a>
     */
    public Link.Builder about(String href) {
        return rel("about").uriReference(href);
    }

    /**
     * Refers to a substitute for this context.
     * 
     * @see <a href="http://www.w3.org/TR/html5/links.html#link-type-alternate">http://www.w3.org/TR/html5/links.html#link-type-alternate</a>
     */
    public Link.Builder alternate(String href) {
        return rel("alternate").uriReference(href);
    }

    /**
     * Refers to an appendix.
     * 
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224">http://www.w3.org/TR/1999/REC-html401-19991224</a>
     */
    public Link.Builder appendix(String href) {
        return rel("appendix").uriReference(href);
    }

    /**
     * Refers to a collection of records, documents, or other materials of historical interest.
     * 
     * @see <a href="http://www.w3.org/TR/2011/WD-html5-20110113/links.html#rel-archives">http://www.w3.org/TR/2011/WD-html5-20110113/links.html#rel-archives</a>
     */
    public Link.Builder archives(String href) {
        return rel("archives").uriReference(href);
    }

    /**
     * Refers to the context's author.
     * 
     * @see <a href="http://www.w3.org/TR/html5/links.html#link-type-author">http://www.w3.org/TR/html5/links.html#link-type-author</a>
     */
    public Link.Builder author(String href) {
        return rel("author").uriReference(href);
    }

    /**
     * Identifies the entity that blocks access to a resource following receipt of a legal demand.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc7725">RFC7725</a>
     */
    public Link.Builder blockedBy(String href) {
        return rel("blocked-by").uriReference(href);
    }

    /**
     * Gives a permanent link to use for bookmarking purposes.
     * 
     * @see <a href="http://www.w3.org/TR/html5/links.html#link-type-bookmark">http://www.w3.org/TR/html5/links.html#link-type-bookmark</a>
     */
    public Link.Builder bookmark(String href) {
        return rel("bookmark").uriReference(href);
    }

    /**
     * Designates the preferred version of a resource (the IRI and its contents).
     * 
     * @see <a href="https://tools.ietf.org/html/rfc6596">RFC6596</a>
     */
    public Link.Builder canonical(String href) {
        return rel("canonical").uriReference(href);
    }

    /**
     * Refers to a chapter in a collection of resources.
     * 
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224">http://www.w3.org/TR/1999/REC-html401-19991224</a>
     */
    public Link.Builder chapter(String href) {
        return rel("chapter").uriReference(href);
    }

    /**
     * Indicates that the link target is preferred over the link context for the purpose of referencing.
     */
    public Link.Builder citeAs(String href) {
        return rel("cite-as").uriReference(href);
    }

    /**
     * The target IRI points to a resource which represents the collection resource for the context IRI.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc6573">RFC6573</a>
     */
    public Link.Builder collection(String href) {
        return rel("collection").uriReference(href);
    }

    /**
     * Refers to a table of contents.
     * 
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224">http://www.w3.org/TR/1999/REC-html401-19991224</a>
     */
    public Link.Builder contents(String href) {
        return rel("contents").uriReference(href);
    }

    /**
     * The document linked to was later converted to the document that contains this link relation. For example, an RFC can have a link to the Internet-Draft that became the RFC; in that case, the link relation would be "convertedFrom".
     * <p>
     * This relation is different than "predecessor-version" in that "predecessor-version" is for items in a version control system. It is also different than "previous" in that this relation is used for converted resources, not those that are part of a sequence of resources.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc7991">RFC7991</a>
     */
    public Link.Builder convertedfrom(String href) {
        return rel("convertedFrom").uriReference(href);
    }

    /**
     * Refers to a copyright statement that applies to the link's context.
     * 
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224">http://www.w3.org/TR/1999/REC-html401-19991224</a>
     */
    public Link.Builder copyright(String href) {
        return rel("copyright").uriReference(href);
    }

    /**
     * The target IRI points to a resource where a submission form can be obtained.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc6861">RFC6861</a>
     */
    public Link.Builder createForm(String href) {
        return rel("create-form").uriReference(href);
    }

    /**
     * Refers to a resource containing the most recent item(s) in a collection of resources.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc5005">RFC5005</a>
     */
    public Link.Builder current(String href) {
        return rel("current").uriReference(href);
    }

    /**
     * Refers to a resource providing information about the link's context.
     * 
     * @see <a href="http://www.w3.org/TR/powder-dr/#assoc-linking">http://www.w3.org/TR/powder-dr/#assoc-linking</a>
     */
    public Link.Builder describedby(String href) {
        return rel("describedby").uriReference(href);
    }

    /**
     * The relationship A 'describes' B asserts that resource A provides a description of resource B. There are no constraints on the format or representation of either A or B, neither are there any further constraints on either resource.
     * <p>
     * This link relation type is the inverse of the 'describedby' relation type. While 'describedby' establishes a relation from the described resource back to the resource that describes it, 'describes' established a relation from the describing resource to the resource it describes. If B is 'describedby' A, then A 'describes' B.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc6892">RFC6892</a>
     */
    public Link.Builder describes(String href) {
        return rel("describes").uriReference(href);
    }

    /**
     * Refers to a list of patent disclosures made with respect to material for which 'disclosure' relation is specified.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc6579">RFC6579</a>
     */
    public Link.Builder disclosure(String href) {
        return rel("disclosure").uriReference(href);
    }

    /**
     * Used to indicate an origin that will be used to fetch required resources for the link context, and that the user agent ought to resolve as early as possible.
     * 
     * @see <a href="https://www.w3.org/TR/resource-hints/">https://www.w3.org/TR/resource-hints/</a>
     */
    public Link.Builder dnsPrefetch(String href) {
        return rel("dns-prefetch").uriReference(href);
    }

    /**
     * Refers to a resource whose available representations are byte-for-byte identical with the corresponding representations of the context IRI.
     * <p>
     * This relation is for static resources. That is, an HTTP GET request on any duplicate will return the same representation. It does not make sense for dynamic or POSTable resources and should not be used for them.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc6249">RFC6249</a>
     */
    public Link.Builder duplicate(String href) {
        return rel("duplicate").uriReference(href);
    }

    /**
     * Refers to a resource that can be used to edit the link's context.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc5023">RFC5023</a>
     */
    public Link.Builder edit(String href) {
        return rel("edit").uriReference(href);
    }

    /**
     * The target IRI points to a resource where a submission form for editing associated resource can be obtained.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc6861">RFC6861</a>
     */
    public Link.Builder editForm(String href) {
        return rel("edit-form").uriReference(href);
    }

    /**
     * Refers to a resource that can be used to edit media associated with the link's context.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc5023">RFC5023</a>
     */
    public Link.Builder editMedia(String href) {
        return rel("edit-media").uriReference(href);
    }

    /**
     * Identifies a related resource that is potentially large and might require special handling.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc4287">RFC4287</a>
     */
    public Link.Builder enclosure(String href) {
        return rel("enclosure").uriReference(href);
    }

    /**
     * An IRI that refers to the furthest preceding resource in a series of resources.
     * <p>
     * This relation type registration did not indicate a reference. Originally requested by Mark Nottingham in December 2004.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc8288">RFC8288</a>
     */
    public Link.Builder first(String href) {
        return rel("first").uriReference(href);
    }

    /**
     * Refers to a glossary of terms.
     * 
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224">http://www.w3.org/TR/1999/REC-html401-19991224</a>
     */
    public Link.Builder glossary(String href) {
        return rel("glossary").uriReference(href);
    }

    /**
     * Refers to context-sensitive help.
     * 
     * @see <a href="http://www.w3.org/TR/html5/links.html#link-type-help">http://www.w3.org/TR/html5/links.html#link-type-help</a>
     */
    public Link.Builder help(String href) {
        return rel("help").uriReference(href);
    }

    /**
     * Refers to a resource hosted by the server indicated by the link context.
     * <p>
     * This relation is used in CoRE where links are retrieved as a "/.well-known/core" resource representation, and is the default relation type in the CoRE Link Format.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc6690">RFC6690</a>
     */
    public Link.Builder hosts(String href) {
        return rel("hosts").uriReference(href);
    }

    /**
     * Refers to a hub that enables registration for notification of updates to the context.
     * <p>
     * This relation type was requested by Brett Slatkin.
     * 
     * @see <a href="http://pubsubhubbub.googlecode.com">http://pubsubhubbub.googlecode.com</a>
     */
    public Link.Builder hub(String href) {
        return rel("hub").uriReference(href);
    }

    /**
     * Refers to an icon representing the link's context.
     * 
     * @see <a href="http://www.w3.org/TR/html5/links.html#link-type-icon">http://www.w3.org/TR/html5/links.html#link-type-icon</a>
     */
    public Link.Builder icon(String href) {
        return rel("icon").uriReference(href);
    }

    /**
     * Refers to an index.
     * 
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224">http://www.w3.org/TR/1999/REC-html401-19991224</a>
     */
    public Link.Builder index(String href) {
        return rel("index").uriReference(href);
    }

    /**
     * The target IRI points to a resource that is a member of the collection represented by the context IRI.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc6573">RFC6573</a>
     */
    public Link.Builder item(String href) {
        return rel("item").uriReference(href);
    }

    /**
     * An IRI that refers to the furthest following resource in a series of resources.
     * <p>
     * This relation type registration did not indicate a reference. Originally requested by Mark Nottingham in December 2004.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc8288">RFC8288</a>
     */
    public Link.Builder last(String href) {
        return rel("last").uriReference(href);
    }

    /**
     * Points to a resource containing the latest (e.g., current) version of the context.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc5829">RFC5829</a>
     */
    public Link.Builder latestVersion(String href) {
        return rel("latest-version").uriReference(href);
    }

    /**
     * Refers to a license associated with this context.
     * <p>
     * For implications of use in HTML, see: http://www.w3.org/TR/html5/links.html#link-type-license
     * 
     * @see <a href="https://tools.ietf.org/html/rfc4946">RFC4946</a>
     */
    public Link.Builder license(String href) {
        return rel("license").uriReference(href);
    }

    /**
     * for information about processing this relation type in host-meta documents. When used elsewhere, it refers to additional links and other metadata. Multiple instances indicate additional LRDD resources. LRDD resources MUST have an "application/xrd+xml" representation, and MAY have others.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc6415">RFC6415</a>
     */
    public Link.Builder lrdd(String href) {
        return rel("lrdd").uriReference(href);
    }

    /**
     * The Target IRI points to a Memento, a fixed resource that will not change state anymore.
     * <p>
     * A Memento for an Original Resource is a resource that encapsulates a prior state of the Original Resource.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc7089">RFC7089</a>
     */
    public Link.Builder memento(String href) {
        return rel("memento").uriReference(href);
    }

    /**
     * Refers to a resource that can be used to monitor changes in an HTTP resource.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc5989">RFC5989</a>
     */
    public Link.Builder monitor(String href) {
        return rel("monitor").uriReference(href);
    }

    /**
     * Refers to a resource that can be used to monitor changes in a specified group of HTTP resources.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc5989">RFC5989</a>
     */
    public Link.Builder monitorGroup(String href) {
        return rel("monitor-group").uriReference(href);
    }

    /**
     * Indicates that the link's context is a part of a series, and that the next in the series is the link target.
     * 
     * @see <a href="http://www.w3.org/TR/html5/links.html#link-type-next">http://www.w3.org/TR/html5/links.html#link-type-next</a>
     */
    public Link.Builder next(String href) {
        return rel("next").uriReference(href);
    }

    /**
     * Refers to the immediately following archive resource.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc5005">RFC5005</a>
     */
    public Link.Builder nextArchive(String href) {
        return rel("next-archive").uriReference(href);
    }

    /**
     * Indicates that the context’s original author or publisher does not endorse the link target.
     * 
     * @see <a href="http://www.w3.org/TR/html5/links.html#link-type-nofollow">http://www.w3.org/TR/html5/links.html#link-type-nofollow</a>
     */
    public Link.Builder nofollow(String href) {
        return rel("nofollow").uriReference(href);
    }

    /**
     * Indicates that no referrer information is to be leaked when following the link.
     * 
     * @see <a href="http://www.w3.org/TR/html5/links.html#link-type-noreferrer">http://www.w3.org/TR/html5/links.html#link-type-noreferrer</a>
     */
    public Link.Builder noreferrer(String href) {
        return rel("noreferrer").uriReference(href);
    }

    /**
     * The Target IRI points to an Original Resource.
     * <p>
     * An Original Resource is a resource that exists or used to exist, and for which access to one of its prior states may be required.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc7089">RFC7089</a>
     */
    public Link.Builder original(String href) {
        return rel("original").uriReference(href);
    }

    /**
     * Indicates a resource where payment is accepted.
     * <p>
     * This relation type registration did not indicate a reference. Requested by Joshua Kinberg and Robert Sayre. It is meant as a general way to facilitate acts of payment, and thus this specification makes no assumptions on the type of payment or transaction protocol. Examples may include a web page where donations are accepted or where goods and services are available for purchase. rel="payment" is not intended to initiate an automated transaction. In Atom documents, a link element with a rel="payment" attribute may exist at the feed/channel level and/or the entry/item level. For example, a rel="payment" link at the feed/channel level may point to a "tip jar" URI, whereas an entry/ item containing a book review may include a rel="payment" link that points to the location where the book may be purchased through an online retailer.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc8288">RFC8288</a>
     */
    public Link.Builder payment(String href) {
        return rel("payment").uriReference(href);
    }

    /**
     * Gives the address of the pingback resource for the link context.
     * 
     * @see <a href="http://www.hixie.ch/specs/pingback/pingback">http://www.hixie.ch/specs/pingback/pingback</a>
     */
    public Link.Builder pingback(String href) {
        return rel("pingback").uriReference(href);
    }

    /**
     * Used to indicate an origin that will be used to fetch required resources for the link context. Initiating an early connection, which includes the DNS lookup, TCP handshake, and optional TLS negotiation, allows the user agent to mask the high latency costs of establishing a connection.
     * 
     * @see <a href="https://www.w3.org/TR/resource-hints/">https://www.w3.org/TR/resource-hints/</a>
     */
    public Link.Builder preconnect(String href) {
        return rel("preconnect").uriReference(href);
    }

    /**
     * Points to a resource containing the predecessor version in the version history.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc5829">RFC5829</a>
     */
    public Link.Builder predecessorVersion(String href) {
        return rel("predecessor-version").uriReference(href);
    }

    /**
     * The prefetch link relation type is used to identify a resource that might be required by the next navigation from the link context, and that the user agent ought to fetch, such that the user agent can deliver a faster response once the resource is requested in the future.
     * 
     * @see <a href="http://www.w3.org/TR/resource-hints/">http://www.w3.org/TR/resource-hints/</a>
     */
    public Link.Builder prefetch(String href) {
        return rel("prefetch").uriReference(href);
    }

    /**
     * Refers to a resource that should be loaded early in the processing of the link's context, without blocking rendering.
     * <p>
     * Additional target attributes establish the detailed fetch properties of the link.
     * 
     * @see <a href="http://www.w3.org/TR/preload/">http://www.w3.org/TR/preload/</a>
     */
    public Link.Builder preload(String href) {
        return rel("preload").uriReference(href);
    }

    /**
     * Used to identify a resource that might be required by the next navigation from the link context, and that the user agent ought to fetch and execute, such that the user agent can deliver a faster response once the resource is requested in the future.
     * 
     * @see <a href="https://www.w3.org/TR/resource-hints/">https://www.w3.org/TR/resource-hints/</a>
     */
    public Link.Builder prerender(String href) {
        return rel("prerender").uriReference(href);
    }

    /**
     * Indicates that the link's context is a part of a series, and that the previous in the series is the link target.
     * 
     * @see <a href="http://www.w3.org/TR/html5/links.html#link-type-prev">http://www.w3.org/TR/html5/links.html#link-type-prev</a>
     */
    public Link.Builder prev(String href) {
        return rel("prev").uriReference(href);
    }

    /**
     * Refers to a resource that provides a preview of the link's context.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc6903">RFC6903</a>
     */
    public Link.Builder preview(String href) {
        return rel("preview").uriReference(href);
    }

    /**
     * Refers to the previous resource in an ordered series of resources. Synonym for "prev".
     * 
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224">http://www.w3.org/TR/1999/REC-html401-19991224</a>
     */
    public Link.Builder previous(String href) {
        return rel("previous").uriReference(href);
    }

    /**
     * Refers to the immediately preceding archive resource.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc5005">RFC5005</a>
     */
    public Link.Builder prevArchive(String href) {
        return rel("prev-archive").uriReference(href);
    }

    /**
     * Refers to a privacy policy associated with the link's context.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc6903">RFC6903</a>
     */
    public Link.Builder privacyPolicy(String href) {
        return rel("privacy-policy").uriReference(href);
    }

    /**
     * Identifying that a resource representation conforms to a certain profile, without affecting the non-profile semantics of the resource representation.
     * <p>
     * Profile URIs are primarily intended to be used as identifiers, and thus clients SHOULD NOT indiscriminately access profile URIs.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc6906">RFC6906</a>
     */
    public Link.Builder profile(String href) {
        return rel("profile").uriReference(href);
    }

    /**
     * Identifies a related resource.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc4287">RFC4287</a>
     */
    public Link.Builder related(String href) {
        return rel("related").uriReference(href);
    }

    /**
     * Identifies the root of RESTCONF API as configured on this HTTP server. The "restconf" relation defines the root of the API defined in RFC8040. Subsequent revisions of RESTCONF will use alternate relation values to support protocol versioning.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc8040">RFC8040</a>
     */
    public Link.Builder restconf(String href) {
        return rel("restconf").uriReference(href);
    }

    /**
     * Identifies a resource that is a reply to the context of the link.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc4685">RFC4685</a>
     */
    public Link.Builder replies(String href) {
        return rel("replies").uriReference(href);
    }

    /**
     * Refers to a resource that can be used to search through the link's context and related resources.
     * 
     * @see <a href="http://www.opensearch.org/Specifications/OpenSearch/1.1">http://www.opensearch.org/Specifications/OpenSearch/1.1</a>
     */
    public Link.Builder search(String href) {
        return rel("search").uriReference(href);
    }

    /**
     * Refers to a section in a collection of resources.
     * 
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224">http://www.w3.org/TR/1999/REC-html401-19991224</a>
     */
    public Link.Builder section(String href) {
        return rel("section").uriReference(href);
    }

    /**
     * Conveys an identifier for the link's context.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc4287">RFC4287</a>
     */
    public Link.Builder self(String href) {
        return rel("self").uriReference(href);
    }

    /**
     * Indicates a URI that can be used to retrieve a service document.
     * <p>
     * When used in an Atom document, this relation type specifies Atom Publishing Protocol service documents by default. Requested by James Snell.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc5023">RFC5023</a>
     */
    public Link.Builder service(String href) {
        return rel("service").uriReference(href);
    }

    /**
     * Refers to the first resource in a collection of resources.
     * 
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224">http://www.w3.org/TR/1999/REC-html401-19991224</a>
     */
    public Link.Builder start(String href) {
        return rel("start").uriReference(href);
    }

    /**
     * Refers to a stylesheet.
     * 
     * @see <a href="http://www.w3.org/TR/html5/links.html#link-type-stylesheet">http://www.w3.org/TR/html5/links.html#link-type-stylesheet</a>
     */
    public Link.Builder stylesheet(String href) {
        return rel("stylesheet").uriReference(href);
    }

    /**
     * Refers to a resource serving as a subsection in a collection of resources.
     * 
     * @see <a href="http://www.w3.org/TR/1999/REC-html401-19991224">http://www.w3.org/TR/1999/REC-html401-19991224</a>
     */
    public Link.Builder subsection(String href) {
        return rel("subsection").uriReference(href);
    }

    /**
     * Points to a resource containing the successor version in the version history.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc5829">RFC5829</a>
     */
    public Link.Builder successorVersion(String href) {
        return rel("successor-version").uriReference(href);
    }

    /**
     * Gives a tag (identified by the given address) that applies to the current document.
     * 
     * @see <a href="http://www.w3.org/TR/html5/links.html#link-type-tag">http://www.w3.org/TR/html5/links.html#link-type-tag</a>
     */
    public Link.Builder tag(String href) {
        return rel("tag").uriReference(href);
    }

    /**
     * Refers to the terms of service associated with the link's context.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc6903">RFC6903</a>
     */
    public Link.Builder termsOfService(String href) {
        return rel("terms-of-service").uriReference(href);
    }

    /**
     * The Target IRI points to a TimeGate for an Original Resource.
     * <p>
     * A TimeGate for an Original Resource is a resource that is capable of datetime negotiation to support access to prior states of the Original Resource.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc7089">RFC7089</a>
     */
    public Link.Builder timegate(String href) {
        return rel("timegate").uriReference(href);
    }

    /**
     * The Target IRI points to a TimeMap for an Original Resource.
     * <p>
     * A TimeMap for an Original Resource is a resource from which a list of URIs of Mementos of the Original Resource is available.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc7089">RFC7089</a>
     */
    public Link.Builder timemap(String href) {
        return rel("timemap").uriReference(href);
    }

    /**
     * Refers to a resource identifying the abstract semantic type of which the link's context is considered to be an instance.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc6903">RFC6903</a>
     */
    public Link.Builder type(String href) {
        return rel("type").uriReference(href);
    }

    /**
     * Refers to a parent document in a hierarchy of documents.
     * <p>
     * This relation type registration did not indicate a reference. Requested by Noah Slater.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc8288">RFC8288</a>
     */
    public Link.Builder up(String href) {
        return rel("up").uriReference(href);
    }

    /**
     * Points to a resource containing the version history for the context.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc5829">RFC5829</a>
     */
    public Link.Builder versionHistory(String href) {
        return rel("version-history").uriReference(href);
    }

    /**
     * Identifies a resource that is the source of the information in the link's context.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc4287">RFC4287</a>
     */
    public Link.Builder via(String href) {
        return rel("via").uriReference(href);
    }

    /**
     * Identifies a target URI that supports the Webmention protcol. This allows clients that mention a resource in some form of publishing process to contact that endpoint and inform it that this resource has been mentioned.
     * <p>
     * This is a similar "Linkback" mechanism to the ones of Refback, Trackback, and Pingback. It uses a different protocol, though, and thus should be discoverable through its own link relation type.
     * 
     * @see <a href="http://www.w3.org/TR/webmention/">http://www.w3.org/TR/webmention/</a>
     */
    public Link.Builder webmention(String href) {
        return rel("webmention").uriReference(href);
    }

    /**
     * Points to a working copy for this resource.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc5829">RFC5829</a>
     */
    public Link.Builder workingCopy(String href) {
        return rel("working-copy").uriReference(href);
    }

    /**
     * Points to the versioned resource from which this working copy was obtained.
     * 
     * @see <a href="https://tools.ietf.org/html/rfc5829">RFC5829</a>
     */
    public Link.Builder workingCopyOf(String href) {
        return rel("working-copy-of").uriReference(href);
    }

}
