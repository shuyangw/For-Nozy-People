package application;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;

/*TODO
 * 1.)	While loop within the thread runs freakishly slow, optimize at some point
 * 2.)	Add nice comments
 * 3.) 	Learn threading properly 
 * 4.) 	Consider adding a feature that reads a preset password instead of going into the code to change the password
 */

public class Main extends Application {
	static int imgcount = 0;
	static boolean running;
	static boolean listenForMovement;
	static long startDetectTime;
	
	@Override
	public void start(Stage stage) {
		running = true;
		startDetectTime = 0;
		stage.setAlwaysOnTop(true);
		stage.initStyle(StageStyle.UTILITY);
		
		stage.setTitle("Locker");
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.TOP_LEFT);
		grid.setVgap(5);
		grid.setHgap(5);
		
		
		Scene scene = new Scene(grid, 240, 80);
		Label passWord = new Label("Password:");
		passWord.setTranslateX(5);
		passWord.setFont(new Font(18));
		grid.add(passWord, 0, 0);
		
		
		TextField pwField = new TextField();
		pwField.textProperty().addListener((observable, oldValue, newValue) -> {
			if(newValue.equals("cynthia")){
				System.err.println("successfully exited");
				running = false;
				System.exit(0);
			}
			else{
				if(listenForMovement){
					if(startDetectTime == 0)
						startDetectTime = System.currentTimeMillis() / 1000;
					
					
					if((System.currentTimeMillis() / 1000) - startDetectTime >= 10){
						System.out.println("should have taken");
						Main.captureFrame();
						startDetectTime = 0;
					}
				}
			}
		});
		
		pwField.setTranslateX(5);
		grid.add(pwField, 0, 1);
		

		stage.setScene(scene);
		stage.show();
	}
	
	public static void main(String[] args) throws AWTException {
		loopThread thr = new loopThread();
		thr.start();

		
		launch(args);
	}
	
	public static void captureFrame(){
		final OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
		OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
		try {
			grabber.start();
			Frame frame = grabber.grab();
			IplImage img = converter.convert(frame);
			if (img != null) {
				cvSaveImage((imgcount++) + "-aa.jpg", img);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	static class loopThread extends Thread{
		public void run(){
			Robot rob = null;
			try {
				rob = new Robot();
			}
			catch(AWTException e) {
				e.printStackTrace();
			}
			
			
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			int centerX = screenSize.width/2;
			int centerY = screenSize.height/2;
			
			long elapsedTime;
			long startTime = System.currentTimeMillis() / 1000;
			while(running){
				rob.mouseMove(centerX, centerY);
				elapsedTime = (System.currentTimeMillis() / 1000) - startTime;
				if(elapsedTime > 60){
					listenForMovement = true;
				}
				
				if(imgcount >= 100){
					Runtime runtime = Runtime.getRuntime();
				    try {
						@SuppressWarnings("unused")
						Process proc = runtime.exec("shutdown -s -t 0");
					}
				    catch (IOException e) {
						e.printStackTrace();
					}
					System.exit(0);
				}
				
			}
		}
	}
}
