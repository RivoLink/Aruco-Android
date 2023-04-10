package mg.rivolink.app.aruco.loader;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class MtlLoader {
    private Map<String, Material> mMaterialMap;

    public MtlLoader(Context context, BufferedReader reader) {
        mMaterialMap = new HashMap<>();

        // Load the MTL file from the resource ID
        Material currentMaterial = null;

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("newmtl ")) {
                    // Start a new material
                    String[] parts = line.split("\\s+");
                    int id = mMaterialMap.size();
                    String materialName = parts[1];
                    currentMaterial = new Material(id, materialName);
                    mMaterialMap.put(materialName, currentMaterial);
                } else if (line.startsWith("Ka ")) {
                    // Ambient color
                    String[] parts = line.split("\\s+");
                    float r = Float.parseFloat(parts[1]);
                    float g = Float.parseFloat(parts[2]);
                    float b = Float.parseFloat(parts[3]);
                    currentMaterial.setAmbientColor(new float[] { r, g, b });
                } else if (line.startsWith("Kd ")) {
                    // Diffuse color
                    String[] parts = line.split("\\s+");
                    float r = Float.parseFloat(parts[1]);
                    float g = Float.parseFloat(parts[2]);
                    float b = Float.parseFloat(parts[3]);
                    currentMaterial.setDiffuseColor(new float[] { r, g, b });
                } else if (line.startsWith("Ks ")) {
                    // Specular color
                    String[] parts = line.split("\\s+");
                    float r = Float.parseFloat(parts[1]);
                    float g = Float.parseFloat(parts[2]);
                    float b = Float.parseFloat(parts[3]);
                    currentMaterial.setSpecularColor(new float[] { r, g, b });
                } else if (line.startsWith("Ns ")) {
                    // Shininess
                    String[] parts = line.split("\\s+");
                    float shininess = Float.parseFloat(parts[1]);
                    currentMaterial.setShininess(shininess);
                } else if (line.startsWith("map_Kd ")) {
                    // Diffuse texture map
                    String[] parts = line.split("\\s+");
                    String textureFilename = parts[1];
                    int textureResourceId = context.getResources().getIdentifier(
                            textureFilename, "drawable", context.getPackageName());
                    currentMaterial.setDiffuseTexture(new Texture(textureResourceId));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Material getMaterial(String materialName) {
        return mMaterialMap.get(materialName);
    }

    public Material[] getMaterials() {
        return (Material[])mMaterialMap.values().toArray();
    }

    public int getMaterialId(String materialName) {
        Material material = mMaterialMap.get(materialName);
        if (material != null) {
            return material.getId();
        } else {
            return -1;
        }
    }
}
