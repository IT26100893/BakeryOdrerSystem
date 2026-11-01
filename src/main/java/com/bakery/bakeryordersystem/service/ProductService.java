package com.bakery.bakeryordersystem.service;
import com.bakery.bakeryordersystem.exception.ProductNotFoundException;
import com.bakery.bakeryordersystem.model.BakeryProduct;
import com.bakery.bakeryordersystem.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
@Service
public class ProductService {
    /** Sub-folder (inside bakery.upload.dir) that holds the product photos; served at /uploads/products/**. */
    public static final String IMAGE_FOLDER = "products";
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp", "gif");

    private final ProductRepository repo;
    private final Path imageDir;

    public ProductService(ProductRepository repo, @Value("${bakery.upload.dir:uploads}") String uploadDir) {
        this.repo = repo;
        this.imageDir = Paths.get(uploadDir, IMAGE_FOLDER).toAbsolutePath().normalize();
    }
    public List<BakeryProduct> search(String q) {
        if (q == null || q.isBlank()) return repo.findAll();
        String k = q.toLowerCase();
        return repo.findAll().stream().filter(p -> p.getName().toLowerCase().contains(k) || p.getCategory().toLowerCase().contains(k)).toList();
    }
    public BakeryProduct find(String id) { return repo.findById(id).orElseThrow(() -> new ProductNotFoundException(id)); }

    /** Adds or updates a product. A new photo replaces the old one; no photo selected = keep the current one. */
    public void save(String id, String cat, String name, double price, int stock, String detail, MultipartFile photo) {
        boolean isNew = id == null || id.isBlank();
        String productId = isNew ? FileRepository.newId("P") : id;
        String oldImage = isNew ? null : repo.findById(id).map(BakeryProduct::getImage).orElse(null);
        String image = oldImage;
        if (photo != null && !photo.isEmpty()) image = storeImage(photo);   // validates first, throws IllegalArgumentException if bad
        repo.save(BakeryProduct.of(cat, productId, name, price, stock, detail, image));
        if (oldImage != null && !oldImage.equals(image)) deleteImageFile(oldImage);
    }
    public void delete(String id) {
        String image = repo.findById(id).map(BakeryProduct::getImage).orElse(null);
        repo.delete(id);
        deleteImageFile(image);
    }

    private String storeImage(MultipartFile f) {
        if (f.getSize() > MAX_IMAGE_BYTES) throw new IllegalArgumentException("Photo is too large (max 5 MB).");
        String original = f.getOriginalFilename() == null ? "" : f.getOriginalFilename();
        int dot = original.lastIndexOf('.');
        String ext = dot < 0 ? "" : original.substring(dot + 1).toLowerCase(Locale.ROOT);
        String type = f.getContentType() == null ? "" : f.getContentType().toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXT.contains(ext) || !type.startsWith("image/"))
            throw new IllegalArgumentException("Please choose a JPG, PNG, WEBP or GIF photo.");
        String fileName = UUID.randomUUID() + "." + ext;      // generated name: no path tricks, no clashes
        try {
            Files.createDirectories(imageDir);
            Files.copy(f.getInputStream(), imageDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) { throw new UncheckedIOException(e); }
        return fileName;
    }
    private void deleteImageFile(String fileName) {
        if (fileName == null || fileName.isBlank()) return;
        try { Files.deleteIfExists(imageDir.resolve(fileName).normalize()); } catch (IOException ignored) { }
    }
}
