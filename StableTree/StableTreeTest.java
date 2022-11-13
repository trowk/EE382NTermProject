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

    private void wait(SpanningTree[] tree){
        int to = 100;
        for(int i = 0; i < 30; i++){
            if(allStabilized(tree)){
                break;
            }
            try {
                Thread.sleep(to);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        boolean stab = allStabilized(tree);
//        assertFalse("Not stabilized", stab);

    }

    private boolean allStabilized(SpanningTree[] tree) {
        int numStabilized = 0;
        for (int i = 0; i < tree.length; ++i) {
            if (tree[i].isDead() || tree[i].stabilized) {
                ++numStabilized;
            }
        }
        return numStabilized == tree.length;
    }

    @Test
    public void TestBasic(){

        final int ntree = 8;
        SpanningTree[] tree = initTree(ntree);

        System.out.println("Test: Starting processes...");
        for(int i = 0; i < ntree; i++){
            tree[i].Start();
        }
        wait(tree);
        for (int i = 0; i < ntree; ++i) {
            tree[i].dead.getAndSet(true);
        }
        System.out.println("Processes being killed");
        for (int i = 0; i < ntree; ++i) {
            if (tree[i].z == 0) {
                tree[i].Call("TreeStats", new Request(tree[i].me, tree[i].depth), tree[i].parent - 1);
            }
        }
        for (int i = 0; i < ntree; ++i) {
            SpanningTree.retStatus r = tree[i].Status();
            System.out.println("P" + (i + 1) + ": code: " + r.code + ", parent: " + r.parent + ", f: " + r.f + ", z:" +
                    r.z + ", stabilized: " + r.stabilized + ", Sent messages: " + tree[i].messagesSent +
                    ", Received messages: " + tree[i].messgesReceived + ", Number of Periods: " + tree[i].numPeriods +
                    ", depth: " + tree[i].depth + ", Number of Children: " + tree[i].children.size());
        }
        cleanup(tree);
    }

    @Test
    public void TestManyKills(){

        final int ntree = 8;
        SpanningTree[] tree = initTree(ntree);

        System.out.println("Test Kills: Starting processes...");
        for(int i = 0; i < ntree; i++){
            tree[i].Start();
        }
        wait(tree);
        for (int i = 0; i < ntree; ++i) {
            tree[i].dead.getAndSet(true);
        }
        System.out.println("Processes being killed");
        for (int i = 0; i < ntree; ++i) {
            if (tree[i].z == 0) {
                tree[i].Call("TreeStats", new Request(tree[i].me, tree[i].depth), tree[i].parent - 1);
            }
        }
        for (int i = 0; i < ntree; ++i) {
            SpanningTree.retStatus r = tree[i].Status();
            System.out.println("P" + (i + 1) + ": code: " + r.code + ", parent: " + r.parent + ", f: " + r.f + ", z:" +
                    r.z + ", stabilized: " + r.stabilized + ", Sent messages: " + tree[i].messagesSent +
                    ", Received messages: " + tree[i].messgesReceived + ", Number of Periods: " + tree[i].numPeriods +
                    ", depth: " + tree[i].depth + ", Number of Children: " + tree[i].children.size());
        }
        cleanup(tree);
    }

}
