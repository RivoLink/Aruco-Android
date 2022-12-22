package min3d.objectPrimitives;

import min3d.Utils;
import min3d.core.Object3dContainer;
import min3d.vos.Color4;

public class Rectangle extends Object3dContainer
{
	public Rectangle(float $width, float $height, int $segsW, int $segsH)
	{
		this($width, $height, $segsW, $segsH, new Color4(255, 0, 0, 255));
	}
	
	public Rectangle(float $width, float $height, int $segsW, int $segsH, Color4 color)
	{
		super(4 * $segsW * $segsH, 2 * $segsW * $segsH);

		int row, col;

		float w = $width / $segsW;
		float h = $height / $segsH;

		float width5 = $width/2f;
		float height5 = $height/2f;
		
		// Add vertices
		
		for (row = 0; row <= $segsH; row++)
		{
			for (col = 0; col <= $segsW; col++)
			{
				this.vertices().addVertex(
					(float)col*w - width5, (float)row*h - height5,0f,	
					(float)col/(float)$segsW, 1 - (float)row/(float)$segsH,	
					0,0,1f,	
					color.r, color.g, color.b, color.a
				);
			}
		}
		
		// Add faces
		
		int colspan = $segsW + 1;
		
		for (row = 1; row <= $segsH; row++)
		{
			for (col = 1; col <= $segsW; col++)
			{
				int lr = row * colspan + col;
				int ll = lr - 1;
				int ur = lr - colspan;
				int ul = ur - 1;
				Utils.addQuad(this, ul,ur,lr,ll);
			}
		}
	}
}
