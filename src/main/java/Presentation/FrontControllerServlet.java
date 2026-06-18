package Presentation;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import Utils.*;

public class FrontControllerServlet extends HttpServlet {
    
    private List<Class<?>> classesControllers;

    @Override
    public void init() throws ServletException {
        String packageCible = getInitParameter("packageToScan");

        if(packageCible == null || packageCible.isEmpty()){
            System.out.println("Erreur : Le paramètre 'packageToScan' n'est pas configuré dans le web.xml !");
            return;
        }

        this.classesControllers = Utilitaire.getClassesWithAnnotation(packageCible, annotation.Controller.class);
        

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uri = request.getRequestURI();

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println(uri + "<br>");
        out.println("\n");
        out.println("\n");

        out.println("Classes avec annotation @Controller : "+ "<br>");

        if(this.classesControllers != null){
            for(Class<?> classe : this.classesControllers){
                out.println(" - " + classe.getName()+ "<br>");

            }
        }else{
                out.println(" Pas de classes avec l'annotation " );

            }
    

    }

   

}
