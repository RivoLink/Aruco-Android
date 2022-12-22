package min3d.parser;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import min3d.Min3d;
import min3d.Shared;
import min3d.animation.AnimationObject3d;
import min3d.animation.KeyFrame;
import min3d.vos.Number3d;
import min3d.vos.Uv;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Log;

public class MD2Parser extends AParser implements IParser {
	private MD2Header header;
	private String currentTextureName;
	private KeyFrame[] frames;

	public MD2Parser(Resources resources, String resourceID, boolean generateMipMap) {
		super(resources, resourceID, generateMipMap);
	}

	@Override
	public AnimationObject3d getParsedAnimationObject() {
		Log.d(Min3d.TAG, "Start object creation");
		Bitmap texture = null;
		AnimationObject3d animObj;

		if (textureAtlas.hasBitmaps()) {
			textureAtlas.generate();
			texture = textureAtlas.getBitmap();
			Shared.textureManager().addTextureId(texture, textureAtlas.getId(), generateMipMap);
		}

		Log.d(Min3d.TAG, "Creating object " + co.name);
		animObj = co.getParsedObject(textureAtlas, materialMap, frames);

		if (textureAtlas.hasBitmaps()) {
			if (texture != null)
				texture.recycle();
		}
		Log.d(Min3d.TAG, "Object creation finished");

		super.cleanup();

		return animObj;
	}

