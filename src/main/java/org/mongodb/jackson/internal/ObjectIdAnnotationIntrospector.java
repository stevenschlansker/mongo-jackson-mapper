package org.mongodb.jackson.internal;

import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.introspect.*;
import org.mongodb.jackson.Id;
import org.mongodb.jackson.ObjectId;

import java.lang.annotation.Annotation;

/**
 * Annotation introspector that supports @ObjectId's
 */
public class ObjectIdAnnotationIntrospector extends NopAnnotationIntrospector {
    @Override
    public boolean isHandled(Annotation ann) {
        return ann.annotationType() == ObjectId.class
                || ann.annotationType() == Id.class
                || ann.annotationType() == javax.persistence.Id.class;
    }

    // Handling of javax.persistence.Id
    @Override
    public String findGettablePropertyName(AnnotatedMethod am) {
        return findPropertyName(am);
    }

    @Override
    public String findSettablePropertyName(AnnotatedMethod am) {
        return findPropertyName(am);
    }

    @Override
    public String findDeserializablePropertyName(AnnotatedField af) {
        return findPropertyName(af);
    }

    @Override
    public String findSerializablePropertyName(AnnotatedField af) {
        return findPropertyName(af);
    }

    @Override
    public String findPropertyNameForParam(AnnotatedParameter param) {
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
    public Object findSerializer(Annotated am, BeanProperty property) {
        if (am.hasAnnotation(ObjectId.class)) {
            return ObjectIdSerializer.class;
        }
        return null;
    }

    @Override
    public Object findDeserializer(Annotated am, BeanProperty property) {
        if (am.hasAnnotation(ObjectId.class)) {
            if (am.getRawType() == String.class) {
                return ObjectIdStringDeserializer.class;
            } else if (am.getRawType() == byte[].class) {
                return ObjectIdByteDeserializer.class;
            }
        }
        return null;
    }

}