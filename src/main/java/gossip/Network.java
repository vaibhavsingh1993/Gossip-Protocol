package main.java.gossip;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import java.security.PrivateKey;
import java.security.PublicKey;

import java.security.NoSuchAlgorithmException;


/**
 * Network emulates the low level network connection workflow.
 * Packets are sent and received as UDP packets using socket programming.
 *
 * @author  TTyler
 * @author  Vaibhav Singh
 * @author  Varun Madathil
 * @author  Wayne Chen
 * @version 1.1
 * @since   2019-02-26
 */
public class Network {
	
	private DatagramSocket socket;
	
	private byte[] receiveBuffer = new byte[1024];
	private DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length); 

	private PrivateKey privKey;

	private PublicKey[] publicKey = new PublicKey[4];
	
	public Network(int portToListenOn) {
		try {
			// TODO: Would need to map the public keys to a list of all public keys instead of 
            // a single public key
		    privKey = CryptoUtil.getPrivate("/home/vavasing/Desktop/workspace/CSC724_ADS/circom_tut/Gossip-Protocol/src/key.der");
			publicKey[0] = CryptoUtil.getPublic("/home/vavasing/Desktop/workspace/CSC724_ADS/circom_tut/Gossip-Protocol/src/public1.der");
			publicKey[1] = CryptoUtil.getPublic("/home/vavasing/Desktop/workspace/CSC724_ADS/circom_tut/Gossip-Protocol/src/public2.der");
			publicKey[2] = CryptoUtil.getPublic("/home/vavasing/Desktop/workspace/CSC724_ADS/circom_tut/Gossip-Protocol/src/public3.der");
			publicKey[3] = CryptoUtil.getPublic("/home/vavasing/Desktop/workspace/CSC724_ADS/circom_tut/Gossip-Protocol/src/public4.der");			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			socket = new DatagramSocket(portToListenOn);
		} catch (SocketException e) {			
			Gossip.logger.log("Could not initialize datagram socket because: " + e.getMessage());
		}
	}
	
	public void sendMessage(Member target, String message) {
        //System.out.println(message + " of type String is being sent out.");
        String signedMessage = new String();
        try {
        	signedMessage = CryptoUtil.getSignature(message, privKey);
        } catch (Exception ex) {
        	ex.printStackTrace();
        }

        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
		try {
			ObjectOutput oo = new ObjectOutputStream(bStream); 
			oo.writeObject(message + ","+ signedMessage);
			System.out.println("Message sent: " + message);
			oo.close();
		} catch (IOException e) {
			Gossip.logger.log("Could not send " + message + " to [" + target.getSocketAddress() + "] because: " + e.getMessage());
		}
		byte[] serializedMessage = bStream.toByteArray();
		sendMessage(target, serializedMessage);
	}
	
	public void sendMessage(Member target, Member message) {
		ByteArrayOutputStream bStream = new ByteArrayOutputStream();
		try {
			ObjectOutput oo = new ObjectOutputStream(bStream); 
			oo.writeObject(message);
			oo.close();
		} catch (IOException e) {
			Gossip.logger.log("Could not send " + message.getNetworkMessage() + " to [" + target.getSocketAddress() + "] because: " + e.getMessage());
		}

		byte[] serializedMessage = bStream.toByteArray();
		
		sendMessage(target, serializedMessage);
		
		
	}
	
	private void sendMessage(Member target, byte[] data) {
		DatagramPacket packet = new DatagramPacket(data, data.length, target.getInetAddress(), target.getPort());
		try {
			socket.send(packet);
		} catch (IOException e) {
			Gossip.logger.log("Fatal error trying to send: " + packet + " to [" + target.getSocketAddress() + "]");
			e.printStackTrace();
			
			System.exit(-1);
		}
	}
	
	public Object receiveMessage() {
		try {
			socket.receive(receivePacket);
			ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(receivePacket.getData()));
            Object rcvdObj = null;
            try {
                rcvdObj = iStream.readObject();
            } catch (ClassNotFoundException e) {
				Gossip.logger.log("Error calling readObject() on an ObjectInputStream object: " + e.getMessage());
			}
	    System.out.println("Message received: " + rcvdObj.toString().substring(0,rcvdObj.toString().lastIndexOf(",")));
            System.out.println(rcvdObj.toString());
            iStream.close();
            return rcvdObj;
            /*Member message = null;
			try {
				message = (Member) iStream.readObject();
			} catch (ClassNotFoundException e) {
				Gossip.logger.log("Error casting Gossip network data to Member class because: " + e.getMessage());
			}
			iStream.close();
			
			return message;*/			
		} catch (IOException e) {
			Gossip.logger.log("Could not properly receive message because: " + e.getMessage());
		}
		return null;
	}
	
}
