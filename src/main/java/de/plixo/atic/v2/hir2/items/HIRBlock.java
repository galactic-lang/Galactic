package de.plixo.atic.v2.hir2.items;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.plixo.atic.lexer.Region;
import de.plixo.atic.v2.hir2.expressions.HIRExpression;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;


@AllArgsConstructor
public final class HIRBlock implements HIRItem {

    private final Region region;
    @Getter
    private final List<HIRExpression> expressions;
    @Override
    public JsonElement toJson() {
        var jsonObject = new JsonObject();
        jsonObject.addProperty("type", this.getClass().getSimpleName());
        jsonObject.add("position", region.toJson());
        jsonObject.addProperty("name", name());
        var array = new JsonArray();
        for (var expression : expressions) {
            array.add(expression.toJson());
        }
        jsonObject.add("expressions", array);
        return jsonObject;
    }

    @Override
    public String name() {
        return "code-block";
    }
}
