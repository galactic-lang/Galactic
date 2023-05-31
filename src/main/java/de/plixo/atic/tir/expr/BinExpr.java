package de.plixo.atic.tir.expr;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.plixo.atic.typing.types.Type;
import de.plixo.atic.common.Operator;
import lombok.Getter;

public final class BinExpr implements Expr {

    @Getter
    private final Expr left;
    @Getter
    private final Expr right;
    @Getter
    private final Operator operator;

    @Getter
    private final Type type;

    public BinExpr(Expr left, Expr right, Operator operator,Type type) {
        this.left = left;
        this.right = right;
        this.operator = operator;
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public JsonElement toJson() {
        var jsonObject = new JsonObject();
        jsonObject.addProperty("class", this.getClass().getSimpleName());
        jsonObject.addProperty("operator", operator.name());
        jsonObject.add("left", left.toJson());
        jsonObject.add("right", right.toJson());
        return jsonObject;
    }

}