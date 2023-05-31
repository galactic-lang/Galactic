package de.plixo.atic.hir.expr;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.plixo.atic.common.Operator;
import de.plixo.atic.lexer.Region;
import lombok.Getter;

public final class HIRUnary extends HIRExpr {

    @Getter
    private final HIRExpr left;
    @Getter
    private final Operator operator;

    public HIRUnary(Region region,HIRExpr left, Operator operator) {
        super(region);
        this.left = left;
        this.operator = operator;
    }

    @Override
    public JsonElement toJson() {
        var jsonObject = new JsonObject();
        jsonObject.addProperty("type", "unary");
        jsonObject.add("position", region().toJson());
        jsonObject.add("prevExpr", left.toJson());
        return jsonObject;
    }
}
