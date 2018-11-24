import javax.swing.*;
import java.io.FileNotFoundException;


public class Home {
    JTextField tfInput;
    public static void main(String[] args) {
        CheckList text = new CheckList(".//fichers//SEMIC, Proveedor Global de Soluciones y Servicios IT_files.html", "logerr.txt");
        try {
            text.checkErrors();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}