package min3d.animation;

import min3d.core.FacesBufferedList;
import min3d.core.Object3d;
import min3d.core.TextureList;
import min3d.core.Vertices;

public class AnimationObject3d extends Object3d {
	private int numFrames;
	private KeyFrame[] frames;
	private int currentFrameIndex;
	private long startTime;
	private long currentTime;
	private boolean isPlaying;
	private float interpolation;
	private float fps = 70;
	private boolean updateVertices = true;	
	private String currentFrameName;
	private int loopStartIndex;
	private boolean loop = false;

	public AnimationObject3d(int $maxVertices, int $maxFaces, int $numFrames) {
		super($maxVertices, $maxFaces);
		this.numFrames = $numFrames;
		this.frames = new KeyFrame[numFrames];
		this.currentFrameIndex = 0;
		this.isPlaying = false;
		this.interpolation = 0;
		this._animationEnabled = true;
	}
	
	public AnimationObject3d(Vertices $vertices, FacesBufferedList $faces, TextureList $textures, KeyFrame[] $frames)
	{
		super($vertices, $faces, $textures);
		numFrames = $frames.length;
		frames = $frames;
	}

	public int getCurrentFrame() {
		return currentFrameIndex;
	}

	public void addFrame(KeyFrame frame) {
		frames[currentFrameIndex++] = frame;
	}

	public void setFrames(KeyFrame[] frames) {
		this.frames = frames;
	}

	public void play() {
		startTime = System.currentTimeMillis();
		isPlaying = true;
		currentFrameName = null;
		loop = false;
	}

	public void play(String name) {
		currentFrameIndex = 0;
		currentFrameName = name;

		for (int i = 0; i < numFrames; i++) {
			if (frames[i].getName().equals(name))
			{
				loopStartIndex = currentFrameIndex = i;
				break;
			}
		}

		startTime = System.currentTimeMillis();
		isPlaying = true;
	}
	
	public void play(String name, boolean loop) {
		this.loop = loop;
		play(name);
	}

	public void stop() {
		isPlaying = false;
		currentFrameIndex = 0;
	}

	public void pause() {
		isPlaying = false;
	}

	public void update() {
		if (!isPlaying || !updateVertices)
			return;
		currentTime = System.currentTimeMillis();
		KeyFrame currentFrame = frames[currentFrameIndex];
		KeyFrame nextFrame = frames[(currentFrameIndex + 1) % numFrames];
		
		if(currentFrameName != null && !currentFrameName.equals(currentFrame.getName()))
		{
			if(!loop)
				stop();
			else
				currentFrameIndex = loopStartIndex;
			return;
		}
		
		float[] currentVerts = currentFrame.getVertices();
		float[] nextVerts = nextFrame.getVertices();
		float[] currentNormals = currentFrame.getNormals();
		float[] nextNormals = nextFrame.getNormals();
		int numVerts = currentVerts.length;
		
		float[] interPolatedVerts = new float[numVerts];
		float[] interPolatedNormals = new float[numVerts];

		for (int i = 0; i < numVerts; i += 3) {
			interPolatedVerts[i] = currentVerts[i] + interpolation * (nextVerts[i] - currentVerts[i]);
			interPolatedVerts[i + 1] = currentVerts[i + 1] + interpolation * (nextVerts[i + 1] - currentVerts[i + 1]);
			interPolatedVerts[i + 2] = currentVerts[i + 2] + interpolation 	* (nextVerts[i + 2] - currentVerts[i + 2]);
			interPolatedNormals[i] = currentNormals[i] + interpolation * (nextNormals[i] - currentNormals[i]);
			interPolatedNormals[i + 1] = currentNormals[i + 1] + interpolation * (nextNormals[i + 1] - currentNormals[i + 1]);
			interPolatedNormals[i + 2] = currentNormals[i + 2] + interpolation * (nextNormals[i + 2] - currentNormals[i + 2]);
		}

		interpolation += fps * (currentTime - startTime) / 1000;
		
		vertices().overwriteNormals(interPolatedNormals);
		vertices().overwriteVerts(interPolatedVerts);
	
		if (interpolation > 1) {
			interpolation = 0;
			currentFrameIndex++;

			if (currentFrameIndex >= numFrames)
				currentFrameIndex = 0;
		}
		
		startTime = System.currentTimeMillis();
	}

	public float getFps() {
		return fps;
	}

	public void setFps(float fps) {
		this.fps = fps;
	}
	
	public Object3d clone(boolean cloneData)
	{
		Vertices v = cloneData ? _vertices.clone() : _vertices;
		FacesBufferedList f = cloneData ? _faces.clone() : _faces;
		//KeyFrame[] fr = cloneData ? getClonedFrames() : frames;
		
		AnimationObject3d clone = new AnimationObject3d(v, f, _textures, frames);
		clone.position().x = position().x;
		clone.position().y = position().y;
		clone.position().z = position().z;
		clone.rotation().x = rotation().x;
		clone.rotation().y = rotation().y;
		clone.rotation().z = rotation().z;
		clone.scale().x = scale().x;
		clone.scale().y = scale().y;
		clone.scale().z = scale().z;
		clone.setFps(fps);
		clone.animationEnabled(animationEnabled());
		return clone;
	}
	
	public KeyFrame[] getClonedFrames()
	{
		int len = frames.length;
		KeyFrame[] cl = new KeyFrame[len];
		
		for(int i=0; i<len; i++)
		{
			cl[i] = frames[i].clone();
		}
		
		return cl;
	}

	public boolean getUpdateVertices() {
		return updateVertices;
	}

	public void setUpdateVertices(boolean updateVertices) {
		this.updateVertices = updateVertices;
	}
}
