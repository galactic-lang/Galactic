package de.plixo.tir.expr;

import de.plixo.tir.scoping.Scope;
import de.plixo.typesys.types.Type;
import lombok.Getter;

public final class VariableExpr implements Expr{

    @Getter
    private final Scope.Variable variable;

    public VariableExpr(Scope.Variable variable) {
        this.variable = variable;
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public void fillType() {

    }
}