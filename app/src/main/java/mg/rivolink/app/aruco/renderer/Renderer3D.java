package mg.rivolink.app.aruco.renderer;

import android.content.Context;
import android.view.MotionEvent;

import org.rajawali3d.Object3D;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.renderer.Renderer;

import mg.rivolink.app.aruco.R;

public class Renderer3D extends Renderer {

	private Object3D model;
	private PointLight light;
	
	public Renderer3D(Context context){
		super(context);
		setFrameRate(60);
	}

	@Override
	protected void initScene(){
		light = new PointLight();
		light.setPosition(0, 0, 4);
		light.setPower(3);

		try {
			LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(), mTextureManager, R.raw.box_obj);
			objParser.parse();

			model = objParser.getParsedObject();
			model.setPosition(0, 0, -10);
			model.setVisible(false);

			getCurrentScene().addLight(light);
			getCurrentScene().addChild(model);

		}
		catch(ParsingException e){
			e.printStackTrace();
		}
	}

	public void transform(double tx, double ty, double tz, double yaw, double pitch, double roll){
		/* Some bugs here
		if(!model.isVisible())
			model.setVisible(true);

		model.setPosition(tx, ty, tz);
		model.setOrientation(new Quaternion().fromEuler(
			Math.toDegrees(yaw),
			Math.toDegrees(pitch),
			Math.toDegrees(roll)
		));
		*/
	}

	@Override
	protected void render(long ellapsedRealtime, double deltaTime){
		super.render(ellapsedRealtime, deltaTime);
		//model.rotate(Vector3.Axis.Y, 0.5);
	}

	@Override
	public void onOffsetsChanged(float x, float y, float z, float w, int i, int j){
		// TODO: Implement this method
	}

	@Override
	public void onTouchEvent(MotionEvent p1){
		// TODO: Implement this method
	}
	
}
