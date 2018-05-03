import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;

public class Obstacle extends Shape
{
	private float translateX;
	private float translateZ;
	private Texture texture;

	public Obstacle(float x, float z, Texture texture)
	{
		this.translateX = x;
		this.translateZ = z;
		this.texture = texture;

		init();

		vertices.add(new float[] { -0.325f, -0.1675f, 0.1675f }); // v1
		vertices.add(new float[] { 0.1675f, -0.1675f, 0.1675f }); // v2
		vertices.add(new float[] { 0.1675f, 0.1675f, 0.1675f }); // v3
		vertices.add(new float[] { -0.325f, 0.1675f, 0.1675f }); // v4
		vertices.add(new float[] { -0.325f, -0.1675f, -0.1675f }); // v5
		vertices.add(new float[] { 0.1675f, -0.1675f, -0.1675f }); // v6
		vertices.add(new float[] { 0.1675f, 0.1675f, -0.1675f }); // v7
		vertices.add(new float[] { -0.325f, 0.1675f, -0.1675f }); // v8

		faces.add(new Face(new int[] { 0, 1, 2, 3 }, new float[] { 1.0f, 0.0f, 0.0f }));
		faces.add(new Face(new int[] { 0, 3, 7, 4 }, new float[] { 1.0f, 1.0f, 0.0f }));
		faces.add(new Face(new int[] { 7, 6, 5, 4 }, new float[] { 1.0f, 0.5f, 0.0f }));
		faces.add(new Face(new int[] { 2, 1, 5, 6 }, new float[] { 0.0f, 1.0f, 0.0f }));
		faces.add(new Face(new int[] { 3, 2, 6, 7 }, new float[] { 0.0f, 0.0f, 1.0f }));
		faces.add(new Face(new int[] { 1, 0, 4, 5 }, new float[] { 0.0f, 1.0f, 1.0f }));
	}

	public void drawTexture(GL2 gl)
	{
		int i = 0;

		for (Face f : faces)
		{
			gl.glPushMatrix();

			if (i == 1 || i == 3)
				gl.glRotated(90, 1, 0, 0);

			f.drawTexture(gl, vertices, texture, false);
			gl.glPopMatrix();

			i++;
		}
	}

	public float getTranslateX()
	{
		return this.translateX;
	}

	public float getTranslateZ()
	{
		return this.translateZ;
	}
}
