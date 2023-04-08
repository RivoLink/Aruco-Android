package mg.rivolink.app.aruco.loader;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class WavefrontObjLoader {
    private float[] mVertices;
    private float[] mTexCoords;
    private int[] mIndices;

    public WavefrontObjLoader(Context context, int resourceId) {
        // Load the OBJ file from the resource ID
        InputStream is = context.getResources().openRawResource(resourceId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        ArrayList<Float> vertexList = new ArrayList<>();
        ArrayList<Float> texCoordList = new ArrayList<>();
        ArrayList<Integer> indexList = new ArrayList<>();

        try {
            String line;
            while ((line = reader.readLine()) != null) {
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
                    }
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
}
