package finallab;

import java.lang.Math;
import java.util.*;
import java.io.*;
import java.awt.Point;

import lcm.lcm.LCM;
import lcm.lcm.LCMDataInputStream;
import lcm.lcm.LCMSubscriber;

import april.util.*;
import april.jmat.*;

import finallab.lcmtypes.*;

public class CatchController implements LCMSubscriber
{
	Projectile predictor;
	BallDetector viewer;
	boolean display = true;
	boolean logs = false;
	boolean started = false;

	final long TURNINGSCALE = (long)((.2)*1000000000.0);
	final long MOVEMENTSCALE = (long)((1.0)*1000000000.0);
	final double BOT_DIST_FROM_KINECT_X = -.92;
	final double BOT_DIST_FROM_KINECT_Y = .61;
	final double BOT_THETA = Math.PI/2;//Math.atan2(BOT_DIST_FROM_KINECT_Y,BOT_DIST_FROM_KINECT_X);
	LCM  lcm;
	//LCM recieve;
	
	Object ballLock;

	CatchController(boolean _display, boolean _logs)
	{
		
		display = _display;
		logs = _logs;
		predictor = new Projectile(_display);
		ballLock = new Object();
		if(!logs)
		{
			viewer = new BallDetector(true);
			viewer.start();
		}
		try{
			this.lcm = new LCM("udpm://239.255.76.67:7667?ttl=1");
		}
		catch(IOException e){
			lcm = LCM.getSingleton();
		}
		

		//recieve = LCM.getSingleton();
		//receive = lcm;
		lcm.subscribe("6_BALL", this);	
		lcm.subscribe("6_RESET", this);
		if (display) {
			lcm.subscribe("6_WAYPOINT", this);
			lcm.subscribe("6_POSE", this);
			lcm.subscribe("6_SCORE_HUMAN", this);
			lcm.subscribe("6_SCORE_ROBOT", this);
			lcm.subscribe("6_SCORE_RESET", this);	
		}
	}

	public Point3D convertToPointRobotNonMat(double[] point)
	{
		double xDist = -BOT_DIST_FROM_KINECT_X + point[0];
		double yDist = -BOT_DIST_FROM_KINECT_Y + point[1];
		return new Point3D(xDist,yDist,0);
	}
	
	public Point3D convertToPointRobot(double[] point)
	{
		double [][] robo_point_array = {{point[0]}, {point[2]}, {0}, {1}};
		double [][] trans_mat = {{1,0,0,BOT_DIST_FROM_KINECT_X},{0,1,0,BOT_DIST_FROM_KINECT_Y},{0,0,1,0},{0,0,0,1}};
		Matrix global_point_mat = new Matrix(trans_mat).times(new Matrix(LinAlg.rotateY(BOT_THETA))).times(new Matrix(robo_point_array));
		return new Point3D(global_point_mat.get(0, 0), global_point_mat.get(1, 0), 0);
	}

	Point3D determineBounceCatch(ArrayList<Parabola> bounces)
	{
		if((bounces == null) || (bounces.size() == 0))
			return null;
		int i = 0;
		for(Parabola bounce: bounces)
		{
			double [] point = bounce.pred_landing;
			System.out.println("Points x y z :" + point[0] + " " + point[1] + " " + point[2]);
			long timeToMove = 0;
			//convert from points of kinect to points in front of robot
			Point3D landing = convertToPointRobotNonMat(point);
			if(i == 1)
				return landing;
			/*double angle = Math.atan2(landing.y,landing.x);
			//if rotation is required then add to time .2 is scale factor for turning .2 sec per radian
			if(Math.abs(angle) > Math.PI/6)
				timeToMove = (long)(angle*TURNINGSCALE);
			double distance = Math.sqrt((landing.y*landing.y)+(landing.x*landing.x));
			//add time to timeToMove based on distance traveled moves 1 second per meter
			timeToMove += (long)(MOVEMENTSCALE*distance);
			long destinationTime = System.nanoTime();
			destinationTime += (long)(timeToMove)*1000000000;
			if(destinationTime < (long)(point[3]))
				return i; */
			i++;
		}
		return null;
	}

