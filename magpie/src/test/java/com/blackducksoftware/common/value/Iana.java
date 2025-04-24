/*
 * Copyright 2018 Synopsys, Inc.
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

import static com.blackducksoftware.common.base.ExtraThrowables.illegalState;
import static com.google.common.base.Strings.nullToEmpty;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

import com.blackducksoftware.common.base.ExtraOptionals;
import com.google.common.base.CharMatcher;
import com.google.common.base.MoreObjects;

/**
 * Data model for IANA registries.
 *
 * @author jgustie
 */
class Iana {

    /**
     * Namespace to use for parsing specifications.
     */
    private static final String NS = "http://www.iana.org/assignments";

    /**
     * Generic reference; use {@link Registry#toUriString(XRef)} to convert to a URI.
     */
    @XmlRootElement(namespace = NS, name = "xref")
    public static class XRef {

        @XmlAttribute
        private String type;

        @XmlAttribute
        private String data;

        public String getType() {
            return type;
        }

        public String getData() {
            return data;
        }
    }

    /**
     * A specification reference.
     */
    @XmlRootElement(namespace = NS)
    public static class Specification {

        @XmlElementRefs({
                @XmlElementRef(namespace = NS, name = "xref", type = XRef.class)
        })
        @XmlMixed
        private List<Object> content;

        public Optional<XRef> getReference() {
            if (content != null) {
                for (Object obj : content) {
                    if (obj instanceof XRef) {
                        return Optional.of((XRef) obj);
                    }
                }
            }
            return Optional.empty();
        }
    }

    /**
     * A reference to a file.
     */
    @XmlRootElement(namespace = NS)
    public static class File {

        @XmlAttribute
        private String type;

        @XmlValue
        private String value;

        public String getType() {
            return type;
        }

        public String getValue() {
            return value;
        }
    }

    @XmlRootElement(namespace = NS)
    public static class Person {

        @XmlAttribute
        private String id;

        @XmlElement(namespace = NS)
        private String name;

        @XmlElement(namespace = NS)
        private String uri;

        @XmlElement(namespace = NS)
        private String updated;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getUri() {
            return uri;
        }

        public Optional<LocalDate> getUpdated() {
            return Optional.ofNullable(updated).map(LocalDate::parse);
        }
    }

    @XmlRootElement(namespace = NS)
    public static class Record {

        @XmlAttribute
        private String date;

        @XmlElement(namespace = NS)
        private String name;

        @XmlElement(namespace = NS)
        private String value;

        @XmlElement(namespace = NS)
        private String description;

        @XmlElement(namespace = NS)
        private String note;

        @XmlElement(namespace = NS)
        public Specification spec;

        public Optional<LocalDate> getDate() {
            return Optional.ofNullable(date).map(LocalDate::parse);
        }

        public String getSubject() {
            return MoreObjects.firstNonNull(name, value);
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }

        public String getNote() {
            return note;
        }

        public Specification getSpec() {
            return spec;
        }
    }

    @XmlRootElement(namespace = NS)
    public static class Registry implements Iterable<Record> {

        @XmlTransient
        private Optional<Registry> parent;

        @XmlAttribute
        private String id;

        @XmlElement(namespace = NS)
        private String title;

        @XmlElement(namespace = NS)
        private String created;

        @XmlElement(namespace = NS)
        public String updated;

        @XmlElement(namespace = NS, name = "registry")
        private List<Registry> registries = new ArrayList<>();

        @XmlElement(namespace = NS, name = "record")
        private List<Record> records = new ArrayList<>();

        @XmlElement(namespace = NS, name = "people")
        private List<Person> people = new ArrayList<>();

        public Optional<Registry> getParent() {
            return parent;
        }

        @Nullable
        public String getId() {
            return id;
        }

        public Optional<String> getTitle() {
            return Optional.ofNullable(title);
        }

        public Optional<LocalDate> getCreated() {
            return Optional.ofNullable(created).map(LocalDate::parse);
        }

        public Optional<LocalDate> getUpdated() {
            return Optional.ofNullable(updated).map(LocalDate::parse);
        }

        public Optional<Registry> getRegistry(String id) {
            return registries.stream().filter(r -> Objects.equals(r.getId(), id)).findFirst();
        }

        public Optional<Person> getPerson(String id) {
            return ExtraOptionals.or(people.stream().filter(p -> Objects.equals(p.getId(), id)).findFirst(),
                    () -> parent.flatMap(p -> p.getPerson(id)));
        }

        @Override
        public Iterator<Record> iterator() {
            return records.iterator();
        }

        @Override
        public Spliterator<Record> spliterator() {
            return records.spliterator();
        }

        public Stream<Record> stream() {
            return records.stream();
        }

        @Nullable
        public String toUriString(XRef xref) {
            switch (nullToEmpty(xref.getType()).toLowerCase()) {
            case "uri":
                return xref.getData();
            case "rfc":
                // TODO OR "https://tools.ietf.org/rfc/" + xref.data + ".txt";
                return "https://tools.ietf.org/html/" + xref.getData();
            case "person":
                return getPerson(xref.getData()).map(Person::getUri)
                        .orElseThrow(illegalState("missing person: %s", xref.getData()));
            case "draft":
                // Nothing we can do
                return null;
            default:
                throw new IllegalStateException("unknown reference type: " + xref.getType());
            }
        }

        public String toTitle(XRef xref) {
            switch (nullToEmpty(xref.getType()).toLowerCase()) {
            case "uri":
                return xref.getData();
            case "rfc":
                return xref.getData().toUpperCase();
            case "person":
                return getPerson(xref.getData()).map(Person::getName)
                        .orElseThrow(illegalState("missing person: %s", xref.getData()));
            case "draft":
                return xref.getData();
            default:
                throw new IllegalStateException("unknown reference type: " + xref.getType());
            }
        }
    }

    private static final Map<String, Registry> registries = new ConcurrentHashMap<>();

    public static Registry linkRelations() {
        return registries.computeIfAbsent("link-relations", Iana::readRegistry);
    }

    private static Registry readRegistry(String id) {
        try {
            JAXBContext context = JAXBContext.newInstance(Registry.class, Record.class, File.class, XRef.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            Registry registry = (Registry) unmarshaller.unmarshal(Iana.class.getResourceAsStream(id + ".xml"));
            cleanRegistry(registry, Optional.empty());
            return registry;
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    private static void cleanRegistry(Registry registry, Optional<Registry> parent) {
        registry.parent = parent;
        registry.registries.forEach(r -> cleanRegistry(r, Optional.of(registry)));
        for (Record record : registry.records) {
            if (record.description != null) {
                record.description = CharMatcher.whitespace().trimAndCollapseFrom(record.description, ' ');
            }
            if (record.note != null) {
                record.note = CharMatcher.whitespace().trimAndCollapseFrom(record.note, ' ');
            }
        }
    }

}
