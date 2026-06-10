/*
    Class Name: PhotoAlbumApplicationTests
    Description: Verifies that the Spring application context loads in tests.
    Date Created: 2026-06-10
*/

package com.photoalbum;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PhotoAlbumApplicationTests {

    @Test
    void contextLoads() {
        // This test ensures that the Spring context loads correctly
    }
}