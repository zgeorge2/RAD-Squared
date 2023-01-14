/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.common.collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NAryTreeNode<T> {
    private NAryTreeNode<T> parent;
    private final Map<String, NAryTreeNode<T>> children;
    private final T data; // the data stored in this node
    private final String name; // the name of this node. used for lookups
    private final int level; // the level of the node in the tree. Root level = 0;

    public NAryTreeNode(String name, T data) {
        this(null, name, data); // the root node if the parent isn't specified.
    }

    public NAryTreeNode(NAryTreeNode<T> parent, String name, T data) {
        this.parent = parent;
        this.name = name;
        this.data = data;
        this.children = new HashMap<>();
        this.level = ((this.parent == null) ? 0 : this.parent.getLevel() + 1);
    }

    /**
     * Get the list of nodes that are the ancestors of this node.
     *
     * @return list of nodes that are the ancestors of this node.
     */
    public List<NAryTreeNode<T>> getPathParts() {
        if (this.isRoot()) {
            List<NAryTreeNode<T>> ret = new ArrayList<>();
            ret.add(this);
            return ret;
        }
        List<NAryTreeNode<T>> ret = this.getParent().getPathParts();
        ret.add(this);
        return ret;
    }

    /**
     * Getter.
     *
     * @return the path names leading upto this node starting with the root of the tree.
     */
    public String getPath() {
        if (this.isRoot()) {
            return String.format("/%s", this.getName());
        }
        return String.format("%s/%s", this.getParent().getPath(), this.getName());
    }

    /**
     * Getter
     *
     * @return the root of this tree.
     */
    public NAryTreeNode<T> getRoot() {
        if (this.isRoot()) {
            return this;
        }
        return this.parent.getRoot();
    }

    /**
     * Finder.
     *
     * @return the first occurrence of a node with the given name in tree rooted at the root node of this node.
     */
    public NAryTreeNode<T> findInTree(String name) {
        if (Objects.isNull(name)) {
            return null;
        }
        NAryTreeNode<T> root = this.getRoot();
        return root.find(name);
    }

    /**
     * Finder.
     *
     * @return the first occurrence of a node with the given name in the subtree rooted at this node.
     */
    public NAryTreeNode<T> find(String name) {
        if (!Objects.isNull(name) && this.getName().equals(name)) {
            return this;
        }
        NAryTreeNode<T> ret = null;
        for (NAryTreeNode<T> child : this.children.values()) {
            if ((ret = child.find(name)) != null) break;
        }
        return ret;
    }

    @Override
    public String toString() {
        return String.format("%s[%d]: [Data: %s]", this.getPath(), this.getLevel(), this.getData());
    }

    /**
     * Perform a breadth first traversal of the tree starting at the root, and perform the provided Function at each
     * node visited.
     */
    public <R> Map<String, R> traverseBreadthFirst(Function<NAryTreeNode<T>, R> func) {
        return this.traverseBreadthFirst(func, Integer.MAX_VALUE);
    }

    /**
     * Perform a breadth first traversal of the tree starting at the root, and perform the provided Function at each
     * node visited. Limit traversal starting from level 0 (root node) upto and including the specified level. maxLevel
     * has to be greater than or equal to 0. if maxLevel is greater than the maxLevel of the tree, all nodes are
     * traversed. If maxLevel is negative, all nodes are traversed.
     */
    public <R> Map<String, R> traverseBreadthFirst(Function<NAryTreeNode<T>, R> func, int maxLevel) {
        maxLevel = ((maxLevel < 0) ? Integer.MAX_VALUE : maxLevel);
        Queue<NAryTreeNode<T>> queue = new LinkedList<>();
        NAryTreeNode<T> root = this.getRoot();
        queue.add(root);
        Map<String, R> resultMap = new HashMap<>();
        this.traverseBreadthFirstHelper(queue, resultMap, func, maxLevel);
        return resultMap;
    }

    private <R> void traverseBreadthFirstHelper(Queue<NAryTreeNode<T>> queue, Map<String, R> resultMap,
                                                Function<NAryTreeNode<T>, R> func, int maxLevel) {
        if (queue.isEmpty()) {
            return;
        }
        NAryTreeNode<T> node = queue.remove(); // pop the queue
        resultMap.put(node.getPath(), func.apply(node));
        queue.addAll(node.getChildren().values().stream()
                .filter(val -> (val.getLevel() <= maxLevel))
                .collect(Collectors.toList()));// add the children for traversal upto and incl maxLevel
        traverseBreadthFirstHelper(queue, resultMap, func, maxLevel);
    }

    /**
     * Getter.
     *
     * @return the parent of this node.
     */
    public NAryTreeNode<T> getParent() {
        return this.parent;
    }

    /**
     * Getter.
     *
     * @return the level of this node in the tree. The root node is at level 0.
     */
    public int getLevel() {
        return level;
    }

    /**
     * Getter.
     *
     * @return the name of this node. The name is a component of the path leading up to this node. No two child nodes of
     * a parent node can have the same name.
     */
    public String getName() {
        return name;
    }

    /**
     * getter.
     *
     * @return the data held by this node
     */
    public T getData() {
        return data;
    }

    /**
     * Getter.
     *
     * @return Returns true if this is the root of the tree. Else false.
     */
    public boolean isRoot() {
        return Objects.isNull(this.getParent());
    }

    /**
     * Getter.
     *
     * @return the children of this node
     */
    public Map<String, NAryTreeNode<T>> getChildren() {
        return this.children;
    }

    /**
     * Remove the child node. If no such node can be found, then returns null. else returns the node that was removed.
     *
     * @return the removed child node or null.
     */
    public NAryTreeNode<T> removeChild(String name) {
        NAryTreeNode<T> child = this.getChildren().get(name);
        if (child != null) {
            this.getChildren().remove(name);
            child.parent = null;
        }

        return child;
    }

    /**
     * add a child node created using the arguments to this node.
     *
     * @return the new child node created and added to this node
     */
    public NAryTreeNode<T> addChild(String name, T data) {
        NAryTreeNode<T> childNode = new NAryTreeNode<>(this, name, data);
        this.children.put(name, childNode);
        return childNode;
    }

    /**
     * Add a child at the correct parent node specified by the node argument.
     */
    public NAryTreeNode<T> addChild(INAryTreeNodeData node) {
        NAryTreeNode<T> parentNode = this.findInTree(node.getParentTreeNodeName());
        if (Objects.isNull(parentNode)) {
            return new NAryTreeNode<>(node.getTreeNodeName(), node.getData());
        }
        return parentNode.addChild(node.getTreeNodeName(), node.getData());
    }

    /**
     * Add a collection of child and descendant nodes specified by INaryTreeNodeData DTOs. The children in the
     * collection are descendants at any level in the tree and hence, is a flattened list, with NO particular ordering.
     * If a node in the list references a non-existent named node in the list, it is NOT added to the tree. So also, if
     * circular relationships exist, then the last one wins and this is indeterminate.
     *
     * @return the root of the resulting tree
     */
    public NAryTreeNode<T> addChildren(List<INAryTreeNodeData> children) {
        NAryTreeNode<T> root = this.getRoot();
        root.addChildrenHelper(children);
        return root;
    }

    /**
     * Recursively add children from the list into the tree rooted at this node
     */
    private void addChildrenHelper(List<INAryTreeNodeData> children) {
        if (children.isEmpty()) {
            return; // terminal condition to end recursion
        }
        // first turn the List of children into a Map referenced by child's treeNodeName
        Map<String, INAryTreeNodeData> childMap =
                children.stream().collect(Collectors.toMap(INAryTreeNodeData::getTreeNodeName, Function.identity()));
        // Create a predicate that will partition the children into immediate children of this node
        // and descendents
        Predicate<INAryTreeNodeData> immediateChildren =
                child -> child.getParentTreeNodeName().equals(this.getName());
        Map<Boolean, List<INAryTreeNodeData>> partitioning =
                childMap.values().stream().collect(Collectors.partitioningBy(immediateChildren));
        // now add the immediate children to this node.
        partitioning.get(true).forEach(iNode -> this.addChild(iNode.getTreeNodeName(), iNode.getData()));
        // then try to add the remaining descendants to the children of this node.
        this.getChildren().values().forEach(node -> node.addChildrenHelper(partitioning.get(false)));
    }

    /**
     * Add a child node to this node
     *
     * @return the child node that was added.
     */
    public NAryTreeNode<T> addChild(NAryTreeNode<T> child) {
        this.children.put(child.getName(), child);
        child.parent = this;
        return child;
    }

    /**
     * Switch the parent of this node to a new Node created using the arguments.
     *
     * @return the Parent Node
     */
    public NAryTreeNode<T> switchParent(String name, T data) {
        if (this.getParent() != null) {
            this.getParent().removeChild(this.getName());
        }
        NAryTreeNode<T> newParent = new NAryTreeNode<>(name, data);
        newParent.addChild(this);
        return newParent;
    }
}
