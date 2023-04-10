package mg.rivolink.app.aruco.loader;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class WavefrontObjMtlLoader {

    private float[] mVertices;
    private float[] mTexCoords;
    private int[] mIndices;
    private int[] mMaterialIds;
    private Material[] mMaterials;

    public WavefrontObjMtlLoader(Context context, int objResourceId, int mtlResourceId) {
        // Load the OBJ file from the resource ID
        InputStream isObj = context.getResources().openRawResource(objResourceId);
        BufferedReader objReader = new BufferedReader(new InputStreamReader(isObj));

        // Load the MTL file from the resource ID
        InputStream isMtl = context.getResources().openRawResource(mtlResourceId);
        BufferedReader mtlReader = new BufferedReader(new InputStreamReader(isMtl));

        ArrayList<Float> vertexList = new ArrayList<>();
        ArrayList<Float> texCoordList = new ArrayList<>();
        ArrayList<Integer> indexList = new ArrayList<>();
        ArrayList<Integer> materialIdList = new ArrayList<>();

        MtlLoader mtlLoader = new MtlLoader(mtlReader);
        mMaterials = mtlLoader.getMaterials();

        try {
            String line;
            int currentMaterialId = -1;
            while ((line = objReader.readLine()) != null) {
                if (line.startsWith("v ")) {
                    // Parse the vertex position data
                    String[] parts = line.split("\\s+");
                    float x = Float.parseFloat(parts[1]);
                    float y = Float.parseFloat(parts[2]);
                    float z = Float.parseFloat(parts[3]);
                    vertexList.add(x);
                    vertexList.add(y);
                    vertexList.add(z);
                } else if (line.startsWith("vt ")) {
                    // Parse the texture coordinate data
                    String[] parts = line.split("\\s+");
                    float u = Float.parseFloat(parts[1]);
                    float v = Float.parseFloat(parts[2]);
                    texCoordList.add(u);
                    texCoordList.add(v);
                } else if (line.startsWith("f ")) {
                    // Parse the face data (vertex indices and texture coordinate indices)
                    String[] parts = line.split("\\s+");
                    for (int i = 1; i < parts.length; i++) {
                        String[] indices = parts[i].split("/");
                        int vertexIndex = Integer.parseInt(indices[0]) - 1;
                        int texCoordIndex = Integer.parseInt(indices[1]) - 1;
                        indexList.add(vertexIndex);
                        indexList.add(texCoordIndex);
                        materialIdList.add(currentMaterialId);
                    }
                } else if (line.startsWith("usemtl ")) {
                    // Set the current material ID for the following faces
                    String materialName = line.substring(7);
                    currentMaterialId = mtlLoader.getMaterialId(materialName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Convert the vertex list, texture coordinate list, and index list to arrays
        mVertices = new float[vertexList.size()];
        for (int i = 0; i < vertexList.size(); i++) {
            mVertices[i] = vertexList.get(i);
        }

        mTexCoords = new float[texCoordList.size()];
        for (int i = 0; i < texCoordList.size(); i++) {
            mTexCoords[i] = texCoordList.get(i);
        }

        mIndices = new int[indexList.size()];
        for (int i = 0; i < indexList.size(); i++) {
            mIndices[i] = indexList.get(i);
        }

        // Interleave the vertex and texture coordinate data
        float[] interleavedData = new float[mVertices.length + mTexCoords.length];
        for (int i = 0; i < mVertices.length / 3; i++) {
            int vertexOffset = i * 3;
            int texCoordOffset = i * 2;
            int interleavedOffset = i * 5;
            interleavedData[interleavedOffset] = mVertices[vertexOffset];
            interleavedData[interleavedOffset + 1] = mVertices[vertexOffset + 1];
            interleavedData[interleavedOffset + 2] = mVertices[vertexOffset + 2];
            interleavedData[interleavedOffset + 3] = mTexCoords[texCoordOffset];
            interleavedData[interleavedOffset + 4] = mTexCoords[texCoordOffset + 1];
        }

        // Convert the material ID list to an integer array
        mMaterialIds = new int[materialIdList.size()];
        for (int i = 0; i < materialIdList.size(); i++) {
            mMaterialIds[i] = materialIdList.get(i);
        }
    }

    public float[] getVertices() {
        return mVertices;
    }

    public float[] getTexCoords() {
        return mTexCoords;
    }

    public int[] getIndices() {
        return mIndices;
    }

    public int[] getMaterialIds() {
        return mMaterialIds;
    }

    public Material[] getMaterials() {
        return mMaterials;
    }
}
