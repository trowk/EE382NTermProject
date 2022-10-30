package StableTree;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

/**
 * This is a subset of entire test cases
 * For your reference only.
 */
public class StableTreeTest {
    private void cleanup(SpanningTree[] tree){
        for(int i = 0; i < tree.length; i++){
            if(tree[i] != null){
                tree[i].Kill();
            }
        }
    }

    private SpanningTree[] initTree(int ntree){
        String host = "127.0.0.1";
        String[] peers = new String[ntree];
        int[] ports = new int[ntree];
        SpanningTree[] tree = new SpanningTree[ntree];
        for(int i = 0 ; i < ntree; i++){
            ports[i] = 1100+i;
            peers[i] = host;
        }
        for(int i = 0; i < ntree; i++){
            tree[i] = new SpanningTree(i, peers, ports);
        }
        return tree;
    }

    @Test
    public void TestBasic(){

        final int ntree = 8;
        SpanningTree[] tree = initTree(ntree);

        System.out.println("Test: Starting processes...");
        for(int i = 0; i < ntree; i++){
            tree[i].Start();
        }
        try {
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Processes being killed");
        cleanup(tree);
        System.out.println(tree[0].logSize());
        for (int i = 0; i < ntree; ++i) {
            System.out.println("P " + (i + 1));
            for (int j = 0; j < tree[i].logSize(); ++j) {
                SpanningTree.retStatus r = tree[i].Status(j);
                System.out.println(j + ": code: " + r.code + ", parent: " + r.parent + ", f: " + r.f + ", z:" + r.z);
            }
            System.out.println();
        }
    }

}
