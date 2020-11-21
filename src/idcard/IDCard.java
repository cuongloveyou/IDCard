/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package idcard;

import java.io.BufferedReader;
import java.util.List;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author qcuon
 */
public class IDCard {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        System.load("D:\\OpenCV\\opencv\\build\\java\\x64\\opencv_java440.dll");
        String file = "D:\\ProjectI\\imgEx1.jpg";
//        Process p = Runtime.getRuntime().exec("cmd /c start cmd.exe /K \"cd cropImg && python YOLO.py -i "
//                + file + " -cl yolo.names -w yolov4-custom_final.weights -c yolov4-custom.cfg && exit\"");
//        BufferedReader is
//                = new BufferedReader(new InputStreamReader(p.getInputStream()));
//        // reading the output 
//        while (is.readLine() != null);
        Scanner sc = new Scanner(new File("D:\\DATN\\NetBeans\\IDCard\\cropImg\\boxes.txt"));
        int classId, x, y, w, h;
        Rect boxNE = null; // box chua Quoc Huy
        ArrayList<Rect> cornerList = new ArrayList<>();
        sc.nextLine();
        while (sc.hasNextLine()) {
            classId = sc.nextInt();
            x = sc.nextInt();
            y = sc.nextInt();
            w = sc.nextInt();
            h = sc.nextInt();
            if (classId == 1) {
                boxNE = new Rect(x, y, w, h);
            } else {
                cornerList.add(new Rect(x, y, w, h));
            }
            System.out.println(classId + " " + x + " " + y + " " + w + " " + h);
        }
        Mat oriMat = Imgcodecs.imread(file);
        IDCard iDCard = new IDCard();
        Mat cropMat = iDCard.cropImg(oriMat, boxNE, cornerList);
        Imgcodecs.imwrite("D:/ProjectI/cropImg.jpg", cropMat);

        //file=args[0];
        Mat outGray = new Mat(new Size(500, 300), CvType.CV_16F);
        Imgproc.cvtColor(cropMat, outGray, Imgproc.COLOR_RGB2GRAY);
        //Mat outGray = Imgcodecs.imread(file, 2); // doc anh gray
        Core.bitwise_not(outGray, outGray);
        Mat outBW = new Mat(new Size(500, 300), CvType.CV_16F);
        Imgproc.cvtColor(cropMat, outBW, Imgproc.COLOR_RGB2GRAY);
        //Mat outBW = Imgcodecs.imread(file, 2);
        Core.bitwise_not(outBW, outBW);         // dao tat ca cac bit
        Imgcodecs.imwrite("D:/ProjectI/gray.jpg", outBW);
        Mat outThresh = outBW;
        Imgproc.threshold(outBW, outThresh, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU); // nhi phan hoa
        Imgcodecs.imwrite("D:/ProjectI/thresh.jpg", outThresh);

