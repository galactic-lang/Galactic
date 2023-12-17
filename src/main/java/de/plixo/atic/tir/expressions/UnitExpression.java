package de.plixo.atic.tir.expressions;

import de.plixo.atic.tir.Context;
import de.plixo.atic.tir.path.Unit;
import de.plixo.atic.types.Type;
import de.plixo.atic.types.VoidType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public final class UnitExpression extends Expression{
    private final Unit unit;

    @Override
    public Type getType(Context context) {
        return new VoidType();
    }
}
