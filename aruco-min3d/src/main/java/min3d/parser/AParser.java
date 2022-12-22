package min3d.parser;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import min3d.Min3d;
import min3d.Shared;
import min3d.Utils;
import min3d.animation.AnimationObject3d;
import min3d.core.Object3dContainer;
import min3d.vos.Color4;
import min3d.vos.Number3d;
import min3d.vos.Uv;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.Log;

/**
 * Abstract parser class with basic parsing functionality.
 * 
 * @author dennis.ippel
 *
 */
public abstract class AParser implements IParser {
	protected Resources resources;
	protected String resourceID;
	protected String packageID;
	protected String currentMaterialKey;
	protected ArrayList<ParseObjectData> parseObjects;
	protected ParseObjectData co;
	protected boolean firstObject;
	protected TextureAtlas textureAtlas;
	protected ArrayList<Number3d> vertices;
	protected ArrayList<Uv> texCoords;
	protected ArrayList<Number3d> normals;
	protected boolean generateMipMap;
	protected HashMap<String, Material> materialMap;
	
	public AParser()
	{
		vertices = new ArrayList<Number3d>();
		texCoords = new ArrayList<Uv>();
		normals = new ArrayList<Number3d>();
		parseObjects = new ArrayList<ParseObjectData>();
		textureAtlas = new TextureAtlas();
		firstObject = true;
		materialMap = new HashMap<String, Material>();
	}
	
	public AParser(Resources resources, String resourceID, Boolean generateMipMap)
	{
		this();
		this.resources = resources;
		this.resourceID = resourceID;
		if (resourceID.indexOf(":") > -1)
			this.packageID = resourceID.split(":")[0];
		this.generateMipMap = generateMipMap;
	}
	
	protected void cleanup()
	{
		parseObjects.clear();
		textureAtlas.cleanup();
		vertices.clear();
		texCoords.clear();
		normals.clear();
	}
	
	/**
	 * Override this in the concrete parser
	 */
	public Object3dContainer getParsedObject() {
		return null;
	}
	
	/**
	 * Override this in the concrete parser if applicable 
	 */
	public AnimationObject3d getParsedAnimationObject() {
		return null;
	}

	protected String readString(InputStream stream) throws IOException {
		String result = new String();
		byte inByte;
		while ((inByte = (byte) stream.read()) != 0)
			result += (char) inByte;
		return result;
	}

	protected int readInt(InputStream stream) throws IOException {
		return stream.read() | (stream.read() << 8) | (stream.read() << 16)
				| (stream.read() << 24);
	}

	protected int readShort(InputStream stream) throws IOException {
		return (stream.read() | (stream.read() << 8));
	}

	protected float readFloat(InputStream stream) throws IOException {
		return Float.intBitsToFloat(readInt(stream));
	}

	/**
	 * Override this in the concrete parser
	 */
	public void parse() {
	}
	

	/**
	 * Contains texture information. UV offsets and scaling is stored here.
	 * This is used with texture atlases.
	 * 
	 * @author dennis.ippel
	 *
	 */
	protected class BitmapAsset
	{
		/**
		 * The texture bitmap
		 */
		public Bitmap bitmap;
		/**
		 * The texture identifier
		 */
		public String key;
		/**
		 * Resource ID
		 */
		public String resourceID;
		/**
		 * U-coordinate offset
		 */
		public float uOffset;
		/**
		 * V-coordinate offset
		 */
		public float vOffset;
		/**
		 * U-coordinate scaling value
		 */
		public float uScale;
		/**
		 * V-coordinate scaling value
		 */
		public float vScale;
		public boolean useForAtlasDimensions;
		
		/**
		 * Creates a new BitmapAsset object
		 * @param bitmap
		 * @param key
		 */
		public BitmapAsset(String key, String resourceID)
		{
			this.key = key;
			this.resourceID = resourceID;
			useForAtlasDimensions = false;
		}
	}
	
	/**
	 * When a model contains per-face textures a texture atlas is created. This
	 * combines multiple textures into one and re-calculates the UV coordinates.
	 * 
	 * @author dennis.ippel
	 * 
	 */
	protected class TextureAtlas {
		/**
		 * The texture bitmaps that should be combined into one.
		 */
		private ArrayList<BitmapAsset> bitmaps;
		/**
		 * The texture atlas bitmap
		 */
		private Bitmap atlas;

		/**
		 * Creates a new texture atlas instance.
		 */
		public TextureAtlas() {
			bitmaps = new ArrayList<BitmapAsset>();
		}
		private String atlasId;

