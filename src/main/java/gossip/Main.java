package main.java.gossip;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
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
    Node firstNode = new Node(new InetSocketAddress("localhost", 8081), config, "0,0", 0);
    /*firstNode.setOnNewMemberHandler( (address) -> {
        System.out.println(address + " connected to node 0 (first node)");
        System.out.println();
    });*/
    //firstNode.start();
    InetSocketAddress[] targetAddress = {new InetSocketAddress("35.236.248.199", 8081),
            new InetSocketAddress("35.230.171.17", 8081), new InetSocketAddress("35.245.51.164", 8081), new InetSocketAddress("35.245.197.58", 8081)
}; // Hardcode the receivers' IPs
    ConcurrentHashMap<String, Member> memberList = new ConcurrentHashMap<>();
    for (int j=0; j<targetAddress.length; j++) {
        Member initialTarget = new Member(targetAddress[j], 0, config, "0,0");
        memberList.put(initialTarget.getUniqueId(), initialTarget);
    }
    for (String key: memberList.keySet()) {
        firstNode.network.sendMessage(memberList.get(key), firstNode.sendMsg);
	    System.out.println("Message '" + firstNode.sendMsg + "' is sent out from '" + firstNode.self.getUniqueId() + "'");
    }
    long currTime;
    long numOfZeros;
    long[] votes;
    long numOfOnes;
    int penultimateStep = -1;
    while(true){
        int step = firstNode.stepNumber % 3;
        currTime = System.currentTimeMillis();
	    //System.out.println("Current time: " + currTime);
        votes = firstNode.getMessages(currTime, step);
        //firstNode.sync(memberList); // Wait until all other nodes is ready for the next step
        firstNode.printVotes(votes);
        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        switch (step) {
            case 0:
    //            firstNode.votes[0] = Integer.valueOf(firstNode.sendMsg);
		        System.out.println("Inside case 0");
    		    numOfZeros = firstNode.numZeros(votes);
                numOfOnes = firstNode.numOnes(votes);
		        System.out.println("numOfZeros: " + numOfZeros);
		        System.out.println("numOfOnes: " + numOfOnes);
                if (numOfZeros >= 2 * votes.length / 3) {
                    firstNode.changeSendMsg("0*,1");
                    System.out.println("0*,1 will be the next sending message from " + firstNode.self.getUniqueId());
                } else if (numOfOnes >= 2 * votes.length / 3) {
                    firstNode.changeSendMsg("1,1");
                } else {
                    firstNode.changeSendMsg("0,1");
                }
                break;
            case 1:
    //            firstNode.votes[0] = Integer.valueOf(firstNode.sendMsg);
		        System.out.println("Inside case 1");
		        numOfZeros = firstNode.numZeros(votes);
                numOfOnes = firstNode.numOnes(votes);
                System.out.println("numOfZeros: " + numOfZeros);
                System.out.println("numOfOnes: " + numOfOnes);
                if (numOfZeros >= 2 * votes.length / 3) {
                    firstNode.changeSendMsg("0,2");
                } else if (numOfOnes >= 2 * votes.length / 3) {
                    firstNode.changeSendMsg("1*,2");
                    System.out.println("1*,1 will be the next sending message from " + firstNode.self.getUniqueId());
                } else {
                    firstNode.changeSendMsg("1,2");
                }
                break;
            case 2:
    //            firstNode.votes[0] = Integer.valueOf(firstNode.sendMsg);
                numOfZeros = firstNode.numZeros(votes);
                numOfOnes = firstNode.numOnes(votes);
		        System.out.println("inside case 2");
                System.out.println("numOfZeros is " + numOfZeros);
                System.out.println("numOfOnes is " + numOfOnes);
                if (numOfZeros >= 2 * votes.length / 3) {
                    firstNode.changeSendMsg("0,0");
                } else if (numOfOnes >= 2 * votes.length / 3) {
                    firstNode.changeSendMsg("1,0");
                } else {
                    //write code to get coin genuinely tossed
                    int b = Math.round((float) Math.random());
                    firstNode.changeSendMsg(Integer.toString(b) + ",0");
                }
                break;
        }
        if(firstNode.sendMsg.contains("*")){
            penultimateStep = firstNode.stepNumber;
            //System.out.println("Message being sent out contains '*' from " + firstNode.self.getPort());
        }
        firstNode.updateStepNumber();
        for (String key : memberList.keySet()) {
            firstNode.network.sendMessage(memberList.get(key), firstNode.sendMsg);
            System.out.println("Message '" + firstNode.sendMsg + "' is sent out from '" + firstNode.self.getUniqueId() + "'");
        }
	    // todo: every node is going to stop after step 0 cuz stepnumber always equals to penultimateStep + 1?!
        if(firstNode.stepNumber == (penultimateStep + 1) /*&& penultimateStep != 0*/){
            break;
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
