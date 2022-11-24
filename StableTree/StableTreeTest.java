package StableTree;

import org.junit.Test;

import java.io.FileWriter;

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

    private SpanningTree[] initTree(int ntree, double[] probs){
        String host = "127.0.0.1";
        String[] peers = new String[ntree];
        int[] ports = new int[ntree];
        SpanningTree[] tree = new SpanningTree[ntree];
        for(int i = 0 ; i < ntree; i++){
            ports[i] = 1100+i;
            peers[i] = host;
        }
        for(int i = 0; i < ntree; i++){
            tree[i] = new SpanningTree(i, peers, ports, probs);
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

//    @Test
//    public void TestBasic(){
//
//        final int ntree = 8;
//        SpanningTree[] tree = initTree(ntree);
//
//        System.out.println("Test: Starting processes...");
//        for(int i = 0; i < ntree; i++){
//            tree[i].Start();
//        }
//        wait(tree);
//        for (int i = 0; i < ntree; ++i) {
//            tree[i].dead.getAndSet(true);
//        }
//        System.out.println("Processes being killed");
//        for (int i = 0; i < ntree; ++i) {
//            if (tree[i].z == 0) {
//                tree[i].Call("TreeStats", new Request(tree[i].me, tree[i].depth), tree[i].parent - 1);
//            }
//        }
//        for (int i = 0; i < ntree; ++i) {
//            SpanningTree.retStatus r = tree[i].Status();
//            System.out.println("P" + (i + 1) + ": code: " + r.code + ", parent: " + r.parent + ", f: " + r.f + ", z:" +
//                    r.z + ", stabilized: " + r.stabilized + ", Sent messages: " + tree[i].messagesSent +
//                    ", Received messages: " + tree[i].messgesReceived + ", Number of Periods: " + tree[i].numPeriods +
//                    ", depth: " + tree[i].depth + ", Number of Children: " + tree[i].children.size());
//        }
//        cleanup(tree);
//    }

    @Test
    public void TestBasic(){
        try (FileWriter writer = new FileWriter("results.csv")) {
            writer.write("messages_sent,num_periods,depth,avg_children,num_leaves,type,num_nodes\n");
            for (int trial = 0; trial < 32; ++trial) {
                for (int type = 0; type < 3; ++type) {
                    for (int ntree = 4; ntree <= 256; ntree += 4) {
                        double[] probs = new double[ntree];
                        for (int j = ntree - 1; j >= 0; --j) {
                            if (type == 0) {
                                probs[j] = 1.0 / ntree;
                            } else if (type == 1) {
                                if (j >= ntree / 2) {
                                    probs[j] = 1.0 / (ntree / 2.0);
                                }
                            } else if (type == 2) {
                                if (j >= 3 * ntree / 4) {
                                    probs[j] = 1.0 / (ntree / 4.0);
                                }
                            }
                        }
                        SpanningTree[] tree = initTree(ntree, probs);
                        for (int i = 0; i < ntree; i++) {
                            tree[i].Start();
                        }
                        wait(tree);
                        for (int i = 0; i < ntree; ++i) {
                            tree[i].dead.getAndSet(true);
                        }
                        for (int i = 0; i < ntree; ++i) {
                            if (tree[i].z == 0) {
                                tree[i].Call("TreeStats", new Request(tree[i].me, tree[i].depth), tree[i].parent - 1);
                            }
                        }
                        cleanup(tree);
                        double avg_msgs_sent = 0.0;
                        int num_periods = 0;
                        double avg_children = 0.0;
                        int depth = 0;
                        int num_leaves = 0;
                        for (int t = 0; t < ntree; ++t) {
                            avg_msgs_sent += tree[t].messagesSent;
                            num_periods = Integer.max(tree[t].numPeriods, num_periods);
                            avg_children += tree[t].children.size();
                            depth = Integer.max(tree[t].depth, depth);
                            if (tree[t].children.size() == 0) {
                                ++num_leaves;
                            }
                        }
                        writer.write(avg_msgs_sent / ntree + "," + num_periods + "," + depth + "," + avg_children / (ntree - num_leaves) + "," + num_leaves + "," + type + "," + ntree + "\n");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
