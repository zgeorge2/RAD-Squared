/*
 * Copyright (c) 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.rad2.common.collection;

/**
 * A DTO class whose implementation provides the attributes that form an NAryTreeNode<T>, where T, by default
 * is the implementation itself. An implementing class usually only needs to provide the parentName, so that
 * the node created from this DTO is added as a child of that parent correctly.
 */
public interface INAryTreeNodeData {
    String getTreeNodeName();

    default <T> T getData() {
        return (T) this;
    }

    String getParentTreeNodeName();
}
