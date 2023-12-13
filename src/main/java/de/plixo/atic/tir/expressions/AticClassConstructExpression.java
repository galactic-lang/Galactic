package de.plixo.atic.tir.expressions;

import de.plixo.atic.tir.Context;
import de.plixo.atic.tir.aticclass.AticClass;
import de.plixo.atic.types.Type;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public final class AticClassConstructExpression extends Expression {

    private final AticClass constructType;

    @Getter
    private final List<Expression> arguments;

    @Override
    public Type getType(Context context) {
        //TODO generics?
        return constructType;
    }
}