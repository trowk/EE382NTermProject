package StableTree;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.TreeMap;
/**
 * This class is the main class you need to implement paxos instances.
 */
public class SpanningTree implements StableRMI, Runnable{

    ReentrantLock mutex;
    String[] peers; // hostname
    int[] ports; // host port
    int me; // index into peers[]

    Registry registry;
    StableRMI stub;

    AtomicBoolean dead;// for testing
    AtomicBoolean unreliable;// for testing

    // Your data here
    // RMI strings
    static final String GET = "Get";
    static final String CHECK = "Check";
    // helper variables
    int n;
    int i;
    // Required for stabilized spanning tree
    int code;
    int parent;
    int f;
    int z;
    Random random;          /* Generates random value for spanning tree algorithm */
    int period;             /* How long a period is in milliseconds */
    boolean stabilized;     /* Whether the tree is changing */
    int periodsUnchanged;   /* How many periods that the tree has remained unchanged */
    // Statistics
    int numPeriods;
    int messagesSent;
    int messgesReceived;
    int depth;
    HashSet<Integer> children;
    double[] probs;

    /**
     * Call the constructor to create a Paxos peer.
     * The hostnames of all the Paxos peers (including this one)
     * are in peers[]. The ports are in ports[].
     */
    public SpanningTree(int me, String[] peers, int[] ports){

        this.me = me;
        this.peers = peers;
        this.ports = ports;
        this.mutex = new ReentrantLock();
        this.dead = new AtomicBoolean(false);
        this.unreliable = new AtomicBoolean(false);

        // Your initialization code here
        n = peers.length;
        i = me + 1;
        code = -1;
        parent = -1;
        f = -1;
        z = -1;
        random = new Random();
        period = 50;

        stabilized = false;
        periodsUnchanged = 0;

        numPeriods = 0;
        messagesSent = 0;
        messgesReceived = 0;
        depth = 0;
        children = new HashSet<>();
        probs = null;

        // register peers, do not modify this part
        try{
            System.setProperty("java.rmi.server.hostname", this.peers[this.me]);
            registry = LocateRegistry.createRegistry(this.ports[this.me]);
            stub = (StableRMI) UnicastRemoteObject.exportObject(this, this.ports[this.me]);
            registry.rebind("Tree", stub);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public SpanningTree(int me, String[] peers, int[] ports, double[] probs){

        this.me = me;
        this.peers = peers;
        this.ports = ports;
        this.mutex = new ReentrantLock();
        this.dead = new AtomicBoolean(false);
        this.unreliable = new AtomicBoolean(false);

        // Your initialization code here
        n = peers.length;
        i = me + 1;
        code = -1;
        parent = -1;
        f = -1;
        z = -1;
        random = new Random();
        period = 50;

        stabilized = false;
        periodsUnchanged = 0;

        numPeriods = 0;
        messagesSent = 0;
        messgesReceived = 0;
        depth = 0;
        children = new HashSet<>();
        this.probs = probs;

        // register peers, do not modify this part
        try{
            System.setProperty("java.rmi.server.hostname", this.peers[this.me]);
            registry = LocateRegistry.createRegistry(this.ports[this.me]);
            stub = (StableRMI) UnicastRemoteObject.exportObject(this, this.ports[this.me]);
            registry.rebind("Tree", stub);
        } catch(Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Call() sends an RMI to the RMI handler on server with
     * arguments rmi name, request message, and server id. It
     * waits for the reply and return a response message if
     * the server responded, and return null if Call() was not
     * be able to contact the server.
     *
     * You should assume that Call() will time out and return
     * null after a while if it doesn't get a reply from the server.
     *
     * Please use Call() to send all RMIs and please don't change
     * this function.
     */
    public Response Call(String rmi, Request req, int id){
        Response callReply = null;

        StableRMI stub;
        try{
            Registry registry=LocateRegistry.getRegistry(this.ports[id]);
            stub=(StableRMI) registry.lookup("Tree");
            if(rmi.equals("Get"))
                callReply = stub.Get(req);
            else if(rmi.equals("Check"))
                callReply = stub.Check(req);
            else if(rmi.equals("TreeStats"))
                callReply = stub.TreeStats(req);
            else
                System.out.println("Wrong parameters!");
        } catch(Exception e){
            return null;
        }
        return callReply;
    }


    /**
     * The application wants Paxos to start agreement on instance seq,
     * with proposed value v. Start() should start a new thread to run
     * Paxos on instance seq. Multiple instances can be run concurrently.
     *
     * Hint: You may start a thread using the runnable interface of
     * Paxos object. One Paxos object may have multiple instances, each
     * instance corresponds to one proposed value/command. Java does not
     * support passing arguments to a thread, so you may reset seq and v
     * in Paxos object before starting a new thread. There is one issue
     * that variable may change before the new thread actually reads it.
     * Test won't fail in this case.
     *
     * Start() just starts a new thread to initialize the agreement.
     * The application will call Status() to find out if/when agreement
     * is reached.
     */
    public void Start(){
        new Thread(this).start();
    }

    void checkR2() {
        if (i == n - 1 && code != n) {
            code = n;
        } else if (i == n && code != 0) {
            code = 0;
        } else if (i != n && (code <= 0 || code > n)) {
            if (probs == null) {
                code = random.nextInt(n) + 1;
            } else {
                double p = Math.random();
                double cum_prob = 0.0;
                for (int j = 0; j < probs.length; ++j) {
                    cum_prob += probs[j];
                    if (p <= cum_prob) {
                        code = j + 1;
                        break;
                    }
                }
            }
        }
    }

    void checkR3() {
        if (i != n && (f <= 0 || f >= n)) {
            f = random.nextInt(n - 1) + 1;
        }
    }

    void checkR4() {
        if (z < 0 || z > n) {
            z = 0;
        }
        if (z != 0) {
            ++messagesSent;
            Response response = Call("Get", new Request(i), z - 1);
            if (response != null && response.code != i) {
                z = 0;
            }
        }
        if (code != 0) {
            ++messagesSent;
            Call("Check", new Request(i), code - 1);
        }
    }

    void checkR5() {
        if (z != 0 && f != z + 1) {
            f = z + 1;
        } else if (z == 0 && f <= z) {
            f = random.nextInt(n - 1) + 1;
        }
    }

    void checkR1() {
        ++messagesSent;
        Response response = Call("Get", new Request(i), f - 1);
        if (response != null && response.code != parent) {
            parent = response.code;
        }
    }

    @Override
    public void run() {
        //Your code here
        while (!isDead()) {
            int prevCode = code, prevParent = parent, prevZ = z, prevF = f;
            checkR2();
            checkR3();
            checkR4();
            checkR5();
            checkR1();
            ++numPeriods;
            if ((prevCode == code && prevParent == parent && prevZ == z && prevF == f) &&
                    (code >= 0 && parent >= 0 && z >= 0 && f >= 0)) {
                ++periodsUnchanged;
            } else {
                periodsUnchanged = 0;
            }
            if (periodsUnchanged >= 2) {
                stabilized = true;
            }
            try {
                Thread.sleep(period);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * the application wants to know whether this
     * peer thinks an instance has been decided,
     * and if so what the agreed value is. Status()
     * should just inspect the local peer state;
     * it should not contact other Paxos peers.
     */
    public retStatus Status(){
        return new retStatus(code, parent, f, z, stabilized);
    }


    @Override
    public Response Get(Request req) throws RemoteException {
        if (isDead()) {
            return null;
        }
        ++messgesReceived;
        return new Response(code);
    }

    @Override
    public Response Check(Request req) throws RemoteException {
        if (isDead()) {
            return null;
        }
        ++messgesReceived;
        if (z < req.senderId) {
            periodsUnchanged = 0;
            z = req.senderId;
        }
        return new Response(code);
    }

    @Override
    public Response TreeStats(Request req) throws RemoteException {
        depth = Integer.max(req.depth + 1, depth);
        children.add(req.senderId);
        Call("TreeStats", new Request(me, depth), parent - 1);
        return null;
    }
    /**
     * helper class for Status() return
     */
    public class retStatus{
        int code;
        int parent;
        int f;
        int z;
        boolean stabilized;

        public retStatus(int _code, int _parent, int _f, int _z, boolean _stabilized) {
            code = _code;
            parent = _parent;
            f = _f;
            z = _z;
            stabilized = _stabilized;
        }
    }

    /**
     * Tell the peer to shut itself down.
     * For testing.
     * Please don't change these four functions.
     */
    public void Kill(){
        this.dead.getAndSet(true);
        if(this.registry != null){
            try {
                UnicastRemoteObject.unexportObject(this.registry, true);
            } catch(Exception e){
                System.out.println("None reference");
            }
        }
    }

    public boolean isDead(){
        return this.dead.get();
    }

    public void setUnreliable(){
        this.unreliable.getAndSet(true);
    }

    public boolean isunreliable(){
        return this.unreliable.get();
    }


}
