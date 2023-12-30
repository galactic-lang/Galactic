package de.plixo.galactic.typed.parsing;

import de.plixo.galactic.boundary.JVMLoader;
import de.plixo.galactic.exception.FlairCheckException;
import de.plixo.galactic.exception.FlairKind;
import de.plixo.galactic.high_level.PrimitiveType;
import de.plixo.galactic.high_level.expressions.*;
import de.plixo.galactic.high_level.items.HIRMethod;
import de.plixo.galactic.high_level.items.HIRParameter;
import de.plixo.galactic.high_level.types.HIRPrimitive;
import de.plixo.galactic.typed.Context;
import de.plixo.galactic.typed.Scope;
import de.plixo.galactic.typed.expressions.*;
import de.plixo.galactic.typed.stellaclass.MethodOwner;
import de.plixo.galactic.typed.stellaclass.Parameter;
import de.plixo.galactic.typed.stellaclass.StellaClass;
import de.plixo.galactic.typed.stellaclass.StellaMethod;
import de.plixo.galactic.types.Class;
import de.plixo.galactic.types.Type;
import de.plixo.galactic.types.VoidType;

import java.util.*;

import static de.plixo.galactic.typed.Scope.INPUT;
import static de.plixo.galactic.typed.Scope.THIS;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;


/**
 * Converts HIRExpression to a Expression, without any checking
 */
public class TIRExpressionParsing {

    public static Expression parse(HIRExpression expression, Context context) {
        return Objects.requireNonNull(switch (expression) {
            case HIRBlock hirBlock -> parseBlock(hirBlock, context);
            case HIRBranch hirBranch -> parseBranchExpression(hirBranch, context);
            case HIRCallNotation hirCallNotation -> parseCallExpression(hirCallNotation, context);
            case HIRConstruct hirConstruct -> parseConstructExpression(hirConstruct, context);
            case HIRDotNotation hirDotNotation -> parseDotNotation(hirDotNotation, context);
            case HIRIdentifier hirIdentifier -> parseIdentifier(hirIdentifier, context);
            case HIRNumber hirNumber -> parseNumber(hirNumber, context);
            case HIRString hirString -> parseStringExpression(hirString, context);
            case HIRVarDefinition hirVarDefinition -> parseVarDefinition(hirVarDefinition, context);
            case HIRAssign hirAssign -> parseConstructExpression(hirAssign, context);
            case HIRFunction hirFunction -> parseFunction(hirFunction, context);
        }, expression.getClass().getName());
    }

    //de-sugar function to class
    private static ConstructExpression parseFunction(HIRFunction function, Context context) {
        var owningUnit = context.unit();
        var classes = owningUnit.classes().size();
        var localName = STR."lambda&\{classes}";
        var superInterface = TIRTypeParsing.parse(function.interfaceType(), context);
        var region = function.region();
        if (!(superInterface instanceof Class superInterfaceClass) ||
                !superInterfaceClass.isInterface()) {
            throw new FlairCheckException(region, FlairKind.TYPE_MISMATCH,
                    "Interface type must be a interface class");
        }
        var defaultSuperClass = JVMLoader.asJVMClass(context.language().defaultSuperClass(), context.loadedBytecode());
        var stellaClass = new StellaClass(region, localName, owningUnit, null, defaultSuperClass);
        stellaClass.interfaces.add(superInterfaceClass);
        var methodsToImplement = stellaClass.implementationLeft();
        if (methodsToImplement.size() != 1) {
            throw new FlairCheckException(region, FlairKind.SIGNATURE,
                    "interface of function has more than one method to implement");
        }
        var methodToImplement = methodsToImplement.iterator().next();
        owningUnit.addClass(stellaClass);
        var classOwner = new MethodOwner.ClassOwner(stellaClass);
        var initMethod =
                new StellaMethod(ACC_PUBLIC, "<init>", List.of(), new VoidType(), null, classOwner);
        stellaClass.addMethod(initMethod, context);

        var functionParams = function.HIRParameters();
        var methodTypes = methodToImplement.arguments();
        if (functionParams.size() != methodTypes.size()) {
            throw new FlairCheckException(region, FlairKind.SIGNATURE,
                    "function has different amount of parameters than the method to implement");
        }

        var parameterIterator = functionParams.iterator();
        var methodTypeIterator = methodTypes.iterator();
        var methodParametersToImplement = new ArrayList<Parameter>();
        while (parameterIterator.hasNext()) {
            var parameter = parameterIterator.next();
            var parameterType = TIRTypeParsing.parse(parameter.type(), context);
            var type = methodTypeIterator.next();
            if (!Type.isAssignableFrom(parameterType, type, context)) {
                throw new FlairCheckException(region, FlairKind.SIGNATURE,
                        "function parameter type doesnt match the method to implement");
            }
            methodParametersToImplement.add(new Parameter(parameter.name(), parameterType));
        }
        var toImplementName = methodToImplement.name();
        var unusedReturnType = new HIRPrimitive(region, PrimitiveType.VOID);
        var unusedParameters = new ArrayList<HIRParameter>();
        var hirMethod = new HIRMethod(toImplementName, unusedParameters, unusedReturnType, function.expression());

        var functionMethod =
                new StellaMethod(ACC_PUBLIC, toImplementName, methodParametersToImplement, methodToImplement.returnType(), hirMethod, classOwner);
        stellaClass.addMethod(functionMethod, context);
        context.pushScope();
        functionMethod.parameters().forEach(ref -> {
            context.scope().addVariable(ref.variable());
        });
        TIRMethodParsing.parse(functionMethod, context);
        functionMethod.thisVariable(new Scope.Variable("this", INPUT | THIS, stellaClass, null));
        context.popScope();
        assert stellaClass.implementationLeft().isEmpty();
        return new ConstructExpression(region, stellaClass, new ArrayList<>());
    }

