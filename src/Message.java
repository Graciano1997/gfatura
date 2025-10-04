import javax.swing.*;

class MessageType{
  final static int Error =0;
  final static int Warning=1;
  final static int Success =2;
}

public class Message{
   public static void show(String title, String sms, int type) {
   
   if(type==MessageType.Error)
      JOptionPane.showMessageDialog(null,sms,title,JOptionPane.ERROR_MESSAGE);

   if(type==MessageType.Warning)
      JOptionPane.showMessageDialog(null,sms,title,JOptionPane.WARNING_MESSAGE); 
   }
}
