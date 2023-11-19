package de.plixo.atic.tir;

import de.plixo.atic.types.AType;
import de.plixo.atic.types.sub.AMethod;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class MethodCollection {
    @Getter
    private final String name;
    @Getter
    private final List<AMethod> methods;

    public MethodCollection(String name, List<AMethod> methods) {
        this.name = name;
        this.methods = methods;
    }


    public MethodCollection(String name, AMethod method) {
        this.name = name;
        this.methods = List.of(method);
    }

    public @Nullable AMethod find(List<AType> signature, Context context) {
        for (AMethod method : methods) {
            if (method.isCallable(signature, context)) {
                return method;
            }
        }
        return null;
    }

    public MethodCollection join(@Nullable MethodCollection method) {
        if (method != null) {
            var objects = new ArrayList<AMethod>();
            objects.addAll(this.methods);
            objects.addAll(method.methods);
            return new MethodCollection(name, objects);
        }
        return this;
    }

    public boolean isEmpty() {
        return methods.isEmpty();
    }

    public MethodCollection filter(Predicate<AMethod> predicate) {
        return new MethodCollection(this.name, this.methods.stream().filter(predicate).toList());
    }
}
