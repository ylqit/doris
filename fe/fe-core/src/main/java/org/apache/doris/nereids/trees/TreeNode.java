// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.doris.nereids.trees;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * interface for all node in Nereids, include plan node and expression.
 *
 * @param <NODE_TYPE> either {@link org.apache.doris.nereids.trees.plans.Plan}
 *                 or {@link org.apache.doris.nereids.trees.expressions.Expression}
 */
public interface TreeNode<NODE_TYPE extends TreeNode<NODE_TYPE>> {

    List<NODE_TYPE> children();

    NODE_TYPE child(int index);

    int arity();

    default NODE_TYPE withChildren(NODE_TYPE... children) {
        return withChildren(ImmutableList.copyOf(children));
    }

    NODE_TYPE withChildren(List<NODE_TYPE> children);

    /**
     * Foreach treeNode. Top-down traverse implicitly.
     * @param func foreach function
     */
    default void foreach(Consumer<TreeNode<NODE_TYPE>> func) {
        func.accept(this);
        for (NODE_TYPE child : children()) {
            child.foreach(func);
        }
    }

    /**
     * iterate top down and test predicate if any matched. Top-down traverse implicitly.
     * @param predicate predicate
     * @return true if any predicate return true
     */
    default boolean anyMatch(Predicate<TreeNode<NODE_TYPE>> predicate) {
        if (predicate.test(this)) {
            return true;
        }
        for (NODE_TYPE child : children()) {
            if (child.anyMatch(predicate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Collect the nodes that satisfied the predicate.
     */
    default <T> T collect(Predicate<TreeNode<NODE_TYPE>> predicate) {
        ImmutableSet.Builder<TreeNode<NODE_TYPE>> result = ImmutableSet.builder();
        foreach(node -> {
            if (predicate.test(node)) {
                result.add(node);
            }
        });
        return (T) result.build();
    }

    /**
     * iterate top down and test predicate if contains any instance of the classes
     * @param types classes array
     * @return true if it has any instance of the types
     */
    default boolean containsType(Class...types) {
        return anyMatch(node -> {
            for (Class type : types) {
                if (type.isInstance(node)) {
                    return true;
                }
            }
            return false;
        });
    }
}
