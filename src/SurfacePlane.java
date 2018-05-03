import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;

public class SurfacePlane extends Shape
{
	private RectanglePlane[][] surface;
	private final int ROWS;
	private final int COLS;
	private float maxX;
	private float minX;
	private float maxY;
	private float minY;

	public SurfacePlane(int givenRows, int givenCols)
	{
		this.ROWS = givenRows;
		this.COLS = givenCols;
		this.maxX = -10000;
		this.minX = 10000;
		this.maxY = -10000;
		this.minY = 10000;

		this.surface = new RectanglePlane[this.ROWS][this.COLS];

		for (int i = 0; i < this.ROWS; i++)
		{
			for (int j = 0; j < this.COLS; j++)
			{
				float[] corner = new float[] { -(this.ROWS / 2) + i, -(this.COLS / 2) + j };

				float[] v1 = new float[] { corner[0], corner[1] };
				float[] v2 = new float[] { corner[0], corner[1] + 1f };
				float[] v3 = new float[] { corner[0] + 1f, corner[1] + 1f };
				float[] v4 = new float[] { corner[0] + 1f, corner[1] };

				if (corner[0] > this.maxX || corner[0] < this.minX || corner[0] > this.maxY || corner[0] < this.minY)
					if (corner[0] > this.maxX)
						this.maxX = corner[0];
				if (corner[0] < this.minX)
					this.minX = corner[0];
				if (corner[0] > this.maxY)
					this.maxY = corner[0];
				if (corner[0] < this.minY)
					this.minY = corner[0];
				if (corner[1] > this.maxX || corner[1] < this.minX || corner[1] > this.maxY || corner[1] < this.minY)
					if (corner[1] > this.maxX)
						this.maxX = corner[1];
				if (corner[1] < this.minX)
					this.minX = corner[1];
				if (corner[1] > this.maxY)
					this.maxY = corner[1];
				if (corner[1] < this.minY)
					this.minY = corner[1];

				this.surface[i][j] = new RectanglePlane(v1, v2, v3, v4);
			}
		}
	}

	public float getMaxX()
	{
		return this.maxX;
	}

	public float getMinX()
	{
		return this.maxX;
	}

	public float getMaxY()
	{
		return this.maxX;
	}

	public float getMinY()
	{
		return this.maxX;
	}

	public void draw(GL2 gl, Texture texture)
	{
		texture.enable(gl);
		texture.bind(gl);

		for (int i = 0; i < surface.length; i++)
		{
			for (int j = 0; j < surface[i].length; j++)
			{
				gl.glBegin(GL2.GL_QUADS);
				gl.glTexCoord2f(0f, 0f);
				gl.glVertex3f(surface[i][j].getV1X(), -1f, surface[i][j].getV1Y());
				gl.glTexCoord2f(1f, 0f);
				gl.glVertex3f(surface[i][j].getV2X(), -1f, surface[i][j].getV2Y());
				gl.glTexCoord2f(1f, 1f);
				gl.glVertex3f(surface[i][j].getV3X(), -1f, surface[i][j].getV3Y());
				gl.glTexCoord2f(0f, 1f);
				gl.glVertex3f(surface[i][j].getV4X(), -1f, surface[i][j].getV4Y());
				gl.glEnd();
			}
		}

		texture.disable(gl);
	}
}