	@Override
	public void parse() {
		InputStream fileIn = resources.openRawResource(resources.getIdentifier(
				resourceID, null, null));
		BufferedInputStream stream = new BufferedInputStream(fileIn);

		co = new ParseObjectData();
		header = new MD2Header();

		Log.d(Min3d.TAG, "Start parsing MD2 file");
		try {
			header.parse(stream);
			frames = new KeyFrame[header.numFrames];
			byte[] bytes = new byte[header.offsetEnd - 68];
			stream.read(bytes);
			getMaterials(stream, bytes);
			getTexCoords(stream, bytes);
			getFrames(stream, bytes);
			getTriangles(stream, bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getMaterials(BufferedInputStream stream, byte[] bytes)
			throws IOException {
		ByteArrayInputStream ba = new ByteArrayInputStream(bytes,
				header.offsetSkins - 68, bytes.length - header.offsetSkins);
		LittleEndianDataInputStream is = new LittleEndianDataInputStream(ba);

		for (int i = 0; i < header.numSkins; i++) {
			String skinPath = is.readString(64);
			StringBuffer texture = new StringBuffer(packageID);
			texture.append(":drawable/");

			skinPath = skinPath.substring(skinPath.lastIndexOf("/") + 1,
					skinPath.length());
			StringBuffer textureName = new StringBuffer(skinPath.toLowerCase());
			int dotIndex = textureName.lastIndexOf(".");
			if (dotIndex > -1)
				texture.append(textureName.substring(0, dotIndex));
			else
				texture.append(textureName);

			currentTextureName = texture.toString();
			textureAtlas.addBitmapAsset(new BitmapAsset(currentTextureName,
					currentTextureName));
		}
	}

	private void getTexCoords(BufferedInputStream stream, byte[] bytes)
			throws IOException {
		ByteArrayInputStream ba = new ByteArrayInputStream(bytes,
				header.offsetTexCoord - 68, bytes.length
						- header.offsetTexCoord);
		LittleEndianDataInputStream is = new LittleEndianDataInputStream(ba);

		for (int i = 0; i < header.numTexCoord; i++) {
			co.texCoords.add(new Uv((float)is.readShort() / (float)header.skinWidth, (float)is.readShort() / (float)header.skinHeight));
		}
	}

	private void getFrames(BufferedInputStream stream, byte[] bytes)
			throws IOException {
		ByteArrayInputStream ba = new ByteArrayInputStream(bytes,
				header.offsetFrames - 68, bytes.length - header.offsetFrames);
		LittleEndianDataInputStream is = new LittleEndianDataInputStream(ba);
		ArrayList<Number3d> firstFrameVerts = new ArrayList<Number3d>();

		for (int i = 0; i < header.numFrames; i++) {
			float scaleX = is.readFloat();
			float scaleY = is.readFloat();
			float scaleZ = is.readFloat();
			float translateX = is.readFloat();
			float translateY = is.readFloat();
			float translateZ = is.readFloat();
			String name = is.readString(16);
			
			if(name.indexOf("_") > 0)
				name = name.subSequence(0, name.lastIndexOf("_")).toString();
			else
				name = name.substring(0, 6).replaceAll("[0-9]{1,2}$", "");
			
			Log.d(Min3d.TAG, "frame name: " + name);
			float vertices[] = new float[header.numVerts * 3];
			int index = 0;

			for (int j = 0; j < header.numVerts; j++) {
				vertices[index++] = scaleX * is.readUnsignedByte() + translateX;
				vertices[index++] = scaleY * is.readUnsignedByte() + translateY;
				vertices[index++] = scaleZ * is.readUnsignedByte() + translateZ;
				
				int normalIndex = is.readUnsignedByte();
				if (i == 0)
					co.vertices.add(new Number3d(vertices[index - 3],
							vertices[index - 2], vertices[index - 1]));
			}

			frames[i] = new KeyFrame(name, vertices);
		}
	}

	private void getTriangles(BufferedInputStream stream, byte[] bytes)
			throws IOException {
		ByteArrayInputStream ba = new ByteArrayInputStream(bytes,
				header.offsetTriangles - 68, bytes.length
						- header.offsetTriangles);
		LittleEndianDataInputStream is = new LittleEndianDataInputStream(ba);
		int[] indices = new int[header.numTriangles*3];
		int index = 0;

		for (int i = 0; i < header.numTriangles; i++) {
			int[] vertexIDs = new int[3];
			int[] uvIDS = new int[3];

			indices[index+2] = vertexIDs[2] = is.readUnsignedShort();
			indices[index+1] = vertexIDs[1] = is.readUnsignedShort();
			indices[index] = vertexIDs[0] = is.readUnsignedShort();
			index += 3;
			uvIDS[2] = is.readUnsignedShort();
			uvIDS[1] = is.readUnsignedShort();
			uvIDS[0] = is.readUnsignedShort();

			ParseObjectFace f = new ParseObjectFace();
			f.v = vertexIDs;
			f.uv = uvIDS;
			f.hasn = f.hasuv = true;
			f.faceLength = 3;
			f.materialKey = currentTextureName;
			co.numFaces++;
			co.faces.add(f);
			co.calculateFaceNormal(f);
		}
		
		for(int j=0; j<header.numFrames; j++)
		{
			frames[j].setIndices(indices);
		}
	}

	private class MD2Header {
		public int id;
		public int version;
		public int skinWidth;
		public int skinHeight;
		public int frameSize;
		public int numSkins;
		public int numVerts;
		public int numTexCoord;
		public int numTriangles;
		public int numGLCommands;
		public int numFrames;
		public int offsetSkins;
		public int offsetTexCoord;
		public int offsetTriangles;
		public int offsetFrames;
		public int offsetGLCommands;
		public int offsetEnd;

		public void parse(InputStream stream) throws Exception {
			id = readInt(stream);
			version = readInt(stream);

			if (id != 844121161 || version != 8)
				throw new Exception("This is not a valid MD2 file.");

			skinWidth = readInt(stream);
			skinHeight = readInt(stream);
			frameSize = readInt(stream);

			numSkins = readInt(stream);
			numVerts = readInt(stream);
			numTexCoord = readInt(stream);
			numTriangles = readInt(stream);
			numGLCommands = readInt(stream);
			numFrames = readInt(stream);

			offsetSkins = readInt(stream);
			offsetTexCoord = readInt(stream);
			offsetTriangles = readInt(stream);
			offsetFrames = readInt(stream);
			offsetGLCommands = readInt(stream);
			offsetEnd = readInt(stream);
		}
	}
}
