package de.dailyfratze.mosaic.gui;

import com.sun.javafx.util.Utils;
import javafx.animation.AnimationTimer;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.util.Callback;

import static javafx.scene.input.KeyCode.A;
import static javafx.scene.input.KeyCode.D;
import static javafx.scene.input.KeyCode.Q;
import static javafx.scene.input.KeyCode.S;
import static javafx.scene.input.KeyCode.SHIFT;
import static javafx.scene.input.KeyCode.W;

/**
 * See <a href="http://stackoverflow.com/a/28437891/1547989">this question at stackoverflow</a>.
 * @author Jason Pollastrini (@jdub1581)
 */
public class SimpleFPSCamera extends Parent {

    public SimpleFPSCamera() {
	initialize();
    }

    private void update() {
	updateControls();
    }

    private void updateControls() {
	if (fwd && !back) {
	    moveForward();
	}
	if (strafeL) {
	    strafeLeft();
	}
	if (strafeR) {
	    strafeRight();
	}
	if (back && !fwd) {
	    moveBack();
	}
	if (up && !down) {
	    moveUp();
	}
	if (down && !up) {
	    moveDown();
	}
    }
    /*==========================================================================
     Initialization
     */
    private final Group root = new Group();
    private final Affine affine = new Affine();
    private final Translate t = new Translate(0, 0, 0);
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS),
	    rotateY = new Rotate(0, Rotate.Y_AXIS),
	    rotateZ = new Rotate(0, Rotate.Z_AXIS);

    private boolean fwd, strafeL, strafeR, back, up, down, shift;

    private double mouseSpeed = 1.0, mouseModifier = 0.1;
    private double moveSpeed = 10.0;
    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;
    private double mouseDeltaX;
    private double mouseDeltaY;

    private void initialize() {
	getChildren().add(root);
	getTransforms().addAll(affine);
	initializeCamera();
	startUpdateThread();
    }

    public void loadControlsForSubScene(SubScene scene) {
	sceneProperty().addListener(l -> {
	    if (getScene() != null) {
		getScene().addEventHandler(KeyEvent.ANY, ke -> {
		    if (ke.getEventType() == KeyEvent.KEY_PRESSED) {
			switch (ke.getCode()) {
			    case Q:
				up = true;
				break;
			    case E:
				down = true;
				break;
			    case W:
				fwd = true;
				break;
			    case S:
				back = true;
				break;
			    case A:
				strafeL = true;
				break;
			    case D:
				strafeR = true;
				break;
			    case SHIFT:
				shift = true;
				moveSpeed = 20;
				break;
			}
		    } else if (ke.getEventType() == KeyEvent.KEY_RELEASED) {
			switch (ke.getCode()) {
			    case Q:
				up = false;
				break;
			    case E:
				down = false;
				break;
			    case W:
				fwd = false;
				break;
			    case S:
				back = false;
				break;
			    case A:
				strafeL = false;
				break;
			    case D:
				strafeR = false;
				break;
			    case SHIFT:
				moveSpeed = 10;
				shift = false;
				break;
			}
		    }
		    ke.consume();
		});
	    }
	});
	scene.addEventHandler(MouseEvent.ANY, me -> {
	    if (me.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
		mousePosX = me.getSceneX();
		mousePosY = me.getSceneY();
		mouseOldX = me.getSceneX();
		mouseOldY = me.getSceneY();

	    } else if (me.getEventType().equals(MouseEvent.MOUSE_DRAGGED)) {
		mouseOldX = mousePosX;
		mouseOldY = mousePosY;
		mousePosX = me.getSceneX();
		mousePosY = me.getSceneY();
		mouseDeltaX = (mousePosX - mouseOldX);
		mouseDeltaY = (mousePosY - mouseOldY);

		mouseSpeed = 1.0;
		mouseModifier = 0.1;

		if (me.isPrimaryButtonDown()) {
		    if (me.isControlDown()) {
			mouseSpeed = 0.1;
		    }
		    if (me.isShiftDown()) {
			mouseSpeed = 1.0;
		    }
		    t.setX(getPosition().getX());
		    t.setY(getPosition().getY());
		    t.setZ(getPosition().getZ());

		    affine.setToIdentity();

		    rotateY.setAngle(
			    Utils.clamp(-360, ((rotateY.getAngle() + mouseDeltaX * (mouseSpeed * mouseModifier)) % 360 + 540) % 360 - 180, 360)
		    ); // horizontal                
		    rotateX.setAngle(
			    Utils.clamp(-45, ((rotateX.getAngle() - mouseDeltaY * (mouseSpeed * mouseModifier)) % 360 + 540) % 360 - 180, 35)
		    ); // vertical
		    affine.prepend(t.createConcatenation(rotateY.createConcatenation(rotateX)));

		} else if (me.isSecondaryButtonDown()) {
		    /*
		     init zoom?
		     */
		} else if (me.isMiddleButtonDown()) {
		    /*
		     init panning?
		     */
		}
	    }
	});

	scene.addEventHandler(ScrollEvent.ANY, se -> {

	    if (se.getEventType().equals(ScrollEvent.SCROLL_STARTED)) {

	    } else if (se.getEventType().equals(ScrollEvent.SCROLL)) {

	    } else if (se.getEventType().equals(ScrollEvent.SCROLL_FINISHED)) {

	    }
	});
    }

    public void loadControlsForScene(Scene scene) {
	scene.addEventHandler(KeyEvent.ANY, ke -> {
	    if (ke.getEventType() == KeyEvent.KEY_PRESSED) {
		switch (ke.getCode()) {
		    case Q:
			up = true;
			break;
		    case E:
			down = true;
			break;
		    case W:
			fwd = true;
			break;
		    case S:
			back = true;
			break;
		    case A:
			strafeL = true;
			break;
		    case D:
			strafeR = true;
			break;
		    case SHIFT:
			shift = true;
			moveSpeed = 20;
			break;
		}
	    } else if (ke.getEventType() == KeyEvent.KEY_RELEASED) {
		switch (ke.getCode()) {
		    case Q:
			up = false;
			break;
		    case E:
			down = false;
			break;
		    case W:
			fwd = false;
			break;
		    case S:
			back = false;
			break;
		    case A:
			strafeL = false;
			break;
		    case D:
			strafeR = false;
			break;
		    case SHIFT:
			moveSpeed = 10;
			shift = false;
			break;
		}
	    }
	    ke.consume();
	});
	scene.addEventHandler(MouseEvent.ANY, me -> {
	    if (me.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
		mousePosX = me.getSceneX();
		mousePosY = me.getSceneY();
		mouseOldX = me.getSceneX();
		mouseOldY = me.getSceneY();

	    } else if (me.getEventType().equals(MouseEvent.MOUSE_DRAGGED)) {
		mouseOldX = mousePosX;
		mouseOldY = mousePosY;
		mousePosX = me.getSceneX();
		mousePosY = me.getSceneY();
		mouseDeltaX = (mousePosX - mouseOldX);
		mouseDeltaY = (mousePosY - mouseOldY);

		mouseSpeed = 1.0;
		mouseModifier = 0.1;

		if (me.isPrimaryButtonDown()) {
		    if (me.isControlDown()) {
			mouseSpeed = 0.1;
		    }
		    if (me.isShiftDown()) {
			mouseSpeed = 1.0;
		    }
		    t.setX(getPosition().getX());
		    t.setY(getPosition().getY());
		    t.setZ(getPosition().getZ());

		    affine.setToIdentity();

		    rotateY.setAngle(
			    Utils.clamp(-360, ((rotateY.getAngle() + mouseDeltaX * (mouseSpeed * mouseModifier)) % 360 + 540) % 360 - 180, 360)
		    ); // horizontal                
		    rotateX.setAngle(
			    Utils.clamp(-45, ((rotateX.getAngle() - mouseDeltaY * (mouseSpeed * mouseModifier)) % 360 + 540) % 360 - 180, 35)
		    ); // vertical
		    affine.prepend(t.createConcatenation(rotateY.createConcatenation(rotateX)));

		} else if (me.isSecondaryButtonDown()) {
		    /*
		     init zoom?
		     */
		} else if (me.isMiddleButtonDown()) {
		    /*
		     init panning?
		     */
		}
	    }
	});

	scene.addEventHandler(ScrollEvent.ANY, se -> {

	    if (se.getEventType().equals(ScrollEvent.SCROLL_STARTED)) {

	    } else if (se.getEventType().equals(ScrollEvent.SCROLL)) {

	    } else if (se.getEventType().equals(ScrollEvent.SCROLL_FINISHED)) {

	    }
	});
    }

    private void initializeCamera() {
	getCamera().setNearClip(0.1);
	getCamera().setFarClip(100000);
	getCamera().setFieldOfView(42);
	getCamera().setVerticalFieldOfView(true);
	//getCamera().getTransforms().add(new Rotate(180, Rotate.Z_AXIS));
	root.getChildren().add(getCamera());
    }

    private void startUpdateThread() {
	new AnimationTimer() {
	    @Override
	    public void handle(long now) {
		update();
	    }
	}.start();
    }
    /*==========================================================================
     Movement
     */

    private void moveForward() {
	affine.setTx(getPosition().getX() + moveSpeed * getN().getX());
	affine.setTy(getPosition().getY() + moveSpeed * getN().getY());
	affine.setTz(getPosition().getZ() + moveSpeed * getN().getZ());
    }

    private void strafeLeft() {
	affine.setTx(getPosition().getX() + moveSpeed * -getU().getX());
	affine.setTy(getPosition().getY() + moveSpeed * -getU().getY());
	affine.setTz(getPosition().getZ() + moveSpeed * -getU().getZ());
    }

    private void strafeRight() {
	affine.setTx(getPosition().getX() + moveSpeed * getU().getX());
	affine.setTy(getPosition().getY() + moveSpeed * getU().getY());
	affine.setTz(getPosition().getZ() + moveSpeed * getU().getZ());
    }

    private void moveBack() {
	affine.setTx(getPosition().getX() + moveSpeed * -getN().getX());
	affine.setTy(getPosition().getY() + moveSpeed * -getN().getY());
	affine.setTz(getPosition().getZ() + moveSpeed * -getN().getZ());
    }

    private void moveUp() {
	affine.setTx(getPosition().getX() + moveSpeed * -getV().getX());
	affine.setTy(getPosition().getY() + moveSpeed * -getV().getY());
	affine.setTz(getPosition().getZ() + moveSpeed * -getV().getZ());
    }

    private void moveDown() {
	affine.setTx(getPosition().getX() + moveSpeed * getV().getX());
	affine.setTy(getPosition().getY() + moveSpeed * getV().getY());
	affine.setTz(getPosition().getZ() + moveSpeed * getV().getZ());
    }

    /*==========================================================================
     Properties
     */
    private final ReadOnlyObjectWrapper<PerspectiveCamera> camera = new ReadOnlyObjectWrapper<>(this, "camera", new PerspectiveCamera(true));

    public final PerspectiveCamera getCamera() {
	return camera.get();
    }

    public ReadOnlyObjectProperty cameraProperty() {
	return camera.getReadOnlyProperty();
    }

    /*==========================================================================
     Callbacks    
     | R | Up| F |  | P|
     U |mxx|mxy|mxz|  |tx|
     V |myx|myy|myz|  |ty|
     N |mzx|mzy|mzz|  |tz|

     */
