/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package idcard;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author qcuon
 */
public class testCmd {

    public static void main(String[] args) throws IOException {
        try 
        { 
            // create a new process 
            System.out.println("Creating Process"); 
            Process p = Runtime.getRuntime().exec("notepad.exe"); 
      
            // cause this process to stop 
                // until process p is terminated 
            p.waitFor(); 
      
            // when you manually close notepad.exe 
                // program will continue here 
            System.out.println("Waiting over"); 
        }  
        catch (Exception ex)  
        { 
            ex.printStackTrace(); 
        } 
    }
}
