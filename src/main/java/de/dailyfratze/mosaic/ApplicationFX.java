package de.dailyfratze.mosaic;

import de.dailyfratze.mosaic.gui.SimpleFPSCamera;
import de.dailyfratze.mosaic.images.Mosaic;
import de.dailyfratze.mosaic.images.Tile;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import static javafx.application.Application.launch;

/**
 * Mathematics for spherical projections by
 * <a href="http://paulbourke.net/geometry/transformationprojection/">Paul
 * Bourke</a>, basic app idea by
 * <a href="http://wecode4fun.blogspot.co.at">Roland</a>.
 *
 * @author RolandC
 * @author Michael J. Simons
 */
public class ApplicationFX extends Application {

    private static final double CAMERA_INITIAL_DISTANCE = -2050;

    private final Group root;

    private final SimpleFPSCamera fpsCam;

    public ApplicationFX() {
	this.root = new Group();
	this.fpsCam = new SimpleFPSCamera();
	this.fpsCam.getCamera().setTranslateZ(CAMERA_INITIAL_DISTANCE);
	this.root.getChildren().add(fpsCam);
    }

    /**
     * Create an ImageView for the given file.
     *
     * @return
     */
    private ImageView createImageView(final String imageFile) {
	try {
	    Image image = new Image(new FileInputStream(imageFile));

	    ImageView c = new ImageView(image);

	    c.setFitWidth(150);
	    c.setFitWidth(113);
	    c.setPreserveRatio(true);

	    return c;
	} catch (IOException e) {
	    throw new RuntimeException(e);
	}
    }

    @Override
    public void start(Stage primaryStage) {

	// wall. the degrees depend on the distance, image size, translate start points, etc. so these values were just as they fit	
	final double ringEndDeg = 47;
	final double angleInc = 3.5;

	final double r = 1950;
	final double yOffset = 90; // offset per image row
	final double yOffsetInitial = 120; // initial y offset from "floor"

	final double angle2 = Math.PI;

	// The year i started dailyfratze.de
	final int baseYear = 2005;
	
	// Create an observable list, used as a target for the mosaic generator.
	final ObservableList<Tile> tiles = FXCollections.observableArrayList();
	// Observe it	
	tiles.addListener((Change<? extends Tile> change) -> {
	    while (change.next()) {
		if (!change.wasAdded()) {
		    continue;
		}
		Random rr  = new Random();
		// Grab all added tiles and map them to image nodes
		final List<Node> newNodes
			= change.getAddedSubList()
			.stream().map((tile) -> {
			    // Compute spherical projection
			    double angle1 = Math.toRadians(ringEndDeg - tile.getX() * angleInc);
			    double x = r * Math.sin(angle1) * Math.cos(angle2);
			    double z = r * Math.cos(angle1) - (tile.getTakenOn().getYear() - baseYear) * 100;
			    
			    final Node rv = createImageView(tile.getAbsoluteFilename());
			    rv.setTranslateX(x);
			    rv.setTranslateY(yOffset * (tile.getY() - 12) - yOffsetInitial);
			    rv.setTranslateZ(z);
			    
			    // rotate towards viewer position
			    final Rotate rx = new Rotate();
			    rx.setAxis(Rotate.Y_AXIS);
			    rx.setAngle(Math.toDegrees(-angle1));
			    rv.getTransforms().addAll(rx);
			    
			    // reflection on bottom row
			    if (tile.getY() == 0) {
				Reflection refl = new Reflection();
				refl.setFraction(0.8f);
				rv.setEffect(refl);
			    }
			    rv.setVisible(true);
			    return rv;
			}).collect(Collectors.toList());

		// Newly created nodes must be added on the JavaFX application thread.
		Platform.runLater(() -> {
		    root.getChildren().addAll(newNodes);
		});
	    }
	});

	final Scene scene = new Scene(root, 1600, 900, Color.BLACK);
	fpsCam.loadControlsForScene(scene);
	scene.setCamera(fpsCam.getCamera());

	final String library = getParameters().getRaw().get(0);
	final String sourceImage = getParameters().getRaw().get(1);
	// Start a thread creating the mosaic.
	final Thread mosaicThread = new Thread(new Task<List<Tile>>() {
	    @Override
	    protected List<Tile> call() throws Exception {
		final Mosaic mosaic = new Mosaic(library, sourceImage);
		return mosaic.create(tiles);
	    }
	});

	primaryStage.addEventHandler(WindowEvent.WINDOW_SHOWN, (WindowEvent window) -> {
	    mosaicThread.start();
	});	

	primaryStage.setScene(scene);
	primaryStage.show();

    }

    public static void main(String[] args) {
	launch("/Volumes/mosaic.michael.2015-03-30/library", "/Volumes/mosaic.michael.2015-03-30/source-images/2014-10-03_orig.jpg");
    }
}
