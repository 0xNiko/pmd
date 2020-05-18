/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.rule.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.pmd.internal.util.IteratorUtil;
import net.sourceforge.pmd.lang.ast.Node;

public class TreeIndex {

    private final LatticeRelation<Class<?>, Node> byClass;
    private final Set<String> interestingNames;
    private final Map<String, List<Node>> byName;


    public TreeIndex(Set<String> namesToIndex,
                     Set<Class<? extends Node>> classesToIndex) {

        byClass = new LatticeRelation<>(
            TopoOrder.TYPE_HIERARCHY_ORDERING,
            classesToIndex,
            Class::getSimpleName
        );
        this.interestingNames = namesToIndex;
        byName = new HashMap<>();
    }

    void indexNode(Node n) {
        if (interestingNames.contains(n.getXPathNodeName())) {
            byName.computeIfAbsent(n.getXPathNodeName(), k -> new ArrayList<>()).add(n);
        }
        byClass.put(n.getClass(), n);
    }

    void complete() {
        byClass.makeReadable();
    }

    void prepare() {
        byClass.makeWritableAndClear();
        byName.clear();
    }

    Iterator<Node> getByName(String n) {
        return byName.getOrDefault(n, Collections.emptyList()).iterator();
    }

    Iterator<Node> getByClass(Class<? extends Node> n) {
        return byClass.get(n).iterator();
    }


    public Iterator<Node> getByName(Collection<String> n) {
        return IteratorUtil.flatMap(n.iterator(), this::getByName);
    }

    public Iterator<Node> getByClass(Collection<? extends Class<? extends Node>> n) {
        return IteratorUtil.flatMap(n.iterator(), this::getByClass);
    }
}
