package com.github.javaparser.metamodel;

import java.util.Optional;

public class SimpleNameMetaModel extends ClassMetaModel {

    public SimpleNameMetaModel(JavaParserMetaModel parent, Optional<ClassMetaModel> superClassMetaModel) {
        super(superClassMetaModel, parent, null, null, null, null, null, false);
    }
}

