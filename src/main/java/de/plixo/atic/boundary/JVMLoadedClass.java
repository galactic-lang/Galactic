package de.plixo.atic.boundary;

import de.plixo.atic.tir.Context;
import de.plixo.atic.tir.MethodCollection;
import de.plixo.atic.tir.ObjectPath;
import de.plixo.atic.types.Class;
import de.plixo.atic.types.Field;
import de.plixo.atic.types.Method;
import lombok.*;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Accessors(fluent = false)
@Setter
public class JVMLoadedClass extends Class {

    private final ObjectPath path;
    private int access = 0;
    private @Nullable Class superClass = null;
    private List<Class> interfaces = new ArrayList<>();
    private List<Method> methods = new ArrayList<>();
    private List<Field> fields = new ArrayList<>();


    @Override
    public ObjectPath path() {
        return path;
    }

    @Override
    public boolean isInterface() {
        return Modifier.isInterface(access);
    }

    @Override
    public List<Method> getAbstractMethods() {
        return methods.stream().filter(Method::isAbstract).toList();
    }

    @Override
    public List<Method> getMethods() {
        var list = new ArrayList<>(methods);
        if (superClass != null) {
            list.addAll(superClass.getMethods());
        }
        for (var anInterface : interfaces) {
            list.addAll(anInterface.getMethods());
        }
        //TODO check collisions in check stage
        return list;
    }

    @Override
    public List<Field> getFields() {
        var list = new ArrayList<>(fields);
        if (superClass != null) {
            list.addAll(superClass.getFields());
        }
        //unnecessary, interfaces cant have fields, in theorie for now at least
        for (var anInterface : interfaces) {
            list.addAll(anInterface.getFields());
        }
        //TODO check collisions in check stage
        return list;
    }

    @Override
    public @Nullable Field getField(String name, Context context) {
        return getFields().stream().filter(ref -> ref.name().equals(name)).findFirst().orElse(null);
    }

    @Override
    public MethodCollection getMethods(String name, Context context) {
        var list = getMethods().stream().filter(ref -> ref.name().equals(name)).toList();
        return new MethodCollection(name, list);
    }

    @Override
    public @Nullable Class getSuperClass() {
        return superClass;
    }

    @SneakyThrows
    @Override
    public List<Class> getInterfaces() {
        return interfaces;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JVMLoadedClass jvmClass = (JVMLoadedClass) o;
        return Objects.equals(path(), jvmClass.path());
    }

    @Override
    public int hashCode() {
        return Objects.hash(path());
    }

}
