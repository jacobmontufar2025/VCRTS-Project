import java.awt.*;
import javax.swing.*;
public class Client extends JFrame {
  private String clientName;
  public Client(String clientName) {
      this.clientName = clientName;
      setTitle("Client Interface");
      setSize(600, 400);
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      setLocationRelativeTo(null);
      
      JLabel label = new JLabel("Welcome, " + clientName + "!", SwingConstants.CENTER);
      add(label);
      setVisible(true);
  }
  
  public void submitJob() {
      System.out.println("Client job submitted!");
  }
}
