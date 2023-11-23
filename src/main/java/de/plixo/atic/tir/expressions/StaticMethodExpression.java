package de.plixo.atic.tir.expressions;

import de.plixo.atic.types.AClass;
import de.plixo.atic.types.AType;
import de.plixo.atic.types.AVoid;
import de.plixo.atic.hir.expressions.HIRExpression;
import de.plixo.atic.tir.MethodCollection;
import de.plixo.atic.tir.parsing.TIRExpressionParsing;
import de.plixo.atic.tir.Context;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public final class StaticMethodExpression extends Expression {
    private final AClass aClass;
    private final MethodCollection method;

    @Override
    public Expression callExpression(List<HIRExpression> arguments, Context context) {
        var expressions =
                arguments.stream().map(ref -> TIRExpressionParsing.parse(ref, context)).toList();
        var typeList = expressions.stream().map(Expression::getType).toList();
        var aMethod = method.find(typeList, context);
        if (aMethod == null) {
            throw new NullPointerException("cant call method " + method.name() + " with arguments " + typeList);
        }
        return new MethodInvokeExpression(null, aMethod, expressions);
    }

    @Override
    public AType getType() {
        return new AVoid();
    }
}