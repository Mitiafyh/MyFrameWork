package Presentation;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;

import Utils.*;
import annotation.WebAPI;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

public class FrontControllerServlet extends HttpServlet {

    private Map<UrlMethod, Mapping> mappingUrls;
    private String prefix;
    private String suffix;
    private Object springContext;
    Utilitaire utilitaire = new Utilitaire();

    @SuppressWarnings("unchecked")
    @Override
    public void init() throws ServletException {
        this.mappingUrls = (Map<UrlMethod, Mapping>) getServletContext().getAttribute("mappingUrls");
        this.springContext = getServletContext().getAttribute("springContext");
        if (this.mappingUrls == null) {
            throw new ServletException(
                    "Le mapping des URL n'a pas été initialisé. Assurez-vous que le RequestControllerListener est correctement configuré.");
        }
        this.prefix = getServletContext().getInitParameter("viewPrefix");
        this.suffix = getServletContext().getInitParameter("viewSuffix");

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        String path = request.getPathInfo();
        if (path == null || path.equals("/")) {
            path = request.getServletPath();
        }

        HttpMethod requestMethod = HttpMethod.valueOf(request.getMethod().toUpperCase());
        UrlMethod urlMethod = new UrlMethod(path, requestMethod);

        if (this.mappingUrls.containsKey(urlMethod)) {
            Mapping cible = this.mappingUrls.get(urlMethod);

            System.out.println("<h3>Route trouvée !</h3>");
            System.out.println("URL  : " + urlMethod.getUrl() + ",methode " + urlMethod.getMethod() + "<br>");
            System.out.println("Classe : " + cible.getControllerInstance().getName() + "<br>");
            System.out.println("Méthode associée : " + cible.getMethode().getName() + "()<br>");

            try {
                Class<?> classeDuControleur = cible.getControllerInstance();
                Object instanceControleur = classeDuControleur.getDeclaredConstructor().newInstance();
                Method methodeAExecuter = cible.getMethode();

                Parameter[] parametres = methodeAExecuter.getParameters();
                Object[] arguments = new Object[parametres.length];

                for (int i = 0; i < parametres.length; i++) {
                    Parameter param = parametres[i];
                    String nomParam = param.getName();

                    if (param.getType() == ApplicationContext.class) {
                        arguments[i] = springContext;
                    } else if (param.getType() == String.class || param.getType() == Integer.class
                            || param.getType() == int.class || param.getType() == double.class
                            || param.getType() == Double.class || param.getType() == Boolean.class
                            || param.getType() == boolean.class) {
                        arguments[i] = utilitaire.ConvertType(request.getParameter(nomParam), param.getType());
                    } else {
                        arguments[i] = param.getType().getDeclaredConstructor().newInstance();
                        Field[] attributs = param.getType().getDeclaredFields();
                        for (int j = 0; j < attributs.length; j++) {
                            Object convert = null;
                            String nomChamp = attributs[j].getName();
                            if (request.getParameterMap().containsKey(nomChamp)) {
                                String valeur = request.getParameter(nomChamp);
                                attributs[j].setAccessible(true);
                                if (valeur == null || valeur.isEmpty()) {
                                    if (!attributs[j].getType().isPrimitive()) {
                                        attributs[j].set(arguments[i], null);
                                    }
                                } else {
                                    convert = utilitaire.ConvertType(valeur, attributs[j].getType());
                                    attributs[j].set(arguments[i], convert);
                                }
                            }

                        }
                    }
                    System.out.println("Nom du paramètre réfléchi : " + param.getName());
                    System.out.println("Clés disponibles dans la requête : " + request.getParameterMap().keySet());

                }

                Object resultat = methodeAExecuter.invoke(instanceControleur, arguments);

                if (methodeAExecuter.isAnnotationPresent(WebAPI.class)) {

                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");

                    ObjectMapper objectMapper = new ObjectMapper();
                    String jsonConverti = objectMapper.writeValueAsString(resultat);

                    PrintWriter out = response.getWriter();
                    out.print(jsonConverti);
                    out.flush();

                } else if (resultat instanceof ModelAndView) {
                    response.setContentType("text/html");
                    PrintWriter out = response.getWriter();

                    ModelAndView mv = (ModelAndView) resultat;

                    for (Map.Entry<String, Object> attribut : mv.getAttribut().entrySet()) {
                        request.setAttribute(attribut.getKey(), attribut.getValue());
                    }
                    String prochaineVue = mv.getViewName();
                    String cheminComplet = this.prefix + prochaineVue + this.suffix;
                    RequestDispatcher dispatcher = request.getRequestDispatcher(cheminComplet);
                    dispatcher.forward(request, response);

                } else {
                    response.setContentType("text/html");
                    PrintWriter out = response.getWriter();
                    out.println("<!DOCTYPE html>");
                    out.println("<h3>Route trouvée mais aucun ModelView renvoyé.</h3>");
                }

            } catch (Exception e) {
                response.setContentType("text/html");
                PrintWriter out = response.getWriter();
                e.printStackTrace(out);
                out.println("<!DOCTYPE html>");
                out.println("<h3>Erreur lors de l'exécution de la méthode : " + e.getMessage() + "</h3>");
            }
        } else

        {
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.println("<!DOCTYPE html>");
            out.println("<h3> Aucune méthode ne correspond à l'URL : " + path + ",methode " + requestMethod + "</h3>");
            out.println("<h3>Liste des routes disponibles :</h3>");
            for (Map.Entry<UrlMethod, Mapping> exist : this.mappingUrls.entrySet()) {
                UrlMethod methode = exist.getKey();
                Mapping mapping = exist.getValue();
                out.println("URL  : " + methode.getUrl() + ", Méthode : " + methode.getMethod() + "<br>");
                out.println("Classe : " + mapping.getControllerInstance().getName() + "<br>");
                out.println("Méthode associée : " + mapping.getMethode().getName() + "()<br>");
            }
        }

    }
}
