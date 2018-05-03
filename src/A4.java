
//------------------------------------------------------
//
// NAME           : William Coombs
// STUDENT NUMBER : 6852347
// COURSE         : COMP 3490
// INSTRUCTOR     : John Braico
// ASSIGNMENT     : #4
// QUESTION       : All
//
// REMARKS: The purpose of this program is to allow the
//          robot from A3 to move through a fully-3D
//          texture-mapped and animated world.
//
//------------------------------------------------------
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.*;
import com.jogamp.opengl.glu.*;

import com.jogamp.opengl.util.awt.ImageUtil;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

public class A4 implements GLEventListener, KeyListener, MouseListener, MouseMotionListener
{
	public static final boolean TRACE = false;

	public static final String WINDOW_TITLE = "A4: William Coombs";
	public static final int INITIAL_WIDTH = 640;
	public static final int INITIAL_HEIGHT = 640;

	private static final GLU glu = new GLU();

	private static final String TEXTURE_PATH = "resources/";
	public static final String INPUT_PATH_NAME = "resources/";

	// TODO: change this
	public static final String[] TEXTURE_FILES = { "TexturesCom_FloorsVarious0051_1_seamless_S.jpg",
			"TexturesCom_Cliffs0256_1_seamless_S.jpg", "lava_texture_by_paintevil-d3it3og.png", "browncloud_up.jpg", "browncloud_dn.jpg",
			"browncloud_ft.jpg", "browncloud_bk.jpg", "browncloud_lf.jpg", "browncloud_rt.jpg",
			"TexturesCom_BunkerLeaking0087_1_seamless_S.jpg", "TexturesCom_Lights0026_S.jpg", "TexturesCom_Electronics0035_S.jpg",
			"TexturesCom_AudioEquipment0058_M.jpg", "TexturesCom_Electronics0036_S.jpg", "black.jpg" };

