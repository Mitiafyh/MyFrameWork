package Presentation;

import java.util.HashMap;
import java.util.Map;

import Utils.Mapping;
import Utils.UrlMethod;
import Utils.Utilitaire;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class RequestControllerListener implements ServletContextListener {

    private static final String SPRING_ROOT_CONTEXT_ATTRIBUTE = "org.springframework.web.context.WebApplicationContext.ROOT";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();

        String packageCible = servletContext.getInitParameter("packageToScan");
        if (packageCible == null || packageCible.isEmpty()) {
            throw new RuntimeException("Le paramètre 'packageToScan' n'est pas défini dans le fichier web.xml");
        }
        Map<UrlMethod, Mapping> mappingUrls;
        try {
            mappingUrls = Utilitaire.getMappings(packageCible);
            servletContext.setAttribute("mappingUrls", mappingUrls);
            System.out.println("Mapping initialisé avec succès pour le package : " + packageCible);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'initialisation des mappings", e);
        }
        Object conteneurSpring = servletContext.getAttribute(SPRING_ROOT_CONTEXT_ATTRIBUTE);

        if (conteneurSpring == null) {
            throw new RuntimeException(" Le conteneur Spring n'a pas pu être récupéré au démarrage. "
                    + "Vérifiez l'ordre des listeners dans le web.xml.");
        }

        servletContext.setAttribute("springContext", conteneurSpring);
        System.out.println("Conteneur Spring lié à 'springContext' avec succès !");
    }
}
