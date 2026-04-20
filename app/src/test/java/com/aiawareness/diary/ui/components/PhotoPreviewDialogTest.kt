package com.aiawareness.diary.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoPreviewDialogTest {

    @Test
    fun photoPreviewContainerColor_isTransparentToAvoidWhiteEdgesOnPortraitPhotos() {
        assertEquals(Color.Transparent, photoPreviewContainerColor())
    }

    @Test
    fun photoPreviewImageContentScale_keepsFullPhotoVisible() {
        assertEquals(ContentScale.Fit, photoPreviewImageContentScale())
    }
}
