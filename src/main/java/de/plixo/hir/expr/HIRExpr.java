package de.plixo.hir.expr;

import com.google.gson.JsonElement;

public abstract sealed class HIRExpr
        permits HIRAssign, HIRBinOp, HIRBlock, HIRBranch, HIRConstant, HIRDefinition, HIRField,
        HIRFunction, HIRIdentifier, HIRIndexed, HIRInvoked, HIRUnary {

    @Override
    public String toString() {
        return "HIRExpr " + this.getClass().getName();
    }

    public abstract JsonElement toJson();
}
