/*
 * Copyright 2011 VZ Netzwerke Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.vz.mongodb.jackson.internal;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.type.TypeFactory;
import net.vz.mongodb.jackson.DBRef;
import net.vz.mongodb.jackson.Id;
import net.vz.mongodb.jackson.ObjectId;

import java.lang.annotation.Annotation;

/**
 * Annotation introspector that supports @ObjectId's
 *
 * @author James Roper
 * @since 1.0
 */
public class MongoAnnotationIntrospector extends NopAnnotationIntrospector {
    private final TypeFactory typeFactory;

    public MongoAnnotationIntrospector(TypeFactory typeFactory) {
        this.typeFactory = typeFactory;
    }

    @Override
    public boolean isHandled(Annotation ann) {
        return ann.annotationType() == ObjectId.class
                || ann.annotationType() == Id.class
                || ann.annotationType() == javax.persistence.Id.class;
    }

    // Handling of javax.persistence.Id
    @Override
    public String findSerializationName(AnnotatedMethod am) {
        return findPropertyName(am);
    }

    @Override
    public String findDeserializationName(AnnotatedMethod am) {
        return findPropertyName(am);
    }

    @Override
    public String findDeserializationName(AnnotatedField af) {
        return findPropertyName(af);
    }

    @Override
    public String findSerializationName(AnnotatedField af) {
        return findPropertyName(af);
    }

    @Override
    public String findDeserializationName(AnnotatedParameter param) {
        return findPropertyName(param);
    }

    private String findPropertyName(Annotated annotated) {

        if (annotated.hasAnnotation(Id.class) || annotated.hasAnnotation(javax.persistence.Id.class)) {
            return "_id";
        }
        return null;
    }


    // Handling of ObjectId annotated properties
    @Override
    public Object findSerializer(Annotated am) {
        if (am.hasAnnotation(ObjectId.class)) {
            return ObjectIdSerializer.class;
        }
        return null;
    }

    @Override
    public Object findDeserializer(Annotated am) {
        if (am.hasAnnotation(ObjectId.class)) {
            return findObjectIdDeserializer(typeFactory.constructType(am.getGenericType()));
        }
        return null;
    }

    @Override
    public JsonDeserializer findContentDeserializer(Annotated am) {
        if (am.hasAnnotation(ObjectId.class)) {
            JavaType type = typeFactory.constructType(am.getGenericType());
            if (type.isCollectionLikeType()) {
                return findObjectIdDeserializer(type.containedType(0));
            } else if (type.isMapLikeType()) {
                return findObjectIdDeserializer(type.containedType(1));
            }
        }
        return null;
    }

    public JsonDeserializer findObjectIdDeserializer(JavaType type) {
        if (type.getRawClass() == String.class) {
            return new ObjectIdDeserializers.ToStringDeserializer();
        } else if (type.getRawClass() == byte[].class) {
            return new ObjectIdDeserializers.ToByteArrayDeserializer();
        } else if (type.getRawClass() == DBRef.class) {
            JavaType dbRefType;
            if (type.isContainerType()) {
                if (type.isCollectionLikeType()) {
                    dbRefType = type.containedType(0);
                } else if (type.isMapLikeType()) {
                    dbRefType = type.containedType(1);
                } else {
                    return null;
                }
            } else {
                dbRefType = type;
            }
            JsonDeserializer keyDeserializer = findObjectIdDeserializer(dbRefType.containedType(1));
            return new DBRefDeserializer(dbRefType.containedType(0), dbRefType.containedType(1), keyDeserializer);
        } else if (type.getRawClass() == org.bson.types.ObjectId.class) {
            // Don't know why someone would annotated an ObjectId with @ObjectId, but handle it
            return new ObjectIdDeserializers.ToObjectIdDeserializer();
        }
        return null;
    }

}
