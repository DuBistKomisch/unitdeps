import java.awt.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

public class Editor extends JFrame
{
  Connection conn;

  public static void main (String args[])
  {
    if (args.length < 1)
    {
      System.out.println("usage: Editor database");
      System.exit(1);
    }

    final String db = args[0];
    SwingUtilities.invokeLater(new Runnable()
      {
        public void run()
        {
          Editor ed = new Editor(db);
          ed.setVisible(true);
        }
      }
    );
  }

  public Editor (String db)
  {
    // import database driver and init connection
    try
    {
      Class.forName("org.sqlite.JDBC");
      conn = DriverManager.getConnection("jdbc:sqlite:" + db);
    }
    catch (ClassNotFoundException e)
    {
      System.out.println("missing SQLite JDBC driver: run with -cp sqlite-jdbc-X.X.X.jar");
      System.out.println("latest jar file available at https://bitbucket.org/xerial/sqlite-jdbc");
      System.exit(2);
    }
    catch (SQLException e)
    {
      // error
    }

    initUI();
  }

  private void initUI ()
  {
    // init window
    setTitle("UnitDeps Database Editor");
    setSize(640, 480);
    setDefaultCloseOperation(EXIT_ON_CLOSE);

    // init layout
    JPanel panel = new JPanel();
    getContentPane().add(panel);
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    // top panel
    JLabel course = new JLabel("COSC1234: Advanced IP Backtracing");
    JButton previous = new JButton("Previous");
    JButton save = new JButton("Save");
    JButton next = new JButton("Next");

    JPanel top = new JPanel();
    top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
    top.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    top.add(course);
    top.add(Box.createHorizontalGlue());
    top.add(previous);
    top.add(Box.createRigidArea(new Dimension(10, 0)));
    top.add(save);
    top.add(Box.createRigidArea(new Dimension(10, 0)));
    top.add(next);

    // centre
    JTextArea desc = new JTextArea("You are expected to have passed COSC1001, COSC1002 or COSC1003, as well as COSC1101 or have experience with making GUIs in Visual Basic.");
    desc.setLineWrap(true);
    desc.setWrapStyleWord(true);

    JPanel middle = new JPanel();
    middle.setLayout(new BorderLayout());
    middle.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
    middle.add(desc, BorderLayout.CENTER);

    // bottom panel
    ArrayList<JPanel> columns = new ArrayList<JPanel>();

    JPanel column = new JPanel();
    column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
    column.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
    column.setAlignmentY(Component.BOTTOM_ALIGNMENT);
    column.add(new JButton("COSC1001"));
    column.add(new JButton("COSC1002"));
    column.add(new JButton("COSC1003"));
    column.add(Box.createRigidArea(new Dimension(0, 10)));
    column.add(new JButton("..."));
    columns.add(column);

    column = new JPanel();
    column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
    column.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
    column.setAlignmentY(Component.BOTTOM_ALIGNMENT);
    column.add(new JButton("COSC1101"));
    column.add(new JButton("Extra"));
    column.add(Box.createRigidArea(new Dimension(0, 10)));
    column.add(new JButton("..."));
    columns.add(column);

    JButton addClause = new JButton("Add Clause");
    addClause.setAlignmentY(Component.BOTTOM_ALIGNMENT);

    JPanel bottom = new JPanel();
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
    bottom.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    for (JPanel c : columns)
    {
      bottom.add(c);
    }
    bottom.add(addClause);

    // layout
    panel.add(top);
    panel.add(middle);
    panel.add(bottom);
  }
}
