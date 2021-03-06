package main.java.gossip;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.Math; 

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Node class emulates a gossip node.
 *
 * @author  TTyler
 * @author  Vaibhav Singh
 * @author  Varun Madathil
 * @author  Wayne Chen
 * @version 1.1
 * @since   2019-02-26
 */
public class Node {
    private int MAX_NODES = 10;

    public final InetSocketAddress listeningAddress;
    public Network network;
    
    static Logger logger = (message) -> {};
    public Member self = null;
    private ConcurrentHashMap<String, Member> memberList = new ConcurrentHashMap<String, Member>();
    private boolean stopped = false;
    public String sendMsg;
    private String rcvdMsg;
    private Config config = null;
    private GossipUpdater onNewMember = null;
    private GossipUpdater onFailedMember = null;
    private GossipUpdater onRemovedMember = null;
    private GossipUpdater onRevivedMember = null;
    public Boolean isAdversary = false;
    private PrivateKey privKey;
    private static PublicKey[] publicKey = new PublicKey[9];
	int stepNumber = -1;
    long[] votes = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    ArrayList<Integer> fixed_votes = new ArrayList<>();

	public void updateStepNumber() {
		this.stepNumber++;
	}

	public long[] getMessages(long currTime, int currStep) {
	    int i = 0;
	    int j = 0;
        for(int k=0; k<fixed_votes.size(); k++) {
            votes[i%votes.length] = (long) fixed_votes.get(k);
            i++;
            j++;
        }
	    while(j < MAX_NODES) {
            Object rcvdObj = network.receiveMessage();
            String rcvMsg = rcvdObj.toString();
            String[] strs = rcvMsg.split(",");
            String bit = strs[0];
            String step = strs[1];
            String signature = strs[2];
            Boolean signatureVerified = verifySignature(bit + "," + step, signature);
            int caseNum = Integer.valueOf(step.trim()) % 3;
            if(caseNum !=currStep) continue;
            System.out.println("Received bit: " + bit + ", Received step: " + step);
            if (caseNum == currStep && bit.length() == 1 && signatureVerified){
                votes[i%votes.length] = (long) Integer.valueOf(bit);
                i++;
            }
            if (caseNum == currStep && bit.length() == 2 && signatureVerified){
                votes[i%votes.length] = (long) Integer.valueOf(Character.toString(bit.charAt(0)));
                fixed_votes.add(Integer.valueOf(Character.toString(bit.charAt(0))));
                i++;
            }
            j++;
	    }
        return votes;
    }

	public void printVotes(long list[]){
		System.out.println("All bits received: ");
		for (int i = 0; i < list.length; i++){
			System.out.print(list[i] + "   ");
		}
	}
	public int numZeros(long[] list){
		int count = 0;
		for (int i =0 ; i < list.length; i++){
			if(list[i] == 0){
				count++;
			}	
		}	
		return count;
	}
	

	public int numOnes(long[] list){
	    int count = 0;
        for (int i =0 ; i < list.length; i++){
                if(list[i] == 1){
                        count++;
                }
        }
        return count;
	}

	
    /**
     * Node constructor - initialize gossip protocol as the first node in the system.
     * create signature for message
     * send votes for the current step
     *
     * */
    public Node(InetSocketAddress listeningAddress, Config config, String message, int steps) {
        try {
            // TODO: Would need to map the public keys to a list of all public keys instead of 
            // a single public key
            privKey = CryptoUtil.getPrivate("/home/vagrant/Gossip-Protocol/src/key.der");
            publicKey[0] = CryptoUtil.getPublic("/home/vagrant/Gossip-Protocol/src/public1.der");
            publicKey[1] = CryptoUtil.getPublic("/home/vagrant/Gossip-Protocol/src/public2.der");
            publicKey[2] = CryptoUtil.getPublic("/home/vagrant/Gossip-Protocol/src/public3.der");
            publicKey[3] = CryptoUtil.getPublic("/home/vagrant/Gossip-Protocol/src/public4.der");  
        } catch (Exception e) {
            //
        }


        this.config = config;
        this.listeningAddress = listeningAddress;
        this.network = new Network(listeningAddress.getPort());
        self = new Member(listeningAddress, 0, config, message);
        sendMsg = message; // Format: Bit, Step
        memberList.put(self.getUniqueId(), self);
        this.stepNumber = steps;
    }

    public Node(InetSocketAddress listeningAddress, Config config, String message, int steps, Boolean isAdversary) {
	this(listeningAddress, config, message, steps);
	this.isAdversary = isAdversary;
    }


    /**
     * Node constructor - Connect to another node in the gossip protocol and begin fault tolerance
     * monitoring.
     * */
    public Node(InetSocketAddress listeningAddress,
                  InetSocketAddress targetAddress, Config config, String message, int steps) {
        this(listeningAddress, config, message, steps);
        Member initialTarget = new Member(targetAddress, 0, config, message);
        memberList.put(initialTarget.getUniqueId(), initialTarget);
    }

    /**
     *
     * @param msg Make sure that the node will send out a message it wants to send later
     */
    public void changeSendMsg(String msg) {
        sendMsg = msg;
    }

