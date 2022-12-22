package min3d.parser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import min3d.Min3d;
import min3d.Shared;
import min3d.core.Object3dContainer;
import min3d.vos.Number3d;
import min3d.vos.Uv;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Log;

public class Max3DSParser extends AParser implements IParser {
	private final int IDENTIFIER_3DS = 0x4D4D;
	private final int MESH_BLOCK = 0x3D3D;
	private final int OBJECT_BLOCK = 0x4000;
	private final int TRIMESH = 0x4100;
	private final int TRI_MATERIAL = 0x4130;
	private final int VERTICES = 0x4110;
	private final int FACES = 0x4120;
	private final int TEXCOORD = 0x4140;
	private final int TEX_MAP = 0xA200;
	private final int TEX_NAME = 0xA000;
	private final int TEX_FILENAME = 0xA300;
	private final int MATERIAL = 0xAFFF;

	private int chunkID;
	private int chunkEndOffset;
	private boolean endReached;
	private String currentObjName;

	public Max3DSParser(Resources resources, String resourceID, boolean generateMipMap) {
		super(resources, resourceID, generateMipMap);
	}

	@Override
	public void parse() {
		InputStream fileIn = resources.openRawResource(resources.getIdentifier(
				resourceID, null, null));
		BufferedInputStream stream = new BufferedInputStream(fileIn);
		
		Log.d(Min3d.TAG, "Start parsing object");
		
		co = new ParseObjectData();
		parseObjects.add(co);

		try {
			readHeader(stream);
			if(chunkID != IDENTIFIER_3DS)
			{
				Log.d(Min3d.TAG, "Not a valid .3DS file!");
				return;
			}
			else
			{
				Log.d(Min3d.TAG, "Found a valid .3DS file");
			}
			while(!endReached)
			{
				readChunk(stream);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Log.d(Min3d.TAG, "End parsing object");
	}

	private void readHeader(InputStream stream) throws IOException {
		chunkID = readShort(stream);
		chunkEndOffset = readInt(stream);
		endReached = chunkID < 0;
	}

	private void readChunk(InputStream stream) throws IOException {
		readHeader(stream);

		switch (chunkID) {
		case MESH_BLOCK:
			break;
		case OBJECT_BLOCK:
			currentObjName = readString(stream);
			Log.d(Min3d.TAG, "Parsing object " + currentObjName);
			break;
		case TRIMESH:
			if(firstObject)
			{
				co.name = currentObjName;
				firstObject = false;
			}
			else
			{
				co = new ParseObjectData();
				co.name = currentObjName;
				parseObjects.add(co);
			}
			break;
		case VERTICES:
			readVertices(stream);
			break;
		case FACES:
			readFaces(stream);
			break;
		case TEXCOORD:
			readTexCoords(stream);
			break;
		case TEX_NAME:
			currentMaterialKey = readString(stream);
			break;
		case TEX_FILENAME:
			String fileName = readString(stream);
			StringBuffer texture = new StringBuffer(packageID);
			texture.append(":drawable/");

			StringBuffer textureName = new StringBuffer(fileName.toLowerCase());
			int dotIndex = textureName.lastIndexOf(".");
			if (dotIndex > -1)
				texture.append(textureName.substring(0, dotIndex));
			else
				texture.append(textureName);
			
			textureAtlas.addBitmapAsset(new BitmapAsset(currentMaterialKey, texture.toString()));
			break;
		case TRI_MATERIAL:
			String materialName = readString(stream);
			int numFaces = readShort(stream);

			for(int i=0; i<numFaces; i++)
			{
				int faceIndex = readShort(stream);
				co.faces.get(faceIndex).materialKey = materialName;
			}
			break;
		case MATERIAL:
			break;
		case TEX_MAP:
			break;
		default:
			skipRead(stream);
		}
	}
	
	private void skipRead(InputStream stream) throws IOException
	{
		for(int i=0; (i<chunkEndOffset - 6) && !endReached; i++)
		{
			endReached = stream.read() < 0;
		}
	}
	
	private void readVertices(InputStream buffer) throws IOException {
        float x, y, z, tmpy;
        int numVertices = readShort(buffer);

        for (int i = 0; i < numVertices; i++) {
            x = readFloat(buffer);
            y = readFloat(buffer);
            z = readFloat(buffer);
            tmpy = y;
            y = z;
            z = -tmpy;
            
            co.vertices.add(new Number3d(x, y, z));
        }
    }
	
	private void readFaces(InputStream buffer) throws IOException {
        int triangles = readShort(buffer);
        for (int i = 0; i < triangles; i++) {
            int[] vertexIDs = new int[3];
            vertexIDs[0] = readShort(buffer);
            vertexIDs[1] = readShort(buffer);
            vertexIDs[2] = readShort(buffer);
            readShort(buffer);
            ParseObjectFace face = new ParseObjectFace();
            face.v = vertexIDs;
            face.uv = vertexIDs;
            face.faceLength = 3;
            face.hasuv = true;
            co.numFaces++;
            co.faces.add(face);
            
            co.calculateFaceNormal(face);
        }
    }
	
	private void readTexCoords(InputStream buffer) throws IOException {
        int numVertices = readShort(buffer);

        for (int i = 0; i < numVertices; i++) {
            Uv uv = new Uv();
            uv.u = readFloat(buffer);
            uv.v = readFloat(buffer) * -1f;
            co.texCoords.add(uv);
        }
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
		
		super.cleanup();
		
		return obj;
	}
}
