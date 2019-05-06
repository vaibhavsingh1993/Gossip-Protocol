package main.java.gossip;
import java.net.InetSocketAddress;

/**
 * GossipUpdater interface for creating a gossip member
 *
 * @author  TTyler
 * @version 1.0
 * @since   2019-02-26
 */
public interface GossipUpdater {
	
	void update(InetSocketAddress address);
}
