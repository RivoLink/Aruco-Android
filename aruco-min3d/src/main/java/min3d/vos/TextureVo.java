package min3d.vos;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

/**
 * Contains the properties of a texture which can be assigned to an object.
 * An object can be assigned multiple TextureVo's by adding them to 
 * the Object3d's TextureList (usually up to just 2 w/ current Android hardware).
 * 
 *  The "textureEnvs" ArrayList defines what texture environment commands 
 *  will be sent to OpenGL for the texture. Typically, this needs to hold
 *  just one TextureEnvVo, but can hold an arbitrary number, for more
 *  complex 'layering' operations. 
 *  
 *  TODO: Allow for adding glTexEnvf commands (float instead of int)
 *  
 *  TODO: Ability to assign arbitrary UV lists per-TextureVo? (Non-trivial...)
 */
public class TextureVo 
{
	/**
	 * The texureId in the TextureManager that corresponds to an uploaded Bitmap
	 */
	public String textureId;
	
	/**
	 * Determines if U and V ("S" and "T" in OpenGL parlance) repeat, or are 'clamped'
	 * (Defaults to true, matching OpenGL's default setting)
	 */
	public boolean repeatU = true;
	public boolean repeatV = true;

	/**
	 * The U/V offsets for the texture (rem, normal range of U and V are 0 to 1)
	 */
	public float offsetU = 0;
	public float offsetV = 0;
	
	/**
	 * A list of TexEnvVo's that define how texture is composited in the output.
	 * Normally contains just one element.
	 */
	public ArrayList<TexEnvxVo> textureEnvs;

	//
	
	public TextureVo(String $textureId, ArrayList<TexEnvxVo> $textureEnvVo)
	{
		textureId = $textureId;
		textureEnvs = $textureEnvVo;
	}
	
	public TextureVo(String $textureId)
	{
		textureId = $textureId;
		textureEnvs = new ArrayList<TexEnvxVo>();
		textureEnvs.add( new TexEnvxVo());
	}
}