        List<MatOfPoint> contours = new ArrayList<>(); // matofpoint??? // ve contours
        Mat hierarchy = new Mat();
        Imgproc.findContours(outThresh, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        Mat drawing = Mat.zeros(outBW.size(), CvType.CV_16F);
        Scalar color = new Scalar(255, 255, 255);
        int s;
        int sImg = outBW.cols() * outBW.rows();
        Rect rect;
        ArrayList<Rect> rectList = new ArrayList<>(); // hcn chua cac contour
        for (int i = 0; i < contours.size(); i++) {
            rect = Imgproc.boundingRect(contours.get(i));
            w = rect.width;
            h = rect.height;
            s = w * h;
            if (50 < s && s < 5000) {
                Imgproc.rectangle(drawing, rect.tl(), rect.br(), color, 1);
                rectList.add(rect);
            }            
        }
        Imgcodecs.imwrite("D:/ProjectI/contours.jpg", drawing);

        // sap xep list tu tren xuong duoi (tang dan theo y)
        Collections.sort(rectList, new Comparator<Rect>() {
            @Override
            public int compare(Rect o1, Rect o2) {
                return o1.y > o2.y ? 1 : -1;
            }
        });
        ArrayList<ArrayList<Rect>> lineList = new ArrayList<>(); // danh sach cac dong
        ArrayList subLineList = new ArrayList();                 // danh sach cac chu cua tung dong
        subLineList.add(rectList.get(0));
        for (int i = 1; i < rectList.size(); i++) {
            if (rectList.get(i).y - rectList.get(i - 1).y > 10) {
                lineList.add(subLineList);
                subLineList = new ArrayList();
            }
            subLineList.add(rectList.get(i));
        }
        lineList.add(subLineList);
        System.out.println(lineList.size());
        // xac dinh box can cuoc cong dan
        Rect rectCCCD = getRect(lineList.get(2));
        System.out.println(rectCCCD.x);
        // tao cac hcn dong moi bao quanh cac hcn chu cac tu
        ArrayList<Rect> lines = new ArrayList<>();
        for (ArrayList<Rect> mLineList : lineList) {
//            // sort theo x
//            Collections.sort(mLineList, new Comparator<Rect>() {
//                @Override
//                public int compare(Rect o1, Rect o2) {
//                    return o1.x > o2.x ? 1 : -1;
//                }
//            });
//            int i = 0;
//            boolean check = false;
//            while (i < mLineList.size() && !check) {
//                float xrate = (float) (mLineList.get(i).x - xQH) / (rectCCCD.x - xQH);
//                float yrate = (float) (mLineList.get(i).y - yQH) / (rectCCCD.y - yQH);
//                if ((xrate > 1.12)
//                        && (yrate > 1.5)) {
//                    check = true;
//                }
//                i++;
//            }
//            i--;
//            System.out.println((mLineList.get(i).x - xQH) + " " + (rectCCCD.x - xQH) + " " + (mLineList.get(i).y - yQH) + " " + (rectCCCD.y - yQH) + check);
//            ArrayList<Rect> newLineList = new ArrayList<>();
//            for (; i < mLineList.size(); i++) {
//                newLineList.add(mLineList.get(i));
//            }
//            if (!newLineList.isEmpty()) {
//                // sap xep list tu tren xuong duoi (tang dan theo y)
//                Collections.sort(newLineList, new Comparator<Rect>() {
//                    @Override
//                    public int compare(Rect o1, Rect o2) {
//                        return o1.y > o2.y ? 1 : -1;
//                    }
//                });
//                Rect rect1 = getRect(newLineList);
//                lines.add(rect1);
//            }
            Rect rect1 = getRect(mLineList);
            lines.add(rect1);
            if (rect1.width > 0.75 * outThresh.width()) {
                // sort theo x
                Collections.sort(mLineList, new Comparator<Rect>() {
                    @Override
                    public int compare(Rect o1, Rect o2) {
                        return o1.x > o2.x ? 1 : -1;
                    }
                });
                ArrayList<Rect> newList = new ArrayList<>();
                newList.add(mLineList.get(0));
                int i = 1;
                while ((mLineList.get(i - 1).y - mLineList.get(i).y < 20) && (i < mLineList.size() - 1)) {
                    newList.add(mLineList.get(i++));
                }
                lines.add(getRect(newList));
                newList = new ArrayList<>();
                while (i < mLineList.size()) {
                    newList.add(mLineList.get(i++));
                }
                lines.add(getRect(newList));
            } else {
                lines.add(rect1);
            }
        }
        // sap xep list tu tren xuong duoi (tang dan theo y)
        Collections.sort(lines, new Comparator<Rect>() {
            @Override
            public int compare(Rect o1, Rect o2) {
                return o1.y > o2.y ? 1 : -1;
            }
        });
        // ve ra xem thu
        Mat contourMat = outGray;
        for (Rect line : lines) {
            Imgproc.rectangle(contourMat, line.tl(), line.br(), color, 1);
        }
        Imgcodecs.imwrite("D:/ProjectI/contoursLine.jpg", contourMat);
        Mat so = new Mat(outGray, lines.get(3));
        Mat ten = new Mat(outGray, lines.get(4));
        Mat ngay = new Mat(outGray, lines.get(5));
        Mat gioi = new Mat(outGray, lines.get(6));
        Mat que = new Mat(outGray, lines.get(7));
        Mat thuong = new Mat(outGray, lines.get(8));
        Mat thuong1 = new Mat(outGray, lines.get(9));
        Mat han = new Mat(outGray, lines.get(10));
        Imgcodecs.imwrite("D:/ProjectI/1so.jpg", so);
        Imgcodecs.imwrite("D:/ProjectI/1ten.jpg", ten);
        Imgcodecs.imwrite("D:/ProjectI/1ngay.jpg", ngay);
        Imgcodecs.imwrite("D:/ProjectI/1gioi.jpg", gioi);
        Imgcodecs.imwrite("D:/ProjectI/1que.jpg", que);
        Imgcodecs.imwrite("D:/ProjectI/1thuong.jpg", thuong);
        Imgcodecs.imwrite("D:/ProjectI/1thuong1.jpg", thuong1);
        Imgcodecs.imwrite("D:/ProjectI/1han.jpg", han);
        System.out.println("Xu ly xong!!!");
    }

