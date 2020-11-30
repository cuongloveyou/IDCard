/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package idcard;

import java.util.ArrayList;
import org.opencv.core.Rect;

/**
 *
 * @author qcuon
 */
public class InforList {

    ArrayList<Rect> list = new ArrayList<>();
    ArrayList<String> title = new ArrayList<>();

    public InforList() {
    }

    void add(Rect rect, String title) {
        list.add(rect);
        this.title.add(title);
    }

    Rect update(int i, Rect preBox) {
        if (i == 1) { // box ho ten ta mo rong chieu cao nham lay dau cho chuan xac
            preBox.y -= 2;
            preBox.height += 4;
        }
        else if (i == 5 || i == 6 || i == 7) { // fix tai box que quan 1, thuong tru 1 
            list.get(i + 1).y = Integer.max(list.get(i + 1).y, preBox.y + 10);
        }
        return preBox;
    }
}