	public void catchStateMachine()
	{
		ArrayList<Parabola> bounces;
		//start up video stuff
		//obtain projectile stuff
		//go to catchmode
		int state = 0;
		int nextState = 0;
		ball_t ball;
		Point3D newWayPoint = new Point3D(0.0,0.0,0.0);
		System.out.println("waiting for landing point");
		do {
			
			bounces = predictor.getParabolas();
		}
		
		while(bounces == null || bounces.size() == 0 || !bounces.get(0).valid);
		System.out.println("done waiting for bounces");
		double[][] startingBounces = new double[2][bounces.size()];
		for(int i = 0; i < bounces.size(); i++)
		{
			startingBounces[0][i] = bounces.get(i).pred_landing[0];
			startingBounces[1][i] = bounces.get(i).pred_landing[1];
		}
		while(true)
		{
			
			// System.out.println("x:"+ball.x+ " y:" +ball.y+ " z:" +ball.z);
			bounces = predictor.getParabolas();
			Point3D land;
			
			switch(state)
			{
				//waiting state
				//waiting for signal of endpoint from projectile
				//once recieved points determine weather good to grab on first bounce second bounce etc...
				case 0:
					// determine if able to catch on first bounce or second bounce
					land = determineBounceCatch(bounces);
					if(land == null)
					{
						nextState = 0;
						continue;
					}
					if(land.x != newWayPoint.x || land.y != newWayPoint.y || land.z != newWayPoint.z)
					{
						//bot takes waypoints looking down positive x
						xyt_t spot = new xyt_t();
						spot.utime = TimeUtil.utime();
						spot.xyt[0] = land.y;
						spot.xyt[1] = -land.x;
						spot.xyt[2] = 0d;
						spot.goFast = true;
						newWayPoint = land.clone();
						// go to point at bounce index
						if (!logs) {
							lcm.publish("6_WAYPOINT",spot);
							System.out.println("sending waypoint - LX: " + spot.xyt[0] + ", LY: " + spot.xyt[1] + "  (" + System.currentTimeMillis() + ")");
						}
						if (display)
							predictor.drawRobotEnd(new Point3D(BOT_DIST_FROM_KINECT_X,BOT_DIST_FROM_KINECT_Y,0.0),spot.xyt);
						
//						for(int i = 0; i < bounces.size(); i++)
//						{
//							double[] endPoint = bounces.get(i).pred_landing;
//							double xDiff = startingBounces[0][i] -endPoint[0], yDiff = startingBounces[1][i] - endPoint[i];
//							System.out.println("Difference From Starting in Bounce " + i + " x: " +xDiff+ " y: " +yDiff);
															
//						}
					}
					else {
						System.out.println("waypoints are the same");
					}
					//to continuously send waypoints of updated position of bounce index 0
					 nextState = 0;
					//to send the waypoint once when first calculated
//					nextState = 1;
				break;
				
				//retrieve state
				//go catch at point	
				case 1:
					//keep updating point with better trajectory
					//check to see if panda bot has reached position
					//if(pose distance is < distance to travel)
						//goToWayPoint(bounces.get(bounceIndex));
				break;

				//deliver state
				case 2:
					//go to point of human interaction 
					//goToWayPoint(humanPoint)
				break;
				
				//deliver_wait
				case 3: 
					//wait for human to take out ball
				break;
				
				//return
				case 4:
					//go to point of origin
				break;
				default:
					//error state
				break;

			}
			state = nextState;
			System.out.println("waiting for ball");
			synchronized(ballLock) {
				try {
					ballLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void blockGameStateMachine()
	{
		int state = 0;
		while(true)
		{
			switch(state)
			{
				//waiting state
				case 0:
				break;
				//retrieve state
				//go catch at point
				case 1:
				break;
				//determine if blocked
				case 2:
				break;
				//go back to previous point
				case 3:
				break;
				default:
				break;

			}
		}
	}


	public static void main(String[] args)
	{
		
		boolean logs = false;
		boolean display = true;
		for(int i = 0; i < args.length; i++)
		{
			if(args[i].equals("log"))
				logs = true;
			if(args[i].equals("speed"))
				display = false;
			if(args[i].equals("DEMO"))
			{
				@SuppressWarnings("unused")
				CatchController demo = new CatchController(true, true);
				while(true);
			}
		}
		CatchController cc = new CatchController(display, logs);
		cc.catchStateMachine();
	}

	@Override
	public void messageReceived(LCM lcm, String channel, LCMDataInputStream dins) {
		if (channel.equals("6_BALL")) {
			System.out.println("got ball from detector (" + System.currentTimeMillis() + ")");
			ball_t ball = null;
			try {
				ball = new ball_t(dins);
//				ball.y += KINECT_HEIGHT;
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(!started)
			{
				started = true;
				xyt_t spot = new xyt_t();
				spot.utime = TimeUtil.utime();
				spot.xyt[0] = 1d;
				spot.xyt[1] = 0.0d;
				spot.xyt[2] = 0.0d;
				spot.goFast = true;
				// go forward to save time
				lcm.publish("6_WAYPOINT",spot);
			}
			predictor.update(ball);
			synchronized (ballLock) {
				ballLock.notify();
			}
		}
		else if (channel.equals("6_RESET")) {
			predictor.reset();
			xyt_t home = new xyt_t();
			home.utime = TimeUtil.utime();
			home.xyt[0] = 0.0d;
			home.xyt[1] = 0.0d;
			home.xyt[2] = 0.0d;
			// go home
			lcm.publish("6_WAYPOINT",home);
			started = false;
			predictor.robotTraj.clear();
		}
		else if (channel.equals("6_WAYPOINT")) {
			try {
				xyt_t point = new xyt_t(dins);
				predictor.drawRobotEnd(new Point3D(BOT_DIST_FROM_KINECT_X, BOT_DIST_FROM_KINECT_Y, 0), point.xyt);
			} catch (Exception e) {
				System.out.println("6_waypoint translation error catchcontroller");
			}
		}
		else if(channel.equals("6_POSE"))
		{
			try
			{
				bot_status_t curr_bot_status = new bot_status_t(dins);
				double _y = curr_bot_status.xyt[1];
				curr_bot_status.xyt[1] = curr_bot_status.xyt[0] + BOT_DIST_FROM_KINECT_Y;
				curr_bot_status.xyt[0] = -_y + BOT_DIST_FROM_KINECT_X;  
				predictor.drawRobot(curr_bot_status);
			}
			catch(Exception e)
			{
				System.out.println("Dins coding error 6_POSE");
			}
		}
		else if(channel.equals("6_SCORE_HUMAN"))
		{
			predictor.scoreBoard.addToHuman();
		}
		else if(channel.equals("6_SCORE_ROBOT"))
		{
			predictor.scoreBoard.addToRobot();
		}
		else if(channel.equals("6_SCORE_RESET"))
		{
			predictor.scoreBoard.clearScoreboard();	
		}
		
	}
		
}

	