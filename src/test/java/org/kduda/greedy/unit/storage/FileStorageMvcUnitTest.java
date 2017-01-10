package org.kduda.greedy.unit.storage;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.kduda.greedy.exception.StorageFileNotFoundException;
import org.kduda.greedy.model.FileModel;
import org.kduda.greedy.repository.CsvRepository;
import org.kduda.greedy.unit.SpringUnitTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class FileStorageMvcUnitTest extends SpringUnitTest {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private CsvRepository csvRepository;

	@Test
	public void shouldListAllFiles() throws Exception {
		given(csvRepository.listAll())
			.willReturn(Arrays.asList(new FileModel("first.txt", "1"), new FileModel("second.txt", "2")));

		mvc.perform(get("/files"))
		   .andExpect(status().isOk())
		   .andExpect(model().attribute("files",
										Matchers.contains(new FileModel("first.txt", "1"), new FileModel("second.txt", "2"))
		   ));
	}

	@Test
	public void shouldSaveUploadedFile() throws Exception {
		MockMultipartFile multipartFile =
			new MockMultipartFile("file", "test.txt", "text/plain", "Storage Tests".getBytes());
		mvc.perform(fileUpload("/files").file(multipartFile))
		   .andExpect(status().isFound())
		   .andExpect(header().string("Location", "/files"));

		then(this.csvRepository).should().store(multipartFile);
	}

	/**
	 * File should not be found.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void should404WhenMissingFile() throws Exception {
		given(csvRepository.loadResourceById("10"))
			.willThrow(StorageFileNotFoundException.class);

		mvc.perform(get("/files/test.txt"))
		   .andExpect(status().isNotFound());
	}

}