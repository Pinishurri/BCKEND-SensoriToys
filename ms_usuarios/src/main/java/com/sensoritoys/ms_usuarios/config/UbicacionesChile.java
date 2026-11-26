package com.sensoritoys.ms_usuarios.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class UbicacionesChile {

    private static final Map<String, List<String>> CIUDADES_POR_REGION = new LinkedHashMap<>();

    static {
        CIUDADES_POR_REGION.put("Arica y Parinacota", List.of("Arica", "Putre"));
        CIUDADES_POR_REGION.put("Tarapacá", List.of("Iquique", "Alto Hospicio", "Pozo Almonte"));
        CIUDADES_POR_REGION.put("Antofagasta", List.of("Antofagasta", "Calama", "Tocopilla", "Mejillones", "San Pedro de Atacama"));
        CIUDADES_POR_REGION.put("Atacama", List.of("Copiapó", "Vallenar", "Caldera", "Chañaral"));
        CIUDADES_POR_REGION.put("Coquimbo", List.of("La Serena", "Coquimbo", "Ovalle", "Illapel", "Vicuña"));
        CIUDADES_POR_REGION.put("Valparaíso", List.of("Valparaíso", "Viña del Mar", "Quilpué", "Villa Alemana", "San Antonio", "Quillota", "Los Andes", "San Felipe", "La Calera", "Limache"));
        CIUDADES_POR_REGION.put("Metropolitana de Santiago", List.of("Santiago", "Puente Alto", "Maipú", "San Bernardo", "Melipilla", "Talagante", "Colina", "Buin", "Peñaflor"));
        CIUDADES_POR_REGION.put("Libertador General Bernardo O'Higgins", List.of("Rancagua", "San Fernando", "Rengo", "Machalí", "Santa Cruz", "San Vicente", "Pichilemu"));
        CIUDADES_POR_REGION.put("Maule", List.of("Talca", "Curicó", "Linares", "Constitución", "Cauquenes", "Parral"));
        CIUDADES_POR_REGION.put("Ñuble", List.of("Chillán", "Chillán Viejo", "San Carlos", "Bulnes", "Quirihue"));
        CIUDADES_POR_REGION.put("Biobío", List.of("Concepción", "Talcahuano", "Los Ángeles", "Coronel", "Chiguayante", "San Pedro de la Paz", "Lota", "Hualpén", "Penco", "Tomé", "Lebu"));
        CIUDADES_POR_REGION.put("La Araucanía", List.of("Temuco", "Padre Las Casas", "Villarrica", "Angol", "Pucón", "Victoria", "Lautaro"));
        CIUDADES_POR_REGION.put("Los Ríos", List.of("Valdivia", "La Unión", "Río Bueno", "Panguipulli"));
        CIUDADES_POR_REGION.put("Los Lagos", List.of("Puerto Montt", "Osorno", "Castro", "Puerto Varas", "Ancud", "Calbuco", "Quellón"));
        CIUDADES_POR_REGION.put("Aysén del General Carlos Ibáñez del Campo", List.of("Coyhaique", "Puerto Aysén", "Chile Chico"));
        CIUDADES_POR_REGION.put("Magallanes y de la Antártica Chilena", List.of("Punta Arenas", "Puerto Natales", "Porvenir"));
    }

    private UbicacionesChile() {
    }

    public static boolean existeRegion(String region) {
        return CIUDADES_POR_REGION.containsKey(region);
    }

    public static boolean ciudadPerteneceARegion(String region, String ciudad) {
        return CIUDADES_POR_REGION.getOrDefault(region, List.of()).contains(ciudad);
    }
}