    public void start() {
        startSendThread();
        startReceiveThread();
        startFailureDetectionThread();
    }

    private void startSendThread() {
        new Thread(() -> {
            while (!stopped) {
                try {
                    Thread.sleep(config.UPDATE_FREQUENCY.toMillis());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sendMemberListToRandomMemeber();
            }
        }).start();
    }

    private void startReceiveThread() {
        new Thread(() -> {
            while (!stopped) {
                receiveMemberList();
            }
        }).start();
    }

    private void startFailureDetectionThread() {
        new Thread(
                () -> {
                    while (!stopped) {
                        detectFailedMembers();
                        try {
                            Thread.sleep(config.FAILURE_DETECTION_FREQUENCY
                                    .toMillis());
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }	).start();
    }

    public void stop() {
        stopped = true;
    }

    private void detectFailedMembers() {
        String[] keys = new String[memberList.size()];
        memberList.keySet().toArray(keys);
        for (String key : keys) {
            Member member = memberList.get(key);
            boolean hadFailed = member.hasFailed();
            member.checkIfFailed();
            // node failure status has changed
            if (member.hasFailed() != hadFailed) {
                if (member.hasFailed()) {
                    if (onFailedMember != null) {
                        onFailedMember.update(member.getSocketAddress());
                    }
                } else {
                    if (onRevivedMember != null) {
                        onRevivedMember.update(member.getSocketAddress());
                    }
                }
            }
            if (member.shouldCleanup()) {
                synchronized (memberList) {
                    memberList.remove(key);
                    if (onRemovedMember != null) {
                        onRemovedMember.update(member.getSocketAddress());
                    }
                }
            }
        }
    }

    private void receiveMemberList() {
        Object rcvdObj = network.receiveMessage();
        Member newMember = null;
        boolean isStr = false;
        if(rcvdObj instanceof String) {
            rcvdMsg = rcvdObj.toString();
            isStr = true;
        } else {
            try {
                newMember  = (Member) rcvdObj;
            } catch (Exception e) {
                Node.logger.log("Error casting Gossip network data to Member class because: " + e.getMessage());
            }
        }
        if (!isStr) {
            Member member = memberList.get(newMember.getUniqueId());
            if (member == null) { // member not in the list
                synchronized (memberList) {
                    newMember.setConfig(config);
                    newMember.updateLastUpdateTime();
                    memberList.put(newMember.getUniqueId(), newMember);
                    if (onNewMember != null) {
                        onNewMember.update(newMember.getSocketAddress());
                    }
                }
            } else { // member was in the list
                member.updateSequenceNumber(newMember.getSequenceNumber());
            }
        } else {
            System.out.println(self.getSocketAddress() + " received message '" + rcvdMsg + "' from a peer.");
        }
    }

    private void sendMemberListToRandomMemeber() {
        self.incremenetSequenceNumber();
        List<String> peersToUpdate = new ArrayList<String>();
        Object[] keys = memberList.keySet().toArray();
        if (keys.length < config.PEERS_TO_UPDATE_PER_INTERVAL) {
            for (int i = 0; i < keys.length; i++) {
                String key = (String) keys[i];
                if (!key.equals(self.getUniqueId())) {
                    peersToUpdate.add(key);
                }
            }
        } else {
            for (int i = 0; i < config.PEERS_TO_UPDATE_PER_INTERVAL; i++) {
                boolean newTargetFound = false;
                while (!newTargetFound) {
                    int randomIndex = (int) (Math.random() * memberList.size() - 1);
                    String targetKey = (String) keys[randomIndex];
                    if (!targetKey.equals(self.getUniqueId())) {
                        newTargetFound = true;
                        peersToUpdate.add(targetKey);
                    }
                }
            }
        }
        for (String targetKey : peersToUpdate) {
            Member target = memberList.get(targetKey);
            if (sendMsg != null) {
                System.out.println(self.getSocketAddress() + " is sending message '" + sendMsg + "' out.");
                network.sendMessage(target, sendMsg);
            }
            for (Member member : memberList.values()) {
                network.sendMessage(target, member);
            }
        }
    }

    public ArrayList<InetSocketAddress> getAllMembers() {
        // used to prevent resizing of ArrayList.
        int initialSize = memberList.size();
        ArrayList<InetSocketAddress> allMembers = new ArrayList<InetSocketAddress>(
                initialSize);
        for (String key : memberList.keySet()) {
            Member member = memberList.get(key);

            String ipAddress = member.getAddress();
            int port = member.getPort();

            allMembers.add(new InetSocketAddress(ipAddress, port));

        }
        return allMembers;
    }

    public static void setLogger(Logger newLogger) {
        if (newLogger != null) {
            logger = newLogger;
        }
    }

    public static Boolean verifySignature(String message, String signature) {
        int retry = 3;
        for (int i = 0; i < 3; i++) {
            try {
                if (retry > 0) {
                    return CryptoUtil.verifySignature(message, signature, publicKey[i]);
                } else {
                    return false;
                }
            } catch (Exception ex) {
                retry--;
            }
        }
        return true;

    }
}