		/**
		 * Adds a bitmap to the atlas
		 * 
		 * @param bitmap
		 */
		public void addBitmapAsset(BitmapAsset ba) {
			BitmapAsset existingBA = getBitmapAssetByResourceID(ba.resourceID);

			if(existingBA == null)
			{
				int bmResourceID = resources.getIdentifier(ba.resourceID, null, null);
				if(bmResourceID == 0)
				{
					Log.d(Min3d.TAG, "Texture not found: " + ba.resourceID);
					return;
				}

				Log.d(Min3d.TAG, "Adding texture " + ba.resourceID);
				
				Bitmap b = Utils.makeBitmapFromResourceId(bmResourceID);
				ba.useForAtlasDimensions = true;
				ba.bitmap = b;
			}
			else
			{
				ba.bitmap = existingBA.bitmap;
			}

			bitmaps.add(ba);
		}
		
		public BitmapAsset getBitmapAssetByResourceID(String resourceID)
		{
			int numBitmaps = bitmaps.size();
			
			for(int i=0; i<numBitmaps; i++)
			{
				if(bitmaps.get(i).resourceID.equals(resourceID))
					return bitmaps.get(i);
			}
			
			return null;
		}

		/**
		 * Generates a new texture atlas
		 */
		public void generate() {
			Collections.sort(bitmaps, new BitmapHeightComparer());

			if(bitmaps.size() == 0) return;
			
			BitmapAsset largestBitmap = bitmaps.get(0);
			int totalWidth = 0;
			int numBitmaps = bitmaps.size();
			int uOffset = 0;
			int vOffset = 0;

			for (int i = 0; i < numBitmaps; i++) {
				if(bitmaps.get(i).useForAtlasDimensions)
					totalWidth += bitmaps.get(i).bitmap.getWidth();
			}

			atlas = Bitmap.createBitmap(totalWidth, largestBitmap.bitmap
					.getHeight(), Config.ARGB_8888);

			for (int i = 0; i < numBitmaps; i++) {
				BitmapAsset ba = bitmaps.get(i);
				BitmapAsset existingBA = getBitmapAssetByResourceID(ba.resourceID);				
				
				if(ba.useForAtlasDimensions)
				{
					Bitmap b = ba.bitmap;
					int w = b.getWidth();
					int h = b.getHeight();
					int[] pixels = new int[w * h];
					
					b.getPixels(pixels, 0, w, 0, 0, w, h);
					atlas.setPixels(pixels, 0, w, uOffset, vOffset, w, h);
					
					ba.uOffset = (float) uOffset / totalWidth;
					ba.vOffset = 0;
					ba.uScale = (float) w / (float) totalWidth;
					ba.vScale = (float) h / (float) largestBitmap.bitmap.getHeight();
					
					uOffset += w;
					b.recycle();
				}
				else
				{
					ba.uOffset = existingBA.uOffset;
					ba.vOffset = existingBA.vOffset;
					ba.uScale = existingBA.uScale;
					ba.vScale = existingBA.vScale;
				}
			}
			/*
			FileOutputStream fos;
			try {
				fos = new FileOutputStream("/data/screenshot.png");
				atlas.compress(Bitmap.CompressFormat.PNG, 100, fos);
				fos.flush();
				fos.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
			setId(Shared.textureManager().getNewAtlasId());
		}

		/**
		 * Returns the generated texture atlas bitmap
		 * 
		 * @return
		 */
		public Bitmap getBitmap() {
			return atlas;
		}

		/**
		 * Indicates whether bitmaps have been added to the atlas.
		 * 
		 * @return
		 */
		public boolean hasBitmaps() {
			return bitmaps.size() > 0;
		}

		/**
		 * Compares the height of two BitmapAsset objects.
		 * 
		 * @author dennis.ippel
		 * 
		 */
		private class BitmapHeightComparer implements Comparator<BitmapAsset> {
			public int compare(BitmapAsset b1, BitmapAsset b2) {
				int height1 = b1.bitmap.getHeight();
				int height2 = b2.bitmap.getHeight();

				if (height1 < height2) {
					return 1;
				} else if (height1 == height2) {
					return 0;
				} else {
					return -1;
				}
			}
		}
		
		/**
		 * Returns a bitmap asset with a specified name.
		 * 
		 * @param materialKey
		 * @return
		 */
		public BitmapAsset getBitmapAssetByName(String materialKey) {
			int numBitmaps = bitmaps.size();

			for (int i = 0; i < numBitmaps; i++) {
				if (bitmaps.get(i).key.equals(materialKey))
					return bitmaps.get(i);
			}

			return null;
		}
		
		public void cleanup()
		{
			int numBitmaps = bitmaps.size();

			for (int i = 0; i < numBitmaps; i++) {
				bitmaps.get(i).bitmap.recycle();
			}
			
			if(atlas != null) atlas.recycle();
			bitmaps.clear();
			vertices.clear();
			texCoords.clear();
			normals.clear();
		}

		public void setId(String newAtlasId) {
			atlasId = newAtlasId;			
		}

		public String getId() {
			return atlasId;
		}
	}
	
	protected class Material {
		public String name;
		public String diffuseTextureMap;
		public Color4 diffuseColor;

		public Material(String name) {
			this.name = name;
		}
	}
}
