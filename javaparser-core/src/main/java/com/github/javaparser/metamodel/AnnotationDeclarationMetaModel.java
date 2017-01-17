package com.github.javaparser.metamodel;

import java.util.Optional;

public class AnnotationDeclarationMetaModel extends ClassMetaModel {

    public AnnotationDeclarationMetaModel(JavaParserMetaModel parent, Optional<ClassMetaModel> superClassMetaModel) {
        super(superClassMetaModel, parent, null, null, null, null, null, false);
    }
}

