package com.github.javaparser.metamodel;

import java.util.Optional;

public class ImportDeclarationMetaModel extends ClassMetaModel {

    public ImportDeclarationMetaModel(JavaParserMetaModel parent, Optional<ClassMetaModel> superClassMetaModel) {
        super(superClassMetaModel, parent, null, null, null, null, null, false);
    }
}

