package min3d.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.StringTokenizer;

import min3d.Min3d;
import min3d.Shared;
import min3d.Utils;
import min3d.core.Object3dContainer;
import min3d.vos.Color4;
import min3d.vos.Number3d;
import min3d.vos.Uv;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Log;

/**
 * Parses Wavefront OBJ files. Basic version, this is still a work in progress!
 * 
 * TODO: proper error handling TODO: handle multiple objects TODO: handle groups
 * TODO: a lot more :-) *
 * 
 * @author dennis.ippel
 * @see http://en.wikipedia.org/wiki/Obj
 * 
 */

public class ObjParser extends AParser implements IParser {
	private final String VERTEX = "v";
	private final String FACE = "f";
	private final String TEXCOORD = "vt";
	private final String NORMAL = "vn";
	private final String OBJECT = "o";
	private final String MATERIAL_LIB = "mtllib";
	private final String USE_MATERIAL = "usemtl";
	private final String NEW_MATERIAL = "newmtl";
	private final String DIFFUSE_COLOR = "Kd";
	private final String DIFFUSE_TEX_MAP = "map_Kd";

	/**
	 * Creates a new OBJ parser instance
	 * 
	 * @param resources
	 * @param resourceID
	 */
	public ObjParser(Resources resources, String resourceID, boolean generateMipMap) {
		super(resources, resourceID, generateMipMap);
	}