	public static void main(String[] args)
	{
		final JFrame frame = new JFrame(WINDOW_TITLE);

		frame.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				if (TRACE)
					System.out.println("closing window '" + ((JFrame) e.getWindow()).getTitle() + "'");
				System.exit(0);
			}
		});

		final GLProfile profile = GLProfile.get(GLProfile.GL2);
		final GLCapabilities capabilities = new GLCapabilities(profile);
		final GLCanvas canvas = new GLCanvas(capabilities);
		try
		{
			Object self = self().getConstructor().newInstance();
			self.getClass().getMethod("setup", new Class[] { GLCanvas.class }).invoke(self, canvas);
			canvas.addGLEventListener((GLEventListener) self);
			canvas.addKeyListener((KeyListener) self);
			canvas.addMouseListener((MouseListener) self);
			canvas.addMouseMotionListener((MouseMotionListener) self);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		canvas.setSize(INITIAL_WIDTH, INITIAL_HEIGHT);

		frame.getContentPane().add(canvas);
		frame.pack();
		frame.setVisible(true);

		canvas.requestFocusInWindow();

		if (TRACE)
			System.out.println("-> end of main().");
	}

	private static Class<?> self()
	{
		// This ugly hack gives us the containing class of a static method
		return new Object()
		{
		}.getClass().getEnclosingClass();
	}

	/*** Instance variables and methods ***/
	// TODO: Add instance variables here
	private Texture[] textures;
	private SkyBox skybox;
	private Obstacle[] obstacles;
	private SurfacePlane floor;
	private SurfacePlane leftWall;
	private SurfacePlane rightWall;
	private SurfacePlane backWall;
	private SurfacePlane frontWall;
	private Structure robot;
	private Structure[] leftUpperArm;
	private Structure[] leftLowerArm;
	private Structure[] rightUpperArm;
	private Structure[] rightLowerArm;
	private Structure[] leftUpperLeg;
	private Structure[] leftLowerLeg;
	private Structure[] rightUpperLeg;
	private Structure[] rightLowerLeg;
	private String direction;

	private boolean jumpUp;
	private boolean jumpDown;
	private boolean freeLook;
	private boolean smoothShading;

	private final float JUMP_MAX = 3f;
	private final float JUMP_INCREMENT = 0.025f;
	private float[] fogColor = new float[] { 0.2f, 0.2f, 0.225f, 0.0f };
	private float fogDensity = 0.2f;
	private float step = 0.01f;
	private float jumpY;

	private final int[] LIGHTS = { GL2.GL_LIGHT0, GL2.GL_LIGHT1, GL2.GL_LIGHT2, GL2.GL_LIGHT3, GL2.GL_LIGHT4 };
	private final int WALK_ROTATION_INCREMENT = 5;
	private final int NUMBER_OF_RINGS_PER = 6;
	private int[][] pillarPositions = { { -7, -7 }, { -7, 7 }, { 7, 7 }, { 7, -7 } };
	private int[] lastDragPosition;
	private int view;
	private int rotate;
	private int rotateIncrement;
	private int rotateR;
	private int rotateIncrementR;
	private int walkRotation;
	private int freeLookX;
	private int freeLookY;
	private int ringRotation;
	private float robotX;
	private float robotZ;
	private float robotY;

	public void reset()
	{
		System.out.println("Hit an obstacle or a wall. Resetting...");

		jumpUp = false;
		jumpDown = false;
		jumpY = 0;
		robotX = 0;
		robotZ = 0;
		robotY = 0;
		step = 0.01f;
		walkRotation = 0;
	}

	public boolean checkObjects()
	{
		boolean result = false;

		for (int i = 0; i < obstacles.length && !result; i++)
		{
			float obstacleX = obstacles[i].getTranslateX();
			float obstacleZ = obstacles[i].getTranslateZ();
			float obstacleMaxX = obstacleX + 0.5f;
			float obstacleMinX = obstacleX - 0.5f;
			float obstacleMaxZ = obstacleZ + 0.5f;
			float obstacleMinZ = obstacleZ - 0.5f;
			float obstacleY = 1f;

			if (robotX >= obstacleMinX && robotX <= obstacleMaxX && robotZ >= obstacleMinZ && robotZ <= obstacleMaxZ && robotY < obstacleY)
				result = true;
		}

		return result;
	}

	public boolean checkWalls()
	{
		boolean result = false;

		if (robotX < -10.75 || robotX > 10.75 || robotZ < -10.75 || robotZ > 10.75)
			result = true;

		return result;
	}

	public boolean checkPillars()
	{
		boolean result = false;

		for (int i = 0; i < pillarPositions.length && !result; i++)
		{
			int currentPillarX = pillarPositions[i][0];
			int currentPillarZ = pillarPositions[i][1];
			float currentPillarMaxX = currentPillarX + 1.5f;
			float currentPillarMinX = currentPillarX - 1.5f;
			float currentPillarMaxZ = currentPillarZ + 1.5f;
			float currentPillarMinZ = currentPillarZ - 1.5f;

			if (robotX >= currentPillarMinX && robotX <= currentPillarMaxX && robotZ >= currentPillarMinZ && robotZ <= currentPillarMaxZ)
				result = true;
		}

		return result;
	}

	public boolean collision()
	{
		boolean result = checkWalls();

		if (!result)
			result = checkObjects();

		if (!result)
			result = checkPillars();

		return result;
	}

	public void drawWorld(GL2 gl)
	{
		gl.glPushMatrix();
		gl.glEnable(GL2.GL_DEPTH_CLAMP_NV);
		drawSky(gl);
		gl.glDisable(GL2.GL_DEPTH_CLAMP_NV);
		drawFloor(gl);
		drawLeftWall(gl);
		drawRightWall(gl);
		drawBackWall(gl);
		drawFrontWall(gl);
		drawObstacles(gl);
		drawGLUObstacles(gl);
		gl.glPopMatrix();
	}

	public void updateRobotPosition()
	{
		robotZ += (float) Math.cos(Math.toRadians(walkRotation)) * step;
		robotX += (float) Math.sin(Math.toRadians(walkRotation)) * step;
		robotY = jumpY;
	}

	public void drawSky(GL2 gl)
	{
		gl.glPushMatrix();
		gl.glEnable(GL2.GL_FRONT_FACE);
		gl.glDisable(GL2.GL_CULL_FACE);
		skybox.drawSky(gl);
		gl.glEnable(GL2.GL_CULL_FACE);
		gl.glDisable(GL2.GL_FRONT_FACE);
		gl.glPopMatrix();
	}

	public void drawLeftWall(GL2 gl)
	{
		gl.glPushMatrix();
		gl.glTranslated(10, 0, 0);
		gl.glRotated(90, 0, 0, 1);
		leftWall.draw(gl, textures[1]);
		gl.glPopMatrix();
	}

	public void drawRightWall(GL2 gl)
	{
		gl.glPushMatrix();
		gl.glTranslated(-10, 0, 0);
		gl.glRotated(90, 0, 0, 1);
		gl.glRotated(180, 1, 0, 0);
		rightWall.draw(gl, textures[1]);
		gl.glPopMatrix();
	}

	public void drawBackWall(GL2 gl)
	{
		gl.glPushMatrix();
		gl.glTranslated(0, 0, -10);
		gl.glRotated(90, 0, 0, 1);
		gl.glRotated(90, 1, 0, 0);
		backWall.draw(gl, textures[1]);
		gl.glPopMatrix();
	}

	public void drawFrontWall(GL2 gl)
	{
		gl.glPushMatrix();
		gl.glTranslated(0, 0, 10);
		gl.glRotated(90, 0, 0, 1);
		gl.glRotated(90, 1, 0, 0);
		gl.glRotated(180, 1, 0, 0);
		frontWall.draw(gl, textures[1]);
		gl.glPopMatrix();
	}

	public void drawFloor(GL2 gl)
	{
		gl.glPushMatrix();
		gl.glTranslatef(0f, 0.75f, 0f);
		floor.draw(gl, textures[0]);
		gl.glPopMatrix();
	}

	public void drawObstacles(GL2 gl)
	{
		for (int i = 0; i < obstacles.length; i++)
		{
			gl.glPushMatrix();
			gl.glTranslatef(obstacles[i].getTranslateX(), 0, obstacles[i].getTranslateZ());
			obstacles[i].drawTexture(gl);
			gl.glPopMatrix();
		}
	}

	public void drawCylinder(GL2 gl)
	{
		for (int i = 0; i < pillarPositions.length; i++)
		{
			gl.glPushMatrix();
			gl.glRotated(90, 1, 0, 0);
			gl.glTranslatef(pillarPositions[i][0], pillarPositions[i][1], -7f);

			GLUquadric cylinder = glu.gluNewQuadric();
			glu.gluQuadricTexture(cylinder, true);
			textures[9].enable(gl);
			textures[9].bind(gl);
			glu.gluCylinder(cylinder, 1, 1, 10, 30, 1);
			textures[9].disable(gl);

			gl.glPopMatrix();
		}
	}

	public void drawRing(GL2 gl)
	{
		for (int i = 0; i < pillarPositions.length; i++)
		{
			for (int j = 0; j < NUMBER_OF_RINGS_PER; j++)
			{
				gl.glPushMatrix();
				gl.glRotated(90, 1, 0, 0);
				gl.glTranslatef(pillarPositions[i][0], pillarPositions[i][1], -j - 1);

				if (j % 2 == 0)
					gl.glRotated(ringRotation, 0, 0, 1);
				else
					gl.glRotated(-ringRotation, 0, 0, 1);

				textures[11].enable(gl);
				textures[11].bind(gl);

				gl.glPushMatrix();
				gl.glRotated(180, 1, 0, 0);
				GLUquadric top = glu.gluNewQuadric();
				glu.gluQuadricTexture(top, true);
				glu.gluDisk(top, 0, 1.5, 30, 5);
				gl.glPopMatrix();

				GLUquadric ring = glu.gluNewQuadric();
				glu.gluQuadricTexture(ring, true);
				glu.gluCylinder(ring, 1.5, 1.5, 0.5, 30, 1);

				gl.glPushMatrix();
				gl.glTranslatef(0f, 0f, 0.5f);
				GLUquadric bottom = glu.gluNewQuadric();
				glu.gluQuadricTexture(bottom, true);
				glu.gluDisk(bottom, 0, 1.5, 30, 5);
				gl.glPopMatrix();

				textures[11].disable(gl);
				gl.glPopMatrix();
			}
		}
	}

	public void drawGLUObstacles(GL2 gl)
	{
		gl.glPushMatrix();
		drawCylinder(gl);
		drawRing(gl);
		gl.glPopMatrix();
	}

	public void drawShadow(GL2 gl)
	{
		gl.glPushMatrix();
		gl.glTranslatef(0f, -0.9f - jumpY, 0.1f);
		gl.glRotated(90, 1, 0, 0);
		gl.glRotated(180, 1, 0, 0);

		if (jumpY != 0)
			gl.glScalef(1f - (jumpY / (JUMP_MAX + 1)), 1f - (jumpY / (JUMP_MAX + 1)), 1f);

		// i couldn't figure out how to simply fill in a glu primitive with a
		// solid color, so I texture-mapped black onto it instead
		textures[14].enable(gl);
		textures[14].bind(gl);
		GLUquadric shadow = glu.gluNewQuadric();
		glu.gluQuadricTexture(shadow, true);
		glu.gluDisk(shadow, 0, 1, 15, 5);
		textures[14].disable(gl);
		gl.glPopMatrix();
	}

	public void drawRobot(GL2 gl)
	{
		gl.glPushMatrix();
		drawShadow(gl);
		drawLeftArm(gl);
		drawRightArm(gl);
		drawLeftLeg(gl);
		drawRightLeg(gl);
		gl.glPopMatrix();

		gl.glPushMatrix();
		robot.drawTexture(gl, textures[12]);
		gl.glPopMatrix();
	}

	public void drawLeftUpperArm(GL2 gl)
	{
		gl.glPushMatrix();
		leftUpperArm[0].drawTexture(gl, textures[13]);
		gl.glPopMatrix();
	}

	public void drawLeftLowerArm(GL2 gl)
	{
		gl.glPushMatrix();
		gl.glTranslatef(0f, 1f, 0f);
		gl.glRotated(rotate + rotateIncrement * 2, 1f, 0f, 0f);
		gl.glTranslatef(0f, -1f, 0f);
		leftLowerArm[0].drawTexture(gl, textures[13]);
		gl.glPopMatrix();
	}

	public void drawLeftArm(GL2 gl)
	{
		gl.glPushMatrix();
		gl.glTranslatef(0f, 2f, 0f);
		gl.glRotated(rotate, 1, 0, 0);
		gl.glTranslatef(0f, -2f, 0f);
		drawLeftUpperArm(gl);
		drawLeftLowerArm(gl);
		gl.glPopMatrix();
	}

	public void drawRightUpperArm(GL2 gl)
	{
		gl.glPushMatrix();
		rightUpperArm[0].drawTexture(gl, textures[13]);
		gl.glPopMatrix();
	}

	public void drawRightLowerArm(GL2 gl)
	{
		gl.glPushMatrix();
		gl.glTranslatef(0f, 1f, 0f);
		gl.glRotated(rotateR + rotateIncrementR * 2, 1f, 0f, 0f);
		gl.glTranslatef(0f, -1f, 0f);
		rightLowerArm[0].drawTexture(gl, textures[13]);
		gl.glPopMatrix();
	}

	public void drawRightArm(GL2 gl)
	{
		gl.glPushMatrix();
		gl.glTranslatef(0f, 2f, 0f);
		gl.glRotated(rotateR, 1, 0, 0);
		gl.glTranslatef(0f, -2f, 0f);
		drawRightUpperArm(gl);
		drawRightLowerArm(gl);
		gl.glPopMatrix();
	}

	public void drawLeftUpperLeg(GL2 gl)
	{
		gl.glPushMatrix();
		leftUpperLeg[0].drawTexture(gl, textures[13]);
		gl.glPopMatrix();
	}

	public void drawLeftLowerLeg(GL2 gl)
	{
		gl.glPushMatrix();
		gl.glRotated(rotateR * 0.5f + rotateIncrementR, 1f, 0f, 0f);
		leftLowerLeg[0].drawTexture(gl, textures[13]);
		gl.glPopMatrix();
	}

	public void drawLeftLeg(GL2 gl)
	{
		gl.glPushMatrix();
		gl.glTranslatef(0f, 1f, 0f);
		gl.glRotated(rotateR, 1, 0, 0);
		gl.glTranslatef(0f, -1f, 0f);
		drawLeftUpperLeg(gl);
		drawLeftLowerLeg(gl);
		gl.glPopMatrix();
	}

	public void drawRightUpperLeg(GL2 gl)
	{
		gl.glPushMatrix();
		rightUpperLeg[0].drawTexture(gl, textures[13]);
		gl.glPopMatrix();
	}

	public void drawRightLowerLeg(GL2 gl)
	{
		gl.glPushMatrix();
		gl.glRotated(rotate * 0.5f + rotateIncrement, 1f, 0f, 0f);
		rightLowerLeg[0].drawTexture(gl, textures[13]);
		gl.glPopMatrix();
	}

	public void drawRightLeg(GL2 gl)
	{
		gl.glPushMatrix();
		gl.glTranslatef(0f, 1f, 0f);
		gl.glRotated(rotate, 1, 0, 0);
		gl.glTranslatef(0f, -1f, 0f);
		drawRightUpperLeg(gl);
		drawRightLowerLeg(gl);
		gl.glPopMatrix();
	}

	public void setup(final GLCanvas canvas)
	{
		// Called for one-time setup
		if (TRACE)
			System.out.println("-> executing setup()");

		new Timer().scheduleAtFixedRate(new TimerTask()
		{
			public void run()
			{
				canvas.repaint();
			}
		}, 1000, 1000 / 60);

		// TODO: Add code here
		floor = new SurfacePlane(40, 40);
		leftWall = new SurfacePlane(5, 40);
		rightWall = new SurfacePlane(5, 40);
		backWall = new SurfacePlane(5, 40);
		frontWall = new SurfacePlane(5, 40);
		obstacles = new Obstacle[12];

		final int NUMBER_OF_SHAPES = 3;
		Shape[] robotShapes = new Shape[NUMBER_OF_SHAPES];
		Shape[] leftUpperArmShape = new Shape[1];
		Shape[] leftLowerArmShape = new Shape[1];
		Shape[] rightUpperArmShape = new Shape[1];
		Shape[] rightLowerArmShape = new Shape[1];
		Shape[] leftUpperLegShape = new Shape[1];
		Shape[] leftLowerLegShape = new Shape[1];
		Shape[] rightUpperLegShape = new Shape[1];
		Shape[] rightLowerLegShape = new Shape[1];

		float[][] robotShapesPositions = new float[NUMBER_OF_SHAPES][3];
		robotShapes[0] = new Shape(); // body
		robotShapes[1] = new Rectangle(); // left shoulder
		robotShapes[2] = new Rectangle(); // right shoulder

		leftUpperArmShape[0] = new Shape(INPUT_PATH_NAME + "diamond.obj"); // left upper-arm
		leftLowerArmShape[0] = new Shape(INPUT_PATH_NAME + "diamond.obj"); // left lower-arm
		rightUpperArmShape[0] = new Shape(INPUT_PATH_NAME + "diamond.obj"); // right upper-arm
		rightLowerArmShape[0] = new Shape(INPUT_PATH_NAME + "diamond.obj"); // right lower-arm

		leftUpperLegShape[0] = new Shape(INPUT_PATH_NAME + "diamond.obj"); // left upper-leg
		leftLowerLegShape[0] = new Shape(INPUT_PATH_NAME + "diamond.obj"); // left lower-leg
		rightUpperLegShape[0] = new Shape(INPUT_PATH_NAME + "diamond.obj"); // right upper-leg
		rightLowerLegShape[0] = new Shape(INPUT_PATH_NAME + "diamond.obj"); // right lower-leg

		float[][] leftUpperArmPosition = new float[1][3];
		float[][] leftLowerArmPosition = new float[1][3];
		float[][] rightUpperArmPosition = new float[1][3];
		float[][] rightLowerArmPosition = new float[1][3];
		float[][] leftUpperLegPosition = new float[1][3];
		float[][] leftLowerLegPosition = new float[1][3];
		float[][] rightUpperLegPosition = new float[1][3];
		float[][] rightLowerLegPosition = new float[1][3];

		robotShapesPositions[0] = new float[] { 0.0f, 1.25f, 0.0f }; // body
		robotShapesPositions[1] = new float[] { -0.91f, 1.83f, 0.0f }; // left shoulder
		robotShapesPositions[2] = new float[] { 1.07f, 1.83f, 0.0f }; // right shoulder

		leftUpperArmPosition[0] = new float[] { -1f, 1.1f, 0.0f }; // left upper-arm
		leftLowerArmPosition[0] = new float[] { -1f, 0.4f, 0.0f }; // left lower-arm
		rightUpperArmPosition[0] = new float[] { 1f, 1.1f, 0.0f }; // right upper-arm
		rightLowerArmPosition[0] = new float[] { 1f, 0.4f, 0.0f }; // right lower-arm

		leftUpperLegPosition[0] = new float[] { -0.4f, -0.1f, 0.0f }; // left upper-leg
		leftLowerLegPosition[0] = new float[] { -0.4f, -0.8f, 0.0f }; // left lower-leg
		rightUpperLegPosition[0] = new float[] { 0.4f, -0.1f, 0.0f }; // right upper-leg
		rightLowerLegPosition[0] = new float[] { 0.4f, -0.8f, 0.0f }; // right lower-leg

		leftUpperArm = new Structure[1];
		leftLowerArm = new Structure[1];
		leftUpperArm[0] = new Structure(leftUpperArmShape, leftUpperArmPosition);
		leftLowerArm[0] = new Structure(leftLowerArmShape, leftLowerArmPosition);

		rightUpperArm = new Structure[1];
		rightLowerArm = new Structure[1];
		rightUpperArm[0] = new Structure(rightUpperArmShape, rightUpperArmPosition);
		rightLowerArm[0] = new Structure(rightLowerArmShape, rightLowerArmPosition);

		leftUpperLeg = new Structure[1];
		leftLowerLeg = new Structure[1];
		leftUpperLeg[0] = new Structure(leftUpperLegShape, leftUpperLegPosition);
		leftLowerLeg[0] = new Structure(leftLowerLegShape, leftLowerLegPosition);

		rightUpperLeg = new Structure[1];
		rightLowerLeg = new Structure[1];
		rightUpperLeg[0] = new Structure(rightUpperLegShape, rightUpperLegPosition);
		rightLowerLeg[0] = new Structure(rightLowerLegShape, rightLowerLegPosition);

		robot = new Structure(robotShapes, robotShapesPositions);

		rotate = -30;
		rotateIncrement = -1;

		rotateR = 15;
		rotateIncrementR = 1;

		jumpUp = false;
		jumpDown = false;
		jumpY = 0;

		lastDragPosition = null;
		freeLookX = 0;
		freeLookY = 0;
		freeLook = false;

		smoothShading = true;

		if (smoothShading)
			System.out.println("Smooth shading. Lighting effects turned on");
		else
			System.out.println("Flat shading. Lighting effects turned off");

		ringRotation = 0;

		robotX = 0;
		robotZ = 0;
		robotY = 0;

		view = 0;
		walkRotation = 0;
	}

	@Override
	public void init(GLAutoDrawable drawable)
	{
		// Called when the canvas is (re-)created - use it for initial GL setup
		if (TRACE)
			System.out.println("-> executing init()");

		final GL2 gl = drawable.getGL().getGL2();

		textures = new Texture[TEXTURE_FILES.length];
		try
		{
			for (int i = 0; i < TEXTURE_FILES.length; i++)
			{
				File infile = new File(TEXTURE_PATH + TEXTURE_FILES[i]);
				BufferedImage image = ImageIO.read(infile);
				ImageUtil.flipImageVertically(image);
				textures[i] = TextureIO.newTexture(AWTTextureIO.newTextureData(gl.getGLProfile(), image, false));
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		// TODO: Add code here
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glDepthFunc(GL2.GL_LEQUAL);
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		gl.glEnable(GL2.GL_CULL_FACE);
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_TEXTURE_2D);

		gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, new float[] { 0.2f, 0.2f, 0.2f, 1.0f }, 0);
		gl.glEnable(GL2.GL_COLOR_MATERIAL);
		gl.glColorMaterial(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE);
		gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, new float[] { 0.8f, 0.8f, 0.8f, 0.8f }, 0);
		gl.glMaterialf(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 80.0f);

		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, new float[] { 0.7f, 0.7f, 0.7f, 1.0f }, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, new float[] { 0.1f, 0.1f, 0.1f, 1.0f }, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, new float[] { 0.0f, 0.0f, 0.0f, 1.0f }, 0);

		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, new float[] { 1.0f, 1.0f, 1.0f, 0.0f }, 0); // directional
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, new float[] { 1.0f, 1.0f, 1.0f, 1.0f }, 0);

		gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_POSITION, new float[] { 0.0f, -1.0f, 1.0f, 1.0f }, 0); // positional
		gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_DIFFUSE, new float[] { 1.0f, 0.5f, 0.5f, 1.0f }, 0); // red matte
		gl.glLightf(GL2.GL_LIGHT2, GL2.GL_CONSTANT_ATTENUATION, 0.1f);
		gl.glLightf(GL2.GL_LIGHT2, GL2.GL_LINEAR_ATTENUATION, 0.2f);
		gl.glLightf(GL2.GL_LIGHT2, GL2.GL_QUADRATIC_ATTENUATION, 0.5f);

		gl.glLightfv(GL2.GL_LIGHT3, GL2.GL_POSITION, new float[] { 0.0f, 0.0f, -2.0f, 0.5f }, 0); // directional
		gl.glLightfv(GL2.GL_LIGHT3, GL2.GL_SPECULAR, new float[] { 0.1f, 0.1f, 0.1f, 0.1f }, 0);

		gl.glLightfv(GL2.GL_LIGHT4, GL2.GL_POSITION, new float[] { -0.1f, 1.0f, 2f, 1.0f }, 0); // positional
		gl.glLightfv(GL2.GL_LIGHT4, GL2.GL_DIFFUSE, new float[] { 1.0f, 0.5f, 0.7f, 1.0f }, 0);
		gl.glLightfv(GL2.GL_LIGHT4, GL2.GL_SPOT_DIRECTION, new float[] { 0.2f, -1.0f, -0.4f }, 0);
		gl.glLightf(GL2.GL_LIGHT4, GL2.GL_SPOT_EXPONENT, 4.0f);
		gl.glLightf(GL2.GL_LIGHT4, GL2.GL_SPOT_CUTOFF, 3.0f); // 1/2 the angle of the cone

		obstacles[0] = new Obstacle(-7, 5, textures[2]);
		obstacles[1] = new Obstacle(-1, 6, textures[2]);
		obstacles[2] = new Obstacle(-3, 3, textures[2]);
		obstacles[3] = new Obstacle(-4, -2, textures[2]);
		obstacles[4] = new Obstacle(-6, -6, textures[2]);
		obstacles[5] = new Obstacle(-1, -7, textures[2]);
		obstacles[6] = new Obstacle(1, -5, textures[2]);
		obstacles[7] = new Obstacle(3, 2, textures[2]);
		obstacles[8] = new Obstacle(5, 4, textures[2]);
		obstacles[9] = new Obstacle(5, 7, textures[2]);
		obstacles[10] = new Obstacle(5, -3, textures[2]);
		obstacles[11] = new Obstacle(5, -6, textures[2]);
		skybox = new SkyBox(textures[3], textures[4], textures[5], textures[6], textures[7], textures[8]);
	}

	@Override
	public void display(GLAutoDrawable drawable)
	{
		// Draws the display
		if (TRACE)
			System.out.println("-> executing display()");

		final GL2 gl = drawable.getGL().getGL2();

		if (smoothShading)
		{
			gl.glShadeModel(GL2.GL_SMOOTH);

			for (int i = 0; i < LIGHTS.length; i++)
			{
				gl.glEnable(LIGHTS[i]);
			}
		}
		else
		{
			gl.glShadeModel(GL2.GL_FLAT);

			for (int i = 0; i < LIGHTS.length; i++)
			{
				gl.glDisable(LIGHTS[i]);
			}
		}

		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		gl.glMatrixMode(GL2.GL_MODELVIEW);

		// TODO: Replace with your drawing code
		if (collision())
		{
			reset();
		}

		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();

		// 50 for the far lets us see from one corner of the world to the other without any cutoff
		glu.gluPerspective(60, 1, 1, 50);

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();

		if (direction != null && !jumpUp && !jumpDown)
		{
			if (direction.equals("left"))
				walkRotation += WALK_ROTATION_INCREMENT;
			else if (direction.equals("right"))
				walkRotation -= WALK_ROTATION_INCREMENT;
			else if (direction.equals("up"))
				if (step < 0.1)
					step += 0.002;
				else if (step > -0.1)
					step -= 0.002;

			direction = null;
		}

		if (view == 0)
		{
			gl.glTranslatef(robotX, -0.75f - (jumpY / 2.5f), robotZ);
			gl.glRotated(180, 0, 1, 0);
			gl.glTranslatef(robotX, 0f, robotZ);

			// restrict freelook in the y-plane
			if (freeLookY < 0 || freeLookY > 200)
				if (freeLookY < 0)
					freeLookY = 0;
				else if (freeLookY > 200)
					freeLookY = 200;

			// restrict freelook in the x-plane
			if (freeLookX < -325 || freeLookX > 325)
				if (freeLookX < -325)
					freeLookX = -325;
				else if (freeLookX > 325)
					freeLookX = 325;

			gl.glRotated(freeLookY / 4, 1, 0, 0);
			gl.glRotated(freeLookX / 4, 0, 1, 0);

			gl.glRotated(-walkRotation, 0, 1, 0);
			gl.glTranslatef(-robotX, 0f, -robotZ);
		}
		else if (view == 1)
		{
			gl.glTranslatef(robotX, -1f, robotZ - 4f);
			gl.glRotated(180, 0, 1, 0);
			gl.glTranslatef(robotX, 0f, robotZ);

			// restrict freelook in the y-plane
			if (freeLookY < -180 || freeLookY > 20)
				if (freeLookY < -180)
					freeLookY = -180;
				else if (freeLookY > 20)
					freeLookY = 20;

			gl.glRotated(freeLookY / 3, 1, 0, 0);
			gl.glRotated(freeLookX / 3, 0, 1, 0);

			gl.glRotated(-walkRotation, 0, 1, 0);
			gl.glTranslatef(-robotX, 0f, -robotZ);
		}

		// TODO: Add your drawing code
		// draw the fog
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);

		gl.glEnable(GL2.GL_FOG);
		gl.glFogfv(GL2.GL_FOG_COLOR, fogColor, 0);
		gl.glFogf(GL2.GL_FOG_DENSITY, fogDensity);
		gl.glFogi(GL2.GL_FOG_MODE, GL2.GL_EXP);

		// draw the world
		gl.glPushMatrix();
		drawWorld(gl);
		gl.glPopMatrix();

		// move the robot
		gl.glPushMatrix();
		gl.glTranslatef(robotX, 0.0f, robotZ);
		gl.glRotated(walkRotation, 0, 1, 0);

		// draw the head
		gl.glPushMatrix();
		gl.glScalef(0.25f, 0.25f, 0.25f);
		gl.glTranslatef(0.0f, 2.5f + jumpY, 0.0f);
		gl.glRotated(-90, 1, 0, 0);
		GLUquadric head = glu.gluNewQuadric();
		glu.gluQuadricTexture(head, true);
		textures[10].enable(gl);
		textures[10].bind(gl);
		glu.gluQuadricDrawStyle(glu.gluNewQuadric(), GLU.GLU_POINT);
		glu.gluSphere(head, 0.5f, 20, 20);
		textures[10].disable(gl);
		gl.glPopMatrix();

		// draw the rest of the robot
		gl.glScalef(0.25f, 0.25f, 0.25f);
		gl.glTranslatef(0.0f, 0f + jumpY, 0.0f);
		drawRobot(gl);
		gl.glPopMatrix();

		rotate += rotateIncrement;
		rotateR += rotateIncrementR;

		if (rotate > 15)
			rotateIncrement = -1;
		if (rotate < -30)
			rotateIncrement = 1;
		if (rotateR > 15)
			rotateIncrementR = -1;
		if (rotateR < -30)
			rotateIncrementR = 1;

		if (jumpUp || jumpDown)
		{
			if (jumpUp)
			{
				jumpY += JUMP_INCREMENT;

				if (jumpY >= JUMP_MAX)
				{
					jumpDown = true;
					jumpUp = false;
				}
			}
			if (jumpDown)
			{
				jumpY -= JUMP_INCREMENT;

				if (jumpY <= 0)
					jumpDown = false;
			}
		}

		ringRotation = (ringRotation + 1) % 360;

		updateRobotPosition();
	}

	@Override
	public void dispose(GLAutoDrawable drawable)
	{
		// Called when the canvas is destroyed (reverse anything from init)
		if (TRACE)
			System.out.println("-> executing dispose()");
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{
		// Called when the canvas has been resized
		// Note: glViewport(x, y, width, height) has already been called so don't bother if that's what you want
		if (TRACE)
			System.out.println("-> executing reshape(" + x + ", " + y + ", " + width + ", " + height + ")");

		final GL2 gl = drawable.getGL().getGL2();

		gl.glMatrixMode(GL2.GL_PROJECTION);

		// TODO: use a perspective projection instead
		// 50 for the far lets us see from one corner of the world to the other without any cutoff
		glu.gluPerspective(60, 1, 1, 50);
	}

	@Override
	public void keyTyped(KeyEvent e)
	{

	}

	@Override
	public void keyReleased(KeyEvent e)
	{
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		// TODO Change this however you like
		direction = null;
		if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyChar() == 'a')
			direction = "left";
		else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyChar() == 'd')
			direction = "right";
		else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyChar() == 'w')
			direction = "up";
		else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyChar() == 's')
			direction = "down";

		if (direction != null)
		{
			System.out.println("Direction key pressed: " + direction);
			((GLCanvas) e.getSource()).repaint();
		}
		if (e.getKeyChar() == ' ')
		{
			if (!jumpUp && !jumpDown)
			{
				System.out.println("Space bar: jump!");
				jumpUp = true;
			}
		}
		if (e.getKeyChar() == '\n')
		{
			if (!freeLook)
			{
				System.out.println("Enter: switch view");
				view = (view + 1) % 2;
				((GLCanvas) e.getSource()).repaint();
			}
			else
				System.out.println("Currently in free-look mode. Release mouse before switching views.");
		}
		if (e.getKeyChar() == 'f')
		{
			smoothShading = !smoothShading;

			if (smoothShading)
				System.out.println("Smooth shading. Lighting effects turned on");
			else
				System.out.println("Flat shading. Lighting effects turned off");

			((GLCanvas) e.getSource()).repaint();
		}
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		// TODO: use this or mouse moved for free look
		int clickX = e.getX();
		int clickY = INITIAL_HEIGHT - e.getY();
		int deltaX = clickX - lastDragPosition[0];
		int deltaY = clickY - lastDragPosition[1];

		if (freeLook)
		{
			freeLookX += deltaX;
			freeLookY += deltaY;
		}

		lastDragPosition[0] = clickX;
		lastDragPosition[1] = clickY;
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{
	}

	@Override
	public void mouseClicked(MouseEvent arg0)
	{
	}

	@Override
	public void mouseEntered(MouseEvent arg0)
	{
	}

	@Override
	public void mouseExited(MouseEvent arg0)
	{
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		// TODO: you may need this
		int clickX = e.getX();
		int clickY = INITIAL_HEIGHT - e.getY();

		freeLook = true;
		System.out.println("Entering free-look mode (release mouse to exit)");

		if (lastDragPosition == null)
			lastDragPosition = new int[] { clickX, clickY };
	}

	@Override
	public void mouseReleased(MouseEvent arg0)
	{
		freeLook = false;
		System.out.println("Exiting free-look mode");

		if (lastDragPosition != null)
		{
			lastDragPosition = null;
			freeLookX = 0;
			freeLookY = 0;
		}
	}
}