//Forward / look direction    
    private final Callback<Transform, Point3D> F = (a) -> {
	return new Point3D(a.getMzx(), a.getMzy(), a.getMzz());
    };
    private final Callback<Transform, Point3D> N = (a) -> {
	return new Point3D(a.getMxz(), a.getMyz(), a.getMzz());
    };
// up direction
    private final Callback<Transform, Point3D> UP = (a) -> {
	return new Point3D(a.getMyx(), a.getMyy(), a.getMyz());
    };
    private final Callback<Transform, Point3D> V = (a) -> {
	return new Point3D(a.getMxy(), a.getMyy(), a.getMzy());
    };
// right direction
    private final Callback<Transform, Point3D> R = (a) -> {
	return new Point3D(a.getMxx(), a.getMxy(), a.getMxz());
    };
    private final Callback<Transform, Point3D> U = (a) -> {
	return new Point3D(a.getMxx(), a.getMyx(), a.getMzx());
    };
//position
    private final Callback<Transform, Point3D> P = (a) -> {
	return new Point3D(a.getTx(), a.getTy(), a.getTz());
    };

    private Point3D getF() {
	return F.call(getLocalToSceneTransform());
    }

    public Point3D getLookDirection() {
	return getF();
    }

    private Point3D getN() {
	return N.call(getLocalToSceneTransform());
    }

    public Point3D getLookNormal() {
	return getN();
    }

    private Point3D getR() {
	return R.call(getLocalToSceneTransform());
    }

    private Point3D getU() {
	return U.call(getLocalToSceneTransform());
    }

    private Point3D getUp() {
	return UP.call(getLocalToSceneTransform());
    }

    private Point3D getV() {
	return V.call(getLocalToSceneTransform());
    }

    public final Point3D getPosition() {
	return P.call(getLocalToSceneTransform());
    }
}
