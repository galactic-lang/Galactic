package de.plixo.galactic.typed;

import de.plixo.galactic.types.Type;
import lombok.*;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A Scope for variables
 */
@RequiredArgsConstructor
public class Scope {

    public float a = 0;
    public float b = 0;




    @Getter
    private @Nullable
    final Scope parent;
    private final List<Variable> variables = new ArrayList<>();

    public @Nullable Variable getVariable(String name) {
        for (var variable : variables) {
            if (variable.name.equals(name)) {
                return variable;
            }
        }
        if (parent != null) {
            return parent.getVariable(name);
        }
        return null;
    }


    public void addVariable(Variable variable) {
        variables.add(variable);
    }


    /**
     * A Variable, the Type will be resolved in the Infer Stage
     */
    @AllArgsConstructor
    @ToString
    public static class Variable {
        @Getter
        private final String name;
        @Getter
        private final int flags;

        @Getter
        @Setter
        @Accessors(fluent = false)
        private @Nullable Type type;

        private final @Nullable Variable outsideClosure;
    }

    /**
     * Flags for Variables types
     */

    public static int INPUT = 1;
    public static int FINAL = 1 << 1;
    public static int CLOSURE_CAPTURE = 1 << 2;
    public static int THIS = 1 << 3;

}
