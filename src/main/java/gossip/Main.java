package main.java.gossip;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import main.java.gossip.Config;
import main.java.gossip.Gossip;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.InputStream;

public class Main {
public static void main(String[] args) {
    Config config = new Config(
            Duration.ofSeconds(2), // time between receiving the last heartbeat and marking a member as failing
            Duration.ofSeconds(2), // time between marking a member as failed and removing it from the list
            Duration.ofMillis(500), // how often the member list is broadcast to other members
            Duration.ofMillis(200), // how often the Gossip protocol checks if any members have failed
            3                       // the number of nodes to send the membership list to when broadcasting.
    );

	Properties prop = new Properties();
        try (InputStream input = new FileInputStream("src/config.properties")) {
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    // Set how the error messages will be handled.
    Node.setLogger((message) -> {
        System.out.println("Gossip Error: " + message);
    });
    String vmname = prop.getProperty("vmname");
    String[] seeds = prop.getProperty("seed").split(",");
    String[] adversaries = prop.getProperty("adversaries").split(",");
    String seed;
    Boolean isNodeAnAdversary;

    // TODO: change this if else statements for more nodes
    if (vmname.equals("gossip1")) {
    	seed = seeds[0];
    	isNodeAnAdversary = Boolean.parseBoolean(adversaries[0]);
    } else if (vmname.equals("gossip2")) {
    	seed = seeds[1];
    	isNodeAnAdversary = Boolean.parseBoolean(adversaries[1]);
    } else if (vmname.equals("gossip3")) {
    	seed = seeds[2];
    	isNodeAnAdversary = Boolean.parseBoolean(adversaries[2]);
    } else if (vmname.equals("gossip4")) {
    	seed = seeds[3];
    	isNodeAnAdversary = Boolean.parseBoolean(adversaries[3]);
    } else if (vmname.equals("gossip5")) {
        seed = seeds[4];
        isNodeAnAdversary = Boolean.parseBoolean(adversaries[4]);
    } else if (vmname.equals("gossip6")) {
        seed = seeds[5];
        isNodeAnAdversary = Boolean.parseBoolean(adversaries[5]);
    } else if (vmname.equals("gossip7")) {
        seed = seeds[6];
        isNodeAnAdversary = Boolean.parseBoolean(adversaries[6]);
    } else if (vmname.equals("gossip8")) {
        seed = seeds[7];
        isNodeAnAdversary = Boolean.parseBoolean(adversaries[7]);
    } else if (vmname.equals("gossip9")) {
        seed = seeds[8];
        isNodeAnAdversary = Boolean.parseBoolean(adversaries[8]);
    } else {
    	 seed = "0";
    	 isNodeAnAdversary = false;
    }
    Node firstNode = new Node(new InetSocketAddress("localhost", 6991), config, seed + ",0", 0, isNodeAnAdversary);
    /*firstNode.setOnNewMemberHandler( (address) -> {
        System.out.println(address + " connected to node 0 (first node)");
        System.out.println();
    });*/
    //firstNode.start();
    // todo: change this list of IPs
    InetSocketAddress[] targetAddress = {new InetSocketAddress("35.236.248.199", 6991),
            new InetSocketAddress("35.245.51.164", 6991), new InetSocketAddress("35.230.171.17", 6991),
            new InetSocketAddress("35.245.215.147", 6991), new InetSocketAddress("35.245.197.58", 6991),
            new InetSocketAddress("35.236.229.113", 6991), new InetSocketAddress("23.251.148.243", 6991),
            new InetSocketAddress("35.232.26.149", 6991), new InetSocketAddress("35.238.119.252", 6991)
}; // Hardcode the receivers' IPs
    ConcurrentHashMap<String, Member> memberList = new ConcurrentHashMap<>();
    for (int j=0; j<targetAddress.length; j++) {
        Member initialTarget = new Member(targetAddress[j], 0, config, "0,0");
        memberList.put(initialTarget.getUniqueId(), initialTarget);
    }


        try {
            TimeUnit.SECONDS.sleep(15);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
	//System.out.println("Inside BA gossip3: " + currTime);
        votes = firstNode.getMessages(currTime, step);
        //firstNode.sync(memberList); // Wait until all other nodes is ready for the next step
        firstNode.printVotes(votes);
        try {
            TimeUnit.SECONDS.sleep(15);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String nodeStep = String.valueOf(firstNode.stepNumber+1);
        if (firstNode.isAdversary) {
            //System.out.println("I am malicious");
            int b = Math.round((float) Math.random());
            firstNode.changeSendMsg(Integer.toString(b) + "," + nodeStep);
        } else {
            switch (step) {
                case 0:
                    //            firstNode.votes[0] = Integer.valueOf(firstNode.sendMsg);
                    //System.out.println("Inside case 0");
                    numOfZeros = firstNode.numZeros(votes);
                    numOfOnes = firstNode.numOnes(votes);
                    //System.out.println("numOfZeros: " + numOfZeros);
                    //System.out.println("numOfOnes: " + numOfOnes);
                    if (numOfZeros >= (2 * votes.length) / 3 + 1) {
                        firstNode.changeSendMsg("0*," + nodeStep);
                        System.out.println("\n0*," + nodeStep + " will be the next sending message from " + firstNode.self.getUniqueId());
                    } else if (numOfOnes >= (2 * votes.length) / 3 + 1) {
                        firstNode.changeSendMsg("1," + nodeStep);
                    } else {
                        firstNode.changeSendMsg("0," + nodeStep);
                    }
                    break;
                case 1:
                    //            firstNode.votes[0] = Integer.valueOf(firstNode.sendMsg);
                    //System.out.println("Inside case 1");
                    numOfZeros = firstNode.numZeros(votes);
                    numOfOnes = firstNode.numOnes(votes);
                    //System.out.println("numOfZeros: " + numOfZeros);
                    //System.out.println("numOfOnes: " + numOfOnes);
                    if (numOfZeros >= (2 * votes.length) / 3 + 1) {
                        firstNode.changeSendMsg("0," + nodeStep);
                    } else if (numOfOnes >= (2 * votes.length) / 3 + 1) {
                        firstNode.changeSendMsg("1*," + nodeStep);
                        System.out.println("\n1*,1 will be the next sending message from " + firstNode.self.getUniqueId());
                    } else {
                        firstNode.changeSendMsg("1," + nodeStep);
                    }
                    break;
                case 2:
                    //            firstNode.votes[0] = Integer.valueOf(firstNode.sendMsg);
                    numOfZeros = firstNode.numZeros(votes);
                    numOfOnes = firstNode.numOnes(votes);
                    //System.out.println("inside case 2");
                    //System.out.println("numOfZeros is " + numOfZeros);
                    //System.out.println("numOfOnes is " + numOfOnes);
                    if (numOfZeros >= (2 * votes.length) / 3 + 1) {
                        firstNode.changeSendMsg("0," + nodeStep);
                    } else if (numOfOnes >= (2 * votes.length) / 3 + 1) {
                        firstNode.changeSendMsg("1," + nodeStep);
                    } else {
                        //write code to get coin genuinely tossed
                        int b = Math.round((float) Math.random());
                        firstNode.changeSendMsg(Integer.toString(b) + "," + nodeStep);
                    }
                    break;
            }
        }
        if(firstNode.sendMsg.contains("*")){
            penultimateStep = firstNode.stepNumber;
            //System.out.println("Message being sent out contains '*' from " + firstNode.self.getPort());
        }
        firstNode.updateStepNumber();
        for (String key : memberList.keySet()) {
            firstNode.network.sendMessage(memberList.get(key), firstNode.sendMsg);
            //System.out.println("Message '" + firstNode.sendMsg + "' is sent out from '" + firstNode.self.getUniqueId() + "'");
        }
        if(firstNode.stepNumber == (penultimateStep + 1) /*&& penultimateStep != 0*/){
            break;
        }
    }
    // Create some nodes that connect in a chair to each other. Despite only 1 node connecting to the
    // first node, the first node will eventually have a membership list with all the nodes in it.
    /*for(int i = 1; i <= 3; i++) {
        Node n = new Node( new InetSocketAddress("127.0.0.1", 6991 + i),
                               new InetSocketAddress("127.0.0.1", 6991 + i - 1), config, "test: node " + i);
        n.start();
    }*/
}

}
