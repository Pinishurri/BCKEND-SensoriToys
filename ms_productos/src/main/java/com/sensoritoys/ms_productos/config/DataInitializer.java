package com.sensoritoys.ms_productos.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.sensoritoys.ms_productos.model.Categoria;
import com.sensoritoys.ms_productos.model.Producto;
import com.sensoritoys.ms_productos.repository.CategoriaRepository;
import com.sensoritoys.ms_productos.repository.ProductoRepository;

@Component
@ConditionalOnProperty(name = "sensoritoys.datos-iniciales", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements ApplicationRunner {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public DataInitializer(CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (categoriaRepository.count() == 0) {
            categoriaRepository.saveAll(List.of(
                    new Categoria(null, "Táctil", "Juguetes de estimulación táctil"),
                    new Categoria(null, "Visual", "Juguetes de estimulación visual"),
                    new Categoria(null, "Motricidad", "Juguetes para motricidad fina y gruesa")));
        }

        if (productoRepository.count() == 0) {
            Categoria tactil = categoriaRepository.findByNombreIgnoreCase("Táctil").orElse(null);
            Categoria visual = categoriaRepository.findByNombreIgnoreCase("Visual").orElse(null);

            productoRepository.saveAll(List.of(
                    producto("Mordedor Sensorial Silicona", "Estimulación táctil", 6990, 10, tactil, "0-3 años",
                            "https://images.unsplash.com/photo-1596461404969-9ae70f2830c1?w=500&h=500&fit=crop"),
                    producto("Lámpara de Burbujas Calmante", "Estimulación visual", 24990, 3, visual, "3-6 años",
                            "https://images.unsplash.com/photo-1513151233558-d860c5398176?w=500&h=500&fit=crop"),
                    producto("Squishy Anti-Estrés Conejo Rosa", "Juguete blando", 4990, 15, tactil, "3-6 años",
                            "https://images.unsplash.com/photo-1587654780291-39c9404d746b?w=500&h=500&fit=crop"),
                    producto("Tubo de Burbujas LED", "Luces cambiantes", 30000, 8, visual, "6+ años",
                            "https://images.unsplash.com/photo-1508873696983-2df515122519?w=500&h=500&fit=crop")));
        }
    }

    private Producto producto(String nombre, String descripcion, int precio, int stock,
                              Categoria categoria, String edad, String imagenUrl) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(BigDecimal.valueOf(precio));
        producto.setStock(stock);
        producto.setCategoria(categoria);
        producto.setEdad(edad);
        producto.setImagenUrl(imagenUrl);
        return producto;
    }
}
