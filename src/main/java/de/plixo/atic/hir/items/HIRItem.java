package de.plixo.atic.hir.items;

/**
 * Top Level Items of a Unit
 */
public sealed interface HIRItem permits HIRTopBlock, HIRClass, HIRImport, HIRStaticMethod {

}
