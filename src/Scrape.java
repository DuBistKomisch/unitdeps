import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

public class Scrape
{
  public static void main (String args[])
    throws Exception
  {
    if (args.length < 2)
    {
      System.out.println("usage: Scrape institute query");
      System.exit(1);
    }

    // import JDBC SQLite driver
    try
    {
      Class.forName("org.sqlite.JDBC");
    }
    catch (ClassNotFoundException e)
    {
      System.out.println("missing SQLite JDBC driver: run with -cp sqlite-jdbc-X.X.X.jar");
      System.out.println("latest jar file available at https://bitbucket.org/xerial/sqlite-jdbc");
      System.exit(2);
    }

    String institute = args[0];
    String query = args[1];

    System.out.print("Institute: ");
    switch (institute)
    {
      case "RMIT":
      case "rmit":
        System.out.println("RMIT");
        rmit(query);
        break;
      default:
        System.out.println("...not found");
        System.exit(3);
        break;
    }
  }

  private static String getURL (String url)
    throws Exception
  {
    URL address = new URL(url);
    HttpURLConnection connection = (HttpURLConnection)address.openConnection();
    connection.setDoOutput(true);
    connection.setReadTimeout(10000);
    connection.connect();

    StringBuilder sb = new StringBuilder();
    BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    String line;
    while ((line = rd.readLine()) != null)
      sb.append(line);

    return sb.toString();
  }

  private static void rmit (String query)
    throws Exception
  {
    final String QUERY = "?CLOCATION=Study%20at%20RMIT%2F&QRY=%2Btype%3Dflexible%20%2Bsubtype%3Dheparta%20%2Bkeywords%3D(" + URLEncoder.encode(query, "utf-8") + ")%20&STYPE=ENTIRE";

    // open database
    Connection conn = DriverManager.getConnection("jdbc:sqlite:db/rmit.db");
    Statement stat = conn.createStatement();
    stat.executeUpdate("drop table if exists course");
    stat.executeUpdate("drop table if exists offering");
    stat.executeUpdate("drop table if exists clause");
    stat.executeUpdate("drop table if exists prereq");
    stat.executeUpdate("drop table if exists extra");
    stat.executeUpdate("create table if not exists course (id string primary key, title string, prereqs string)");
    stat.executeUpdate("create table if not exists offering (code string primary key, course string references course (id), campus string, career string, school string, mode string)");
    stat.executeUpdate("create table if not exists clause (id integer primary key autoincrement, course string references offering (code))");
    stat.executeUpdate("create table if not exists prereq (clause integer references clause (id), course string references course (code), primary key (clause, course))");
    stat.executeUpdate("create table if not exists extra (clause integer references clause (id), extra string, primary key (clause, extra))");

    // find pages
    for (int curpos = 1; ; curpos += 15)
    {
      System.out.println("Results " + curpos + " to " + (curpos + 14));

      String response = "";
      try
      {
        response = getURL("http://rmit.edu.au/browse/;CURPOS=" + curpos + QUERY);
      }
      catch (IOException e)
      {
        System.out.println("No more results, done.");
        break;
      }

      // find courses
      for (int index = 1; index > 0; index++)
      {
        index = response.indexOf("browse;ID=", index);
        String id = response.substring(index + 10, response.indexOf(";", index + 10));

        if (id.substring(6).equals("heparta"))
        {
          id = id.substring(0, 6);
          System.out.print(" * course " + id);

          String course = getURL("http://rmit.edu.au/courses/" + id);

          int index2 = course.indexOf("Course Title") + 14;
          String title = course.substring(index2, course.indexOf("</h1>", index2) - 1);
          System.out.print(", " + title);

          // find codes for this course
          int counter = -1;
          String offering[] = new String[5];
          for (index2 = 1; index2 > 0; index2++)
          {
            index2 = course.indexOf("<td><p>", index2);
            String data = course.substring(index2 + 7, course.indexOf("</p>", index2 + 7));
            
            if (data.substring(4, 8).matches("[0-9]{4}"))
            {
              System.out.print("\n ==> " + data);
              offering[4] = data;
              counter = 4;
            }
            else if (counter > 0)
            {
              System.out.print(", " + data);
              offering[--counter] = data;
            }

            if (counter == 0)
            {
              counter = -1;
              try
              {
                stat.executeUpdate("insert into offering (code, course, campus, career, school, mode) values ('" + offering[4] + "', '" + id + "', '" + offering[3] + "', '" + offering[2] + "', '" + offering[1] + "', '" + offering[0] + "')");
              }
              catch (SQLException e)
              {
                System.out.println("\nSQLException: " + e.getMessage());
                e.printStackTrace(System.out);
              }
            }
          }

          // find prereqs
          index2 = course.indexOf("<strong>Pre");
          String prereqs = course.substring(course.indexOf("<p>", index2), course.indexOf("<strong>Course", index2+1) - 3);

          stat.executeUpdate("insert into course (id, title, prereqs) values ('" + id + "', '" + title + "', '" + prereqs + "')");

          System.out.println();
        }
      }
    }
  }
}
