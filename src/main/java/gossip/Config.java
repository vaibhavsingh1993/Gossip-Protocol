package main.java.gossip;

import java.io.Serializable;
import java.time.Duration;

/**
 * Config wrapper class to easily set up
 * heartbeat configuration.
 *
 * @author  TTyler
 * @version 1.0
 * @since   2019-02-26
 */
public class Config implements Serializable {
	
	// Values for members class to use
	public final Duration MEMBER_FAILURE_TIMEOUT;
	public final Duration MEMBER_CLEANUP_TIMEOUT;
	
	// Values for Gossip class to use
	public final int PEERS_TO_UPDATE_PER_INTERVAL; 
	public final Duration UPDATE_FREQUENCY;
	public final Duration FAILURE_DETECTION_FREQUENCY;
	
    /** 
     * Config constructor
     *
     * @param memberFailureTimeout Time after which a member is identified as failed.
     * @param memberCleanupTimeout Time after which a member is cleaned from the members list.
     * @param UpdateFrequency Heartbeat frequency
     * @param failureDetectionFrequency Failure detection frequency
     * @param peersToUpdatePerInterval Number of peers to update every round
     */
	public Config(Duration memberFailureTimeout, Duration memberCleanupTimeout, 
			Duration UpdateFrequency, Duration failureDetectionFrequency, int peersToUpdatePerInterval) {
		MEMBER_FAILURE_TIMEOUT = memberFailureTimeout;
		MEMBER_CLEANUP_TIMEOUT = memberCleanupTimeout;
		
		PEERS_TO_UPDATE_PER_INTERVAL = peersToUpdatePerInterval;
		UPDATE_FREQUENCY = UpdateFrequency;
		FAILURE_DETECTION_FREQUENCY = failureDetectionFrequency;
		
	}
}
