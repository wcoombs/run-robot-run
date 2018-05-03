import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;

public class SkyBox extends Shape
{
	private Texture textureTop;
	private Texture textureBottom;
	private Texture textureFront;
	private Texture textureBack;
	private Texture textureLeft;
	private Texture textureRight;

	public SkyBox(Texture top, Texture bot, Texture front, Texture back, Texture left, Texture right)
	{
		this.textureTop = top;
		this.textureBottom = bot;
		this.textureFront = front;
		this.textureBack = back;
		this.textureLeft = left;
		this.textureRight = right;

		init();

		vertices.add(new float[] { -1.0f, -1.0f, 1.0f });
		vertices.add(new float[] { 1.0f, -1.0f, 1.0f });
		vertices.add(new float[] { 1.0f, 1.0f, 1.0f });
		vertices.add(new float[] { -1.0f, 1.0f, 1.0f });
		vertices.add(new float[] { -1.0f, -1.0f, -1.0f });
		vertices.add(new float[] { 1.0f, -1.0f, -1.0f });
		vertices.add(new float[] { 1.0f, 1.0f, -1.0f });
		vertices.add(new float[] { -1.0f, 1.0f, -1.0f });

		faces.add(new Face(new int[] { 0, 1, 2, 3 }, new float[] { 1.0f, 0.0f, 0.0f }));
		faces.add(new Face(new int[] { 0, 3, 7, 4 }, new float[] { 1.0f, 1.0f, 0.0f }));
		faces.add(new Face(new int[] { 7, 6, 5, 4 }, new float[] { 1.0f, 0.5f, 0.0f }));
		faces.add(new Face(new int[] { 2, 1, 5, 6 }, new float[] { 0.0f, 1.0f, 0.0f }));
		faces.add(new Face(new int[] { 3, 2, 6, 7 }, new float[] { 0.0f, 0.0f, 1.0f }));
		faces.add(new Face(new int[] { 1, 0, 4, 5 }, new float[] { 0.0f, 1.0f, 1.0f }));
	}

	public void drawSky(GL2 gl)
	{
		int i = 0;

		for (Face f : faces)
		{
			gl.glPushMatrix();

			// fix the orientation on some of the faces
			if (i == 1 || i == 3)
				gl.glRotated(90, 1, 0, 0);
			if (i == 2)
				gl.glRotated(180, 0, 0, 1);
			if (i == 5)
				gl.glRotated(180, 0, 1, 0);

			// draw the sky faces
			if (i == 0)
				f.drawTexture(gl, vertices, textureRight, true);
			else if (i == 1)
				f.drawTexture(gl, vertices, textureBack, true);
			else if (i == 2)
				f.drawTexture(gl, vertices, textureLeft, true);
			else if (i == 3)
				f.drawTexture(gl, vertices, textureFront, true);
			else if (i == 4)
				f.drawTexture(gl, vertices, textureTop, true);
			else if (i == 5)
				f.drawTexture(gl, vertices, textureBottom, true);

			i++;

			gl.glPopMatrix();
		}
	}
}
