package mg.rivolink.app.aruco.loader;

import mg.rivolink.app.aruco.loader.Texture;

public class Material {
    private int mId;
    private String mName;
    private float[] mAmbientColor;
    private float[] mDiffuseColor;
    private float[] mSpecularColor;
    private float mShininess;
    private Texture mDiffuseTexture;

    public Material(int id, String name) {
        mId = id;
        mName = name;
        mAmbientColor = new float[]{1.0f, 1.0f, 1.0f};
        mDiffuseColor = new float[]{1.0f, 1.0f, 1.0f};
        mSpecularColor = new float[]{1.0f, 1.0f, 1.0f};
        mShininess = 0.0f;
        mDiffuseTexture = null;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public void setAmbientColor(float[] color) {
        mAmbientColor = color;
    }

    public float[] getAmbientColor() {
        return mAmbientColor;
    }

    public void setDiffuseColor(float[] color) {
        mDiffuseColor = color;
    }

    public float[] getDiffuseColor() {
        return mDiffuseColor;
    }

    public void setSpecularColor(float[] color) {
        mSpecularColor = color;
    }

    public float[] getSpecularColor() {
        return mSpecularColor;
    }

    public void setShininess(float shininess) {
        mShininess = shininess;
    }

    public float getShininess() {
        return mShininess;
    }

    public void setDiffuseTexture(Texture texture) {
        mDiffuseTexture = texture;
    }

    public Texture getDiffuseTexture() {
        return mDiffuseTexture;
    }
}
