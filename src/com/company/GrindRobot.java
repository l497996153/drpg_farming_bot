package com.company;

import com.sun.imageio.plugins.common.ImageUtil;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GrindRobot extends Robot implements Runnable {

    private BufferedImage screen;
    private BufferedImage card_slash;
    private BufferedImage digi_text;
    private BufferedImage indaramon_battle;
    private BufferedImage jyureimon_battle;
    private BufferedImage attack;
    private BufferedImage attack2;
    private BufferedImage summon_plus;
    private BufferedImage digi_code_text;
    private BufferedImage evp;
    private BufferedImage summon;
    private BufferedImage summon2;
    private BufferedImage win;
    private BufferedImage lose;
    private BufferedImage shop_close;
    private BufferedImage dice_close;
    private final URL shop_close_url = ImageUtil.class.getResource("/images/shop_close.png");
    private final URL dice_close_url = ImageUtil.class.getResource("/images/dice_close.png");
    private final URL digi_text_url = ImageUtil.class.getResource("/images/drpg.png");
    private final URL card_slash_url = ImageUtil.class.getResource("/images/card_slash.png");
    private final URL indaramon_battle_url = ImageUtil.class.getResource("/images/indaramon_battle.png");
    private final URL jyureimon_battle_url = ImageUtil.class.getResource("/images/jyureimon_battle.png");
    private final URL attack_url = ImageUtil.class.getResource("/images/attack.png");
    private final URL attack2_url = ImageUtil.class.getResource("/images/attack2.png");
    private final URL summon_plus_url = ImageUtil.class.getResource("/images/summon_plus.png");
    private final URL digi_code_text_url = ImageUtil.class.getResource("/images/digi_code_text.png");
    private final URL evp_url = ImageUtil.class.getResource("/images/evp.png");
    private final URL summon_url = ImageUtil.class.getResource("/images/summon.png");
    private final URL summon2_url = ImageUtil.class.getResource("/images/summon2.png");
    private final URL win_url = ImageUtil.class.getResource("/images/win.png");
    private final URL lose_url = ImageUtil.class.getResource("/images/lose.png");
     private final GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    private final int width = gd.getDisplayMode().getWidth();
    private final int height = gd.getDisplayMode().getHeight();
    private Rectangle rect;
    private boolean running;

    private CVMatch cvMatch = new CVMatch();
    private File pathIndaramon;
    private File pathJyureimon;
    private File[] allDigiImageFiles;
    private Mat[] allDigiImages;

    private int x_off = 0;
    private int y_off = 0;

    ArrayList<Point> battlePoints;

    private boolean battleEnded = false;
    private ExecutorService es = Executors.newFixedThreadPool(8);

    public GrindRobot() throws AWTException {
        pathIndaramon = new File("resources/images/indaramon");
        pathJyureimon = new File("resources/images/jyureimon");
        allDigiImageFiles = pathJyureimon.listFiles();//TODO: let user choose then set when running
        allDigiImages = new Mat[allDigiImageFiles.length];
        rect = new Rectangle(width, height);
        running = true;//TODO: change this later
        try {
            card_slash = ImageIO.read(card_slash_url);
            shop_close = ImageIO.read(shop_close_url);
            dice_close = ImageIO.read(dice_close_url);
            digi_text = ImageIO.read(digi_text_url);
            attack = ImageIO.read(attack_url);
            attack2 = ImageIO.read(attack2_url);
            indaramon_battle = ImageIO.read(indaramon_battle_url);
            jyureimon_battle = ImageIO.read(jyureimon_battle_url);
            summon_plus = ImageIO.read(summon_plus_url);
            digi_code_text = ImageIO.read(digi_code_text_url);
            evp = ImageIO.read(evp_url);
            summon = ImageIO.read(summon_url);
            summon2 = ImageIO.read(summon2_url);
            win = ImageIO.read(win_url);
            lose = ImageIO.read(lose_url);
            for(int i = 0; i < allDigiImageFiles.length; i++)
                allDigiImages[i] = Imgcodecs.imread(allDigiImageFiles[i].getPath());
        }catch(IOException ex) {
            ex.printStackTrace();
        }
        battlePoints = new ArrayList<>();
        battlePoints.add(new Point(192, 170));//front middle
        battlePoints.add(new Point(64, 238));//front bottom
        battlePoints.add(new Point(320, 102));//front top
        battlePoints.add(new Point(197, 80));//back top
        battlePoints.add(new Point(90, 153));//back bottom
    }

    public void updateScreen() {
        if(screen != null)
            screen.flush();
        screen = createScreenCapture(rect);
        /*Point offsetPoint = bruteMatch(digi_text, screen);
        if (offsetPoint != null) {
            x_off = offsetPoint.x;
            y_off = offsetPoint.y;
            screen = screen.getSubimage(offsetPoint.x, offsetPoint.y, 798, 630);//width-height of drpg window
        }
        if (bruteMatch(win, screen) != null && !battleEnded)
            battleEnded = true;
        if (bruteMatch(lose, screen) != null && !battleEnded)
            battleEnded = true;
        */
    }

    public static Point bruteMatch(BufferedImage subimage, BufferedImage image) {
        // brute force N^2 check all places in the image
        for (int i = 0; i <= image.getWidth() - subimage.getWidth(); i++) {
            check_subimage:
            for (int j = 0; j <= image.getHeight() - subimage.getHeight(); j++) {
                for (int ii = 0; ii < subimage.getWidth(); ii++) {
                    for (int jj = 0; jj < subimage.getHeight(); jj++) {
                        if (subimage.getRGB(ii, jj) != image.getRGB(i + ii, j + jj)) {
                            continue check_subimage;
                        }
                    }
                }
                // if here, all pixels matched
                return new Point(i, j);
            }
        }
        return null;
    }

    public void locateEntity() throws Exception {
        Point found = null;
        while (running && found == null) {
            while (bruteMatch(evp, screen) == null) {
                Point sc_p = bruteMatch(shop_close, screen);
                Point dc_p = bruteMatch(dice_close, screen);
                if(sc_p != null) {
                    mouseMove((int)sc_p.getX()+(shop_close.getWidth()/2)+x_off, (int)sc_p.getY()+(shop_close.getHeight()/2)+y_off);
                    delay(200);
                    mousePress(KeyEvent.BUTTON1_DOWN_MASK);
                    delay(200);
                    mouseRelease(KeyEvent.BUTTON1_DOWN_MASK);
                    delay(500);
                    System.out.println("Closing shop");
                }else if(dc_p != null) {
                    mouseMove((int)dc_p.getX()+(dice_close.getWidth()/2)+x_off, (int)dc_p.getY()+(dice_close.getHeight()/2)+y_off);
                    delay(200);
                    mousePress(KeyEvent.BUTTON1_DOWN_MASK);
                    delay(200);
                    mouseRelease(KeyEvent.BUTTON1_DOWN_MASK);
                    delay(500);
                    System.out.println("Closing dice game");
                }else {
                    System.out.println("Waiting on EVP to regenerate");
                }
                delay(3000);
                updateScreen();
            }
            System.out.println("Looking for entity...");
            updateScreen();
            cvMatch.setTempl(screen);
            Set<Callable<Point>> callables = new HashSet<>();
            for (Mat i : allDigiImages) {
                callables.add(() -> {
                    cvMatch.setImg(i);
                    return cvMatch.match(0.90);
                });
            }

            java.util.List<Future<Point>> futures = es.invokeAll(callables);
            for(Future<Point> future: futures) {
                found = future.get();
                if(found != null) {
                    break;
                }
            }

            if (found != null)
                startBattle(found);
        }
    }

    public void startBattle(Point p) throws Exception {
        //System.out.println("Found. Attempting to start battle!");
        mouseMove((int)p.getX()+20+x_off, (int)p.getY()+60+y_off);
        delay(200);
        mousePress(KeyEvent.BUTTON1_DOWN_MASK);
        delay(200);
        mouseRelease(KeyEvent.BUTTON1_DOWN_MASK);
        delay(500);

        updateScreen();
        Point csPoint = bruteMatch(card_slash, screen);
        if(csPoint != null) {
            System.out.println("Battle started");
            //megaEvolution(true);
            summonDigi();
            summonDigi();
            summonDigi();
            delay(8000);
            //prepareCards("First Card", "Second Card", "Third Card");
            chooseAttack("F3");
            attackEntity();
        }else{
            locateEntity();
        }
    }

    public void attackEntity() throws Exception {
        updateScreen();
        Point p = bruteMatch(card_slash, screen);
        Point attackPoint;
        Point attack2Point;
        if(running && p != null) {
            for (int i = 0; i < battlePoints.size(); i++) {
                attackPoint = bruteMatch(attack, screen);
                attack2Point = bruteMatch(attack2, screen);
                if (p != null && attackPoint == null && attack2Point == null && !battleEnded) {
                    mouseMove((int) (p.getX() + battlePoints.get(i).getX() + x_off), (int) (p.getY() + battlePoints.get(i).getY() + y_off));
                    delay(200);
                    mousePress(KeyEvent.BUTTON1_DOWN_MASK);
                    delay(200);
                    mouseRelease(KeyEvent.BUTTON1_DOWN_MASK);
                    delay(200);
                } else {
                    break;
                }
                updateScreen();
                p = bruteMatch(card_slash, screen);
                if (bruteMatch(win, screen) != null && !battleEnded)
                    battleEnded = true;
                if (bruteMatch(lose, screen) != null && !battleEnded)
                    battleEnded = true;
            }
            /*attackPoint = bruteMatch(attack, screen);
            attack2Point = bruteMatch(attack2, screen);
            Point be = findBattleEntity("indaramon");
            if(attackPoint == null && attack2Point == null && !battleEnded && be != null) {
                mouseMove((int) (be.getX() + x_off), (int) (p.getY() + be.getY() + y_off));
                delay(200);
                mousePress(KeyEvent.BUTTON1_DOWN_MASK);
                delay(200);
                mouseRelease(KeyEvent.BUTTON1_DOWN_MASK);
                delay(200);
            }*/
            //Collections.reverse(battlePoints);
            if(p != null) {
                System.out.println("Waiting");
                delay(200);
                attackEntity();
            }
        }else if(running) {
            battleEnded = false;
            //lookForDrops();
            locateEntity();
        }
    }

    public void nextDigi() {
        keyPress(KeyEvent.VK_TAB);
        delay(200);
        keyRelease(KeyEvent.VK_TAB);
    }

    public Point findBattleEntity(String s) throws Exception {
        Point p = null;
        if(s.equals("indaramon"))
             p = cvMatch.match(indaramon_battle, screen, 0.95);

        return p;
    }

    public void lookForDrops() {
        System.out.println("Looking for drops");
        keyPress(KeyEvent.VK_ALT);
        delay(1000);
        updateScreen();
        Point digi_code_p = bruteMatch(digi_code_text, screen);
        while(digi_code_p != null) {
            System.out.println("Found Digi Code");
            mouseMove((int)(digi_code_p.getX()+x_off)+digi_code_text.getWidth()/2, (int)(digi_code_p.getY()+y_off+digi_code_text.getHeight()/2));
            delay(200);
            mousePress(KeyEvent.BUTTON1_DOWN_MASK);
            delay(200);
            mouseRelease(KeyEvent.BUTTON1_DOWN_MASK);
            delay(200);
            updateScreen();
            digi_code_p = bruteMatch(digi_code_text, screen);
            delay(2000);
        }
        keyRelease(KeyEvent.VK_ALT);
    }

    public void megaEvolution(boolean megaChoice) {
        if(megaChoice) {
            keyPress(KeyEvent.VK_SHIFT);
            keyPress(KeyEvent.VK_Z);
            delay(100);
            keyRelease(KeyEvent.VK_SHIFT);
            keyRelease(KeyEvent.VK_Z);
        }
    }

    public void prepareCards(String slotOneChoice, String slotTwoChoice, String slotThreeChoice) {
        if(slotOneChoice != null || slotTwoChoice != null || slotThreeChoice != null || !slotOneChoice.equals("None") || !slotTwoChoice.equals("None") || !slotThreeChoice.equals("None")) {

            //Pick the first card slot
            if (slotOneChoice != null && !slotOneChoice.equals("None")) {
                keyPress(KeyEvent.VK_1);
                delay(100);
                keyRelease(KeyEvent.VK_1);
                delay(100);
                switch (slotOneChoice) {
                    case "First Card":
                        keyPress(KeyEvent.VK_1);
                        delay(100);
                        keyRelease(KeyEvent.VK_1);
                        delay(100);
                        break;
                    case "Second Card":
                        keyPress(KeyEvent.VK_2);
                        delay(100);
                        keyRelease(KeyEvent.VK_2);
                        delay(100);
                        break;
                    case "Third Card":
                        keyPress(KeyEvent.VK_3);
                        delay(100);
                        keyRelease(KeyEvent.VK_3);
                        delay(100);
                        break;
                    case "Fourth Card":
                        keyPress(KeyEvent.VK_4);
                        delay(100);
                        keyRelease(KeyEvent.VK_4);
                        delay(100);
                        break;
                    case "Fifth Card":
                        keyPress(KeyEvent.VK_5);
                        delay(100);
                        keyRelease(KeyEvent.VK_5);
                        delay(100);
                        break;
                    case "Sixth Card":
                        keyPress(KeyEvent.VK_6);
                        delay(100);
                        keyRelease(KeyEvent.VK_6);
                        delay(100);
                        break;
                    default:
                    /*keyPress(KeyEvent.VK_1);
                    delay(100);
                    keyRelease(KeyEvent.VK_1);
                    delay(100);
                    break;*/
                }
            }

            if (slotTwoChoice != null && !slotTwoChoice.equals("None")) {
                //Pick the second card slot
                keyPress(KeyEvent.VK_2);
                delay(100);
                keyRelease(KeyEvent.VK_2);
                delay(100);
                switch (slotTwoChoice) {
                    case "First Card":
                        keyPress(KeyEvent.VK_1);
                        delay(100);
                        keyRelease(KeyEvent.VK_1);
                        delay(100);
                        break;
                    case "Second Card":
                        keyPress(KeyEvent.VK_2);
                        delay(100);
                        keyRelease(KeyEvent.VK_2);
                        delay(100);
                        break;
                    case "Third Card":
                        keyPress(KeyEvent.VK_3);
                        delay(100);
                        keyRelease(KeyEvent.VK_3);
                        break;
                    case "Fourth Card":
                        keyPress(KeyEvent.VK_4);
                        delay(100);
                        keyRelease(KeyEvent.VK_4);
                        delay(100);
                        break;
                    case "Fifth Card":
                        keyPress(KeyEvent.VK_5);
                        delay(100);
                        keyRelease(KeyEvent.VK_5);
                        delay(100);
                        break;
                    case "Sixth Card":
                        keyPress(KeyEvent.VK_6);
                        delay(100);
                        keyRelease(KeyEvent.VK_6);
                        delay(100);
                        break;
                    default:
                    /*keyPress(KeyEvent.VK_1);
                    delay(100);
                    keyRelease(KeyEvent.VK_1);
                    delay(100);*/
                        break;
                }
            }

            if (slotThreeChoice != null && !slotThreeChoice.equals("None")) {
                //Pick the second card slot
                keyPress(KeyEvent.VK_3);
                delay(100);
                keyRelease(KeyEvent.VK_3);
                delay(100);
                switch (slotThreeChoice) {
                    case "First Card":
                        keyPress(KeyEvent.VK_1);
                        delay(100);
                        keyRelease(KeyEvent.VK_1);
                        delay(100);
                        break;
                    case "Second Card":
                        keyPress(KeyEvent.VK_2);
                        delay(100);
                        keyRelease(KeyEvent.VK_2);
                        delay(100);
                        break;
                    case "Third Card":
                        keyPress(KeyEvent.VK_3);
                        delay(100);
                        keyRelease(KeyEvent.VK_3);
                        delay(100);
                        break;
                    case "Fourth Card":
                        keyPress(KeyEvent.VK_4);
                        delay(100);
                        keyRelease(KeyEvent.VK_4);
                        delay(100);
                        break;
                    case "Fifth Card":
                        keyPress(KeyEvent.VK_5);
                        delay(100);
                        keyRelease(KeyEvent.VK_5);
                        delay(100);
                        break;
                    case "Sixth Card":
                        keyPress(KeyEvent.VK_6);
                        delay(100);
                        keyRelease(KeyEvent.VK_6);
                        delay(100);
                        break;
                    default:
                    /*keyPress(KeyEvent.VK_1);
                    delay(100);
                    keyRelease(KeyEvent.VK_1);
                    delay(100);*/
                        break;
                }
            }
        }
    }

    public void chooseAttack(String skillChoice) {
        //Press the function key for the selected skill
        switch (skillChoice) {
            case "F1":
                keyPress(KeyEvent.VK_F1);
                delay(100);
                keyRelease(KeyEvent.VK_F1);
                break;
            case "F2":
                keyPress(KeyEvent.VK_F2);
                delay(100);
                keyRelease(KeyEvent.VK_F2);
                break;
            case "F3":
                keyPress(KeyEvent.VK_F3);
                delay(100);
                keyRelease(KeyEvent.VK_F3);
                break;
            default:
                break;
        }
    }

    public void summonDigi() {
        keyPress(KeyEvent.VK_R);
        delay(200);
        keyRelease(KeyEvent.VK_R);
        delay(200);
        updateScreen();
        Point summon_p = bruteMatch(summon_plus, screen);
        if(summon_p != null) {
            mouseMove((int)summon_p.getX()+x_off, (int)summon_p.getY()+y_off);
            delay(200);
            mousePress(KeyEvent.BUTTON1_DOWN_MASK);
            delay(200);
            mouseRelease(KeyEvent.BUTTON1_DOWN_MASK);
            delay(200);
        }
    }

    @Override
    public void run() {
        updateScreen();
        while(running) {
            try {
                if (bruteMatch(card_slash, screen) != null)
                    attackEntity();
                else
                    locateEntity();
            }catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        es.shutdownNow();
        keyRelease(KeyEvent.VK_ALT);
    }
}
