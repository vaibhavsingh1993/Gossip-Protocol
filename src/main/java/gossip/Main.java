package main.java.gossip;
import java.net.InetSocketAddress;
import java.time.Duration;

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
  
    Node firstNode = new Node(new InetSocketAddress("127.0.0.1", 8080), config, "test: node 0", 0);
  
/*    firstNode.setOnNewMemberHandler( (address) -> {
        System.out.println(address + " connected to node 0 (first node)");
        System.out.println();
    });*/
  
    //firstNode.start();	
    long currTime= System.currentTimeMillis();
    
    long votes =    firstNode.getMessages(currTime);
    firstNode.printVotes();
    if ((firstNode.stepNumber % 3) == 0){
        long numOfZeros = firstNode.numZeros(firstNode.votes);
        long numOfOnes = firstNode.numOnes(firstNode.votes);
        if (numOfZeros >= 2* firstNode.votes.length/3){
            firstNode.str = "0*";
        }
        if(numOfOnes >= 2*firstNode.votes.length/3){
            firstNode.str = "1";
        }
        else{
            firstNode.str = "0";
        }

    }
    else if(firstNode.stepNumber % 3 == 1){
        long numOfZeros = firstNode.numZeros(firstNode.votes);
        long numOfOnes = firstNode.numOnes(firstNode.votes);
        if (numOfZeros >= 2*firstNode.votes.length/3){
            firstNode.str = "0";
        }
        if(numOfOnes >= 2*firstNode.votes.length/3){
            firstNode.str = "1*";
        }
        else{
            firstNode.str = "1";
        }
    }
    else{
        long numOfZeros = firstNode.numZeros(firstNode.votes);
        long numOfOnes = firstNode.numOnes(firstNode.votes);
        if (numOfZeros >= 2*firstNode.votes.length/3){
            firstNode.str = "0";
        }
        if(numOfOnes >= 2*firstNode.votes.length/3){
            firstNode.str = "1";
        }
        else{
            //write code to get coin genuinely tossed
            int b = Math.round((float)Math.random());
            firstNode.str = Integer.toString(b);
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
