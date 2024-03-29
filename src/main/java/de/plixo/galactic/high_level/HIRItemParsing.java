package de.plixo.galactic.high_level;

import de.plixo.galactic.high_level.items.*;
import de.plixo.galactic.high_level.utils.DotWordChain;
import de.plixo.galactic.parsing.Node;


/**
 * Parses an item node from a Unit a HIRItem
 */
public class HIRItemParsing {

    public static HIRItem parse(Node node) {
        node.assertType("item");
        if (node.has("import")) {
            var anImportNode = node.get("import");
            return parseImport(anImportNode);
        } else if (node.has("class")) {
            var aClass = node.get("class");
            return parseClass(aClass);
        } else if (node.has("block")) {
            return parseBlock(node.get("block"));
        } else if (node.has("method")) {
            return new HIRStaticMethod(HIRMethodParsing.parse(node.get("method")));
        }
        throw new NullPointerException("Unknown item");
    }


    private static HIRTopBlock parseBlock(Node node) {
        node.assertType("block");
        var blockExpr = node.get("blockExpr");
        var list = blockExpr.list("blockExprList", "expression").stream()
                .map(HIRExpressionParsing::parse).toList();
        return new HIRTopBlock(node.region(), list);
    }

    private static HIRClass parseClass(Node node) {
        return HIRClassParsing.parse(node);
    }

    private static HIRImport parseImport(Node node) {
        node.assertType("import");
        var importTypeNode = node.get("importType");
        var dotWordChain = node.get("dotWordChain");
        var wordList = new DotWordChain(dotWordChain);
        var importName = node.get("importName");
        String name;
        if (importName.has("id")) {
            name = importName.getID();
        } else {
            name = "*";
        }
        String importType = null;
        if (importTypeNode.has("id")) {
            importType = importTypeNode.getID();
        }
        return new HIRImport(dotWordChain.region(), name, importType, wordList.asObjectPath());
    }


}
