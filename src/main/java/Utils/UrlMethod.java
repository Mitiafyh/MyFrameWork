package Utils;

import java.util.Objects;

public class UrlMethod {

    String url;
    HttpMethod method;

    public UrlMethod(String url, HttpMethod method) {
        this.url = url;
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        UrlMethod urlMethod = (UrlMethod) obj;

        return Objects.equals(url, urlMethod.url) && method == urlMethod.method;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, method);
    }

}
