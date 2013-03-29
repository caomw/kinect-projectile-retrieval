package finallab;

import java.util.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;

import april.util.*;
import april.jmat.*;
import april.vis.*;
import april.image.*;
import lcm.lcm.*;
import finallab.lcmtypes.*;

public class Projectile extends VisEventAdapter implements LCMSubscriber
{
 
	LCM lcm;
	ArrayList<double[]> aballs;
	ArrayList<double[]> pballs;
	int num_meas;				//number of measurements used in prediction
	int ball_i;
	double starttime;
	double[] v_not; 			//x,y,z initial velocities, used in model
	boolean can_calib;
	boolean released;
	boolean verbose;

	JFrame jf;
	VisWorld vw;
	VisLayer vl;
	VisCanvas vc;
	ParameterGUI pg;	
	
	Projectile()
	{
		//vis initializations
		jf = new JFrame("RobotGUI");
		vw = new VisWorld();
		vl = new VisLayer(vw);
		vc = new VisCanvas(vl);
		pg = new ParameterGUI();
		jf.setLayout(new BorderLayout());
		jf.add(vc, BorderLayout.CENTER);
		jf.add(pg, BorderLayout.SOUTH);
		jf.setSize(800,600);
		jf.setVisible(true);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		vl.addEventHandler(this);
		vl.cameraManager.uiLookAt(new double[] {-2.66075, 1.22066, 1.70393 },
					new double[] {1.75367, -0.06226,  0.00000 },
					new double[] {0.33377, -0.09695,  0.93766 }, true);
		VisWorld.Buffer vb = vw.getBuffer("Ground");
		vb.addBack(new VisChain(LinAlg.translate(0,0,-0.025),new VzBox(30,30,0.05,new VzMesh.Style(Color.darkGray))));
		vb.swap();

		
		// try {
		// 	this.lcm = LCM.getSingleton();
		// 	this.lcm.subscribe("6_BALL", this);
		// } catch (Exception ex) {
  //           System.out.println("Exception: " + ex);
  //       }
		
		
	
		//projectile initializations
		aballs = new ArrayList<double[]>();
		num_meas = 3;
		ball_i=-1;
		v_not = new double[3];
		can_calib = false;
		released = false;
		verbose = true;		
	}

	public void messageReceived(LCM lcm, String channel, LCMDataInputStream dins)
	{
		try
		{
			if(channel.equals("6_BALL"))
			{
				ball_t curr_ball = new ball_t(dins);
				
				double[] xyzt = new double[4];
				xyzt[0] = curr_ball.x;
				xyzt[1] = curr_ball.y;
				xyzt[2] = curr_ball.z;
				xyzt[3] = curr_ball.utime;

				aballs.add(xyzt);
				ball_i++;

				for (int i=0; i<4; i++)
					System.out.printf("balls[%d][%d]:%f\n",ball_i,aballs.get(ball_i)[i],i);
				System.out.println();
		
				if (!can_calib && (ball_i >= num_meas))
				{
					starttime = aballs.get(ball_i)[3];
					can_calib = true;
				} 
			
				if (!released && can_calib)
				{
					if (DetermineReleased())
					{
						released = true;
						starttime = aballs.get(ball_i)[3];
					}
				}

				//if (released)

		 	}
		 }
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public boolean DetermineReleased()
	{
		double prev_dt = aballs.get(ball_i-1)[3] - aballs.get(ball_i-2)[3];
		v_not[0] = (aballs.get(ball_i-1)[0] - aballs.get(ball_i-2)[0]) / prev_dt;
		v_not[1] = (aballs.get(ball_i-1)[1] - aballs.get(ball_i-2)[1]) / prev_dt;
		v_not[2] = (aballs.get(ball_i-1)[2] - aballs.get(ball_i-2)[2]) / prev_dt;		

		
		double cur_dt = aballs.get(ball_i)[3] - aballs.get(ball_i-1)[3];
		double[] predict_loc = Predict(cur_dt);
		
		double[] errors = new double[3];
		errors[0] = predict_loc[0]-aballs.get(ball_i)[0];
		errors[1] = predict_loc[1]-aballs.get(ball_i)[1];
		errors[2] = predict_loc[2]-aballs.get(ball_i)[2];

		if (verbose)
		{
			for (int i=0; i<3; i++)
			{
				System.out.printf("v_not[%d]: %f\n",i,v_not[i]);
				System.out.printf("predict_loc[%d]: %f\n",i,predict_loc[i]);
				System.out.printf("errors[%d]: %f\n",i,errors[i]);
			}
		}

		if ((Math.abs(errors[0]) < 0.1) && (Math.abs(errors[1]) < 0.1) && (Math.abs(errors[2]) < 0.1))
			return true;
		else
			return false;
	}

	public double[] Predict(double dt)
	{
		double[] predict_loc = new double[3];
		predict_loc[0] = aballs.get(ball_i)[0] + (v_not[0] * dt); //deltaX = Vo,x * dt
		predict_loc[1] = aballs.get(ball_i)[1] + (v_not[1] * dt) - 0.5*(9806000000000f)*dt*dt;
		predict_loc[2] = aballs.get(ball_i)[2] + (v_not[2] * dt); 	
		return predict_loc;
	}


	public static void main(String[] args) throws Exception
	{
		Projectile p = new Projectile();

	}
}













