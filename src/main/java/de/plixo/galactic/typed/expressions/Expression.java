package de.plixo.galactic.typed.expressions;

import de.plixo.galactic.lexer.Region;
import de.plixo.galactic.typed.Context;
import de.plixo.galactic.types.Type;

/**
 * Base class for all expressions,
 * Not all Expressions are used in all stages
 */
public sealed interface Expression
        permits ArrayLengthExpression, AssignExpression, BlockExpression, BooleanExpression, BranchExpression,
        CallNotation, CastCheckExpression, CastExpression, ConstructExpression, DotNotation,
        ExtensionBitExpression, FieldExpression, GetMethodExpression, InstanceCreationExpression,
        LocalVariableAssign, MethodCallExpression, NumberExpression, PutFieldExpression,
        PutStaticFieldExpression, StaticClassExpression, StaticFieldExpression,
        StaticMethodExpression, StellaClassConstructExpression, StellaPackageExpression,
        StringExpression, SuperCallExpression, SymbolExpression, ThisExpression, UnitExpression,
        VarDefExpression, VarExpression, WhileExpression {


    Type getType(Context context);

    Region region();

}
