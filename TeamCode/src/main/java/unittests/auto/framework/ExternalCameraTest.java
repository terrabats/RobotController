package unittests.auto.framework;

import autoutil.vision.TeamElementScanner;
import unittests.auto.AutoUnitTest;

import static global.General.bot;
import static global.General.log;

public class ExternalCameraTest extends AutoUnitTest {
    /**
     * Scanner for team element
     */
    TeamElementScanner teamElementScanner = new TeamElementScanner();


    /**
     * Team element scanner setting and starting
     */
    @Override
    public void init() {
        camera.setExternalScanner(teamElementScanner);
        camera.startExternalCamera();
    }

    /**
     * Loop through and log fps
     */
    @Override
    protected void run() {
        whileActive(() -> {
            log.show("External Camera FPS", camera.getInternalFPS());
            log.show("Detected Case", teamElementScanner.getCase());
        });
    }

    /**
     * Stop external camera
     */
    @Override
    public void stop() {
        camera.stopExternalCamera();
    }
}
