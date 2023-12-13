package de.plixo.atic.tir.expressions;

import de.plixo.atic.tir.Context;
import de.plixo.atic.tir.path.Package;
import de.plixo.atic.types.Type;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public final class AticPackageExpression extends Expression{
    private final Package thePackage;

    @Override
    public Type getType(Context context) {
        //TODO throw sensible error here, or return void
        throw new NullPointerException("AticPackage has no type");
    }
}