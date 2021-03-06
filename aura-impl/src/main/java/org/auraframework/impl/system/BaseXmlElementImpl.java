/*
 * Copyright (C) 2013 salesforce.com, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.auraframework.impl.system;

import java.util.Collections;
import java.util.Set;

import org.auraframework.builder.ElementBuilder;
import org.auraframework.def.BaseXmlElement;
import org.auraframework.def.DefDescriptor;
import org.auraframework.def.Definition;
import org.auraframework.def.DefinitionAccess;
import org.auraframework.system.Location;
import org.auraframework.throwable.AuraExceptionInfo;
import org.auraframework.throwable.quickfix.InvalidDefinitionException;
import org.auraframework.throwable.quickfix.QuickFixException;
import org.auraframework.util.text.Hash;
import org.auraframework.validation.ReferenceValidationContext;

import com.google.common.collect.Sets;

/**
 * Base implementation for an element.
 */
public abstract class BaseXmlElementImpl implements BaseXmlElement {
    private static final long serialVersionUID = 5836732915093913670L;

    protected final Location location;
    protected final String apiVersion;
    protected final String description;
    protected final String fullyQualifiedName;

    protected transient final QuickFixException parseError;
    private transient Set<DefDescriptor<?>> dependencySet;
    protected final String ownHash;
    protected final DefinitionAccess access;
    private boolean valid;

    protected BaseXmlElementImpl(Location location) {
        this(null, location, null, null, null, null, null);
    }

    protected BaseXmlElementImpl(BaseBuilderImpl builder) {
        this(builder.fullyQualifiedName, builder.getLocation(), builder.apiVersion, builder.description,
                builder.getAccess(), builder.getOwnHash(), builder.getParseError());
    }

    BaseXmlElementImpl(String fullyQualifiedName, Location location,
                       String apiVersion, String description, DefinitionAccess access, String ownHash,
                       QuickFixException parseError) {
        this.fullyQualifiedName = fullyQualifiedName;
        this.location = location;
        this.apiVersion = apiVersion;
        this.description = description;
        if (ownHash == null) {
            ownHash = "";
        }
        this.ownHash = ownHash;
        this.parseError = parseError;
        this.access = access;
    }

    /**
     * @see Definition#getLocation()
     */
    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public DefinitionAccess getAccess() {
        return access;
    }

    /**
     * @see Definition#getName()
     */
    @Override
    public String getName() {
        return fullyQualifiedName;
    }

    @Override
    public String getOwnHash() {
        return ownHash;
    }

    /**
     * @throws QuickFixException
     * @see Definition#appendDependencies(java.util.Set)
     */
    @Override
    public void appendDependencies(Set<DefDescriptor<?>> dependencies) {
    }

    @Override
    public Set<DefDescriptor<?>> getDependencySet() {
        if (this.dependencySet == null) {
            Set<DefDescriptor<?>> newDeps = Sets.newLinkedHashSet();
            this.appendDependencies(newDeps);
            this.dependencySet = Collections.unmodifiableSet(newDeps);
        }
        return this.dependencySet;
    }

    /**
     * @throws QuickFixException
     * @see Definition#validateDefinition()
     */
    @Override
    public void validateDefinition() throws QuickFixException {
        if (parseError != null) {
            throw parseError;
        }
    }

    @Override
    public void markValid() {
        this.valid = true;
    }

    @Override
    public boolean isValid() {
        return this.valid;
    }

    /**
     * @throws QuickFixException
     * @see Definition#validateReferences(ReferenceValidationContext)
     */
    @Override
    public void validateReferences(ReferenceValidationContext validationContext) throws QuickFixException {
    }

    @Override
    public String toString() {
        // getDescriptor is not always non-null (though is should be). Avoid
        // throwing a null pointer
        // exception when someone asks for a string representation.
        if (fullyQualifiedName != null) {
            return fullyQualifiedName;
        } else {
            return "INVALID[" + this.location + "]: " + this.description;
        }
    }


    @Override
    public String getAPIVersion() {
        return apiVersion;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public abstract static class BaseBuilderImpl implements ElementBuilder {
        final Class<?> defClass;
        public Location location;
        public String apiVersion;
        public String description;
        public Hash hash;
        public String ownHash;
        QuickFixException parseError;
        private DefinitionAccess access;
        private String fullyQualifiedName;

        protected BaseBuilderImpl(Class<?> defClass) {
            this.defClass = defClass;
            //this.ownHash = String.valueOf(System.currentTimeMillis());
        }

        public DefinitionAccess getAccess() {
            return access;
        }

        @Override
        public BaseBuilderImpl setAccess(DefinitionAccess access) {
            this.access = access;
            return this;
        }

        @Override
        public BaseBuilderImpl setLocation(String fileName, int line, int column, long lastModified) {
            location = new Location(fileName, line, column, lastModified);
            return this;
        }

        @Override
        public BaseBuilderImpl setLocation(String fileName, long lastModified) {
            location = new Location(fileName, lastModified);
            return this;
        }

        @Override
        public Location getLocation() {
            return this.location;
        }

        @Override
        public BaseBuilderImpl setLocation(Location location) {
            this.location = location;
            return this;
        }

        @Override
        public BaseBuilderImpl setAPIVersion(String apiVersion) {
            this.apiVersion = apiVersion;
            return this;
        }

        @Override
        public BaseBuilderImpl setDescription(String description) {
            this.description = description;
            return this;
        }

        @Override
        public BaseBuilderImpl setOwnHash(Hash hash) {
            if (hash != null) {
                this.ownHash = null;
            }
            this.hash = hash;
            return this;
        }

        public String getOwnHash() {
            //
            // Try to make sure that we have a hash string.
            //
            if (ownHash == null && hash != null && hash.isSet()) {
                ownHash = hash.toString();
            }
            return ownHash;
        }

        @Override
        public BaseBuilderImpl setOwnHash(String ownHash) {
            this.ownHash = ownHash;
            return this;
        }

        @Override
        public ElementBuilder setTagName(String fullyQualifiedName) {
            this.fullyQualifiedName = fullyQualifiedName;
            return this;
        }

        @Override
        public QuickFixException getParseError() {
            return parseError;
        }

        @Override
        public void setParseError(Throwable cause) {
            if (this.parseError != null) {
                return;
            }
            if (cause instanceof QuickFixException) {
                this.parseError = (QuickFixException) cause;
            } else {
                Location location = null;

                if (cause instanceof AuraExceptionInfo) {
                    AuraExceptionInfo aei = (AuraExceptionInfo) cause;
                    location = aei.getLocation();
                }
                this.parseError = new InvalidDefinitionException(cause.getMessage(), location, cause);
            }
        }
    }
}
