package Presentation;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;

import Utils.*;
import annotation.WebAPI;

import java.lang.reflect.*;

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
                    } else if (List.class.isAssignableFrom(param.getType())) {

                        Type genericType = param.getParameterizedType();
                        Class<?> itemClass = Object.class;
                        if (genericType instanceof ParameterizedType) {
                            ParameterizedType pt = (ParameterizedType) genericType;
                            itemClass = (Class<?>) pt.getActualTypeArguments()[0];
                        }
                        String prefixeParam = param.getName().toLowerCase();
                        String prefixeClasse = itemClass.getSimpleName().toLowerCase();

                        java.util.Set<Integer> indices = new java.util.TreeSet<>();
                        for (String key : request.getParameterMap().keySet()) {
                            String lowerKey = key.toLowerCase();
                            
                            if ((lowerKey.startsWith(prefixeParam + "[") || lowerKey.startsWith(prefixeClasse + "["))
                                    && lowerKey.contains("]")) {
                                try {
                                    int start = lowerKey.indexOf('[') + 1;
                                    int end = lowerKey.indexOf(']');
                                    int index = Integer.parseInt(lowerKey.substring(start, end));
                                    indices.add(index);
                                } catch (NumberFormatException ignored) {
                                }
                            }
                        }
                        List<Object> listeObjets = new ArrayList<>();
                        for (int index : indices) {
                            if (itemClass == String.class || itemClass == Integer.class
                                    || itemClass == int.class || itemClass == double.class
                                    || itemClass == Double.class || itemClass == Boolean.class
                                    || itemClass == boolean.class) {

                                String cle1 = prefixeParam + "[" + index + "]";
                                String cle2 = prefixeClasse + "[" + index + "]";

                                String valeur = null;
                                if (request.getParameterMap().containsKey(cle1)) {
                                    valeur = request.getParameter(cle1);
                                } else if (request.getParameterMap().containsKey(cle2)) {
                                    valeur = request.getParameter(cle2);
                                }

                                if (valeur != null && !valeur.isEmpty()) {
                                    Object convert = utilitaire.ConvertType(valeur, itemClass);
                                    listeObjets.add(convert);
                                }

                            } else {
                                Object itemInstance = itemClass.getDeclaredConstructor().newInstance();
                                Field[] attributs = itemClass.getDeclaredFields();

                                for (Field attribut : attributs) {
                                    String nomChamp = attribut.getName().toLowerCase();

                                    String cle1 = prefixeParam + "[" + index + "]." + nomChamp;
                                    String cle2 = prefixeClasse + "[" + index + "]." + nomChamp;
                                    String cle3 = prefixeParam + "[" + index + "][" + nomChamp + "]";

                                    String valeur = null;
                                    if (request.getParameterMap().containsKey(cle1)) {
                                        valeur = request.getParameter(cle1);
                                    } else if (request.getParameterMap().containsKey(cle2)) {
                                        valeur = request.getParameter(cle2);
                                    } else if (request.getParameterMap().containsKey(cle3)) {
                                        valeur = request.getParameter(cle3);
                                    }

                                    if (valeur != null) {
                                        attribut.setAccessible(true);
                                        if (valeur.isEmpty()) {
                                            if (!attribut.getType().isPrimitive()) {
                                                attribut.set(itemInstance, null);
                                            }
                                        } else {
                                            Object convert = utilitaire.ConvertType(valeur, attribut.getType());
                                            attribut.set(itemInstance, convert);
                                        }
                                    }
                                }
                                listeObjets.add(itemInstance);
                            }
                        }
                        arguments[i] = listeObjets;

                    } else {
                        arguments[i] = param.getType().getDeclaredConstructor().newInstance();
                        Field[] attributs = param.getType().getDeclaredFields();

                        for (int j = 0; j < attributs.length; j++) {
                            Object convert = null;
                            String nomChamp = attributs[j].getName();

                            String cleParamObject = nomParam + "." + nomChamp;
                            String cleParamForm = nomParam + "_" + nomChamp;

                            String valeur = null;
                            if (request.getParameterMap().containsKey(cleParamForm)) {
                                valeur = request.getParameter(cleParamForm);
                            } else if (request.getParameterMap().containsKey(cleParamObject)) {
                                valeur = request.getParameter(cleParamObject);
                            } else if (request.getParameterMap().containsKey(nomChamp)) {
                                valeur = request.getParameter(nomChamp);
                            }

                            if (valeur != null) {
                                attributs[j].setAccessible(true);
                                if (valeur.isEmpty()) {
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
