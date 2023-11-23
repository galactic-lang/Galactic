package de.plixo.atic.tir.expressions;

import de.plixo.atic.types.APrimitive;
import de.plixo.atic.types.AType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class BooleanExpression extends Expression{

    @Getter
    private final boolean value;
    @Override
    public AType getType() {
        return new APrimitive(APrimitive.APrimitiveType.BOOLEAN);
    }
}