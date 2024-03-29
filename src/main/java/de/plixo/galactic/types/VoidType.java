package de.plixo.galactic.types;

import java.util.Objects;

/**
 * void type (only used for return types)
 */
public class VoidType extends Type {


    @Override
    public String toString() {
        return "void";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return obj.getClass() == VoidType.class;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode("V");
    }

    @Override
    public char getJVMKind() {
        return 'V';
    }

    @Override
    public String getDescriptor() {
        return String.valueOf(getJVMKind());
    }
}
