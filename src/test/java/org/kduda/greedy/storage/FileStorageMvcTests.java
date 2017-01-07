package org.kduda.greedy.storage;

import org.kduda.greedy.SpringTest;
import org.kduda.greedy.exception.StorageFileNotFoundException;
import org.kduda.greedy.service.StorageService;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class FileStorageMvcTests extends SpringTest {

	@Autowired
	private MockMvc mvc;

	@MockBean
	@Qualifier("mongoGridFsStorageService2")
	private StorageService storageService;

	@Test
	public void shouldListAllFiles() throws Exception {
		given(storageService.loadAll())
			.willReturn(Stream.of(Paths.get("first.txt"), Paths.get("second.txt")));

		mvc.perform(get("/files"))
		   .andExpect(status().isOk())
		   .andExpect(model().attribute("files",
										Matchers.contains("http://localhost/files/first.txt", "http://localhost/files/second.txt")));
	}

	@Test
	public void shouldSaveUploadedFile() throws Exception {
		MockMultipartFile multipartFile =
			new MockMultipartFile("file", "test.txt", "text/plain", "Storage Tests".getBytes());
		mvc.perform(fileUpload("/files").file(multipartFile))
		   .andExpect(status().isFound())
		   .andExpect(header().string("Location", "/files"));

		then(this.storageService).should().store(multipartFile);
	}

	/**
	 * File should not be found.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void should404WhenMissingFile() throws Exception {
		given(storageService.loadAsResource("test.txt"))
			.willThrow(StorageFileNotFoundException.class);

		mvc.perform(get("/files/test.txt"))
		   .andExpect(status().isNotFound());
	}

}