    private static AssignExpression parseConstructExpression(HIRAssign hirAssign, Context context) {
        return new AssignExpression(hirAssign.region(), parse(hirAssign.left(), context),
                parse(hirAssign.right(), context));
    }

    private static ConstructExpression parseConstructExpression(HIRConstruct hirConstruct,
                                                                Context context) {
        var type = TIRTypeParsing.parse(hirConstruct.hirType(), context);
        var arguments =
                hirConstruct.parameters().stream().map(ref -> parse(ref.value(), context)).toList();
        return new ConstructExpression(hirConstruct.region(), type, arguments);
    }

    private static StringExpression parseStringExpression(HIRString hirString, Context context) {
        return new StringExpression(hirString.region(), hirString.string());
    }

    private static BranchExpression parseBranchExpression(HIRBranch hirBranch, Context context) {
        var condition = parse(hirBranch.condition(), context);
        var code = parse(hirBranch.body(), context);
        Expression elseExpression = null;
        if (hirBranch.elseBody() != null) {
            elseExpression = parse(hirBranch.elseBody(), context);
        }

        return new BranchExpression(hirBranch.region(), condition, code, elseExpression);
    }

    private static VarDefExpression parseVarDefinition(HIRVarDefinition varDefinition,
                                                       Context context) {
        Type type;
        if (varDefinition.type() != null) {
            type = TIRTypeParsing.parse(varDefinition.type(), context);
        } else {
            type = null;
        }
        var expression = parse(varDefinition.value(), context);
        var name = varDefinition.name();
        return new VarDefExpression(varDefinition.region(), name, type, expression, null);
    }

    private static NumberExpression parseNumber(HIRNumber number, Context context) {
        return new NumberExpression(number.region(), number.value(), number.type());
    }

    private static DotNotation parseDotNotation(HIRDotNotation dotNotation, Context context) {
        var obj = parse(dotNotation.object(), context);
        var id = dotNotation.id();
        return new DotNotation(dotNotation.region(), obj, id);
    }

    private static CallNotation parseCallExpression(HIRCallNotation callNotation, Context context) {
        var obj = parse(callNotation.object(), context);
        var arguments = callNotation.arguments().stream().map(ref -> parse(ref, context)).toList();
        return new CallNotation(callNotation.region(), obj, arguments);
    }

    private static SymbolExpression parseIdentifier(HIRIdentifier identifier, Context context) {
        var id = identifier.id();
        return new SymbolExpression(identifier.region(), id);
    }

    private static BlockExpression parseBlock(HIRBlock expression, Context context) {
        var list = expression.expressions().stream()
                .map(expr -> TIRExpressionParsing.parse(expr, context)).toList();
        return new BlockExpression(expression.region(), list);
    }
}