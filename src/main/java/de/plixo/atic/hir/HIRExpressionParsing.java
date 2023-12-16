package de.plixo.atic.hir;

import de.plixo.atic.hir.expressions.*;
import de.plixo.atic.hir.types.HIRType;
import de.plixo.atic.hir.utils.ExpressionCommaList;
import de.plixo.atic.parsing.Node;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * Parses an expression node from the CFG into a HIRExpression
 */
public class HIRExpressionParsing {
    public static HIRExpression parse(Node node) {
        node.assertType("expression");
        if (node.has("factor")) {
            return parseFactor(node.get("factor"));
        } else if (node.has("variableDefinition")) {
            return parseVarDefinition(node.get("variableDefinition"));
        }
        throw new NullPointerException("unknown expression");
    }

    private static HIRBranch parseBranch(Node node) {
        node.assertType("branch");
        var expression = HIRExpressionParsing.parse(node.get("expression"));
        var body = node.get("body");
        var bodyExpression = HIRExpressionParsing.parse(body.get("expression"));
        var branchOpt = node.get("branchOpt");
        HIRExpression elsePart = null;
        if (branchOpt.has("expression")) {
            elsePart = HIRExpressionParsing.parse(branchOpt.get("expression"));
        }

        return new HIRBranch(expression, bodyExpression, elsePart);
    }

    private static HIRExpression parseFactor(Node node) {
        node.assertType("factor");
        var memberNodeList = node.list("memberList", "memberAccess");

        var objectNode = node.get("object");
        var currentExpression = parseObject(objectNode);
        for (var memberNode : memberNodeList) {
            currentExpression = parseMember(currentExpression, memberNode);
        }

        var assignOpt = node.get("assignOpt");
        if (assignOpt.has("expression")) {
            var expression = assignOpt.get("expression");
            var value = HIRExpressionParsing.parse(expression);
            return new HIRAssign(currentExpression, value);
        }

        return currentExpression;
    }

    private static HIRExpression parseMember(HIRExpression previous, Node node) {
        node.assertType("memberAccess");
        if (node.has("id")) {
            return new HIRDotNotation(previous, node.getID());
        } else if (node.has("callAccess")) {
            var callAccess = node.get("callAccess");
            List<HIRExpression> list;
            if (callAccess.has("expression")) {
                var expressionNode = callAccess.get("expression");
                var hirExpression = HIRExpressionParsing.parse(expressionNode);
                list = new ArrayList<>();
                list.add(hirExpression);
            } else {
                list = ExpressionCommaList.toList(callAccess);
            }
            return new HIRCallNotation(previous, list);
        }
        throw new NullPointerException("Unknown member");
    }

    private static HIRExpression parseObject(Node node) {
        node.assertType("object");
        if (node.has("blockExpr")) {
            var list = node.get("blockExpr").list("blockExprList", "expression").stream()
                    .map(HIRExpressionParsing::parse).toList();
            return new HIRBlock(list);
        } else if (node.has("number")) {
            var nodeNumber = node.getNumber();
            return new HIRNumber(nodeNumber);
        } else if (node.has("string")) {
            var nodeNumber = node.getString();
            var literal = nodeNumber.substring(1, nodeNumber.length() - 1);
            return new HIRString(literal);
        } else if (node.has("id")) {
            var id = node.getID();
            return new HIRIdentifier(id);
        } else if (node.has("expression")) {
            return HIRExpressionParsing.parse(node.get("expression"));
        } else if (node.has("initialisation")) {
            return parseConstruct(node.get("initialisation"));
        } else if (node.has("branch")) {
            return parseBranch(node.get("branch"));
        }
        throw new NullPointerException("Unknown Object " + node);
    }

    private static HIRConstruct parseConstruct(Node node) {
        node.assertType("initialisation");
        var type = HIRTypeParsing.parse(node.get("type"));
        var initialisationList = node.get("initialisationList");
        var list = initialisationList.list("initialisationFieldList", "initialisationFieldListOpt",
                "initialisationField").stream().map(ref -> {
            var hirExpression = HIRExpressionParsing.parse(ref.get("expression"));
            return new HIRConstruct.ConstructParam("expr", hirExpression);
        }).toList();
        return new HIRConstruct(type, list);
    }

    private static HIRVarDefinition parseVarDefinition(Node node) {
        node.assertType("variableDefinition");
        var name = node.getID();
        var typeHint = node.get("typeHint");
        HIRType type;
        if (typeHint.has("type")) {
            type = HIRTypeParsing.parse(typeHint.get("type"));
        } else {
            type = null;
        }

        var values = HIRExpressionParsing.parse(node.get("expression"));
        return new HIRVarDefinition(name, type, values);
    }
}
