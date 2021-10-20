/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

/**
 *
 * @author barry
*/
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ServerFileHandler {
    private String filename;
    
    public ServerFileHandler(String pFilename){
        filename = System.getProperty("user.dir") + "\\" + pFilename;
        System.out.println(filename);
    }
    
    
    
    
    public List<String> readlines(){
        /**
         * This function will read each line of the file 1 by 1 and return a list of the lines in the file.
         */
         
        List<String> lines = new ArrayList<>();
        try (BufferedReader read = new BufferedReader(new FileReader(filename))){
            for(String line; (line = read.readLine()) != null;){               
                lines.add(line);              
            }
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
        return lines;        
    }
    
    public boolean writeline(String line){
        /**
         * This function takes the line given and writes it to the file (with a newline character added to the end to ensure the next input is on the next line)
         */
      
        try{
            File file = new File(filename);
            FileWriter write = new FileWriter(file,true);
            write.write(line+"\n");
            write.close();
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
        return true;
    }
    
     public boolean replaceline(String original, String replacement){
        /**
         * This function iterates through each line in the file and checks to see if it is the same as the line to be removed.
         * If the line is not the one being removed it will be added to temp.txt. Once all lines have been iterated through the 
         * original file is deleted and the old one renamed field should be 0 for deletion by username only, 1 for deletion by 
         * password only
         */

        try{
            File infile = new File(filename);
            File tempfile = new File("temp.txt");
            BufferedReader reader = new BufferedReader(new FileReader(infile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempfile));
            String curline;
            while((curline = reader.readLine()) != null){
                if(curline.equals(original)){
                    writer.write(replacement + "\n");   
                }
                writer.write(curline + "\n");         
            }
            writer.close();
            reader.close();
            if(!infile.delete()){
                System.out.println("could not delete file");
                return false;
            }
            if(!tempfile.renameTo(infile)){
                System.out.println("could not rename file");
                return false;
            }
                       
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
        return true;
    }
    
    public boolean removeline(String line){
        /**
         * This function iterates through each line in the file and checks to see if it is the same as the line to be removed.
         * If the line is not the one being removed it will be added to temp.txt. Once all lines have been iterated through the 
         * original file is deleted and the old one renamed field should be 0 for deletion by username only, 1 for deletion by 
         * password only
         */

        try{
            File infile = new File(filename);
            File tempfile = new File("temp.txt");
            BufferedReader reader = new BufferedReader(new FileReader(infile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempfile));
            String curline;
            while((curline = reader.readLine()) != null){
                if(curline.equals(line)){
                    continue;
                }
                writer.write(curline+"\n");         
            }
            writer.close();
            reader.close();
            if(!infile.delete()){
                System.out.println("could not delete file");
                return false;
            }
            if(!tempfile.renameTo(infile)){
                System.out.println("could not rename file");
                return false;
            }
                       
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    return true;
    }
    
    
    public boolean updateFieldInfo(String line){
        /**
         * This function iterates through each line in the file and checks to see if it is the same as the line to be removed.
         * If the line is not the one being removed it will be added to temp.txt. Once all lines have been iterated through the 
         * original file is deleted and the old one renamed field should be 0 for deletion by username only, 1 for deletion by 
         * password only
         */

        try{
            File infile = new File(filename);
            if(!infile.delete()){
                System.out.println("could not delete file");
                return false;
            }
            File tempfile = new File(filename);
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempfile));
            writer.write(line+"\n");         

            writer.close();
                                 
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
    return true;
    }
}
