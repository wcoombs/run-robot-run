
public class Rectangle extends Shape
{
	public Rectangle()
	{
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
}
