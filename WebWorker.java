/**
* Web worker: an object of this class executes in its own new thread
* to receive and respond to a single HTTP request. After the constructor
* the object executes on its "run" method, and leaves when it is done.
*
* One WebWorker object is only responsible for one client connection.
* This code uses Java threads to parallelize the handling of clients:
* each WebWorker runs in its own thread. This means that you can essentially
* just think about what is happening on one client at a time, ignoring
* the fact that the entirety of the webserver execution might be handling
* other clients, too.
*
* This WebWorker class (i.e., an object of this class) is where all the
* client interaction is done. The "run()" method is the beginning -- think
* of it as the "main()" for a client interaction. It does three things in
* a row, invoking three methods in this class: it reads the incoming HTTP
* request; it writes out an HTTP header to begin its response, and then it
* writes out some HTML content for the response content. HTTP requests and
* responses are just lines of text (in a very particular format).
*
**/

import java.net.Socket;
import java.lang.Runnable;
import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import java.util.TimeZone;

public class WebWorker implements Runnable
{

private Socket socket;

/**
* Constructor: must have a valid open socket
**/
public WebWorker(Socket s)
{
   socket = s;
}

/**
* Worker thread starting point. Each worker handles just one HTTP
* request and then returns, which destroys the thread. This method
* assumes that whoever created the worker created it with a valid
* open socket object.
**/
public void run()
{
   String filePath = "";
   System.err.println("Handling connection...");
   try {
      InputStream  is = socket.getInputStream();
      OutputStream os = socket.getOutputStream();
      //readHTTPRequest(is);
      filePath = readHTTPRequest(is);
      System.err.println("FilePath = " + filePath);
      writeHTTPHeader(os,"text/html", filePath);
      writeContent(os, filePath);
      os.flush();
      socket.close();
   } catch (Exception e) {
      System.err.println("Output error: "+e);
   }
   System.err.println("Done handling connection.");
   return;
}

/**
* Read the HTTP request header.
**/
private String readHTTPRequest(InputStream is)
{
   String line;
   String filePath = "";
   BufferedReader r = new BufferedReader(new InputStreamReader(is));
   while (true) {
      try {
         while (!r.ready()) Thread.sleep(1);
         line = r.readLine();
         System.err.println("Request line: ("+line+")");

         // If its the GET line of the HTTP header than grab just the url path of the file they are requesting.
         if( line.startsWith("GET "))
         {
           System.err.println("GET LINE: ("+line+")");
           String GetLineString = line.replace("GET /","");
           String[] Getline = GetLineString.split(" ");
           System.err.println("Getline[0]: " + Getline[0]);
           // This assumes that there are no spaces in the name of the file. Additional processing would be necessary to handle spaces within the name.
           filePath = Getline[0];
         }

         if (line.length()==0) break;
      } catch (Exception e) {
         System.err.println("Request error: "+e);
         break;
      }
   }
   return filePath;
}

/**
* Write the HTTP header lines to the client network connection.
* @param os is the OutputStream object to write to
* @param contentType is the string MIME content type (e.g. "text/html")
**/
private void writeHTTPHeader(OutputStream os, String contentType, String filePath) throws Exception
{
    File f = new File(filePath);
    if(f.exists() && !f.isDirectory()) {
      Date d = new Date();
      DateFormat df = DateFormat.getDateTimeInstance();
      df.setTimeZone(TimeZone.getTimeZone("GMT"));
      os.write("HTTP/1.1 200 OK\n".getBytes());
      os.write("Date: ".getBytes());
      os.write((df.format(d)).getBytes());
      os.write("\n".getBytes());
      os.write("Server: Jon's very own server\n".getBytes());
      //os.write("Last-Modified: Wed, 08 Jan 2003 23:11:55 GMT\n".getBytes());
      //os.write("Content-Length: 438\n".getBytes());
      os.write("Connection: close\n".getBytes());
      os.write("Content-Type: ".getBytes());
      os.write(contentType.getBytes());
      os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
      return;
    }
    else{
      Date d = new Date();
      DateFormat df = DateFormat.getDateTimeInstance();
      df.setTimeZone(TimeZone.getTimeZone("GMT"));
      os.write("HTTP/1.1 404 Not Found\n".getBytes());
      os.write("Date: ".getBytes());
      os.write((df.format(d)).getBytes());
      os.write("\n".getBytes());
      os.write("Server: Jon's very own server\n".getBytes());
      //os.write("Last-Modified: Wed, 08 Jan 2003 23:11:55 GMT\n".getBytes());
      //os.write("Content-Length: 438\n".getBytes());
      os.write("Connection: close\n".getBytes());
      os.write("Content-Type: ".getBytes());
      os.write(contentType.getBytes());
      os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
      //    To send hardcoded Data
         os.write("<html><head><title>404 - File or directory not found.</title></head><body>\n".getBytes());
         os.write("<h3>404 Not Found</h3>\n".getBytes());
         os.write("</body></html>\n".getBytes());
      return;
    }
}

/**
* Write the data content to the client network connection. This MUST
* be done after the HTTP header has been written out.
* @param os is the OutputStream object to write to
**/
private void writeContent(OutputStream os, String filePath) throws Exception
{
//  Reads the requested filePath and sends it back to the browser.
    try {
      BufferedReader br = new BufferedReader(new FileReader(filePath));

      try {
          StringBuilder sb = new StringBuilder();
          String line = br.readLine();

          while (line != null) {
              sb.append(line);
              sb.append(System.lineSeparator());
              line = br.readLine();
          }
          String fileContent = sb.toString();
          Date d = new Date();
          DateFormat df = DateFormat.getDateTimeInstance();
          df.setTimeZone(TimeZone.getTimeZone("MST"));
          fileContent = fileContent.replace("<cs371date>",df.format(d)); //Replace <cs371data> with the date
          String server = "JFS Server";
          fileContent = fileContent.replace("<cs371server>",server); //Replace <cs371server> with server name Tag
          os.write(fileContent.getBytes());
      } catch(IOException e) {
        System.err.println("ERROR Reading file contents: "+e);
        }
        finally {
          br.close();
      }
    } catch(IOException e) {
      System.err.println("ERROR Reading File: "+e);
    }



//    To send hardcoded Data
//    os.write("<html><head></head><body>\n".getBytes());
//    os.write("<h3>Jon Waz Here!!!!!</h3>\n".getBytes());
//    os.write("</body></html>\n".getBytes());
}

} // end class
