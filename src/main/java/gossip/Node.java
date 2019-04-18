package main.java.gossip;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.Math; 
public class Node {

	int stepNumber = -1;
	String str = "";
    long[][] roundMessages = {{0,10},{1,5},{0,3},{0,10},{1,1}};
    long[] votes = {0, 0, 0, 0};
    int currentStep;
    long currTime= System.currentTimeMillis();
    long endTime = currTime + 5000;

	public void updateStepNumber() {
		this.stepNumber++;
	}

	public long[] getMessages(long currTime) {
	    int i = 0;
	    int j = 0;
	    //while(System.currentTimeMillis()<currTime + 5000){
	    while(j < 4) {
		 System.out.println("inside getMEssages");
		 Object rcvdObj = network.receiveMessage();
		 String rcvMsg = rcvdObj.toString();
		 System.out.println("rcvMsg is " + rcvMsg);
		 //String rcvMsg = "0";
		 if (rcvMsg.length() == 1){
			votes[i%votes.length] = (long) Integer.valueOf(rcvMsg);
			i++;
		 }
        if (rcvMsg.length() == 2){
            votes[i%votes.length] = (long) Integer.valueOf(rcvMsg.charAt(0));
            i++;
        }
	    //listen for messages
		    //verify messages and then add the messages to the array roundMessages
            //put messages into arrays
		j++;
	    }
        return votes;
    }
	public void printVotes(long list[]){
		System.out.println("from printVotes");
		for (int i = 0; i < list.length; i++){
			System.out.println(list[i]);
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

	//create signature for message
	
	//send vote for the current step

    public final InetSocketAddress listeningAddress;
    public Network network;
    // instantiate a default logger that does not log anything
    static Logger logger = (message) -> {};
    private Member self = null;
    private ConcurrentHashMap<String, Member> memberList = new ConcurrentHashMap<String, Member>();
    private boolean stopped = false;
    public String sendMsg;
    private String rcvdMsg;
    // configurable values
    private Config config = null;
    private GossipUpdater onNewMember = null;
    private GossipUpdater onFailedMember = null;
    private GossipUpdater onRemovedMember = null;
    private GossipUpdater onRevivedMember = null;

    /**
     * initialize gossip protocol as the first node in the system.
     * */
    public Node(InetSocketAddress listeningAddress, Config config, String message, int steps) {
        this.config = config;
        this.listeningAddress = listeningAddress;
        this.network = new Network(listeningAddress.getPort());
        self = new Member(listeningAddress, 0, config, message);
        sendMsg = message;
        memberList.put(self.getUniqueId(), self);
        this.stepNumber = steps;
    }

    /**
     * Connect to another node in the gossip protocol and begin fault tolerance
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
}