	@Override
	public void parse() {
		long startTime = Calendar.getInstance().getTimeInMillis();

		InputStream fileIn = resources.openRawResource(resources.getIdentifier(
				resourceID, null, null));
		BufferedReader buffer = new BufferedReader(
				new InputStreamReader(fileIn));
		String line;
		co = new ParseObjectData(vertices, texCoords, normals);
		parseObjects.add(co);

		Log.d(Min3d.TAG, "Start parsing object " + resourceID);
		Log.d(Min3d.TAG, "Start time " + startTime);

		try {
			while ((line = buffer.readLine()) != null) {
				// remove duplicate whitespace
				// line = line.replaceAll("\\s+", " ");
				// String[] parts = line.split(" ");
				StringTokenizer parts = new StringTokenizer(line, " ");
				int numTokens = parts.countTokens();
				if (numTokens == 0)
					continue;
				String type = parts.nextToken();

				if (type.equals(VERTEX)) {
					Number3d vertex = new Number3d();
					vertex.x = Float.parseFloat(parts.nextToken());
					vertex.y = Float.parseFloat(parts.nextToken());
					vertex.z = Float.parseFloat(parts.nextToken());
					vertices.add(vertex);
				} else if (type.equals(FACE)) {
					if (numTokens == 4) {
						co.numFaces++;
						co.faces.add(new ObjFace(line, currentMaterialKey, 3));
					} else if (numTokens == 5) {
						co.numFaces += 2;
						co.faces.add(new ObjFace(line, currentMaterialKey, 4));
					}
				} else if (type.equals(TEXCOORD)) {
					Uv texCoord = new Uv();
					texCoord.u = Float.parseFloat(parts.nextToken());
					texCoord.v = Float.parseFloat(parts.nextToken()) * -1f;
					texCoords.add(texCoord);
				} else if (type.equals(NORMAL)) {
					Number3d normal = new Number3d();
					normal.x = Float.parseFloat(parts.nextToken());
					normal.y = Float.parseFloat(parts.nextToken());
					normal.z = Float.parseFloat(parts.nextToken());
					normals.add(normal);
				} else if (type.equals(MATERIAL_LIB)) {
					readMaterialLib(parts.nextToken());
				} else if (type.equals(USE_MATERIAL)) {
					currentMaterialKey = parts.nextToken();
				} else if (type.equals(OBJECT)) {
					String objName = parts.hasMoreTokens() ? parts.nextToken() : ""; 
					if(firstObject)
					{
						Log.d(Min3d.TAG, "Create object " + objName);
						co.name = objName;
						firstObject = false;
					}
					else
					{
						Log.d(Min3d.TAG, "Create object " + objName);
						co = new ParseObjectData(vertices, texCoords, normals);
						co.name = objName;
						parseObjects.add(co);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		long endTime = Calendar.getInstance().getTimeInMillis();
		Log.d(Min3d.TAG, "End time " + (endTime - startTime));
	}

	public Object3dContainer getParsedObject() {
		Log.d(Min3d.TAG, "Start object creation");
		Object3dContainer obj = new Object3dContainer(0, 0);
		int numObjects = parseObjects.size();
		Bitmap texture = null;

		if(textureAtlas.hasBitmaps())
		{
			textureAtlas.generate();
			texture = textureAtlas.getBitmap();
			Shared.textureManager().addTextureId(texture, textureAtlas.getId(), generateMipMap);
		}
		
		for (int i = 0; i < numObjects; i++) {
			ParseObjectData o = parseObjects.get(i);
			Log.d(Min3d.TAG, "Creating object " + o.name);
			obj.addChild(o.getParsedObject(materialMap, textureAtlas));
		}
		
		if(textureAtlas.hasBitmaps())
		{
			if(texture != null) texture.recycle();
		}
		Log.d(Min3d.TAG, "Object creation finished");
		
		cleanup();
		
		return obj;
	}

	private void readMaterialLib(String libID) {
		StringBuffer resourceID = new StringBuffer(packageID);
		StringBuffer libIDSbuf = new StringBuffer(libID);
		int dotIndex = libIDSbuf.lastIndexOf(".");
		if (dotIndex > -1)
			libIDSbuf = libIDSbuf.replace(dotIndex, dotIndex + 1, "_");

		resourceID.append(":raw/");
		resourceID.append(libIDSbuf.toString());

		InputStream fileIn = resources.openRawResource(resources.getIdentifier(
				resourceID.toString(), null, null));
		BufferedReader buffer = new BufferedReader(
				new InputStreamReader(fileIn));
		String line;
		String currentMaterial = "";

		try {
			while ((line = buffer.readLine()) != null) {
				String[] parts = line.split(" ");
				if (parts.length == 0)
					continue;
				String type = parts[0];

				if (type.equals(NEW_MATERIAL)) {
					if (parts.length > 1) {
						currentMaterial = parts[1];
						materialMap.put(currentMaterial, new Material(
								currentMaterial));
					}
				} else if(type.equals(DIFFUSE_COLOR) && !type.equals(DIFFUSE_TEX_MAP)) {
					Color4 diffuseColor = new Color4(Float.parseFloat(parts[1]) * 255.0f, Float.parseFloat(parts[2]) * 255.0f, Float.parseFloat(parts[3]) * 255.0f, 255.0f);
					materialMap.get(currentMaterial).diffuseColor = diffuseColor;
				} else if (type.equals(DIFFUSE_TEX_MAP)) {
					if (parts.length > 1) {
						materialMap.get(currentMaterial).diffuseTextureMap = parts[1];
						StringBuffer texture = new StringBuffer(packageID);
						texture.append(":drawable/");
						
						StringBuffer textureName = new StringBuffer(parts[1]);
						dotIndex = textureName.lastIndexOf(".");
						if (dotIndex > -1)
							texture.append(textureName.substring(0, dotIndex));
						else
							texture.append(textureName);
						
						int bmResourceID = resources.getIdentifier(texture
								.toString(), null, null);
						Bitmap b = Utils.makeBitmapFromResourceId(bmResourceID);
						textureAtlas.addBitmapAsset(new BitmapAsset(currentMaterial, texture.toString()));
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void cleanup() {
		super.cleanup();
		materialMap.clear();
	}

	private class ObjFace extends ParseObjectFace {
		public ObjFace(String line, String materialKey, int faceLength) {
			super();
			this.materialKey = materialKey;
			this.faceLength = faceLength;
			boolean emptyVt = line.indexOf("//") > -1;
			if(emptyVt) line = line.replace("//", "/");
			StringTokenizer parts = new StringTokenizer(line);
			parts.nextToken();
			StringTokenizer subParts = new StringTokenizer(parts.nextToken(), "/");
			int partLength = subParts.countTokens();
			hasuv = partLength >= 2 && !emptyVt;
			hasn = partLength == 3 || (partLength == 2 && emptyVt);

			v = new int[faceLength];
			if (hasuv)
				uv = new int[faceLength];
			if (hasn)
				n = new int[faceLength];

			for (int i = 1; i < faceLength + 1; i++) {
				if (i > 1)
					subParts = new StringTokenizer(parts.nextToken(), "/");

				int index = i - 1;
				v[index] = (short) (Short.parseShort(subParts.nextToken()) - 1);
				if (hasuv)
					uv[index] = (short) (Short.parseShort(subParts.nextToken()) - 1);
				if (hasn)
					n[index] = (short) (Short.parseShort(subParts.nextToken()) - 1);
			}
		}
	}
}
