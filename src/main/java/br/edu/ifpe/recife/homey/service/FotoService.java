package br.edu.ifpe.recife.homey.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FotoService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public String salvarFoto(MultipartFile file, Long usuarioId) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio fornecido");
        }

        // Valida se é imagem
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Apenas imagens são permitidas. Tipo fornecido: " + contentType);
        }

        // Cria pasta se não existir: uploads/usuarios/{id}
        String subDir = "usuarios/" + usuarioId;
        Path uploadPath = Paths.get(uploadDir, subDir);
        Files.createDirectories(uploadPath);

        // Nome único: uuid + extensão original
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID() + extension;

        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);

        // URL relativa que o frontend vai usar
        String fotoUrl = "/uploads/" + subDir + "/" + filename;
        return fotoUrl;
    }

    public void deletarFoto(String fotoUrl) {
        if (fotoUrl != null && fotoUrl.startsWith("/uploads/")) {
            try {
                // Remove "/uploads/" para obter o caminho relativo dentro da pasta de upload
                String relativePath = fotoUrl.substring("/uploads/".length());
                Path path = Paths.get(uploadDir).resolve(relativePath);
                Files.deleteIfExists(path);
            } catch (IOException e) {
                // logar erro, mas não falhar o processo
            }
        }
    }
}
