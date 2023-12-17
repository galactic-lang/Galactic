package de.plixo.atic.tir.expressions;

import de.plixo.atic.tir.Context;
import de.plixo.atic.types.Class;
import de.plixo.atic.types.Type;
import de.plixo.atic.types.Field;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public final class FieldExpression extends Expression {
    private final Expression object;
    private final Type owner;
    private final Field field;


    @Override
    public Type getType(Context context) {
        return field.type();
    }
}
