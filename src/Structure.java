import com.jogamp.opengl.GL2;

import com.jogamp.opengl.util.texture.Texture;

class Structure extends Shape
{
	// this array can include other structures...
	private Shape[] contents;
	private float[][] positions;

	public Structure(Shape[] contents, float[][] positions)
	{
		super();
		init(contents, positions);
	}

	public Structure(String filename, Shape[] contents, float[][] positions)
	{
		// super(filename); // commented out to prevent drawing the default shape every time
		init(contents, positions);
	}

	private void init(Shape[] contents, float[][] positions)
	{
		this.contents = new Shape[contents.length];
		this.positions = new float[positions.length][3];
		System.arraycopy(contents, 0, this.contents, 0, contents.length);
		for (int i = 0; i < positions.length; i++)
		{
			System.arraycopy(positions[i], 0, this.positions[i], 0, 3);
		}
	}

	public void draw(GL2 gl)
	{
		// super.draw(gl); // commented out to prevent drawing the default shape every time
		for (int i = 0; i < contents.length; i++)
		{
			gl.glPushMatrix();
			gl.glTranslatef(positions[i][0], positions[i][1], positions[i][2]);
			contents[i].draw(gl);
			gl.glPopMatrix();
		}
	}

	public void drawTexture(GL2 gl, Texture texture)
	{
		// super.draw(gl); // commented out to prevent drawing the default shape every time
		for (int i = 0; i < contents.length; i++)
		{
			gl.glPushMatrix();
			gl.glTranslatef(positions[i][0], positions[i][1], positions[i][2]);
			contents[i].drawTexture(gl, texture);
			gl.glPopMatrix();
		}
	}

	public Shape[] getContents()
	{
		return this.contents;
	}
}