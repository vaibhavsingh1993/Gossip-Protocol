package main.java.gossip;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import main.java.gossip.Config;
import main.java.gossip.Gossip;

public class Main {
public static void main(String[] args) {
    Config config = new Config(
            Duration.ofSeconds(2), // time between receiving the last heartbeat and marking a member as failing
            Duration.ofSeconds(2), // time between marking a member as failed and removing it from the list
            Duration.ofMillis(500), // how often the member list is broadcast to other members
            Duration.ofMillis(200), // how often the Gossip protocol checks if any members have failed
            3                       // the number of nodes to send the membership list to when broadcasting.
    );

    // Set how the error messages will be handled.
    Node.setLogger((message) -> {
        System.out.println("Gossip Error: " + message);
    });

    Node firstNode = new Node(new InetSocketAddress("127.0.0.1", 8080), config, "0", 0);
  
/*    firstNode.setOnNewMemberHandler( (address) -> {
        System.out.println(address + " connected to node 0 (first node)");
        System.out.println();
    });*/

    //firstNode.start();
    InetSocketAddress[] targetAddress = {, , , ,}; // TODO: Hardcode the receivers' IPs
    ConcurrentHashMap<String, Member> memberList = new ConcurrentHashMap<String, Member>();
    for (int i = 0; i < 5; i++) {
        Member initialTarget = new Member(targetAddress[i], 0, config, "0");
        memberList.put(initialTarget.getUniqueId(), initialTarget);
        firstNode.network.sendMessage(initialTarget, firstNode.sendMsg);
    }
    long currTime = System.currentTimeMillis();
    long[] votes = firstNode.getMessages(currTime);
    firstNode.printVotes(votes);
    long numOfZeros;
    long numOfOnes;
    switch (firstNode.stepNumber % 3) {
        case 0:
            numOfZeros = firstNode.numZeros(firstNode.votes);
            numOfOnes = firstNode.numOnes(firstNode.votes);
            if (numOfZeros >= 2 * firstNode.votes.length / 3) {
                firstNode.changeSendMsg("0*");
            }
            if (numOfOnes >= 2 * firstNode.votes.length / 3) {
                firstNode.changeSendMsg("1");
            } else {
                firstNode.changeSendMsg("0");
            }
        case 1:
            numOfZeros = firstNode.numZeros(firstNode.votes);
            numOfOnes = firstNode.numOnes(firstNode.votes);
            if (numOfZeros >= 2 * firstNode.votes.length / 3) {
                firstNode.changeSendMsg("0");
            }
            if (numOfOnes >= 2 * firstNode.votes.length / 3) {
                firstNode.changeSendMsg("1*");
            } else {
                firstNode.changeSendMsg("1");
            }
        case 2:
            numOfZeros = firstNode.numZeros(firstNode.votes);
            numOfOnes = firstNode.numOnes(firstNode.votes);
            if (numOfZeros >= 2 * firstNode.votes.length / 3) {
                firstNode.changeSendMsg("0");
            }
            if (numOfOnes >= 2 * firstNode.votes.length / 3) {
                firstNode.changeSendMsg("1");
            } else {
                //write code to get coin genuinely tossed
                int b = Math.round((float) Math.random());
                firstNode.changeSendMsg(Integer.toString(b));
            }
    }
    firstNode.updateStepNumber();

    // Create some nodes that connect in a chair to each other. Despite only 1 node connecting to the
    // first node, the first node will eventually have a membership list with all the nodes in it.
    /*for(int i = 1; i <= 3; i++) {
        Node n = new Node( new InetSocketAddress("127.0.0.1", 8080 + i),
                               new InetSocketAddress("127.0.0.1", 8080 + i - 1), config, "test: node " + i);
        n.start();
    }*/
}

}
