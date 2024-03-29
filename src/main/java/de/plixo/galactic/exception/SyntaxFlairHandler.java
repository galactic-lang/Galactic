package de.plixo.galactic.exception;

import de.plixo.galactic.lexer.TokenRecord;
import de.plixo.galactic.macros.Macro;
import de.plixo.galactic.parsing.Grammar;
import de.plixo.galactic.parsing.Parser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects different Errors and throws them at the end.
 * Used in the Parser Stage
 */
@Getter
public class SyntaxFlairHandler {
    private final List<SyntaxFlair> records = new ArrayList<>();

    public void add(SyntaxFlair record) {
        records.add(record);
    }

    public void handle() {
        if (records.isEmpty()) {
            return;
        }

        var strings = records.stream().map(ref -> switch (ref) {
            case FailedRule failedRule -> {
                if (failedRule.records.isEmpty()) {
                    yield STR."Failed to parse \{failedRule.failedRule.name()}";
                } else {
                    var failedRecord = failedRule.records.getFirst();
                    yield STR."Failed \{failedRule.failedRule.name()}: \{failedRecord.errorMessage()}";
                }
            }
            case FailedLiteral failedLiteral ->
                    STR."Failed \{failedLiteral.parentRule.name()}: Expected literal '\{failedLiteral.expectedLiteral}' but got '\{failedLiteral.consumedLiteral.literal()}' at \{failedLiteral.consumedLiteral.position()
                            .toString()}";
            case FailedMacro failedMacro -> STR."Failed Macro \{failedMacro.macro.name()}: \{failedMacro.message}";
        }).toList();
        var msg = String.join("\n", strings);
        throw new FlairException(STR."\n\{msg}");
    }


    public static abstract sealed class SyntaxFlair {

    }

    @AllArgsConstructor
    public static final class FailedRule extends SyntaxFlair {
        List<TokenRecord> records;
        Grammar.Rule failedRule;
        @Nullable Grammar.Rule parentRule;
    }

    @AllArgsConstructor
    public static final class FailedLiteral extends SyntaxFlair {
        List<TokenRecord> records;
        Grammar.Rule parentRule;
        String expectedLiteral;
        TokenRecord consumedLiteral;
    }

    @AllArgsConstructor
    @Getter
    public static final class FailedMacro extends SyntaxFlair {
        List<TokenRecord> records;
        Macro macro;
        String message;
    }

}
