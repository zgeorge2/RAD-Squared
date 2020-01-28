package com.rad2.sb.app;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility to scan for components matching a given class in a given package
 */
public class SBComponentScanner<T> {
    private Class classToScan; // the class of the parametric type T
    private String packagePrefix; // the packages to scan for.

    /**
     * @param classToScan   e.g. BaseModelRegistry.class
     * @param packagePrefix e.g. com.foo.bar
     */
    public SBComponentScanner(Class classToScan, String packagePrefix) {
        this.classToScan = classToScan;
        this.packagePrefix = packagePrefix.replace('.', '/');
    }

    public List<T> createInstances() {
        ClassPathScanningCandidateComponentProvider provider =
            new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(this.classToScan));
        // scan in packages
        return provider
            .findCandidateComponents(this.packagePrefix)
            .stream()
            .filter(this::hasANoArgConstructor) // filter out instances without def cons
            .map(this::construct) // construct using def cons
            .collect(Collectors.toList());
    }

    public boolean isAnnotationClassPresent() {
        return getClassesWithAnnotation().size() > 0;
    }

    private List<AnnotatedBeanDefinition> getClassesWithAnnotation() {
        ClassPathScanningCandidateComponentProvider provider =
            new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(this.classToScan));
        // scan in packages
        return provider
            .findCandidateComponents(this.packagePrefix)
            .stream()
            .map(this::castTo)
            .collect(Collectors.toList());
    }

    private AnnotatedBeanDefinition castTo(BeanDefinition beanDef) {
        return (AnnotatedBeanDefinition) beanDef;
    }

    private boolean hasANoArgConstructor(BeanDefinition beanDef) {
        boolean ret = false;
        try {
            Class beanClass = Class.forName(beanDef.getBeanClassName());
            ret = Stream.of(beanClass.getDeclaredConstructors())
                .anyMatch(cons -> cons.getParameterCount() == 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private T construct(BeanDefinition beanDef) {
        T ret = null;
        try {
            Class beanClass = Class.forName(beanDef.getBeanClassName());
            ret = (T) beanClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return ret;
    }
}
