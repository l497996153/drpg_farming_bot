package com.company;

import java.awt.*;

public class Main {

    static GrindRobot robot;

    public static void main(String[] args) throws AWTException {
        nu.pattern.OpenCV.loadLocally();
        robot = new GrindRobot();
        robot.run();
    }

}
