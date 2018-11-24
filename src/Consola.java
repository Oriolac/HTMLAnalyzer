import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.io.FileNotFoundException;

public class Consola {
    private JTextField tfInput;
    private JButton btnSubmit;
    private JPanel jpanel;
    private String output = "logerr.txt";
    private static Dimension dimension;

    public Consola() {
        btnSubmit.addMouseListener(new MouseAdapter() {
        });
        btnSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CheckList text = new CheckList(tfInput.getText(), output);
                try {
                    text.checkErrors();
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
    public static void main(String[] args){
        JFrame frame = new JFrame("Consola");
        frame.setContentPane(new Consola().jpanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        dimension = new Dimension(300,300);
        frame.setSize(dimension);
        frame.setVisible(true);
    }
}
