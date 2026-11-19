package handler;

import com.sun.net.httpserver.HttpExchange;
import dao.TagDAO;
import model.Tag;

import java.io.IOException;
import java.util.*;

/**
 * REST Handler for Tag queries.
 */
public class TagHandler extends BaseHandler {

    private final TagDAO tagDAO;

    public TagHandler(TagDAO tagDAO) {
        this.tagDAO = tagDAO;
    }

    @Override
    protected void processRequest(HttpExchange exchange) throws Exception {
        String method = exchange.getRequestMethod().toUpperCase();
        String path = exchange.getRequestURI().getPath();

        if ("GET".equals(method) && ("/api/tags".equals(path) || "/api/tags/".equals(path))) {
            List<Tag> tags = tagDAO.getAllTags();
            Map<Integer, Integer> counts = tagDAO.getTagCounts();

            List<Map<String, Object>> response = new ArrayList<>();
            for (Tag t : tags) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", t.getId());
                map.put("name", t.getName());
                map.put("count", counts.getOrDefault(t.getId(), 0));
                response.add(map);
            }

            sendJson(exchange, 200, response);
            return;
        }

        sendError(exchange, 404, "Endpoint not found: " + path);
    }
}
