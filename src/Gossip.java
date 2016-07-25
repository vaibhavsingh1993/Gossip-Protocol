import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;


public class Gossip {
	
	public int listeningPort = 8080;
	
	private Network network;
	
	private Member self = null;
	private HashMap<String, Member> memberList = new HashMap<String, Member>();
	
	private boolean stopped = false;
	
	// configurable values
	private int peersToUpdatePerInterval = 3; 
	private int updateFrequencyInMilliseconds = 500;
	private int failureDetectionFrequency = 200;
		
	
	/**
	 * initialize gossip protocol as the first node in the system.
	 * */
	public Gossip(String localIpAddress, int listeningPort) {
		this.listeningPort = listeningPort;
		this.network = new Network(listeningPort);
		
		self = new Member(localIpAddress, listeningPort, 0);
		memberList.put(self.getUniqueId(), self);
	}
	
	/**
	 * Connect to another node in the gossip protocol and 
	 * begin fault tolerance monitoring.
	 * */
	public Gossip(String localIpAddress, int listeningPort, String ipAddress, int port) {
		this(localIpAddress, listeningPort);
		
		Member initialTarget = new Member(ipAddress, port, 0);
		memberList.put(initialTarget.getUniqueId(), initialTarget);
		
		/*Member x = new Member("127.0.0.1", 8082, 0);
		memberList.put(x.getUniqueId(), x);
		
		Member y = new Member("127.0.0.1", 8083, 0);
		memberList.put(y.getUniqueId(), y);*/
	}
	
	public void start() {
		startSendThread();
		startReceiveThread();
		startFailureDetectionThread();
	}
	
	private void startSendThread() {
		new Thread(() -> {
			while(!stopped) {
				try {
					Thread.sleep(updateFrequencyInMilliseconds);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				sendMemberListToRandomMemeber();
			}
		}).start();
	}
	
	private void startReceiveThread() {
		new Thread(() -> {
			while(!stopped) {
				receiveMemberList();
			}
		}).start();
	}
	
	private void startFailureDetectionThread() {
		new Thread(() -> {
			while(!stopped) {
				detectFailedMembers();
				try {
					Thread.sleep(failureDetectionFrequency);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public void stop() {
		stopped = true;
	}
	
	private void detectFailedMembers() {
		String[] keys = new String[memberList.size()];
		memberList.keySet().toArray(keys);
				
		for (String key : keys) {
			Member member = memberList.get(key);
			
			member.checkIfFailed();
			
			if(member.shouldCleanup()) {
				synchronized(memberList) {
					memberList.remove(key);
				}
			}
		}
	}
	
	private void receiveMemberList() {
		Member newMemeber = network.receiveMessage();
		System.out.println(self.getNetworkMessage() + " - Received: " + newMemeber.getNetworkMessage());
		
		Member member = memberList.get(newMemeber.getUniqueId());
		if (member == null) { // member not in the list
			synchronized(memberList) {
				memberList.put(newMemeber.getUniqueId(), newMemeber);
			}
		} else { // member was in the list
			member.updateSequenceNumber(newMemeber.getSequenceNumber());
		}
	}
	
	private void sendMemberListToRandomMemeber() {
		self.incremenetSequenceNumber();
		
		List<String> peersToUpdate = new ArrayList<String>();
		Object[] keys = memberList.keySet().toArray();
		
		if (keys.length < peersToUpdatePerInterval) {
			for (int i = 0; i < keys.length; i++) {
				String key = (String) keys[i];
				if (!key.equals(self.getUniqueId())) {
					peersToUpdate.add(key);
				}
			}
		} else {
			for (int i = 0; i < peersToUpdatePerInterval; i++) {
				boolean newTargetFound = false;
				
				while(!newTargetFound) {
					int randomIndex = (int) (Math.random() * memberList.size());
				
					String targetKey = (String) keys[randomIndex];
					
					if(!targetKey.equals(self.getUniqueId())) {
						newTargetFound = true;
						peersToUpdate.add(targetKey);
					}
				}
			}
		}
		
		
		
		for (String targetKey : peersToUpdate) {
			Member target = memberList.get(targetKey);
			System.out.println(self.getNetworkMessage() + " - sending to: " + memberList.get(targetKey).getNetworkMessage());
			
			for(Member member : memberList.values()) {
				network.sendMessage(target, member);
			}
		}
	}
	
	public ArrayList<InetSocketAddress> getAliveMembers() {
		// assume that most members are alive in the list and so 
		// we set the initial size of the list to return to prevent arrayList resizing.
		int initialSize = memberList.size();
		ArrayList<InetSocketAddress> aliveMembers = new ArrayList<InetSocketAddress>(initialSize);
		
		
		for (String key : memberList.keySet()) {
			Member member = memberList.get(key);
			if (!member.hasFailed()) {
				String ipAddress = member.getAddress();
				int port = member.getPort();
				
				aliveMembers.add(new InetSocketAddress(ipAddress, port));
			}
		}
		
		return aliveMembers;
	}
	
	public ArrayList<InetSocketAddress> getFailedMembers() {
		ArrayList<InetSocketAddress> failedMembers = new ArrayList<InetSocketAddress>();
		
		for (String key : memberList.keySet()) {
			Member member = memberList.get(key);
			if (member.hasFailed()) {
				String ipAddress = member.getAddress();
				int port = member.getPort();
				
				failedMembers.add(new InetSocketAddress(ipAddress, port));
			}
		}
		
		return failedMembers;
	}
	
	
}
