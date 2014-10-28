import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

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
  
/**
 * A skeleton for those {@link Client}s that correspond to clients on other computers.
 * @author Geoffrey Washburn &lt;<a href="mailto:geoffw@cis.upenn.edu">geoffw@cis.upenn.edu</a>&gt;
 * @version $Id: RemoteClient.java 342 2004-01-23 21:35:52Z geoffw $
 */

public class RemoteClient extends Client{
		
		/**
	     * The {@link MulticastSocket} object we use in Client class.
	     */
	    //private static MulticastSocket MS;
	    //private static InetAddress MCGroup;
	    //private static int MCPort;
	    
	    /**
	     * Parameters that records the start point and direction of this remote client.
	     */
	    public int StartX;
	    public int StartY;
	    public String StartDir;
	    
	    /**
	     * PID of this remote client.
	     */
	    public int PID;
	    
		/**
	     * The {@link Thread} object we use to run the client control code.
	     */
	    //private final Thread thread;
	    
	    /** 
	     * Flag to say whether the control thread should be running.
	     */
	    //private boolean active = false;
	    
	    /** 
         * Override the abstract {@link Client}'s registerMaze method so that we know when to start 
         * control thread.
         * @param maze The {@link Maze} that we are begin registered with.
         */
        public synchronized void registerMaze(Maze maze) {
                assert(maze != null);
                super.registerMaze(maze);

                // Get the control thread going.
               // active = true;
                //thread.start();
        }
        
        /** 
         * Override the abstract {@link Client}'s unregisterMaze method so we know when to stop the 
         * control thread. 
         */
        public synchronized void unregisterMaze() {
                // Signal the control thread to stop
                //active = false; 
                // Wait half a second for the thread to complete.
        	/*
                try {
                        thread.join(500);
                } catch(Exception e) {
                        // Shouldn't happen
                }
                */
                super.unregisterMaze();
        }
        
        /**
         * Create a remotely controlled {@link Client}.
         * @param name Name of this {@link RemoteClient}.
         */
        public RemoteClient(String name, String StartX, String StartY, String StartDir, int PID) {
                super(name);
                this.StartX = Integer.parseInt(StartX);
                this.StartY = Integer.parseInt(StartY);
                this.StartDir = StartDir;
                this.PID = PID;
               
					
        }
       
        /**
         * Move the client forward.
         * @return <code>true</code> if move was successful, otherwise <code>false</code>.
         */
        protected boolean forward() {
                return super.forward();
        }
        
        /**
         * Move the client backward.
         * @return <code>true</code> if move was successful, otherwise <code>false</code>.
         */
        protected boolean backup() {
                return super.backup();
        }
        
        /**
         * Turn the client ninety degrees counter-clockwise.
         */
        protected void turnLeft() {
                super.turnLeft();
        }
        
        /**
         * Turn the client ninety degrees clockwise.
         */
        protected void turnRight() {
                super.turnRight();
        }
        
        /**
         * Fire a projectile.
         * @return 
         */
        protected boolean fire() {
                return super.fire();
        }
}
