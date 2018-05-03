import java.util.ArrayList;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;

class Face
{
	private int[] indices;
	private float[] colour;

	public Face(int[] indices, float[] colour)
	{
		this.indices = new int[indices.length];
		this.colour = new float[colour.length];
		System.arraycopy(indices, 0, this.indices, 0, indices.length);
		System.arraycopy(colour, 0, this.colour, 0, colour.length);
	}

	public void drawTexture(GL2 gl, ArrayList<float[]> vertices, Texture texture, boolean v4Flag)
	{
		texture.enable(gl);
		texture.bind(gl);

		int count = 0;

		if (indices.length == 3)
		{
			gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
			gl.glBegin(GL2.GL_TRIANGLES);

			for (int i : indices)
			{
				if (count == 0)
					gl.glTexCoord2f(0f, 0f);
				else if (count == 1)
					gl.glTexCoord2f(1f, 0f);
				else if (count == 2)
					gl.glTexCoord2f(1f, 1f);

				if (v4Flag)
					gl.glVertex4f(vertices.get(i)[0], vertices.get(i)[1], vertices.get(i)[2], 0);
				else
					gl.glVertex3f(vertices.get(i)[0], vertices.get(i)[1], vertices.get(i)[2]);

				count++;
			}
		}
		else if (indices.length == 4)
		{
			gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
			gl.glBegin(GL2.GL_QUADS);

			for (int i : indices)
			{
				if (count == 0)
					gl.glTexCoord2f(0f, 0f);
				else if (count == 1)
					gl.glTexCoord2f(1f, 0f);
				else if (count == 2)
					gl.glTexCoord2f(1f, 1f);
				else if (count == 3)
					gl.glTexCoord2f(0f, 1f);

				if (v4Flag)
					gl.glVertex4f(vertices.get(i)[0], vertices.get(i)[1], vertices.get(i)[2], 0);
				else
					gl.glVertex3f(vertices.get(i)[0], vertices.get(i)[1], vertices.get(i)[2]);

				count++;
			}
		}

		gl.glEnd();
		texture.disable(gl);
	}

	public void draw(GL2 gl, ArrayList<float[]> vertices, boolean useColour)
	{
		if (useColour)
		{
			if (colour.length == 3)
				gl.glColor3f(colour[0], colour[1], colour[2]);
			else
				gl.glColor4f(colour[0], colour[1], colour[2], colour[3]);
		}

		if (indices.length == 1)
		{
			gl.glBegin(GL2.GL_POINTS);
		}
		else if (indices.length == 2)
		{
			gl.glBegin(GL2.GL_LINES);
		}
		else if (indices.length == 3)
		{
			gl.glBegin(GL2.GL_TRIANGLES);
		}
		else if (indices.length == 4)
		{
			gl.glBegin(GL2.GL_QUADS);
		}
		else
		{
			gl.glBegin(GL2.GL_POLYGON);
		}

		for (int i : indices)
		{
			gl.glVertex3f(vertices.get(i)[0], vertices.get(i)[1], vertices.get(i)[2]);
		}

		gl.glEnd();
	}
}
