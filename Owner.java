import java.awt.*;
import javax.swing.*;
public class Owner extends JFrame {
  private String OwnerName;
  public Owner(String OwnerName) {
      this.OwnerName = OwnerName;
      setTitle("Client Interface");
      setSize(700, 500);
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      setLocationRelativeTo(null);
      
      
      JLabel label = new JLabel("Welcome, " + OwnerName + "!", SwingConstants.CENTER);
      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
      mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));// padding top,left,bottom, right

      mainPanel.setBackground(new Color(187, 213, 237));
      add(mainPanel);
      mainPanel.add(label);
      setVisible(true);
  }
  
  public void rentCar() {
      System.out.println("Car rented!");
  }
}
