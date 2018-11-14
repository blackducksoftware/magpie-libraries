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

import static com.blackducksoftware.common.base.ExtraStrings.ensureSuffix;
import static com.blackducksoftware.common.base.ExtraThrowables.illegalState;
import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_HYPHEN;

import java.io.PrintStream;

/**
 * Code generator for the {@code Link.Builder} relations.
 *
 * @author jgustie
 */
public class LinkBuilderRelationsGenerator {

    private static final String LINK_RELATIONS = "link-relations-1";

    public static void main(String[] args) {
        PrintStream out = System.out;

        Iana.Registry linkRelations = Iana.linkRelations();
        out.format("package com.blackducksoftware.common.value;%n%n");
        out.format("import java.util.Objects;%n");
        out.format("import javax.annotation.Generated;%n%n");
        out.format("@Generated(value = \"%s\", date = \"%s\")%n", LinkBuilderRelationsGenerator.class.getName(), linkRelations.getUpdated().get());
        out.format("public class RegisteredLinkRelations {%n%n");
        out.format("    private final Link.Builder builder;%n%n");
        out.format("    RegisteredLinkRelations(Link.Builder builder) {%n");
        out.format("        this.builder = Objects.requireNonNull(builder);%n");
        out.format("    }%n%n");
        out.format("    private Link.Builder rel(String rel) {%n");
        out.format("        return builder.rel(rel);%n");
        out.format("    }%n%n");

        Iana.Registry registry = linkRelations.getRegistry(LINK_RELATIONS).orElseThrow(illegalState("Expected '%s' registry to exist", LINK_RELATIONS));
        for (Iana.Record record : registry) {
            out.format("    /**%n");
            out.format("     * %s%n", ensureSuffix(record.getDescription(), "."));
            if (record.getNote() != null) {
                out.format("     * <p>%n");
                out.format("     * %s%n", record.getNote());
            }

            record.getSpec().getReference().ifPresent(xref -> {
                String uri = registry.toUriString(xref);
                if (uri != null) {
                    out.format("     * %n");
                    out.format("     * @see <a href=\"%s\">%s</a>%n", uri, registry.toTitle(xref));
                }
            });

            out.format("     */%n");
            out.format("    public Link.Builder %s(String href) {%n", LOWER_HYPHEN.to(LOWER_CAMEL, record.getSubject()));
            out.format("        return rel(\"%s\").uriReference(href);%n", record.getSubject());
            out.format("    }%n%n");
        }
        out.format("}%n");
    }

}
