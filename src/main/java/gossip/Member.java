package main.java.gossip;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Member class emulates a member in the membership list for all nodes.
 *
 * @author  TTyler
 * @author  Vaibhav Singh
 * @author  Varun Madathil
 * @author  Wayne Chen
 * @version 1.1
 * @since   2019-02-26
 */
public class Member implements Serializable {
	private InetSocketAddress address = null;
	
	private long heartbeatSequenceNumber = 0;
	private LocalDateTime lastUpdateTime = null;
	
	private boolean hasFailed = false;
	
	private Config config;
    private String message;
	
	public Member(InetSocketAddress address, long initialHearbeatSequenceNumber, Config config) {
		this.address = address;
		this.config = config;
		
		updateLastUpdateTime();
	}

    public Member(InetSocketAddress address, long initialHearbeatSequenceNumber, Config config, String message) {
         this.address = address;
         this.config = config;
         this.message = message;
     
         updateLastUpdateTime();
     }

	public void setConfig(Config config) {
		this.config = config;
	}
	
	public String getAddress() {
		return address.getHostName();
	}
	
	public InetAddress getInetAddress() {
		return address.getAddress();
	}
	
	public InetSocketAddress getSocketAddress() {
		return address;
	}
	
	public int getPort() {
		return address.getPort();
	}
	
	public String getUniqueId() {
		return address.toString();
	}
	
	public long getSequenceNumber() {
		return heartbeatSequenceNumber;
	}

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
	
	public void updateSequenceNumber(long newSequenceNumber) {
		if (newSequenceNumber > heartbeatSequenceNumber) {
			heartbeatSequenceNumber = newSequenceNumber;
			updateLastUpdateTime();
		}
	}
	
	public void updateLastUpdateTime() {
		lastUpdateTime = LocalDateTime.now();
	}
	
	public void incremenetSequenceNumber() {
		heartbeatSequenceNumber++;
		updateLastUpdateTime();
	}
	
	public void checkIfFailed() {
		LocalDateTime failureTime = lastUpdateTime.plus(config.MEMBER_FAILURE_TIMEOUT);
		LocalDateTime now = LocalDateTime.now();
		
		hasFailed = now.isAfter(failureTime);
	}
	
	public boolean shouldCleanup() {
		if (hasFailed) {
			Duration cleanupTimeout = config.MEMBER_FAILURE_TIMEOUT.plus(config.MEMBER_CLEANUP_TIMEOUT);
			LocalDateTime cleanupTime = lastUpdateTime.plus(cleanupTimeout);
			LocalDateTime now = LocalDateTime.now();
			
			return now.isAfter(cleanupTime);
		} else {
			return false;			
		}
	}
	
	public boolean hasFailed() {
		return hasFailed;
	}
	
	
	
	public String getNetworkMessage() {
		return "[" + address.getHostName() + ":" + address.getPort() + "-" + heartbeatSequenceNumber + "testbla" + "]";
	}
	
	
}
