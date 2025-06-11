package com.ijse.gdse;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.apache.commons.dbcp2.BasicDataSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;
import java.util.UUID;

@WebServlet("/api/v1/employee")
@MultipartConfig
public class EmployeeServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String ename = req.getParameter("ename");
            String eaddress = req.getParameter("eaddress");
            String eemail = req.getParameter("eemail");

            Part photoPart = req.getPart("photo"); // This gets the uploaded file
            String fileName = UUID.randomUUID() + "_" + photoPart.getSubmittedFileName();

            // Save to server folder (e.g., /uploads)
            String uploadPath = req.getServletContext().getRealPath("/") + "uploads/";
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get(uploadPath)); // Ensure directory exists
            photoPart.write(uploadPath + fileName);

            // Save employee data and file name to DB
            ServletContext sc = req.getServletContext();
            BasicDataSource dataSource = (BasicDataSource) sc.getAttribute("ds");
            Connection connection = dataSource.getConnection();

            PreparedStatement pstm = connection.prepareStatement(
                    "INSERT INTO employee (eid, ename, eaddress, eemail, photo) VALUES (?, ?, ?, ?, ?)");
            pstm.setString(1, UUID.randomUUID().toString());
            pstm.setString(2, ename);
            pstm.setString(3, eaddress);
            pstm.setString(4, eemail);
            pstm.setString(5, fileName);

            int executed = pstm.executeUpdate();

            PrintWriter out = resp.getWriter();
            resp.setContentType("application/json");

            ObjectMapper mapper = new ObjectMapper();
            if (executed > 0) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                mapper.writeValue(out, Map.of(
                        "code", "201",
                        "status", "success",
                        "message", "Employee saved with image"
                ));
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                mapper.writeValue(out, Map.of(
                        "code", "400",
                        "status", "error",
                        "message", "Failed to save employee"
                ));
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            ObjectMapper mapper = new ObjectMapper();
            PrintWriter out = resp.getWriter();
            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(out, Map.of(
                    "code", "500",
                    "status", "error",
                    "message", "Internal Server Error"
            ));
        }
    }
}
//    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        try {
//            //read json obj and write in to hashmap
//            ObjectMapper mapper = new ObjectMapper();
//            Map<String,String> employee = mapper.readValue(req.getInputStream(), Map.class);
//
//            //save user in database
//            ServletContext sc = req.getServletContext();
//            BasicDataSource dataSource= (BasicDataSource) sc.getAttribute("ds");
//
//            Connection connection=dataSource.getConnection();
//            PreparedStatement pstm = connection.prepareStatement("INSERT INTO employee" +
//                    "(eid,ename,eaddress,eemail) Values (?,?,?,?)");
//            pstm.setString(1, UUID.randomUUID().toString());
//            pstm.setString(2, employee.get("ename"));
//            pstm.setString(3,employee.get("eaddress"));
//            pstm.setString(4,employee.get("eemail"));
//
//            int executed=pstm.executeUpdate();
//            PrintWriter out=resp.getWriter();
//            resp.setContentType("application/json");
//            if(executed>0){
//                resp.setStatus(HttpServletResponse.SC_CREATED);
//                mapper.writeValue(out,Map.of(
//                        "code","201",
//                        "status","success",
//                        "message","Employee saved successfully"
//                ));
//            }else {
//                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                mapper.writeValue(out,Map.of(
//                        "code","400",
//                        "status","error",
//                        "message","Bad Request"
//                ));
//            }
//            connection.close();
//        } catch (Exception e) {
//            ObjectMapper mapper = new ObjectMapper();
//            PrintWriter out=resp.getWriter();
//            resp.setContentType("application/json");
//            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            mapper.writeValue(out,Map.of(
//                    "code","500",
//                    "status","error",
//                    "message","Internal Server Error"
//            ));
//            throw new RuntimeException(e);
//        }
//    }
//}
