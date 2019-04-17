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
    // TODO: Distribute a seed to nodes
    Node firstNode = new Node(new InetSocketAddress("127.0.0.1", 8080), config, "0", 0);
  
/*    firstNode.setOnNewMemberHandler( (address) -> {
        System.out.println(address + " connected to node 0 (first node)");
        System.out.println();
    });*/

    //firstNode.start();
    InetSocketAddress[] targetAddress = {new InetSocketAddress("10.152.56.137", 8080),
            new InetSocketAddress("10.154.58.59", 8080),
    }; // TODO: Hardcode the receivers' IPs
    ConcurrentHashMap<String, Member> memberList = new ConcurrentHashMap<String, Member>();
    for (int i = 0; i < 3; i++) {
        Member initialTarget = new Member(targetAddress[i], 0, config, "0");
        memberList.put(initialTarget.getUniqueId(), initialTarget);
        firstNode.network.sendMessage(initialTarget, firstNode.sendMsg);
    }
    long currTime = System.currentTimeMillis();

    long numOfZeros;
    long[] votes;
    long numOfOnes;
    int penultimateStep = 0;
while(true){
    votes = firstNode.getMessages(currTime);
    firstNode.printVotes(votes);
    switch (firstNode.stepNumber % 3) {
        case 0:
//            firstNode.votes[0] = Integer.valueOf(firstNode.sendMsg);
            numOfZeros = firstNode.numZeros(votes);
            numOfOnes = firstNode.numOnes(votes);
            if (numOfZeros >= 2 * votes.length / 3) {
                firstNode.changeSendMsg("0*");

            }
            if (numOfOnes >= 2 * votes.length / 3) {
                firstNode.changeSendMsg("1");
            } else {
                firstNode.changeSendMsg("0");
            }
            break;
        case 1:
//            firstNode.votes[0] = Integer.valueOf(firstNode.sendMsg);
            numOfZeros = firstNode.numZeros(votes);
            numOfOnes = firstNode.numOnes(votes);
            if (numOfZeros >= 2 * votes.length / 3) {
                firstNode.changeSendMsg("0");
            }
            if (numOfOnes >= 2 * votes.length / 3) {
                firstNode.changeSendMsg("1*");
            } else {
                firstNode.changeSendMsg("1");
            }
            break;
        case 2:
//            firstNode.votes[0] = Integer.valueOf(firstNode.sendMsg);
            numOfZeros = firstNode.numZeros(votes);
            numOfOnes = firstNode.numOnes(votes);
            if (numOfZeros >= 2 * votes.length / 3) {
                firstNode.changeSendMsg("0");
            }
            if (numOfOnes >= 2 * votes.length / 3) {
                firstNode.changeSendMsg("1");
            } else {
                //write code to get coin genuinely tossed
                int b = Math.round((float) Math.random());
                firstNode.changeSendMsg(Integer.toString(b));
            }
            break;
        }
    if(firstNode.str.contains("*")){
	    penultimateStep = firstNode.stepNumber;
	}
    firstNode.updateStepNumber();
    if(firstNode.stepNumber == (penultimateStep + 1) && penultimateStep != 0){

		break;
	}
    for (int i = 0; i < 3; i++) {
        Member initialTarget = new Member(targetAddress[i], 0, config, "0");
        memberList.put(initialTarget.getUniqueId(), initialTarget);
        firstNode.network.sendMessage(initialTarget, firstNode.sendMsg);
    }
}
    // Create some nodes that connect in a chair to each other. Despite only 1 node connecting to the
    // first node, the first node will eventually have a membership list with all the nodes in it.
    /*for(int i = 1; i <= 3; i++) {
        Node n = new Node( new InetSocketAddress("127.0.0.1", 8080 + i),
                               new InetSocketAddress("127.0.0.1", 8080 + i - 1), config, "test: node " + i);
        n.start();
    }*/
}

}