    public class dis {
        int id;
        int distance;
        public dis(int id, int distance) {
            this.id = id;
            this.distance = distance;
        }
    }

    // ham tinh tam cua box
    public Point getCenterBox(Rect rect) {
        return new Point(rect.x + rect.width / 2, rect.y + rect.height / 2);
    }

    // noi rong box chung minh 
    public Point fixCenterBox(Point centerBoxNE, Point mPoint) {
        double x = centerBoxNE.x > mPoint.x ? mPoint.x - 5 : mPoint.x + 5;
        double y = centerBoxNE.y > mPoint.y ? mPoint.y - 5 : mPoint.y + 5;
        return new Point(x, y);
    }

    public Mat cropImg(Mat oriMat, Rect boxNe, ArrayList<Rect> cornerList) {
        ArrayList<dis> disList = new ArrayList<>();
        for (int i = 0; i < cornerList.size(); i++) {
            int m = (int) Math.sqrt(Math.pow(cornerList.get(i).x - boxNe.x, 2) 
                        + Math.pow(cornerList.get(i).y - boxNe.y, 2));
            disList.add(new dis(i, m));
        }
        // sap xep list tu tren xuong duoi (tang dan theo khoang cach)
        Collections.sort(disList, new Comparator<dis>() {
            @Override
            public int compare(dis o1, dis o2) {
                return o1.distance > o2.distance ? 1 : -1;
            }
        });
        //toa do tam box cua cac goc
        Point top_left = getCenterBox(cornerList.get(disList.get(0).id));
        Point bottom_left = getCenterBox(cornerList.get(disList.get(1).id));
        Point top_right = getCenterBox(cornerList.get(disList.get(2).id));
        Point bottom_right = getCenterBox(cornerList.get(disList.get(3).id));
        Point centerBoxNe = getCenterBox(boxNe);
        // fix lai toa do cac goc 1 chut
        top_left = fixCenterBox(centerBoxNe, top_left);
        bottom_left = fixCenterBox(centerBoxNe, bottom_left);
        top_right = fixCenterBox(centerBoxNe, top_right);
        bottom_right = fixCenterBox(centerBoxNe, bottom_right);

        MatOfPoint2f src = new MatOfPoint2f(top_left, top_right, bottom_right, bottom_left);
        MatOfPoint2f dst = new MatOfPoint2f(new Point(0, 0), new Point(500, 0), new Point(500, 300), new Point(0, 300));
        Mat m = Imgproc.getPerspectiveTransform(src, dst); // dua vao bao cao 2 ham nay
        Mat out = new Mat(new Size(500, 300), CvType.CV_16F);
        Imgproc.warpPerspective(oriMat, out, m, new Size(500, 300));
        return out;
    }

    // ve box bao quanh LineList da duoc sort theo y
    public static Rect getRect(ArrayList<Rect> mLineList) {
        // cac tu da duoc sap xep theo y tu truoc
        int yRect = mLineList.get(0).y; // y cua hcn moi la y cua tu dau tien (sort theo y)
        int hRect = 0;                  // chieu cao la max cua h tat cac tu
        int xRect = mLineList.get(0).x;  // x la x cua tu dau tien (sort theo x)
        int xMax = 0, csXMax = 0;
        for (int i = 0; i < mLineList.size(); i++) {
            hRect = Integer.max(hRect, mLineList.get(i).y + mLineList.get(i).height - yRect);
            xRect = Integer.min(xRect, mLineList.get(i).x);
            if (xMax < mLineList.get(i).x) {
                xMax = mLineList.get(i).x;
                csXMax = i;
            }
        }
        int wRect = mLineList.get(csXMax).x - xRect + mLineList.get(csXMax).width;
        // chieu rong =  x cuoi - x dau + w cua cuoi
        //return new Rect(xRect, yRect, wRect, hRect);
        return new Rect(xRect - 1, yRect - 1, wRect + 2, hRect + 2);
    }
}
