package min3d.interfaces;

import android.os.Handler;

/**
 * Interface to handle the initialization of the Scene 
 * and the 'on-enter-frame' updates to the Scene (think 'model').
 * 
 * The RendererActivity class implements this interface.
 * 
 * But you could use any other class to implement this interface, 
 * not just the Activity class.
 *   
 */
public interface ISceneController 
{
	/**
	 * Initialization of scene objects happens here. 
	 * 
	 * It is called after the GL Surface is created, which means not only at startup, 
	 * but also when the application resumes after losing focus.
	 * 
	 * It would be the end-user's responsibility to save and restore state if so desired...
	 */
	public void initScene();

	/**
	 * Updating properties of scene objects happens here.
	 * This is called on every frame right before the render routine.
	 */
	public void updateScene();
	
	
	public Handler getInitSceneHandler();
	public Runnable getInitSceneRunnable();
	
	public Handler getUpdateSceneHandler();
	public Runnable getUpdateSceneRunnable();
}
