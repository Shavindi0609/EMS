package com.ijse.gdse;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.dbcp2.BasicDataSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

@WebServlet("/api/v1/signup")
public class SignUpServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            //read json obj and write in to hashmap
            ObjectMapper mapper = new ObjectMapper();//json object eka read karagena key value walata object map karanna
            Map<String,String> user = mapper.readValue(request.getInputStream(), Map.class); // key value set karagannawa

            //save user in database
            ServletContext sc = request.getServletContext();
            BasicDataSource dataSource= (BasicDataSource) sc.getAttribute("ds");//datasourse eke connection 5 thiyei

            Connection connection=dataSource.getConnection();
            PreparedStatement pstm=
                    connection.prepareStatement("INSERT INTO users" +
                            "(userId,userName,userEmail,userPassword) Values (?,?,?,?)");
            pstm.setString(1, UUID.randomUUID().toString());
            pstm.setString(2, user.get("userName"));
            pstm.setString(3,user.get("userEmail"));
            pstm.setString(4,user.get("userPassword"));

            int executed=pstm.executeUpdate();
            PrintWriter out=response.getWriter();
            response.setContentType("application/json");
            if(executed>0){ //exception handling
                response.setStatus(200);
                response.setStatus(HttpServletResponse.SC_ACCEPTED);
                mapper.writeValue(out,Map.of(
                        "code","201",
                        "status","success",
                        "message","User signed up successfully"
                ));
            }else {
                response.setStatus(500);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                mapper.writeValue(out,Map.of(
                        "code","400",
                        "status","error",
                        "message","Bad Request" //request eke enna oni data tika hariyata nathi unama ena messsage eka
                ));
            }

            connection.close();
        } catch (Exception e) {
            ObjectMapper mapper = new ObjectMapper();
            PrintWriter out=response.getWriter();
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(out,Map.of(
                    "code","500",
                    "status","error",
                    "message","Internal Server Error"
            ));
            throw new RuntimeException(e);
        }


    }
}
