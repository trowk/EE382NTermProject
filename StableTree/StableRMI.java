package StableTree;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This is the interface of Paxos RMI call. You should implement each method defined below.
 * Prepare is the RMI that proposer sends prepare request to acceptors.
 * Accept is the RMI that proposer sends accept request to acceptors.
 * Decide is the RMI that proposer broadcasts decision once consensus reaches.
 * Please don't change the interface.
 */
public interface StableRMI extends Remote{
    Response Get(Request req) throws RemoteException;
    Response Check(Request req) throws RemoteException;
    Response TreeStats(Request req) throws RemoteException;
}
