/*
Copyright (C) 2004 Geoffrey Alan Washburn
     
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.
     
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
     
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
USA.
*/
 
import java.util.Random;
import java.util.Vector;
import java.lang.Runnable;


/**
 * A very naive implementation of a computer controlled {@link LocalClient}.  Basically
 * it stumbles about and shoots.  
 * @author Geoffrey Washburn &lt;<a href="mailto:geoffw@cis.upenn.edu">geoffw@cis.upenn.edu</a>&gt;
 * @version $Id: RobotClient.java 345 2004-01-24 03:56:27Z geoffw $
 */
 
public class RobotClient extends LocalClient implements Runnable {

        /**
         * Random number generator so that the robot can be
         * "non-deterministic".
         */ 
         private final Random randomGen = new Random();

         /**
          * The {@link Thread} object we use to run the robot control code.
          */
         private final Thread thread;
         
        /** 
         * Flag to say whether the control thread should be
         * running.
         */
        private boolean active = false;
        
        /**
         * PID assigned for this robot.
         */
        public final int PID;
        
        /**
         * Create a pointer pointing to the running local client process.
         */
        private LocalClient lc;
        
        /**
         * A queue that is used to wait for ACKs from the others.
         * There are also some related parameters.
         */
        public ConfirmQueue[] confirmQueue = new ConfirmQueue[PACKET_LIMIT];
        
        /**
         * Create sequence numbers and counters used in this thread.
         */
        
        /**
         * A boolean var used to see if the confirm queue is idle or not.
         */
        private boolean idle = true;
        
        /**
         * Create a computer controlled {@link LocalClient}.
         * @param name The name of this {@link RobotClient}.
         */
        public RobotClient(String name, int PID, LocalClient lc) {
                super(name);
                assert(name != null);
                this.PID = PID;
                this.lc = lc;
                this.seq_mine = 0;
                this.local_curr = 1;
                // Create our thread
                thread = new Thread(this);
        }
   
        /** 
         * Override the abstract {@link Client}'s registerMaze method so that we know when to start 
         * control thread.
         * @param maze The {@link Maze} that we are begin registered with.
         */
        public synchronized void registerMaze(Maze maze) {
                assert(maze != null);
                super.registerMaze(maze);

                // Get the control thread going.
                active = true;
                thread.start();
        }
        
        /** 
         * Override the abstract {@link Client}'s unregisterMaze method so we know when to stop the 
         * control thread. 
         */
        public synchronized void unregisterMaze() {
                // Signal the control thread to stop
                active = false; 
                // Wait half a second for the thread to complete.
                try {
                        thread.join(500);
                } catch(Exception e) {
                        // Shouldn't happen
                }
                super.unregisterMaze();
        }
    
        /** 
         * This method is the control loop for an active {@link RobotClient}. 
         */
        public void run() {
        		// Join the game.
        		seq_mine++;
        		lc.send(this, "JOIN", null, null, 0);
        		
                // Put a spiffy message in the console
                Mazewar.consolePrintLn("Robot client \"" + this.getName() + "\" activated.");

                // Loop while we are active
                while(active) {
                        // Try to move forward
                        if(!forward()) {
                                // If we fail...
                                if(randomGen.nextInt(3) == 1) {
                                        // turn left!
                                        turnLeft();
                                } else {
                                        // or perhaps turn right!
                                        turnRight();
                                }
                        }

                        // Shoot at things once and a while.
                        if(randomGen.nextInt(10) == 1) {
                                fire();
                        }
                        
                        // Sleep so the humans can possibly compete.
                        try {
                                thread.sleep(1000);
                        } catch(Exception e) {
                                // Shouldn't happen.
                        }
                }
        }
        /**
         * Locally enqueue the movement, in order to be prepared to be sent in a certain order.
         */
        private void localEnqueue(String movement, int seq){
        	// Check if the queue is idle; if yes, send this packet right now.
        	boolean old_status = idle;
        	
        	// Insert node into proper position.
        	ConfirmQueue node = new ConfirmQueue();
        	node.movement = movement;
        	confirmQueue[seq] = node;
        	idle = false;
        	System.out.println("RRRRRRRRRRR!!!!!! Robot Enqueuing!!!!!!");
        	if(old_status){
        		local_curr = seq;
        		localDequeue();
        	}
        }
        
        /**
         * Locally dequeue the movement queue (as in confirmQueue).
         */
        private void localDequeue(){
        	// Find next node in the confirm queue and send the movement as a packet.
        	while(confirmQueue[local_curr] != null){
        		lc.PID_to_seq[this.PID] = local_curr;
        		// Span to wait for the newly joined client.
        		send(this, "MOVE", confirmQueue[local_curr].movement, null, local_curr);
        		//confirmQueue[seq] = null;
        		System.out.println("RRRRRRRRRRRR!!!!!! Robot Dequeuing!!!!!!");
        		local_curr++;
        	}
        	idle = true;

        }
        
        /**
         * A helper function for a client to make a movement
         * @param client can be local or remote
         * @param movement
         */
        protected void makeMovement(Client client, String movement){
        	super.makeMovement(this, movement);
        }
        
        /**
         * Rejoin the game when died.
         */
        protected void rejoin(){
        	assert(maze != null);
        	seq_mine ++;
        	send(this, "REJOIN", null, null, 0);
        }
        
        /**
         * Move the client forward.
         * @return <code>true</code> if move was successful, otherwise <code>false</code>.
         */
        protected boolean forward() {
                assert(maze != null);
                
                if(maze.testClientForward(this)) {
                	if(!only_me_and_rbc){
                		seq_mine++;
                		localEnqueue("FORWARD", seq_mine);
                	}
                	else{
                		maze.moveClientForward(this);
                		super.notifyMoveForward();
                	}
                    return true;
                } else {
                        return false;
                }
        }
        
        /**
         * Move the client backward.
         * @return <code>true</code> if move was successful, otherwise <code>false</code>.
         */
        protected boolean backup() {
                assert(maze != null);

                if(maze.testClientBackward(this)) {
                	if(!only_me_and_rbc){
                		seq_mine++;
                		localEnqueue("BACKWARD", seq_mine);
                	}
                	else{
                		maze.moveClientBackward(this);
                		super.notifyMoveBackward();
                	}
                		
                    return true;
                } else {
                        return false;
                }
        }
        
        /**
         * Turn the client ninety degrees counter-clockwise.
         */
        protected void turnLeft() {
        	if(!only_me_and_rbc){
        		seq_mine++;
        		localEnqueue("LEFT", seq_mine);
        	}
        	else
        		super.notifyTurnLeft();
        }
        
        /**
         * Turn the client ninety degrees clockwise.
         */
        protected void turnRight() {
        	if(!only_me_and_rbc){
        		seq_mine++;
        		localEnqueue("RIGHT", seq_mine);
        	}
        	else
        		super.notifyTurnRight();
        }
        
        /**
         * Fire a projectile.
         * @return <code>true</code> if a projectile was successfully launched, otherwise <code>false</code>.
         */
        protected boolean fire() {
                assert(maze != null);

                if(maze.testFire(this)) {
                	if(!only_me_and_rbc){
                		seq_mine ++;
                		localEnqueue("FIRE", seq_mine);
                	}
                	else{
                		super.notifyFire();
                	}
                	return true;
                } else {
                        return false;
                }
        }
}
