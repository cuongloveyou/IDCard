/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package idcard;
import java.io.File; 
  
import net.sourceforge.tess4j.Tesseract; 
import net.sourceforge.tess4j.TesseractException;
/**
 *
 * @author qcuon
 */
public class tess {
    public static void main(String[] args) 
    { 
        Tesseract tesseract = new Tesseract();  
        tesseract.setLanguage("vie");
        try { 
  //BủI ũửc THMG
  // Bủì nủc mổme
  // em nức niềm
  //BỦI Đức THẢNG
            tesseract.setDatapath("D:\\DATN\\NetBeans\\IDCard\\tessdata");
  
            // the path of your tess data folder 
            // inside the extracted file 
            String text 
                = tesseract.doOCR(new File("D:\\ProjectI\\1han_dung.jpg")); 
  
            // path of your image file 
            System.out.print(text); 
        } 
        catch (TesseractException e) { 
            e.printStackTrace(); 
        } 
    }  
}
