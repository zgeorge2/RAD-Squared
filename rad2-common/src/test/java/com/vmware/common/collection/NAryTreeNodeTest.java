package com.vmware.common.collection;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class NAryTreeNodeTest extends TestCase {
    private NAryTreeNode<String> getTestNAryTree() {
        NAryTreeNode<String> root = new NAryTreeNode<>("root", "root");
        for (int ii = 0; ii < 5; ii++) {
            String cName = "child" + ii;
            NAryTreeNode<String> child = root.addChild(cName, cName);
            for (int jj = 0; jj < 5; jj++) {
                String gcName = "grandChild" + ii + "_" + jj;
                NAryTreeNode<String> gc = child.addChild(gcName, gcName);
                for (int kk = 0; kk < 5; kk++) {
                    String ggcName = "greatGrandChild" + ii + "_" + jj + "_" + kk;
                    gc.addChild(ggcName, ggcName);
                }
            }
        }
        return root;
    }

    public void testFindVariousChildren() {
        NAryTreeNode<String> root = this.getTestNAryTree();
        String rootName = "root";
        root = root.find(rootName); // find the root through searching, use that.
        this.testFindingAChild(root, "root");
        this.testFindingAChild(root, "child1");
        this.testFindingAChild(root, "child0");
        this.testFindingAChild(root, "grandChild1_1");
        this.testFindingAChild(root, "greatGrandChild1_1_1");
        this.testFindingAChild(root, "greatGrandChild4_4_4");
        this.testNotFindingAChild(root, "unknown");
    }

    private void testFindingAChild(NAryTreeNode<String> root, String childName) {
        NAryTreeNode<String> child = root.find(childName);
        assertTrue(child.getName().equals(childName));
    }

    private void testNotFindingAChild(NAryTreeNode<String> root, String unknown) {
        NAryTreeNode<String> nonChild = root.find(unknown);
        assertNull(nonChild);
    }

    public void testPathParts() {
        String expectedParts[] = {"root", "child1", "grandChild1_1", "greatGrandChild1_1_1"};
        NAryTreeNode<String> root = this.getTestNAryTree();
        NAryTreeNode<String> greatGrandChild1_1_1 = root.find("greatGrandChild1_1_1");
        int count = 0;
        for (NAryTreeNode<String> part : greatGrandChild1_1_1.getPathParts()) {
            assertTrue(part.getName().equals(expectedParts[count++]));
        }
    }

    public void testSwitchingParent() {
        NAryTreeNode<String> root = this.getTestNAryTree();
        NAryTreeNode<String> child1 = root.find("child1");
        NAryTreeNode<String> anotherRoot = child1.switchParent("AnotherRoot", "AnotherRoot");
        assertTrue(anotherRoot.getName().equals("AnotherRoot"));
        NAryTreeNode<String> greatGrandChild1_1_1 = root.find("greatGrandChild1_1_1");
        assertNull(greatGrandChild1_1_1); // parent is switched. Not in root anymore.
        greatGrandChild1_1_1 = anotherRoot.find("greatGrandChild1_1_1");
        assertTrue(greatGrandChild1_1_1.getName().equals("greatGrandChild1_1_1"));
    }

    public void testFindingRootFromChildNode() {
        NAryTreeNode<String> root = this.getTestNAryTree();
        NAryTreeNode<String> child1 = root.find("child1");
        NAryTreeNode<String> greatGrandChild1_1_1 = root.find("greatGrandChild1_1_1");
        NAryTreeNode<String> rootBySearchFromChild = greatGrandChild1_1_1.getRoot();
        assertTrue(rootBySearchFromChild.getName().equals(root.getName()));
        // Switch parent from root to newRoot
        NAryTreeNode<String> newRoot = child1.switchParent("newRoot", "newRoot");
        // greatGrandChild1_1_1 is now part of newRoot's tree.
        greatGrandChild1_1_1 = newRoot.find("greatGrandChild1_1_1");
        NAryTreeNode<String> newRootBySearchFromChild = greatGrandChild1_1_1.getRoot();
        assertTrue(newRootBySearchFromChild.getName().equals(newRoot.getName()));
    }

    public void testFindingSomewhereInTree() {
        NAryTreeNode<String> root = this.getTestNAryTree();
        NAryTreeNode<String> greatGrandChild1_1_1 = root.find("greatGrandChild1_1_1");
        NAryTreeNode<String> searchChild1 = greatGrandChild1_1_1.findInTree("child1");
        assertTrue(searchChild1 != null && "child1".equals(searchChild1.getName()));
    }

    public void testAddingToRightPlaceInTree() {
        NAryTreeNode<String> root = this.getTestNAryTree();
        NAryTreeNode<String> greatGrandChild1_1_1 = root.find("greatGrandChild1_1_1");
        NAryTreeNode<String> grandChild1_5 = greatGrandChild1_1_1.addChild(new INAryTreeNodeData() {
            @Override
            public String getTreeNodeName() {
                return "grandChild1_5";
            }

            @Override
            public String getData() {
                return "grandChild1_5";
            }

            @Override
            public String getParentTreeNodeName() {
                return "child1";
            }
        });
        assertTrue(grandChild1_5.getName().equals("grandChild1_5")
            && grandChild1_5.getParent().getName().equals("child1"));
    }

    public void testAddingChildren() {
        String rootName = "root";
        NAryTreeNode<String> root = new NAryTreeNode<>(rootName, rootName);
        List<INAryTreeNodeData> children = new ArrayList<>();
        for (int ii = 0; ii < 5; ii++) {
            String cName = "child" + ii;
            children.add(createINAryNode(rootName, cName));
            for (int jj = 0; jj < 5; jj++) {
                String gcName = "grandChild" + ii + "_" + jj;
                children.add(createINAryNode(cName, gcName));
                for (int kk = 0; kk < 5; kk++) {
                    String ggcName = "greatGrandChild" + ii + "_" + jj + "_" + kk;
                    children.add(createINAryNode(gcName, ggcName));
                }
            }
        }
        Collections.shuffle(children);
        root.addChildren(children);
        this.testFindingAChild(root, "root");
        this.testFindingAChild(root, "child1");
        this.testFindingAChild(root, "child0");
        this.testFindingAChild(root, "grandChild1_1");
        this.testFindingAChild(root, "greatGrandChild1_1_1");
        this.testFindingAChild(root, "greatGrandChild4_4_4");
        this.testNotFindingAChild(root, "unknown");
    }

    private INAryTreeNodeData createINAryNode(String parentName, String name) {
        return new INAryTreeNodeData() {
            @Override
            public String getTreeNodeName() {
                return name;
            }

            @Override
            public String getParentTreeNodeName() {
                return parentName;
            }

            @Override
            public String getData() {
                return name;
            }
        };
    }

    public void testBFTraversal() {
        NAryTreeNode<String> root = this.getTestNAryTree();
        // test with maxLevel set to INT MAX. All nodes must return
        Map<String, String> results = root.traverseBreadthFirst(k -> k.getPath(), Integer.MAX_VALUE);
        results.entrySet().stream().forEach(e -> assertEquals(e.getKey(), e.getValue()));
        assertTrue(results.size() == (1 + 5 + 5 * 5 + 5 * 5 * 5));
        // test with maxLevel set to 0, Only root node must return
        results = root.traverseBreadthFirst(k -> k.getPath(), 0);
        assertTrue(results.size() == 1);
        // test with maxLevel set to 1, Only root node plus 5 children must return
        results = root.traverseBreadthFirst(k -> k.getPath(), 1);
        assertTrue(results.size() == 1 + 5);
        // test with maxLevel set to 3 (the actual maxLevel), All nodes must return
        results = root.traverseBreadthFirst(k -> k.getPath(), 3);
        assertTrue(results.size() == (1 + 5 + 5 * 5 + 5 * 5 * 5));
        // test without setting maxLevel, All nodes must return
        results = root.traverseBreadthFirst(k -> k.getPath());
        assertTrue(results.size() == (1 + 5 + 5 * 5 + 5 * 5 * 5));
    }
}