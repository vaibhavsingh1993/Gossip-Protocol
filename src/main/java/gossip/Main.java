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
    String[] nodelist = prop.getProperty("nodelist").split(",");
    String seed;
    String node;
    Boolean isNodeAnAdversary;

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
    } else if (vmname.equals("gossip10")) {
        seed = seeds[9];
        isNodeAnAdversary = Boolean.parseBoolean(adversaries[9]);
    } else {
    	 seed = "0";
    	 isNodeAnAdversary = false;
    }
    Node firstNode = new Node(new InetSocketAddress("localhost", 6991), config, seed + ",0", 0, isNodeAnAdversary);

    InetSocketAddress[] targetAddress = {new InetSocketAddress(nodelist[0], 6991),
            new InetSocketAddress(nodelist[1], 6991), new InetSocketAddress(nodelist[2], 6991),
            new InetSocketAddress(nodelist[3], 6991), new InetSocketAddress(nodelist[4], 6991),
            new InetSocketAddress(nodelist[5], 6991), new InetSocketAddress(nodelist[6], 6991),
            new InetSocketAddress(nodelist[7], 6991), new InetSocketAddress(nodelist[8], 6991),
            new InetSocketAddress(nodelist[9], 6991)
};
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



    if (firstNode.isAdversary) {
        for (String key : memberList.keySet()) {
            int b = Math.round((float) Math.random());
            firstNode.changeSendMsg(Integer.toString(b) + ",0");
            firstNode.network.sendMessage(memberList.get(key), firstNode.sendMsg);
            //System.out.println("Message '" + firstNode.sendMsg + "' is sent out from '" + firstNode.self.getUniqueId() + "'");
        }
    } else {
        for (String key : memberList.keySet()) {
            firstNode.network.sendMessage(memberList.get(key), firstNode.sendMsg);
            //System.out.println("Message '" + firstNode.sendMsg + "' is sent out from '" + firstNode.self.getUniqueId() + "'");
        }
    }
    long currTime;
    long numOfZeros;
    long[] votes;
    long numOfOnes;
    int penultimateStep = -1;
    while(true){
        int step = firstNode.stepNumber % 3;
        currTime = System.currentTimeMillis();
        votes = firstNode.getMessages(currTime, step);
        //firstNode.sync(memberList); // Wait until all other nodes is ready for the next step
        firstNode.printVotes(votes);
        try {
            TimeUnit.SECONDS.sleep(15);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String nodeStep = String.valueOf(firstNode.stepNumber+1);
        if (!firstNode.isAdversary) {
            switch (step) {
                case 0:
                    //firstNode.votes[0] = Integer.valueOf(firstNode.sendMsg);
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
                    //firstNode.votes[0] = Integer.valueOf(firstNode.sendMsg);
                    //System.out.println("Inside case 1");
                    numOfZeros = firstNode.numZeros(votes);
                    numOfOnes = firstNode.numOnes(votes);
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
                    //firstNode.votes[0] = Integer.valueOf(firstNode.sendMsg);
                    numOfZeros = firstNode.numZeros(votes);
                    numOfOnes = firstNode.numOnes(votes);
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
        if (firstNode.isAdversary) {
            for (String key : memberList.keySet()) {
                int b = Math.round((float) Math.random());
                firstNode.changeSendMsg(Integer.toString(b) + "," + nodeStep);
                firstNode.network.sendMessage(memberList.get(key), firstNode.sendMsg);
                //System.out.println("Message '" + firstNode.sendMsg + "' is sent out from '" + firstNode.self.getUniqueId() + "'");
            }
        } else {
            for (String key : memberList.keySet()) {
                firstNode.network.sendMessage(memberList.get(key), firstNode.sendMsg);
                //System.out.println("Message '" + firstNode.sendMsg + "' is sent out from '" + firstNode.self.getUniqueId() + "'");
            }
        }
        firstNode.updateStepNumber();
        if(firstNode.stepNumber == (penultimateStep + 1)){
            break;
        }
    }
}
}
