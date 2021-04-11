package com.smart.utils;

import java.io.*;
import java.nio.file.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepo;
import com.smart.entities.Contact;

public class FileUploadUtil {
	@Autowired
	private static ContactRepo contactRepo;
	
	private static Contact contact;

	public static void saveFile(String fileName, MultipartFile multipartFile) throws IOException {
		Path uploadPath = Paths.get("user-photos");

		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}

		try (InputStream inputStream = multipartFile.getInputStream()) {
			Path filePath = uploadPath.resolve(fileName);
			Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException ioe) {
			throw new IOException("Could not save image file: " + fileName, ioe);
		}
	}

	public static void deleteFile(String fileName, MultipartFile multipartFile) throws IOException {
		
		Path uploadPath = Paths.get("user-photos");
		try  {
			Path filePath = uploadPath.resolve(fileName);
			Files.delete(filePath);
		} catch (IOException ioe) {
			throw new IOException("Could not save image file: " + fileName, ioe);
		}
	}

	}

