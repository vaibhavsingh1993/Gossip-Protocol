package main.java.gossip;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.Math; 
public class Node {

	int stepNumber = -1;
	String str = ""
	
	public Node( int steps) {
		this.stepNumber = steps;
	}

	public void updateStepNumber() {
		this.stepNumber++;
	}
	
	long[][] roundMessages = new long[5][2];
	long[] votes = new long[5]
	int currentStep;
	long currTime= System.currentTimeMillis();
	long endTime = currTime + 5000;
	while(System.currentTimeMillis()<end){
		//listen for messages
		//verify messages and then add the messages to the array roundMessages
		roundMessages = [[0,10],[1,5],[0,3],[0,10],[1,1]];
		votes = [0,1,0,0,1];
	}
	


	public int numZeros(int[] list){
		int count = 0;
		for (int i =0 ; i < list.length(); i++){
			if(list[i] == 0){
				count++;
			}	
		}	
		return count;
	}
	

	public int numOnes(int[] list){
                int count = 0;
                for (int i =0 ; i < list.length(); i++){
                        if(list[i] == 1){
                                count++;
                        }
                }
                return count;
        }
	if (stepNumber % 3 == 0){
		long numOfZeros = numZeros(votes);
		long numOfOnes = numOnes(votes);
		if (numOfZeros >= 2* votes.length()/3){
			str = "0*";			
		}
		if(numOfOnes >= 2*votes.length()/3){
			str = "1";
		}		
		else{
			str = "0";
		}
		
	}
	else if(stepNumber % 3 == 1){
		long numOfZeros = numZeros(votes);
                long numOfOnes = numOnes(votes);
                if (numOfZeros >= 2* votes.length()/3){
                        str = "0";             
                }
                if(numOfOnes >= 2*votes.length()/3){
                        str = "1*";
                }
                else{
                        str = "1";
                }
	}
	else{
		long numOfZeros = numZeros(votes);
                long numOfOnes = numOnes(votes);
                if (numOfZeros >= 2* votes.length()/3){
                        str = "0";  
                }
                if(numOfOnes >= 2*votes.length()/3){
                        str = "1";
                }
                else{
                        //write code to get coin genuinely tossed
			int b = Math.round((float)Math.random());
			str = Integer.toString(b);
                }
	}
	//create signature for message
	
	//send vote for the current step

}
