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
        String file = "D:\\DATN\\Img\\sample\\standard\\imgEx1.jpg";
        Process p = Runtime.getRuntime().exec("cmd /c start cmd.exe /K \"cd cropImg && python YOLO.py -i "
                + file + " -cl yolo.names -w yolov4-custom_final.weights -c yolov4-custom.cfg && exit\"");
        BufferedReader is
                = new BufferedReader(new InputStreamReader(p.getInputStream()));
        // reading the output 
        while (is.readLine() != null);
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

        int imgW = cropMat.cols();
        int imgH = cropMat.rows();
        //file=args[0];
        Mat outGray = new Mat(new Size(imgW, imgH), CvType.CV_16F);
        Imgproc.cvtColor(cropMat, outGray, Imgproc.COLOR_RGB2GRAY);
        //Imgproc.equalizeHist(outGray, outGray);
        //Mat outGray = Imgcodecs.imread(file, 2); // doc anh gray
        Core.bitwise_not(outGray, outGray);

        Mat outBW = new Mat(new Size(imgW, imgH), CvType.CV_16F);
        Imgproc.cvtColor(cropMat, outBW, Imgproc.COLOR_RGB2GRAY);
        //Imgproc.equalizeHist(outBW, outBW);
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
        Scalar color1 = new Scalar(222, 111, 111);
        int s;
        int sImg = outBW.cols() * outBW.rows();
        Rect rect;
        ArrayList<Rect> rectList = new ArrayList<>(); // box chua cac contour
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

        // list toa do cung cua cac tt can detect
        InforList inforList = new InforList();
        inforList.add(new Rect(new Point(0.432 * imgW, 0.24 * imgH), new Point(imgW, 0.35 * imgH)), "so");// 0 so
        inforList.add(new Rect(new Point(0.44 * imgW, 0.35 * imgH), new Point(imgW, 0.48 * imgH)), "ten");// 1 ten
        inforList.add(new Rect(new Point(0.58 * imgW, 0.48 * imgH), new Point(imgW, 0.58 * imgH)), "ngay_sinh");// 2 ngay sinh
        inforList.add(new Rect(new Point(0.426 * imgW, 0.58 * imgH), new Point(0.582 * imgW, 0.66 * imgH)), "gioi_tinh");// 3 gioi tinh
        inforList.add(new Rect(new Point(0.704 * imgW, 0.58 * imgH), new Point(imgW, 0.66 * imgH)), "quoc_tich");// 4 quoc tich
        inforList.add(new Rect(new Point(0.447 * imgW, 0.66 * imgH), new Point(imgW, 0.735 * imgH)), "que_quan_1");// 5 que dong 1
        inforList.add(new Rect(new Point(0.328 * imgW, 0.735 * imgH), new Point(imgW, 0.78 * imgH)), "que_quan_2");// 6 que dong 2
        inforList.add(new Rect(new Point(0.488 * imgW, 0.78 * imgH), new Point(imgW, 0.862 * imgH)), "thuong_tru_1");// 7 thuong tru 1
        inforList.add(new Rect(new Point(0.35 * imgW, 0.862 * imgH), new Point(imgW, imgH)), "thuong_tru_2");// 8 thuong tru 2
        inforList.add(new Rect(new Point(0.1729 * imgW, 0.926 * imgH), new Point(0.35 * imgW, imgH)), "han_dung");// 9 han

        ArrayList subLineList = new ArrayList();                 // danh sach cac chu cua tung dong
        // tao cac box chua dong moi bao quanh cac box chu cac tu
        ArrayList<Rect> lines = new ArrayList<>();
        // sap xep list tu tren xuong duoi (tang dan theo y)
        Collections.sort(rectList, new Comparator<Rect>() {
            @Override
            public int compare(Rect o1, Rect o2) {
                return o1.y > o2.y ? 1 : -1;
            }
        });
        for (int i = 0; i < inforList.list.size(); i++) {
            Rect boxInfor = inforList.list.get(i);
            subLineList = new ArrayList();
            for (Rect boxText : rectList) {
                if (checkBox(boxText, boxInfor)) { // kiem tra neu box chua chu nam trong box cung thi dua vao dong
                    subLineList.add(boxText);
                }
            }
            if (subLineList.size() > 0) {
                subLineList = fixSubLineList(subLineList);
                Rect r = getRect(subLineList);
                r = inforList.update(i, r);
                lines.add(r);
                Mat outLine = new Mat(outGray, r);
                Imgcodecs.imwrite("D:/ProjectI/1" + inforList.title.get(i) + ".jpg", outLine);
            }
        }

        // ve ra xem thu
        Mat contourMat = outGray;
        for (Rect line : lines) {
            Imgproc.rectangle(contourMat, line.tl(), line.br(), color, 1);
        }
        Imgcodecs.imwrite("D:/ProjectI/contoursLine.jpg", contourMat);
        //for (Rect line : inforList.list) {Imgproc.rectangle(contourMat, line.tl(), line.br(), color1, 1);}Imgcodecs.imwrite("D:/ProjectI/contoursLine.jpg", contourMat);

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

    // loai bo bot box khong thuoc line (co khoang cach toi line > )
    public static ArrayList<Rect> fixSubLineList(ArrayList<Rect> mSubLineList) {
        // sap xep list theo x
        Collections.sort(mSubLineList, new Comparator<Rect>() {
            @Override
            public int compare(Rect o1, Rect o2) {
                return o1.x > o2.x ? 1 : -1;
            }
        });
        ArrayList<Rect> newSubLineList = new ArrayList<>();
        newSubLineList.add(mSubLineList.get(0));
        int i = 0;
        for (int j = 1; j < mSubLineList.size(); j++) {
            if (mSubLineList.get(j).x - newSubLineList.get(i).x < 80) {
                newSubLineList.add(mSubLineList.get(j));
                i++;
            }
        }
        return newSubLineList;
    }

    // kiem tra diem tren trai cua box1 nam trong box2 khong
    public static boolean checkBox(Rect box1, Rect box2) {
        if (box1.x < box2.x || box1.y < box2.y
                || box1.x > box2.x + box2.width || box1.y > box2.y + box2.height) {
            return false;
        }
        return true;
    }

    // ve box bao quanh LineList da duoc sort theo y
    public static Rect getRect(ArrayList<Rect> mLineList) {
        // sap xep list tu tren xuong duoi (tang dan theo y)
        Collections.sort(mLineList, new Comparator<Rect>() {
            @Override
            public int compare(Rect o1, Rect o2) {
                return o1.y > o2.y ? 1 : -1;
            }
        });
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
        hRect = yRect + hRect + 1 < 300 ? hRect + 2 : hRect + 1;
        return new Rect(xRect - 1, yRect - 1, wRect + 2, hRect);
    }

}
