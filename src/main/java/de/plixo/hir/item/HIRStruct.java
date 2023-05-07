package de.plixo.hir.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.plixo.hir.parsing.ArgDefinition;

import java.util.List;

public final class HIRStruct extends HIRItem {
    public String name;
    public List<ArgDefinition> fields;
    public List<String> generics;

    public HIRStruct(String name,List<ArgDefinition> fields, List<String> generics, List<HIRAnnotation> annotations) {
        super(annotations);
        this.name = name;
        this.fields = fields;
        this.generics = generics;
    }

    @Override
    public JsonElement toJson() {
        var jsonObject = new JsonObject();
        jsonObject.addProperty("type", "struct");
        jsonObject.addProperty("name", name);
        var array = new JsonArray();
        fields.forEach(ref -> array.add(ref.toJson()));
        jsonObject.add("fields", array);
        var generics = new JsonArray();
        this.generics.forEach(generics::add);
        jsonObject.add("generics", generics);
        return jsonObject;
    }

    @Override
    public String name() {
        return name;
    }
}