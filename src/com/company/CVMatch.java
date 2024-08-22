package com.company;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class CVMatch {

    private Mat img;
    private Mat templ;

    public CVMatch() {
        templ = new Mat();
        img = new Mat();
    }

    public void setImg(Mat m) {
        img = m;
    }

    public void setTempl(Mat m) {
        templ = m;
    }

    public void setImg(BufferedImage i) {
        img = img2Mat(i);
    }

    public void setTempl(BufferedImage i) {
        templ = img2Mat(i);
    }

    public java.awt.Point match(double threshold) {
        Mat result = new Mat();
        Imgproc.matchTemplate(img, templ, result, Imgproc.TM_CCOEFF_NORMED);
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
        if(mmr.maxVal >= threshold)
            return new java.awt.Point((int)mmr.maxLoc.x, (int)mmr.maxLoc.y);
        return null;
    }

    public java.awt.Point match(BufferedImage i1,  BufferedImage i2, double threshold) throws IOException {
        Mat i = img2Mat(i1);//img
        Mat t = img2Mat(i2);//template
        Mat result = new Mat();
        Imgproc.matchTemplate(i, t, result, Imgproc.TM_CCOEFF_NORMED);
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
        System.out.println(mmr.maxVal);
        if(mmr.maxVal >= threshold)
            return new java.awt.Point((int)mmr.maxLoc.x, (int)mmr.maxLoc.y);
        return null;
    }

    public Mat img2Mat(BufferedImage in) {
        Mat out;
        byte[] data;
        int r, g, b;

        if (in.getType() == BufferedImage.TYPE_INT_RGB) {
            out = new Mat(in.getHeight(), in.getWidth(), CvType.CV_8UC3);
            data = new byte[in.getWidth() * in.getHeight() * (int) out.elemSize()];
            int[] dataBuff = in.getRGB(0, 0, in.getWidth(), in.getHeight(), null, 0, in.getWidth());
            for (int i = 0; i < dataBuff.length; i++) {
                data[i * 3] = (byte) ((dataBuff[i] >> 0) & 0xFF);
                data[i * 3 + 1] = (byte) ((dataBuff[i] >> 8) & 0xFF);
                data[i * 3 + 2] = (byte) ((dataBuff[i] >> 16) & 0xFF);
            }
        } else {
            out = new Mat(in.getHeight(), in.getWidth(), CvType.CV_8UC1);
            data = new byte[in.getWidth() * in.getHeight() * (int) out.elemSize()];
            int[] dataBuff = in.getRGB(0, 0, in.getWidth(), in.getHeight(), null, 0, in.getWidth());
            for (int i = 0; i < dataBuff.length; i++) {
                r = (byte) ((dataBuff[i] >> 0) & 0xFF);
                g = (byte) ((dataBuff[i] >> 8) & 0xFF);
                b = (byte) ((dataBuff[i] >> 16) & 0xFF);
                data[i] = (byte) ((0.21 * r) + (0.71 * g) + (0.07 * b));
            }
        }
        out.put(0, 0, data);
        return out;
    }
}
