package de.plixo.atic;

import de.plixo.atic.common.JsonUtil;
import de.plixo.atic.exceptions.reasons.FileIOFailure;
import de.plixo.atic.exceptions.reasons.ThreadFailure;
import de.plixo.atic.files.FileTree;
import de.plixo.atic.files.PathEntity;
import de.plixo.atic.hir.HIRItemParsing;
import de.plixo.atic.hir.items.HIRClass;
import de.plixo.atic.hir.items.HIRItem;
import de.plixo.atic.hir.items.HIRStaticMethod;
import de.plixo.atic.tir.Context;
import de.plixo.atic.tir.TreeBuilding;
import de.plixo.atic.tir.aticclass.AticClass;
import de.plixo.atic.tir.parsing.TIRClassParsing;
import de.plixo.atic.tir.parsing.TIRUnitParsing;
import de.plixo.atic.tir.path.CompileRoot;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@AllArgsConstructor

public class Language extends Object {
    public static int TASK_CREATED = 0;

    private final ParseConfig config;


    public void parse(File file) {
        var time = System.currentTimeMillis();
        removeDebugFiles("resources/out/");

        var lex = readAndLex(file);
        var hir2 = buildHIR2(lex);
        debugHIR(hir2);
        var tree2 = buildTree2(hir2);
        TIR(tree2, hir2);

        System.out.println("created " + TASK_CREATED + " tasks");
        var currentTime = System.currentTimeMillis();
        System.out.println("Took " + (currentTime - time) + " ms");

        try {
            File file2 = new File("resources\\");
            URL url = file2.toURI().toURL();
            URL[] urls = new URL[]{url};
            ClassLoader cl = new URLClassLoader(urls);
            var cls = cl.loadClass("test.OutClass");
            cls.getConstructors()[0].newInstance();
            cls.getMethod("method").invoke(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("end");

    }

    private void TIR(Tree2 tree2, HIR2 hir2) {
        var root = tree2.root();
        var units = root.listUnits();
        for (var unit : units) {
            var pathUnit = unit.pathUnit();
            var hirItems = Objects.requireNonNull(hir2.mapping.get(pathUnit));
            unit.hirItems(hirItems);
            TIRClassParsing.parse(unit);
        }
        var classes = new ArrayList<AticClass>();
        for (var unit : units) {
            classes.addAll(unit.classes());
        }

        for (var aClass : classes) {
            var context = new Context(null, aClass.unit(), root);
            TIRClassParsing.fillSuperclasses(aClass, context);
        }


        //types are known here
        for (var aClass : classes) {
            var context = new Context(null, aClass.unit(), root);
            TIRClassParsing.fillFields(aClass, context);
        }
        for (var aClass : classes) {
            var context = new Context(null, aClass.unit(), root);
            TIRClassParsing.fillMethodShells(aClass, context);
        }
        for (var aClass : classes) {
            aClass.addAllFieldsConstructor();
        }
        for (var aClass : classes) {
            var context = new Context(null, aClass.unit(), root);
            TIRClassParsing.fillMethodExpressions(aClass, context);
        }
        for (var aClass : classes) {
            TIRClassParsing.assertMethodsImplemented(aClass);
        }
    }


    private ReadAndLex readAndLex(File file) {
        var fileTree = new FileTree(file, config);
        var projectPath = fileTree.toPath();
        return new ReadAndLex(fileTree, projectPath);
    }

    private HIR2 buildHIR2(ReadAndLex lex) {
        var pathUnits = lex.projectPath().listUnits();
        var mapping = new HashMap<PathEntity.PathUnit, List<HIRItem>>();
        for (var pathUnit : pathUnits) {
            var items = pathUnit.node().list("itemList", "item");
            if (config.threaded()) {
                try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                    var tasks = new ArrayList<Future<HIRItem>>();
                    for (var item : items) {
                        TASK_CREATED += 1;
                        tasks.add(executor.submit(() -> HIRItemParsing.parse(item)));
                    }
                    var list = new ArrayList<HIRItem>();
                    for (var task : tasks) {
                        try {
                            list.add(task.get());
                        } catch (InterruptedException | ExecutionException e) {
                            throw new ThreadFailure(e).create();
                        }
                    }
                    mapping.put(pathUnit, list);
                }
            } else {
                var list = items.stream().map(HIRItemParsing::parse).toList();
                mapping.put(pathUnit, list);
            }
        }
        return new HIR2(lex.projectPath(), mapping);
    }


    private Tree2 buildTree2(HIR2 hir2) {
        var tree = TreeBuilding.toTree(hir2);
        return new Tree2(tree);
    }


    private static void debugHIR(HIR2 hir) {
        hir.mapping.forEach((pathUnit, hirItem) -> {
            hirItem.forEach(ref -> {
                var pathname = "resources/out/items/" +
                        FilenameUtils.removeExtension(pathUnit.file().toString())
                                .replace("\\", ".") + "." + ref.toPrintName() + ".json";
                var debugFile = new File(pathname);
                JsonUtil.saveJsonObj(debugFile, ref.toJson());
            });
        });
    }

    private static void removeDebugFiles(String pathname) {
        var dump = new File(pathname);
        try {
            FileUtils.deleteDirectory(dump);
        } catch (IOException e) {
            throw new FileIOFailure(dump, FileIOFailure.FileType.TEMP_FILES).create();
        }
    }

    private record Tree2(CompileRoot root) {

    }

    private record ReadAndLex(FileTree fileTree, PathEntity projectPath) {
    }

    public record HIR2(PathEntity projectPath, Map<PathEntity.PathUnit, List<HIRItem>> mapping) {

    }
}
