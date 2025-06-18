package cr.ac.itcr.zsnails.pureharvest.util;

import java.util.List;
import java.util.stream.Collectors;

import cr.ac.itcr.zsnails.pureharvest.data.model.Product;

public class ProductFilterUtils {

    public static List<Product> filterProducts(
            String searchQuery,
            List<Product> products,
            boolean filterByName,
            boolean filterByType,
            boolean filterByAcidity,
            float maxPrice
    ) {
        String query = searchQuery.toLowerCase();

        return products.stream()
                .filter(p -> {
                    // Always filter by price
                    if (p.getPrice() > maxPrice) return false;

                    boolean matchesText = false;

                    // Si no hay ningún filtro activo, no se filtra por texto
                    boolean hasActiveChip = filterByName || filterByType || filterByAcidity;

                    if (!hasActiveChip || query.isEmpty()) {
                        return true; // Solo filtra por precio
                    }

                    if (filterByName && p.getName().toLowerCase().contains(query)) {
                        matchesText = true;
                    }

                    if (filterByType && p.getType().toLowerCase().contains(query)) {
                        matchesText = true;
                    }

                    if (filterByAcidity && p.getAcidity() != null &&
                            p.getAcidity().toLowerCase().contains(query)) {
                        matchesText = true;
                    }

                    return matchesText;
                })
                .collect(Collectors.toList());
    }
}
