
public class RectanglePlane
{
	private float[] v1;
	private float[] v2;
	private float[] v3;
	private float[] v4;

	public RectanglePlane(float[] v1, float[] v2, float[] v3, float[] v4)
	{
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
		this.v4 = v4;
	}

	public float getV1X()
	{
		return this.v1[0];
	}

	public float getV1Y()
	{
		return this.v1[1];
	}

	public float getV2X()
	{
		return this.v2[0];
	}

	public float getV2Y()
	{
		return this.v2[1];
	}

	public float getV3X()
	{
		return this.v3[0];
	}

	public float getV3Y()
	{
		return this.v3[1];
	}

	public float getV4X()
	{
		return this.v4[0];
	}

	public float getV4Y()
	{
		return this.v4[1];
	}
}
